package org.maplibre.navigation.android.navigation.ui.v5

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.annotation.UiThread
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.core.utils.TextUtils
import okhttp3.Request
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Projection
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.ui.v5.camera.NavigationCamera
import org.maplibre.navigation.android.navigation.ui.v5.instruction.ImageCreator
import org.maplibre.navigation.android.navigation.ui.v5.instruction.InstructionView
import org.maplibre.navigation.android.navigation.ui.v5.instruction.NavigationAlertView
import org.maplibre.navigation.android.navigation.ui.v5.map.NavigationMapLibreMap
import org.maplibre.navigation.android.navigation.ui.v5.map.NavigationMapLibreMapInstanceState
import org.maplibre.navigation.android.navigation.ui.v5.map.WayNameView
import org.maplibre.navigation.android.navigation.ui.v5.route.NavigationMapRoute
import org.maplibre.navigation.android.navigation.ui.v5.route.NavigationRoute
import org.maplibre.navigation.android.navigation.ui.v5.summary.SummaryBottomSheet
import org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigation
import org.maplibre.navigation.android.navigation.v5.utils.DistanceFormatter
import org.maplibre.navigation.android.navigation.v5.utils.LocaleUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * View that creates the drop-in UI.
 *
 *
 * Once started, this view will check if the [Activity] that inflated
 * it was launched with a [DirectionsRoute].
 *
 *
 * Or, if not found, this view will look for a set of [Point] coordinates.
 * In the latter case, a new [DirectionsRoute] will be retrieved from [NavigationRoute].
 *
 *
 * Once valid data is obtained, this activity will immediately begin navigation
 * with [MapboxNavigation].
 *
 *
 * If launched with the simulation boolean set to true, a [ReplayRouteLocationEngine]
 * will be initialized and begin pushing updates.
 *
 *
 * This activity requires user permissions ACCESS_FINE_LOCATION
 * and ACCESS_COARSE_LOCATION have already been granted.
 *
 *
 * A Mapbox access token must also be set by the developer (to initialize navigation).
 *
 * @since 0.7.0
 */
