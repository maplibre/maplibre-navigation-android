package org.maplibre.navigation.android.navigation.ui.v5;

import android.app.Application;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.maplibre.geojson.Point;
import org.maplibre.android.location.engine.LocationEngine;
import org.maplibre.navigation.android.navigation.ui.v5.camera.DynamicCamera;
import org.maplibre.navigation.android.navigation.ui.v5.instruction.BannerInstructionModel;
import org.maplibre.navigation.android.navigation.ui.v5.instruction.InstructionModel;
import org.maplibre.navigation.android.navigation.ui.v5.summary.SummaryModel;
import org.maplibre.navigation.android.navigation.ui.v5.voice.NavigationSpeechPlayer;
import org.maplibre.navigation.android.navigation.ui.v5.voice.SpeechAnnouncement;
import org.maplibre.navigation.android.navigation.ui.v5.voice.SpeechPlayer;
import org.maplibre.navigation.android.navigation.ui.v5.voice.SpeechPlayerProvider;
import org.maplibre.navigation.android.navigation.ui.v5.route.MapLibreRouteFetcher;
import org.maplibre.navigation.android.navigation.v5.milestone.BannerInstructionMilestone;
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone;
import org.maplibre.navigation.android.navigation.v5.milestone.MilestoneEventListener;
import org.maplibre.navigation.android.navigation.v5.milestone.VoiceInstructionMilestone;
import org.maplibre.navigation.android.navigation.v5.models.BannerInstructions;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions;
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigation;
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions;
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationEventListener;
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationTimeFormat;
import org.maplibre.navigation.android.navigation.v5.navigation.camera.Camera;
import org.maplibre.navigation.android.navigation.v5.offroute.OffRouteListener;
import org.maplibre.navigation.android.navigation.v5.route.FasterRouteListener;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;
import org.maplibre.navigation.android.navigation.v5.utils.DistanceFormatter;
import org.maplibre.navigation.android.navigation.v5.utils.LocaleUtils;
import org.maplibre.navigation.android.navigation.v5.utils.RouteUtils;

import org.jetbrains.annotations.TestOnly;

import java.util.List;

public class NavigationViewModel extends AndroidViewModel {

    private static final String EMPTY_STRING = "";
    private static final String OKHTTP_INSTRUCTION_CACHE = "okhttp-instruction-cache";
    private static final long TEN_MEGABYTE_CACHE_SIZE = 10 * 1024 * 1024;

    public final MutableLiveData<InstructionModel> instructionModel = new MutableLiveData<>();
    public final MutableLiveData<BannerInstructionModel> bannerInstructionModel = new MutableLiveData<>();
    public final MutableLiveData<SummaryModel> summaryModel = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isOffRoute = new MutableLiveData<>();
    private final MutableLiveData<Location> navigationLocation = new MutableLiveData<>();
    private final MutableLiveData<DirectionsRoute> route = new MutableLiveData<>();
    private final MutableLiveData<Boolean> shouldRecordScreenshot = new MutableLiveData<>();
    private final MutableLiveData<Point> destination = new MutableLiveData<>();

    private MapLibreNavigation navigation;
    private NavigationViewRouter router;
    private LocationEngineConductor locationEngineConductor;
    private NavigationViewEventDispatcher navigationViewEventDispatcher;
    private SpeechPlayer speechPlayer;
    private int voiceInstructionsToAnnounce = 0;
    private RouteProgress routeProgress;

    Milestone milestone;
    private String language;
    private RouteUtils routeUtils;
    private LocaleUtils localeUtils;
    private DistanceFormatter distanceFormatter;
    @NavigationTimeFormat.Type
    private int timeFormatType;
    private boolean isRunning;
    private boolean isChangingConfigurations;

    public NavigationViewModel(Application application) {
        super(application);
        initializeLocationEngine();
        initializeRouter();
        this.routeUtils = new RouteUtils();
        this.localeUtils = new LocaleUtils();
    }

    @TestOnly
        // Package private (no modifier) for testing purposes
    NavigationViewModel(Application application, MapLibreNavigation navigation,
                        NavigationViewRouter router) {
        super(application);
        this.navigation = navigation;
        this.router = router;
    }