class NavigationRouteView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) :
    CoordinatorLayout(context!!, attrs, defStyleAttr), LifecycleOwner, OnMapReadyCallback,
    NavigationContract.View {
    private var mapView: MapView? = null
    private var instructionView: InstructionView? = null
    private var summaryBehavior: BottomSheetBehavior<*>? = null
    private var recenterBtn: RecenterButton? = null
    private var wayNameView: WayNameView? = null

    private var navigationPresenter: NavigationPresenter? = null
    private var navigationViewEventDispatcher: NavigationViewEventDispatcher? = null
    private var navigationViewModel: NavigationViewModel? = null
    private var navigationMap: NavigationMapLibreMap? = null
    private var onNavigationReadyCallback: OnNavigationReadyCallback? = null
    private var onTrackingChangedListener: NavigationOnCameraTrackingChangedListener? = null
    private var mapInstanceState: NavigationMapLibreMapInstanceState? = null
    private var initialMapCameraPosition: CameraPosition? = null
    private var isMapInitialized = false
    private var isSubscribed = false
    private var lifecycleRegistry: LifecycleRegistry? = null
    public var mapboxMap: MapLibreMap? = null
    private var locationComponent: LocationComponent? = null
    private var route: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var baseCameraPosition: CameraPosition? = null
    private var isMapReinitialized: Boolean = false
    private var mapStyle: String? = null
    private var attrs: AttributeSet? = null

    init {
        this.attrs = attrs
    }

    /**
     * Uses savedInstanceState as a cue to restore state (if not null).
     *
     * @param savedInstanceState to restore state if not null
     */
    fun onCreate(navigationCallback: OnNavigationReadyCallback, savedInstanceState: Bundle?, context: ComponentActivity, style: String) {
        ThemeSwitcher.setTheme(context, attrs, style)
        initializeNavigationViewModel(context)
        initializeView()
        mapStyle = style
        mapView?.let {
            it.apply {
                if (!isMapInitialized) {
                    it.getMapAsync(this@NavigationRouteView)
                    it.onCreate(savedInstanceState)
                } else {
                    onNavigationReadyCallback!!.onNavigationReady(navigationViewModel!!.isRunning)
                }
            }
        }
        updatePresenterState(savedInstanceState)
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry!!.markState(Lifecycle.State.CREATED)
        this.onNavigationReadyCallback = navigationCallback
        instructionView?.setupStyle(style)
    }

    /**
     * Low memory must be reported so the [MapView]
     * can react appropriately.
     */
    fun onLowMemory() {
        mapView!!.onLowMemory()
    }

    /**
     * If the instruction list is showing and onBackPressed is called,
     * hide the instruction list and do not hide the activity or fragment.
     *
     * @return true if back press handled, false if not
     */
    fun onBackPressed(): Boolean {
        return instructionView!!.handleBackPressed()
    }

    /**
     * Used to store the bottomsheet state and re-center
     * button visibility.  As well as anything the [MapView]
     * needs to store in the bundle.
     *
     * @param outState to store state variables
     */
    fun onSaveInstanceState(outState: Bundle) {
        val bottomSheetBehaviorState =
            if (summaryBehavior == null) INVALID_STATE else summaryBehavior!!.state
        val isWayNameVisible = wayNameView!!.visibility == VISIBLE
        val navigationViewInstanceState = NavigationViewInstanceState(
            bottomSheetBehaviorState,
            recenterBtn!!.visibility,
            instructionView!!.isShowingInstructionList,
            isWayNameVisible,
            wayNameView!!.retrieveWayNameText(),
            navigationViewModel!!.isMuted
        )
        val instanceKey = context.getString(R.string.navigation_view_instance_state)
        outState.putParcelable(instanceKey, navigationViewInstanceState)
        outState.putBoolean(
            context.getString(R.string.navigation_running),
            navigationViewModel!!.isRunning
        )
        mapView!!.onSaveInstanceState(outState)
        saveNavigationMapInstanceState(outState)
    }

    /**
     * Used to restore the bottomsheet state and re-center
     * button visibility.  As well as the [MapView]
     * position prior to rotation.
     *
     * @param savedInstanceState to extract state variables
     */
    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val instanceKey = context.getString(R.string.navigation_view_instance_state)
        val navigationViewInstanceState =
            savedInstanceState.getParcelable<NavigationViewInstanceState>(instanceKey)
        recenterBtn!!.visibility = navigationViewInstanceState!!.recenterButtonVisibility
        wayNameView!!.visibility =
            if (navigationViewInstanceState.isWayNameVisible) VISIBLE else INVISIBLE
        wayNameView!!.updateWayNameText(navigationViewInstanceState.wayNameText)
        resetBottomSheetState(navigationViewInstanceState.bottomSheetBehaviorState)
        updateInstructionListState(navigationViewInstanceState.isInstructionViewVisible)
        updateInstructionMutedState(navigationViewInstanceState.isMuted)
        mapInstanceState = savedInstanceState.getParcelable(MAP_INSTANCE_STATE_KEY)
    }

    /**
     * Called to ensure the [MapView] is destroyed
     * properly.
     *
     *
     * In an [Activity] this should be in [Activity.onDestroy].
     *
     *
     * In a [Fragment], this should
     * be in [Fragment.onDestroyView].
     */
    @UiThread
    fun onDestroy() {
//        shutdown()
        stopNavigation()
        lifecycleRegistry!!.markState(Lifecycle.State.DESTROYED)
    }

    fun onDestroy2() {
        shutdown()
    }

    fun onStart() {
        mapView!!.onStart()
        if (navigationMap != null) {
            navigationMap!!.onStart()
        }
        lifecycleRegistry!!.markState(Lifecycle.State.STARTED)
    }

    fun onResume() {
        mapView!!.onResume()
        lifecycleRegistry!!.markState(Lifecycle.State.RESUMED)
    }

    fun onPause() {
        mapView!!.onPause()
    }

    fun onStop() {
        mapView!!.onStop()
        if (navigationMap != null) {
            navigationMap!!.onStop()
        }
    }

    /**
     * Fired after the map is ready, this is our cue to finish
     * setting up the rest of the plugins / location engine.
     *
     *
     * Also, we check for launch data (coordinates or route).
     *
     * @param mapboxMap used for route, camera, and location UI
     * @since 0.6.0
     */
    override fun onMapReady(mapboxMap: MapLibreMap) {
        val builder = Style.Builder().fromUri(
            mapStyle!!
        )
        this.mapboxMap = mapboxMap
        if (!isMapInitialized) {
            mapboxMap.setStyle((builder)) {
                isMapInitialized = true
                enableLocationComponent(it)
                baseCameraPosition?.let {
                    mapboxMap.cameraPosition = it
                }
                navigationMapRoute = NavigationMapRoute(
                    mapView!!,
                    mapboxMap
                )
            }
        } else {
            mapboxMap.getStyle {
                enableLocationComponent(it)
                baseCameraPosition?.let {
                    mapboxMap.cameraPosition = it
                }
                navigationMapRoute = NavigationMapRoute(
                    mapView!!,
                    mapboxMap
                )
            }
        }
        onNavigationReadyCallback!!.onMapReadyCallback()
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        // Get an instance of the component
        locationComponent = mapboxMap!!.locationComponent

        locationComponent?.let {
            // Activate with a built LocationComponentActivationOptions object
            it.activateLocationComponent(
                LocationComponentActivationOptions.builder(context, style).build(),
            )

            // Enable to make component visible
            it.isLocationComponentEnabled = true

            // Set the component's camera mode
            it.cameraMode = CameraMode.TRACKING_GPS_NORTH

            // Set the component's render mode
            it.renderMode = RenderMode.NORMAL
        }
    }

    fun calculateRoute(mapRouteData: MapRouteData) {
//        startRouteButton.setOnClickListener {
            val destination = Point.fromLngLat(76.930137, 43.230361)
            val origin = Point.fromLngLat(mapRouteData.userLocation.first, mapRouteData.userLocation.second)
            val navigationRouteBuilder = NavigationRoute.builder(context).apply {
                this.accessToken(mapRouteData.accessToken)
                this.origin(origin)

                this.voiceUnits(DirectionsCriteria.PROFILE_DRIVING)
                this.alternatives(true)
                // If you are using this with the GraphHopper Directions API, you need to uncomment user and profile here.
                this.user("gh")
                this.profile("car")
                this.baseUrl(context.getString(R.string.base_url))
            }
            for (point in mapRouteData.routeList) {
                navigationRouteBuilder.addWaypoint(
                    Point.fromLngLat(point.first, point.second)
                )
            }
            navigationRouteBuilder.build().getRoute(object : Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>,
                ) {
                    Timber.d("Url: %s", (call.request() as Request).url.toString())
                    response.body()?.let { response ->
                        if (response.routes.isNotEmpty()) {
                            val maplibreResponse =
                                DirectionsResponse.fromJson(
                                    response.toJson()
                                );
                            this@NavigationRouteView.route = maplibreResponse.routes.first()
                            navigationMapRoute?.addRoutes(maplibreResponse.routes)

                            val options = NavigationLauncherOptions.builder()
                                .directionsRoute(route)
//                    .shouldSimulateRoute(simulateRoute)
                                .initialMapCameraPosition(
                                    CameraPosition.Builder().target(
                                        LatLng(
                                            mapRouteData.userLocation.first,
                                            mapRouteData.userLocation.second
                                        )
                                    ).build()
                                )
                                .lightThemeResId(R.style.TestNavigationViewLight)
                                .darkThemeResId(R.style.TestNavigationViewDark)
                                .build()
                            baseCameraPosition = mapboxMap!!.cameraPosition
                            NavigationLauncher.startNavigation(context, options)
                            route?.let {
                                if (isMapReinitialized) {
                                    onNavigationReadyCallback!!.onNavigationReady(navigationViewModel!!.isRunning)
                                } else {
                                    isMapReinitialized = true
                                    if (options.initialMapCameraPosition() != null) {
                                        initialize(
                                            options.initialMapCameraPosition()!!
                                        )
                                    } else {
                                        initialize()
                                    }
                                }
                            }
//                        binding.startRouteLayout.visibility = View.VISIBLE
                        }
                    }

                }

                override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                    Timber.e(throwable, "onFailure: navigation.getRoute()")
                }
            })
    }

    override fun setSummaryBehaviorState(state: Int) {
        summaryBehavior!!.state = state
    }

    override fun setSummaryBehaviorHideable(isHideable: Boolean) {
        summaryBehavior!!.isHideable = isHideable
    }

    override fun isSummaryBottomSheetHidden(): Boolean {
        return summaryBehavior!!.state == BottomSheetBehavior.STATE_HIDDEN
    }

    override fun resetCameraPosition() {
        if (navigationMap != null) {
            navigationMap!!.resetPadding()
            navigationMap!!.resetCameraPositionWith(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS)
        }
    }

    override fun showRecenterBtn() {
        recenterBtn!!.show()
    }

    override fun hideRecenterBtn() {
        recenterBtn!!.hide()
    }

    override fun isRecenterButtonVisible(): Boolean {
        return recenterBtn!!.visibility == VISIBLE
    }

    override fun drawRoute(directionsRoute: DirectionsRoute) {
        if (navigationMap != null) {
            navigationMap!!.drawRoute(directionsRoute)
        }
    }

    override fun addMarker(position: Point) {
        if (navigationMap != null) {
            navigationMap!!.addDestinationMarker(position)
        }
    }

    val isWayNameVisible: Boolean
        /**
         * Provides the current visibility of the way name view.
         *
         * @return true if visible, false if not visible
         */
        get() = wayNameView!!.visibility == VISIBLE

    /**
     * Updates the text of the way name view below the
     * navigation icon.
     *
     *
     * If you'd like to use this method without being overridden by the default way names
     * values we provide, please disabled auto-query with
     * [NavigationMapboxMap.updateWaynameQueryMap].
     *
     * @param wayName to update the view
     */
    override fun updateWayNameView(wayName: String) {
        wayNameView!!.updateWayNameText(wayName)
    }

    /**
     * Updates the visibility of the way name view that is show below
     * the navigation icon.
     *
     *
     * If you'd like to use this method without being overridden by the default visibility values
     * values we provide, please disabled auto-query with
     * [NavigationMapboxMap.updateWaynameQueryMap].
     *
     * @param isVisible true to show, false to hide
     */
    override fun updateWayNameVisibility(isVisible: Boolean) {
        var isVisible = isVisible
        if (TextUtils.isEmpty(wayNameView!!.retrieveWayNameText())) {
            isVisible = false
        }
        wayNameView!!.updateVisibility(isVisible)
        if (navigationMap != null) {
            navigationMap!!.updateWaynameQueryMap(isVisible)
        }
    }

    /**
     * Used when starting this [android.app.Activity]
     * for the first time.
     *
     *
     * Zooms to the beginning of the [DirectionsRoute].
     *
     * @param directionsRoute where camera should move to
     */
    override fun startCamera(directionsRoute: DirectionsRoute) {
        if (navigationMap != null) {
            navigationMap!!.startCamera(directionsRoute)
        }
    }

    /**
     * Used after configuration changes to resume the camera
     * to the last location update from the Navigation SDK.
     *
     * @param location where the camera should move to
     */
    override fun resumeCamera(location: Location) {
        if (navigationMap != null) {
            navigationMap!!.resumeCamera(location)
        }
    }

    override fun updateNavigationMap(location: Location) {
        if (navigationMap != null) {
            navigationMap!!.updateLocation(location)
        }
    }

    override fun updateCameraRouteOverview() {
        if (navigationMap != null) {
            val padding = buildRouteOverviewPadding(context)
            navigationMap!!.showRouteOverview(padding)
        }
    }

    /**
     * Should be called when this view is completely initialized.
     *
     * @param options with containing route / coordinate data
     */
    fun startNavigation(options: NavigationViewOptions) {
        initializeNavigation(options)
    }

    /**
     * Call this when the navigation session needs to end navigation without finishing the whole view
     *
     * @since 0.16.0
     */
    @UiThread
    fun stopNavigation() {
        instructionView!!.visibility = GONE
        navigationPresenter!!.onNavigationStopped()
        navigationViewModel!!.stopNavigation()
        baseCameraPosition?.let {
            mapboxMap!!.cameraPosition = it
        }
        if (navigationMap != null) {
            navigationMap!!.removeOnCameraTrackingChangedListener(onTrackingChangedListener)
            navigationMap!!.removeRoute()
            navigationMap!!.clearMarkers()
        }
        mapboxMap!!.markers.forEach {
            mapboxMap!!.removeMarker(it)
        }
        navigationMapRoute?.removeRoute()

    }

    /**
     * Should be called after [NavigationView.onCreate].
     *
     *
     * This method adds the [OnNavigationReadyCallback],
     * which will fire the ready events for this view.
     *
     * @param onNavigationReadyCallback to be set to this view
     */
    fun initialize() {
        if (!isMapInitialized) {
            mapView!!.getMapAsync(this)
        } else {
            initializeNavigationMap(mapView, mapboxMap!!)
            initializeWayNameListener()
            onNavigationReadyCallback!!.onNavigationReady(navigationViewModel!!.isRunning)
        }
    }

    /**
     * Should be called after [NavigationView.onCreate].
     *
     *
     * This method adds the [OnNavigationReadyCallback],
     * which will fire the ready events for this view.
     *
     *
     * This method also accepts a [CameraPosition] that will be set as soon as the map is
     * ready.  Note, this position is ignored during rotation in favor of the last known map position.
     *
     * @param onNavigationReadyCallback to be set to this view
     * @param initialMapCameraPosition  to be shown once the map is ready
     */
    fun initialize(
        initialMapCameraPosition: CameraPosition
    ) {
        this.initialMapCameraPosition = initialMapCameraPosition
        if (!isMapInitialized) {
            mapView!!.getMapAsync(this)
        } else {
            initializeNavigationMap(mapView, mapboxMap!!)
            initializeWayNameListener()
            onNavigationReadyCallback!!.onNavigationReady(navigationViewModel!!.isRunning)
        }
    }

    /**
     * Gives the ability to manipulate the map directly for anything that might not currently be
     * supported. This returns null until the view is initialized.
     *
     *
     * The [NavigationMapboxMap] gives direct access to the map UI (location marker, route, etc.).
     *
     * @return navigation mapbox map object, or null if view has not been initialized
     */
    fun retrieveNavigationMapboxMap(): NavigationMapLibreMap? {
        return navigationMap
    }

    /**
     * Returns the instance of [MapboxNavigation] powering the [NavigationView]
     * once navigation has started.  Will return null if navigation has not been started with
     * [NavigationView.startNavigation].
     *
     * @return mapbox navigation, or null if navigation has not started
     */
    fun retrieveMapboxNavigation(): MapLibreNavigation? {
        return navigationViewModel!!.retrieveNavigation()
    }

    /**
     * Returns the sound button used for muting instructions
     *
     * @return sound button
     */
    fun retrieveSoundButton(): NavigationButton {
        return instructionView!!.retrieveSoundButton()
    }


    /**
     * Returns the re-center button for recentering on current location
     *
     * @return recenter button
     */
    fun retrieveRecenterButton(): NavigationButton? {
        return recenterBtn
    }

    /**
     * Returns the [NavigationAlertView] that is shown during off-route events with
     * "Report a Problem" text.
     *
     * @return alert view that is used in the instruction view
     */
    fun retrieveAlertView(): NavigationAlertView {
        return instructionView!!.retrieveAlertView()
    }

    private fun initializeView() {
        inflate(context, R.layout.navigation_view_layout, this)
        bind()
//        initializeNavigationViewModel()
        initializeNavigationEventDispatcher()
        initializeNavigationPresenter()
        initializeInstructionListListener()
        initializeClickListeners()
    }

    private fun bind() {
        mapView = findViewById(R.id.navigationMapView)
        instructionView = findViewById(R.id.instructionView)
        instructionView?.let {
            ViewCompat.setElevation(it, 10f)
        }
        recenterBtn = findViewById(R.id.recenterBtn)
        wayNameView = findViewById(R.id.wayNameView)
    }

    fun initializeNavigationViewModel(context: ComponentActivity) {
        try {
            navigationViewModel = NavigationViewModel(context)
        } catch (exception: ClassCastException) {
            throw ClassCastException("Please ensure that the provided Context is a valid FragmentActivity")
        }
    }

    private fun initializeNavigationEventDispatcher() {
        navigationViewEventDispatcher = NavigationViewEventDispatcher()
        navigationViewModel!!.initializeEventDispatcher(navigationViewEventDispatcher)
    }

    private fun initializeInstructionListListener() {
        instructionView!!.setInstructionListListener(
            NavigationInstructionListListener(
                navigationPresenter,
                navigationViewEventDispatcher
            )
        )
    }

    private fun initializeNavigationMap(mapView: MapView?, map: MapLibreMap) {
        if (initialMapCameraPosition != null) {
            map.cameraPosition = initialMapCameraPosition!!
        }
        navigationMap = NavigationMapLibreMap(mapView!!, map)
        navigationMap!!.updateLocationLayerRenderMode(RenderMode.GPS)
        if (mapInstanceState != null) {
            navigationMap!!.restoreFrom(mapInstanceState)
            return
        }
    }

    private fun initializeWayNameListener() {
        val wayNameListener = NavigationViewWayNameListener(navigationPresenter)
        navigationMap!!.addOnWayNameChangedListener(wayNameListener)
    }

    private fun saveNavigationMapInstanceState(outState: Bundle) {
        if (navigationMap != null) {
            navigationMap!!.saveStateWith(MAP_INSTANCE_STATE_KEY, outState)
        }
    }

    private fun resetBottomSheetState(bottomSheetState: Int) {
        if (bottomSheetState > INVALID_STATE) {
            val isShowing = bottomSheetState == BottomSheetBehavior.STATE_EXPANDED
            summaryBehavior!!.isHideable = !isShowing
            summaryBehavior!!.state = bottomSheetState
        }
    }

    private fun updateInstructionListState(visible: Boolean) {
        if (visible) {
            instructionView!!.showInstructionList()
        } else {
            instructionView!!.hideInstructionList()
        }
    }

    private fun updateInstructionMutedState(isMuted: Boolean) {
        if (isMuted) {
            (instructionView!!.retrieveSoundButton() as SoundButton).soundFabOff()
        }
    }

    private fun buildRouteOverviewPadding(context: Context): IntArray {
        val resources = context.resources
        val leftRightPadding =
            resources.getDimension(R.dimen.route_overview_left_right_padding).toInt()
        val paddingBuffer = resources.getDimension(R.dimen.route_overview_buffer_padding).toInt()
        val instructionHeight =
            (resources.getDimension(R.dimen.instruction_layout_height) + paddingBuffer).toInt()
        val summaryHeight = resources.getDimension(R.dimen.summary_bottomsheet_height).toInt()
        return intArrayOf(leftRightPadding, instructionHeight, leftRightPadding, summaryHeight)
    }

    private val isChangingConfigurations: Boolean
        get() {
            try {
                return (context as FragmentActivity).isChangingConfigurations
            } catch (exception: ClassCastException) {
                throw ClassCastException("Please ensure that the provided Context is a valid FragmentActivity")
            }
        }

    private fun initializeNavigationPresenter() {
        navigationPresenter = NavigationPresenter(this)
    }

    private fun updatePresenterState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val navigationRunningKey = context.getString(R.string.navigation_running)
            val resumeState = savedInstanceState.getBoolean(navigationRunningKey)
            navigationPresenter!!.updateResumeState(resumeState)
        }
    }

    private fun initializeNavigation(options: NavigationViewOptions) {
        establish(options)
        navigationViewModel!!.initialize(options)
        initializeNavigationListeners(options, navigationViewModel)
        setupNavigationMapboxMap(options)

        if (!isSubscribed) {
            initializeClickListeners()
            initializeOnCameraTrackingChangedListener()
            subscribeViewModels()
        }
        instructionView!!.visibility = VISIBLE
    }

    private fun initializeClickListeners() {
        recenterBtn!!.addOnClickListener(RecenterBtnClickListener(navigationPresenter))
    }

    private fun initializeOnCameraTrackingChangedListener() {
        onTrackingChangedListener =
            NavigationOnCameraTrackingChangedListener(navigationPresenter, summaryBehavior)
        navigationMap!!.addOnCameraTrackingChangedListener(onTrackingChangedListener)
    }

    private fun establish(options: NavigationViewOptions) {
        val localeUtils = LocaleUtils()
        establishDistanceFormatter(localeUtils, options)
        establishTimeFormat(options)
    }

    private fun establishDistanceFormatter(
        localeUtils: LocaleUtils,
        options: NavigationViewOptions
    ) {
        val unitType = establishUnitType(localeUtils, options)
        val language = establishLanguage(localeUtils, options)
        val roundingIncrement = establishRoundingIncrement(options)
        val distanceFormatter = DistanceFormatter(context, language, unitType, roundingIncrement)

        instructionView!!.setDistanceFormatter(distanceFormatter)
    }

    private fun establishRoundingIncrement(navigationViewOptions: NavigationViewOptions): Int {
        val mapboxNavigationOptions = navigationViewOptions.navigationOptions()
        return mapboxNavigationOptions.roundingIncrement
    }

    private fun establishLanguage(
        localeUtils: LocaleUtils,
        options: NavigationViewOptions
    ): String {
        return localeUtils.getNonEmptyLanguage(context, options.directionsRoute().voiceLanguage)
    }

    private fun establishUnitType(
        localeUtils: LocaleUtils,
        options: NavigationViewOptions
    ): String {
        val routeOptions = options.directionsRoute().routeOptions
        val voiceUnits = routeOptions?.voiceUnits
        return localeUtils.retrieveNonNullUnitType(context, voiceUnits)
    }

    private fun establishTimeFormat(options: NavigationViewOptions) {
    }

    private fun initializeNavigationListeners(
        options: NavigationViewOptions,
        navigationViewModel: NavigationViewModel?
    ) {
        navigationMap!!.addProgressChangeListener(navigationViewModel!!.retrieveNavigation()!!)
        navigationViewEventDispatcher!!.initializeListeners(options, navigationViewModel)
    }

    private fun setupNavigationMapboxMap(options: NavigationViewOptions) {
        navigationMap!!.updateWaynameQueryMap(options.waynameChipEnabled())
    }

    /**
     * Subscribes the [InstructionView] and [SummaryBottomSheet] to the [NavigationViewModel].
     *
     *
     * Then, creates an instance of [NavigationViewSubscriber], which takes a presenter.
     *
     *
     * The subscriber then subscribes to the view models, setting up the appropriate presenter / listener
     * method calls based on the [androidx.lifecycle.LiveData] updates.
     */
    private fun subscribeViewModels() {
        instructionView!!.subscribe(this, navigationViewModel)

        NavigationViewSubscriber(this, navigationViewModel, navigationPresenter).subscribe()
        isSubscribed = true
    }

    @SuppressLint("MissingPermission")
    private fun shutdown() {
        if (navigationMap != null) {
            navigationMap!!.removeOnCameraTrackingChangedListener(onTrackingChangedListener)
            navigationMap!!.removeRoute()
        }
//        navigationViewModel?.let {
//            it.isOffRoute.removeObservers(this)
//            it.instructionModel.removeObservers(this)
//            it.bannerInstructionModel.removeObservers(this)
//            it.summaryModel.removeObservers(this)
//            it.retrieveNavigationLocation().removeObservers(this)
//            it.retrieveRoute().removeObservers(this)
//            it.retrieveShouldRecordScreenshot().removeObservers(this)
//            it.retrieveDestination().removeObservers(this)
//        }
        isMapInitialized = false
        NavigationViewSubscriber(this, navigationViewModel, navigationPresenter).unsubscribe()
        navigationViewModel!!.stopNavigation()
        mapboxMap!!.markers.forEach {
            mapboxMap!!.removeMarker(it)
        }
        navigationViewEventDispatcher!!.onDestroy(navigationViewModel!!.retrieveNavigation())
        mapboxMap!!.getStyle {
            mapboxMap!!.locationComponent.isLocationComponentEnabled = false
        }
//        mapView!!.onDestroy()
//        mapView!!.getMapAsync(this)

        baseCameraPosition?.let {
            mapboxMap!!.cameraPosition = it
        }

        navigationViewModel!!.onDestroy(false)
        ImageCreator.getInstance().shutdown()
        navigationMap = null

        navigationMapRoute?.removeRoute()

        isSubscribed = false
    }

    private fun shutDown2() {
        if (navigationMap != null) {
            navigationMap!!.removeOnCameraTrackingChangedListener(onTrackingChangedListener)
        }
        NavigationViewSubscriber(this, navigationViewModel, navigationPresenter).unsubscribe()
        navigationViewEventDispatcher!!.onDestroy(navigationViewModel!!.retrieveNavigation())
//        mapView!!.onDestroy()
        navigationViewModel!!.onDestroy(false)
        ImageCreator.getInstance().shutdown()
        navigationMap = null
    }

    fun setMinZoomLevel(minZoomLevel: Double) {
        mapboxMap!!.setMinZoomPreference(minZoomLevel)
    }

    fun setMaxZoomLevel(maxZoomLevel: Double) {
        mapboxMap!!.setMaxZoomPreference(maxZoomLevel)
    }

    fun getProjection() : Projection {
        return mapboxMap!!.projection
    }

    fun addMarker(icon: Int, latitude: Double, longitude: Double, id: String) {
        mapboxMap!!.addMarker(MarkerOptions())
//        val icon = Icon(id, BitmapFactory.decodeResource(resources, icon))
//        val markerOptions = MarkerOptions()
//            .position(position)
//            .icon(icon)
//        mapboxMap!!.setOnMarkerClickListener {
//        }
    }

    companion object {
        private const val MAP_INSTANCE_STATE_KEY = "navgation_mapbox_map_instance_state"
        private const val INVALID_STATE = 0
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry!!

}