    @TestOnly
        // Package private (no modifier) for testing purposes
    NavigationViewModel(Application application, MapLibreNavigation navigation,
                        LocationEngineConductor conductor, NavigationViewEventDispatcher dispatcher, SpeechPlayer speechPlayer) {
        super(application);
        this.navigation = navigation;
        this.locationEngineConductor = conductor;
        this.navigationViewEventDispatcher = dispatcher;
        this.speechPlayer = speechPlayer;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        destroyRouter();
    }

    public void onDestroy(boolean isChangingConfigurations) {
        this.isChangingConfigurations = isChangingConfigurations;
        if (!isChangingConfigurations) {
            endNavigation();
            deactivateInstructionPlayer();
            isRunning = false;
        }
        clearDynamicCameraMap();
        navigationViewEventDispatcher = null;
    }

    public void setMuted(boolean isMuted) {
        speechPlayer.setMuted(isMuted);
    }


    /**
     * Returns the current instance of {@link MapLibreNavigation}.
     * <p>
     * Will be null if navigation has not been initialized.
     */
    @Nullable
    public MapLibreNavigation retrieveNavigation() {
        return navigation;
    }

    void initializeEventDispatcher(NavigationViewEventDispatcher navigationViewEventDispatcher) {
        this.navigationViewEventDispatcher = navigationViewEventDispatcher;
    }

    /**
     * This method will pass {@link MapLibreNavigationOptions} from the {@link NavigationViewOptions}
     * to this view model to be used to initialize {@link MapLibreNavigation}.
     *
     * @param options to init NavigationView
     */
    void initialize(NavigationViewOptions options) {
        MapLibreNavigationOptions navigationOptions = options.navigationOptions();
        navigationOptions = navigationOptions.toBuilder().isFromNavigationUi(true).build();
        initializeLanguage(options);
        initializeTimeFormat(navigationOptions);
        initializeDistanceFormatter(options);
        if (!isRunning()) {
            LocationEngine locationEngine = initializeLocationEngineFrom(options);
            initializeNavigation(getApplication(), navigationOptions, locationEngine);
            addMilestones(options);
            initializeNavigationSpeechPlayer(options);
        }
        router.extractRouteOptions(options);
    }

    boolean isRunning() {
        return isRunning;
    }

    boolean isMuted() {
        if (speechPlayer == null) {
            return false;
        }
        return speechPlayer.isMuted();
    }

    void stopNavigation() {
        navigation.removeProgressChangeListener(null);
        navigation.removeMilestoneEventListener(null);
        navigation.stopNavigation();
    }

    boolean isOffRoute() {
        try {
            return isOffRoute.getValue();
        } catch (NullPointerException exception) {
            return false;
        }
    }

    void updateRoute(DirectionsRoute route) {
        this.route.setValue(route);
        if (!isChangingConfigurations) {
            startNavigation(route);
            updateReplayEngine(route);
            sendEventOnRerouteAlong(route);
            isOffRoute.setValue(false);
        }
        resetConfigurationFlag();
    }

    void updateRouteProgress(RouteProgress routeProgress) {
        this.routeProgress = routeProgress;
        sendEventArrival(routeProgress, milestone);
        instructionModel.setValue(new InstructionModel(distanceFormatter, routeProgress));
        summaryModel.setValue(new SummaryModel(getApplication(), distanceFormatter, routeProgress, timeFormatType));
    }

    void updateLocation(Location location) {
        router.updateLocation(location);
        navigationLocation.setValue(location);
    }

    void sendEventFailedReroute(String errorMessage) {
        if (navigationViewEventDispatcher != null) {
            navigationViewEventDispatcher.onFailedReroute(errorMessage);
        }
    }

    MutableLiveData<Location> retrieveNavigationLocation() {
        return navigationLocation;
    }

    MutableLiveData<DirectionsRoute> retrieveRoute() {
        return route;
    }

    MutableLiveData<Point> retrieveDestination() {
        return destination;
    }

    MutableLiveData<Boolean> retrieveShouldRecordScreenshot() {
        return shouldRecordScreenshot;
    }

    private void initializeRouter() {
        MapLibreRouteFetcher onlineRouter = new MapLibreRouteFetcher(getApplication());
        Context applicationContext = getApplication().getApplicationContext();
        ConnectivityStatusProvider connectivityStatus = new ConnectivityStatusProvider(applicationContext);
        router = new NavigationViewRouter(onlineRouter, connectivityStatus, routeEngineListener);
    }

    private void initializeLocationEngine() {
        locationEngineConductor = new LocationEngineConductor();
    }

    private void initializeLanguage(NavigationUiOptions options) {
        RouteOptions routeOptions = options.directionsRoute().routeOptions();
        language = localeUtils.inferDeviceLanguage(getApplication());
        if (routeOptions != null) {
            language = routeOptions.language();
        }
    }

    private String initializeUnitType(NavigationUiOptions options) {
        RouteOptions routeOptions = options.directionsRoute().routeOptions();
        String unitType = localeUtils.getUnitTypeForDeviceLocale(getApplication());
        if (routeOptions != null) {
            unitType = routeOptions.voiceUnits();
        }
        return unitType;
    }

    private void initializeTimeFormat(MapLibreNavigationOptions options) {
        timeFormatType = options.timeFormatType();
    }

    private int initializeRoundingIncrement(NavigationViewOptions options) {
        MapLibreNavigationOptions navigationOptions = options.navigationOptions();
        return navigationOptions.roundingIncrement();
    }

    private void initializeDistanceFormatter(NavigationViewOptions options) {
        String unitType = initializeUnitType(options);
        int roundingIncrement = initializeRoundingIncrement(options);
        distanceFormatter = new DistanceFormatter(getApplication(), language, unitType, roundingIncrement);
    }

    private void initializeNavigationSpeechPlayer(NavigationViewOptions options) {
        SpeechPlayer speechPlayer = options.speechPlayer();
        if (speechPlayer != null) {
            this.speechPlayer = speechPlayer;
            return;
        }
        boolean isVoiceLanguageSupported = options.directionsRoute().voiceLanguage() != null;
        SpeechPlayerProvider speechPlayerProvider = initializeSpeechPlayerProvider(isVoiceLanguageSupported);
        this.speechPlayer = new NavigationSpeechPlayer(speechPlayerProvider);
    }

    @NonNull
    private SpeechPlayerProvider initializeSpeechPlayerProvider(boolean voiceLanguageSupported) {
        return new SpeechPlayerProvider(getApplication(), language, voiceLanguageSupported);
    }

    private LocationEngine initializeLocationEngineFrom(NavigationViewOptions options) {
        LocationEngine locationEngine = options.locationEngine();
        boolean shouldReplayRoute = options.shouldSimulateRoute();
        locationEngineConductor.initializeLocationEngine(getApplication(), locationEngine, shouldReplayRoute);
        return locationEngineConductor.obtainLocationEngine();
    }

    private void initializeNavigation(Context context, MapLibreNavigationOptions options, LocationEngine locationEngine) {
        navigation = new MapLibreNavigation(context, options, locationEngine);
        addNavigationListeners();
    }

    private void addNavigationListeners() {
        navigation.addProgressChangeListener(new NavigationViewModelProgressChangeListener(this));
        navigation.addOffRouteListener(offRouteListener);
        navigation.addMilestoneEventListener(milestoneEventListener);
        navigation.addNavigationEventListener(navigationEventListener);
        navigation.addFasterRouteListener(fasterRouteListener);
    }

    private void addMilestones(NavigationViewOptions options) {
        List<Milestone> milestones = options.milestones();
        if (milestones != null && !milestones.isEmpty()) {
            navigation.addMilestones(milestones);
        }
    }

    private OffRouteListener offRouteListener = new OffRouteListener() {
        @Override
        public void userOffRoute(Location location) {
            speechPlayer.onOffRoute();
            Point newOrigin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            handleOffRouteEvent(newOrigin);
        }
    };

    private MilestoneEventListener milestoneEventListener = (routeProgress, instruction, milestone) -> {
        NavigationViewModel.this.milestone = milestone;
        playVoiceAnnouncement(milestone);
        updateBannerInstruction(routeProgress, milestone);
        sendEventArrival(routeProgress, milestone);
    };

    private NavigationEventListener navigationEventListener = isRunning -> {
        NavigationViewModel.this.isRunning = isRunning;
        sendNavigationStatusEvent(isRunning);
    };

    private FasterRouteListener fasterRouteListener = directionsRoute -> updateRoute(directionsRoute);

    private ViewRouteListener routeEngineListener = new NavigationViewRouteEngineListener(this);

    private void startNavigation(DirectionsRoute route) {
        if (route != null) {
            navigation.startNavigation(route);
            voiceInstructionsToAnnounce = 0;
        }
    }

    private void updateReplayEngine(DirectionsRoute route) {
        if (locationEngineConductor.updateSimulatedRoute(route)) {
            LocationEngine replayEngine = locationEngineConductor.obtainLocationEngine();
            navigation.setLocationEngine(replayEngine);
        }
    }

    private void destroyRouter() {
        if (router != null) {
            router.onDestroy();
        }
    }

    private void endNavigation() {
        if (navigation != null) {
            navigation.onDestroy();
        }
    }

    private void clearDynamicCameraMap() {
        if (navigation != null) {
            Camera cameraEngine = navigation.getCameraEngine();
            boolean isDynamicCamera = cameraEngine instanceof DynamicCamera;
            if (isDynamicCamera) {
                ((DynamicCamera) cameraEngine).clearMap();
            }
        }
    }

    private void deactivateInstructionPlayer() {
        if (speechPlayer != null) {
            speechPlayer.onDestroy();
        }
    }

    private void playVoiceAnnouncement(Milestone milestone) {
        if (milestone instanceof VoiceInstructionMilestone) {
            voiceInstructionsToAnnounce++;
            SpeechAnnouncement announcement = SpeechAnnouncement.builder()
                .voiceInstructionMilestone((VoiceInstructionMilestone) milestone).build();
            announcement = retrieveAnnouncementFromSpeechEvent(announcement);
            speechPlayer.play(announcement);
        }
    }

    private void updateBannerInstruction(RouteProgress routeProgress, Milestone milestone) {
        if (milestone instanceof BannerInstructionMilestone) {
            BannerInstructions instructions = ((BannerInstructionMilestone) milestone).getBannerInstructions();
            instructions = retrieveInstructionsFromBannerEvent(instructions);
            if (instructions != null) {
                BannerInstructionModel model = new BannerInstructionModel(distanceFormatter, routeProgress, instructions);
                bannerInstructionModel.setValue(model);
            }
        }
    }

    private void sendEventArrival(RouteProgress routeProgress, Milestone milestone) {
        if (milestone == null || routeProgress == null) {
            return;
        }
        if (navigationViewEventDispatcher != null && routeUtils.isArrivalEvent(routeProgress, milestone)) {
            navigationViewEventDispatcher.onArrival();
        }
    }

    private void handleOffRouteEvent(Point newOrigin) {
        if (navigationViewEventDispatcher != null && navigationViewEventDispatcher.allowRerouteFrom(newOrigin)) {
            navigationViewEventDispatcher.onOffRoute(newOrigin);
            router.findRouteFrom(routeProgress);
            isOffRoute.setValue(true);
        }
    }

    private void sendNavigationStatusEvent(boolean isRunning) {
        if (navigationViewEventDispatcher != null) {
            if (isRunning) {
                navigationViewEventDispatcher.onNavigationRunning();
            } else {
                navigationViewEventDispatcher.onNavigationFinished();
            }
        }
    }

    private void sendEventOnRerouteAlong(DirectionsRoute route) {
        if (navigationViewEventDispatcher != null && isOffRoute()) {
            navigationViewEventDispatcher.onRerouteAlong(route);
        }
    }

    private void resetConfigurationFlag() {
        if (isChangingConfigurations) {
            isChangingConfigurations = false;
        }
    }

    private SpeechAnnouncement retrieveAnnouncementFromSpeechEvent(SpeechAnnouncement announcement) {
        if (navigationViewEventDispatcher != null) {
            announcement = navigationViewEventDispatcher.onAnnouncement(announcement);
        }
        return announcement;
    }

    private BannerInstructions retrieveInstructionsFromBannerEvent(BannerInstructions instructions) {
        if (navigationViewEventDispatcher != null) {
            instructions = navigationViewEventDispatcher.onBannerDisplay(instructions);
        }
        return instructions;
    }
}