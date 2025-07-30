package org.maplibre.navigation.android.navigation.ui.v5

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.annotation.UiThread
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import okhttp3.Request
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.maps.Style.OnStyleLoaded
import org.maplibre.android.plugins.annotation.OnSymbolClickListener
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
import org.maplibre.navigation.android.navigation.ui.v5.camera.NavigationCamera
import org.maplibre.navigation.android.navigation.ui.v5.instruction.ImageCreator
import org.maplibre.navigation.android.navigation.ui.v5.instruction.InstructionView
import org.maplibre.navigation.android.navigation.ui.v5.instruction.NavigationAlertView
import org.maplibre.navigation.android.navigation.ui.v5.map.NavigationMapLibreMap
import org.maplibre.navigation.android.navigation.ui.v5.map.NavigationMapLibreMapInstanceState
import org.maplibre.navigation.android.navigation.ui.v5.map.WayNameView
import org.maplibre.navigation.android.navigation.ui.v5.route.NavigationRoute
import org.maplibre.navigation.android.navigation.ui.v5.utils.DistanceFormatter
import org.maplibre.navigation.android.navigation.ui.v5.utils.LocaleUtils
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.models.DirectionsResponse
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.models.UnitType
import org.maplibre.navigation.core.navigation.MapLibreNavigation
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class NavigationRouteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : CoordinatorLayout(context, attrs, defStyleAttr), LifecycleOwner, OnMapReadyCallback,
    NavigationContract.View {
    private lateinit var mapView: MapView
    private lateinit var instructionView: InstructionView
    private lateinit var recenterBtn: RecenterButton
    private lateinit var wayNameView: WayNameView

    private lateinit var navigationPresenter: NavigationPresenter
    private var navigationViewEventDispatcher: NavigationViewEventDispatcher? = null
    private lateinit var navigationViewModel: NavigationViewModel
    private var navigationMap: NavigationMapLibreMap? = null
    private var preNavigationLocationEngine: PreNavigationLocationEngine? = null
    private var navigationRoute: NavigationRoute? = null
    private var onTrackingChangedListener: NavigationOnCameraTrackingChangedListener? = null
    private var mapInstanceState: NavigationMapLibreMapInstanceState? = null
    private var isMapInitialized = false
    private var isSubscribed = false
    private lateinit var lifecycleRegistry: LifecycleRegistry
    private var onMapReadyCallback: OnMapReadyCallback? = null
    private var symbolManager: SymbolManager? = null

    private var mapStyleUri: String? = null

    init {
        ThemeSwitcher.setTheme(context, attrs)
        initializeView()
    }

    /**
     * Uses savedInstanceState as a cue to restore state (if not null).
     *
     * @param savedInstanceState to restore state if not null
     */
    fun onCreate(
        savedInstanceState: Bundle?,
        mapStyleUri: String
    ) {
        mapView.onCreate(savedInstanceState)
        updatePresenterState(savedInstanceState)
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        if (mapStyleUri != null) {
            this.mapStyleUri = mapStyleUri
        }
    }

    /**
     * Low memory must be reported so the [MapView]
     * can react appropriately.
     */
    fun onLowMemory() {
        mapView.onLowMemory()
    }

    /**
     * If the instruction list is showing and onBackPressed is called,
     * hide the instruction list and do not hide the activity or fragment.
     *
     * @return true if back press handled, false if not
     */
    fun onBackPressed(): Boolean {
        return instructionView.handleBackPressed()
    }

    /**
     * Used to store the bottomsheet state and re-center
     * button visibility.  As well as anything the [MapView]
     * needs to store in the bundle.
     *
     * @param outState to store state variables
     */
    fun onSaveInstanceState(outState: Bundle) {
        val isWayNameVisible = wayNameView.isVisible
        val navigationViewInstanceState = NavigationViewInstanceState(
            recenterBtn.visibility,
            instructionView.isShowingInstructionList,
            isWayNameVisible,
            wayNameView.retrieveWayNameText(),
            navigationViewModel.isMuted
        )
        val instanceKey = context.getString(R.string.navigation_view_instance_state)
        outState.putParcelable(instanceKey, navigationViewInstanceState)
        outState.putBoolean(
            context.getString(R.string.navigation_running),
            navigationViewModel.isRunning
        )
        mapView.onSaveInstanceState(outState)
        saveNavigationMapInstanceState(outState)
    }

    /**
     * Used to re-center
     * button visibility.  As well as the [MapView]
     * position prior to rotation.
     *
     * @param savedInstanceState to extract state variables
     */
    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val instanceKey = context.getString(R.string.navigation_view_instance_state)
        val navigationViewInstanceState =
            savedInstanceState.getParcelable<NavigationViewInstanceState>(instanceKey)
        recenterBtn.visibility = navigationViewInstanceState?.recenterButtonVisibility ?: View.GONE
        wayNameView.visibility =
            if (navigationViewInstanceState?.isWayNameVisible == true) VISIBLE else INVISIBLE
        wayNameView.updateWayNameText(navigationViewInstanceState?.wayNameText)
        updateInstructionListState(navigationViewInstanceState?.isInstructionViewVisible ?: false)
        updateInstructionMutedState(navigationViewInstanceState?.isMuted ?: true)
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
        shutdown()
        stopNavigation()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    fun onStart() {
        mapView.onStart()
        navigationMap?.onStart()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun onResume() {
        mapView.onResume()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onPause() {
        mapView.onPause()
    }

    fun onStop() {
        mapView.onStop()
        navigationMap?.onStop()
    }

    /**
     * Fired after the map is ready, this is our cue to finish
     * setting up the rest of the plugins / location engine.
     *
     *
     * Also, we check for launch data (coordinates or route).
     *
     * @param mapLibreMap used for route, camera, and location UI
     * @since 0.6.0
     */
    override fun onMapReady(mapLibreMap: MapLibreMap) {
        val onStyleLoaded = OnStyleLoaded { style ->
            initializeSymbolManager(mapView, mapLibreMap, style)
            initializeNavigationMap(mapView, mapLibreMap)
            initializeWayNameListener()
            initializePreNavigationLocationEngine(mapLibreMap)
            onMapReadyCallback?.onMapReady(mapLibreMap)
            isMapInitialized = true
        }

        mapStyleUri?.let { mapLibreMap.setStyle(Style.Builder().fromUri(it), onStyleLoaded) }
            ?: mapLibreMap.setStyle(ThemeSwitcher.retrieveMapStyle(context), onStyleLoaded)
    }

    fun calculateRouteAndStartNavigation(request: NavigationRequest) {
        val navigationSource = request.routingService
        val navigationRouteBuilder = NavigationRoute.builder(context).apply {
            this.origin(request.origin)
            this.destination(request.destination)
            request.stops?.forEach { this.addWaypoint(it) }
            this.voiceUnits(UnitType.METRIC)
            this.language(request.language)
            this.alternatives(true)
            // If you are using this with the GraphHopper Directions API, you need to uncomment user and profile here.
            if (navigationSource is RoutingService.GraphHopper) {
                this.user("gh")
                this.profile("car")
            }
            this.accessToken(navigationSource.accessToken)
            this.baseUrl(navigationSource.baseUrl)
        }
        navigationRoute = navigationRouteBuilder.build()
        navigationRoute?.getRoute(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>,
            ) {
                Timber.d("Response: $response")
                response.body()?.let { responseBody ->
                    if (responseBody.routes.isNotEmpty()) {
                        val maplibreResponse = DirectionsResponse.fromJson(responseBody.toJson())
                        startNavigation(maplibreResponse.routes, request.navigationOptions)
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                Timber.e(throwable, "onFailure: navigation.getRoute()")
            }
        })
    }

    private fun startNavigation(
        routes: List<DirectionsRoute>,
        navigationOptions: MapLibreNavigationOptions
    ) {
        preNavigationLocationEngine?.stop()
        val route = routes.first()
        navigationMap?.drawRoutes(routes)
        val options = NavigationViewOptions.builder()
        options.directionsRoute(route)
        options.navigationOptions(navigationOptions)
        initializeNavigation(options.build())
    }

    override fun resetCameraPosition() {
        navigationMap?.resetPadding()
        navigationMap?.resetCameraPositionWith(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS)
    }

    override fun showRecenterBtn() {
        recenterBtn.show()
    }

    override fun hideRecenterBtn() {
        recenterBtn.hide()
    }

    override fun isRecenterButtonVisible(): Boolean {
        return recenterBtn.isVisible
    }

    override fun drawRoute(directionsRoute: DirectionsRoute) {
        navigationMap?.drawRoute(directionsRoute)
    }

    fun addSymbol(symbolOptions: SymbolOptions): Symbol {
        return symbolManager?.create(symbolOptions)
            ?: throw RuntimeException("Map is not initialized")
    }

    fun removeSymbol(symbol: Symbol) {
        symbolManager?.delete(symbol)
    }

    fun addOnSymbolClickListener(listener: OnSymbolClickListener) {
        symbolManager?.addClickListener(listener)
    }

    fun updateSymbol(symbol: Symbol) {
        symbolManager?.update(symbol)
    }

    val isWayNameVisible: Boolean
        /**
         * Provides the current visibility of the way name view.
         *
         * @return true if visible, false if not visible
         */
        get() = wayNameView.isVisible

    /**
     * Updates the text of the way name view below the
     * navigation icon.
     *
     *
     * If you'd like to use this method without being overridden by the default way names
     * values we provide, please disabled auto-query with
     * [NavigationMapLibreMap.updateWaynameQueryMap].
     *
     * @param wayName to update the view
     */
    override fun updateWayNameView(wayName: String) {
        wayNameView.updateWayNameText(wayName)
    }

    /**
     * Updates the visibility of the way name view that is show below
     * the navigation icon.
     *
     *
     * If you'd like to use this method without being overridden by the default visibility values
     * values we provide, please disabled auto-query with
     * [NavigationMapLibreMap.updateWaynameQueryMap].
     *
     * @param isVisible true to show, false to hide
     */
    override fun updateWayNameVisibility(isVisible: Boolean) {
        var visible = isVisible
        if (wayNameView.retrieveWayNameText().isEmpty()) {
            visible = false
        }
        wayNameView.updateVisibility(visible)
        navigationMap?.updateWaynameQueryMap(visible)
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
        navigationMap?.startCamera(directionsRoute)
    }

    /**
     * Used after configuration changes to resume the camera
     * to the last location update from the Navigation SDK.
     *
     * @param location where the camera should move to
     */
    override fun resumeCamera(location: Location) {
        navigationMap?.resumeCamera(location)
    }

    override fun updateNavigationMap(location: Location) {
        navigationMap?.updateLocation(location)
    }

    override fun updateCameraRouteOverview() {
        val padding = buildRouteOverviewPadding(context)
        navigationMap?.showRouteOverview(padding)
    }

    /**
     * Call this when the navigation session needs to end navigation without finishing the whole view
     *
     * @since 0.16.0
     */
    @UiThread
    fun stopNavigation() {
        preNavigationLocationEngine?.start()
        navigationRoute?.cancelCall()
        navigationPresenter.onNavigationStopped()
        navigationViewModel.stopNavigation()
    }


    /**
     * Should be called after [NavigationView.onCreate].
     */
    fun initialize(
        shouldSimulateRoute: Boolean,
        onMapReadyCallback: OnMapReadyCallback,
    ) {
        this.onMapReadyCallback = onMapReadyCallback
        if (!isMapInitialized) {
            mapView.getMapAsync(this)
            navigationViewModel.initializeNavigation(shouldSimulateRoute)
        }
    }


    fun enableNavigatorSound(enabled: Boolean) {
        navigationViewModel.isMuted = !enabled
    }

    /**
     * Gives the ability to manipulate the map directly for anything that might not currently be
     * supported. This returns null until the view is initialized.
     *
     *
     * The [NavigationMapLibreMap] gives direct access to the map UI (location marker, route, etc.).
     *
     * @return navigation mapbox map object, or null if view has not been initialized
     */
    fun retrieveNavigationMapLibreMap(): NavigationMapLibreMap? {
        return navigationMap
    }

    /**
     * Returns the instance of [MapLibreNavigation] powering the [NavigationView]
     * once navigation has started.  Will return null if navigation has not been started with
     * [NavigationView.startNavigation].
     *
     * @return mapbox navigation, or null if navigation has not started
     */
    fun retrieveMapLibreNavigation(): MapLibreNavigation? {
        return navigationViewModel.retrieveNavigation()
    }

    /**
     * Returns the sound button used for muting instructions
     *
     * @return sound button
     */
    fun retrieveSoundButton(): NavigationButton {
        return instructionView.retrieveSoundButton()
    }


    /**
     * Returns the re-center button for recentering on current location
     *
     * @return recenter button
     */
    fun retrieveRecenterButton(): NavigationButton {
        return recenterBtn
    }

    /**
     * Returns the [NavigationAlertView] that is shown during off-route events with
     * "Report a Problem" text.
     *
     * @return alert view that is used in the instruction view
     */
    fun retrieveAlertView(): NavigationAlertView {
        return instructionView.retrieveAlertView()
    }

    fun showInstructionView() {
        instructionView.isVisible = true
    }

    fun hideInstructionView() {
        instructionView.isVisible = false
    }

    private fun initializeView() {
        inflate(context, R.layout.navigation_view_layout, this)
        bind()
        initializeNavigationViewModel(context)
        initializeNavigationEventDispatcher()
        initializeNavigationPresenter()
        initializeInstructionListListener()
        initializeClickListeners()
    }

    private fun bind() {
        mapView = findViewById(R.id.navigationMapView)
        instructionView = findViewById(R.id.instructionView)
        instructionView.let {
            ViewCompat.setElevation(it, 10f)
        }
        recenterBtn = findViewById(R.id.recenterBtn)
        wayNameView = findViewById(R.id.wayNameView)
    }

    private fun initializeNavigationViewModel(context: Context) {
        try {
            navigationViewModel = NavigationViewModel(context)
        } catch (exception: ClassCastException) {
            throw ClassCastException("Please ensure that the provided Context is a valid FragmentActivity")
        }
    }

    private fun initializeNavigationEventDispatcher() {
        navigationViewEventDispatcher = NavigationViewEventDispatcher()
        navigationViewModel.initializeEventDispatcher(navigationViewEventDispatcher)
    }

    private fun initializeInstructionListListener() {
        instructionView.setInstructionListListener(
            NavigationInstructionListListener(
                navigationPresenter,
                navigationViewEventDispatcher
            )
        )
    }

    private fun initializeNavigationMap(mapView: MapView, map: MapLibreMap) {
        navigationMap = NavigationMapLibreMap(mapView, map)
        navigationMap?.updateLocationLayerRenderMode(RenderMode.GPS)
        if (mapInstanceState != null) {
            navigationMap?.restoreFrom(mapInstanceState)
            return
        }
    }

    private fun initializeSymbolManager(mapView: MapView, mapLibreMap: MapLibreMap, style: Style) {
        symbolManager = SymbolManager(mapView, mapLibreMap, style).apply {
            iconAllowOverlap = true
            iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT
        }
    }

    private fun initializeWayNameListener() {
        val wayNameListener = NavigationViewWayNameListener(navigationPresenter)
        navigationMap?.addOnWayNameChangedListener(wayNameListener)
    }

    private fun initializePreNavigationLocationEngine(map: MapLibreMap) {
        val locationEngine = navigationViewModel.retrieveNavigation()?.locationEngine ?: return
        preNavigationLocationEngine = PreNavigationLocationEngine(
            locationEngine = locationEngine,
            locationComponent = map.locationComponent
        )
        preNavigationLocationEngine?.start()
    }


    private fun saveNavigationMapInstanceState(outState: Bundle) {
        navigationMap?.saveStateWith(MAP_INSTANCE_STATE_KEY, outState)
    }

    private fun updateInstructionListState(visible: Boolean) {
        if (visible) {
            instructionView.showInstructionList()
        } else {
            instructionView.hideInstructionList()
        }
    }

    private fun updateInstructionMutedState(isMuted: Boolean) {
        if (isMuted) {
            (instructionView.retrieveSoundButton() as SoundButton).soundFabOff()
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
            return try {
                (context as Activity).isChangingConfigurations
            } catch (exception: ClassCastException) {
                false
            }
        }

    private fun initializeNavigationPresenter() {
        navigationPresenter = NavigationPresenter(this)
    }

    private fun updatePresenterState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val navigationRunningKey = context.getString(R.string.navigation_running)
            val resumeState = savedInstanceState.getBoolean(navigationRunningKey)
            navigationPresenter.updateResumeState(resumeState)
        }
    }

    private fun initializeNavigation(options: NavigationViewOptions) {
        establish(options)
        navigationViewModel.initialize(options)
        initializeNavigationListeners(options, navigationViewModel)
        setupNavigationMapLibreMap(options)

        if (!isSubscribed) {
            initializeClickListeners()
            initializeOnCameraTrackingChangedListener()
            subscribeViewModels()
        }
    }

    private fun initializeClickListeners() {
        recenterBtn.addOnClickListener(RecenterBtnClickListener(navigationPresenter))
    }

    private fun initializeOnCameraTrackingChangedListener() {
        onTrackingChangedListener =
            NavigationOnCameraTrackingChangedListener(navigationPresenter)
        navigationMap?.addOnCameraTrackingChangedListener(onTrackingChangedListener)
    }

    private fun establish(options: NavigationViewOptions) {
        val localeUtils = LocaleUtils()
        establishDistanceFormatter(localeUtils, options)
    }

    private fun establishDistanceFormatter(
        localeUtils: LocaleUtils,
        options: NavigationViewOptions
    ) {
        val unitType = establishUnitType(localeUtils, options)
        val language = establishLanguage(localeUtils, options)
        val roundingIncrement = establishRoundingIncrement(options)
        val distanceFormatter = DistanceFormatter(context, language, unitType, roundingIncrement)

        instructionView.setDistanceFormatter(distanceFormatter)
    }

    private fun establishRoundingIncrement(navigationViewOptions: NavigationViewOptions): MapLibreNavigationOptions.RoundingIncrement {
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
    ): UnitType {
        val routeOptions = options.directionsRoute().routeOptions
        val voiceUnits = routeOptions?.voiceUnits
        return localeUtils.retrieveNonNullUnitType(context, voiceUnits)
    }

    private fun initializeNavigationListeners(
        options: NavigationViewOptions,
        navigationViewModel: NavigationViewModel?
    ) {
        navigationMap?.addProgressChangeListener(navigationViewModel?.retrieveNavigation()!!)
        navigationViewEventDispatcher?.initializeListeners(options, navigationViewModel)
    }

    private fun setupNavigationMapLibreMap(options: NavigationViewOptions) {
        navigationMap?.updateWaynameQueryMap(options.waynameChipEnabled())
    }

    /**
     * Subscribes the [InstructionView] to the [NavigationViewModel].
     *
     *
     * Then, creates an instance of [NavigationViewSubscriber], which takes a presenter.
     *
     *
     * The subscriber then subscribes to the view models, setting up the appropriate presenter / listener
     * method calls based on the [androidx.lifecycle.LiveData] updates.
     */
    private fun subscribeViewModels() {
        instructionView.subscribe(this, navigationViewModel)

        NavigationViewSubscriber(this, navigationViewModel, navigationPresenter).subscribe()
        isSubscribed = true
    }

    private fun shutdown() {
        navigationMap?.removeOnCameraTrackingChangedListener(onTrackingChangedListener)
        navigationMap?.onDestroy()
        preNavigationLocationEngine?.stop()

        navigationViewEventDispatcher?.onDestroy(navigationViewModel.retrieveNavigation())
        mapView.onDestroy()
        navigationViewModel.onDestroy(isChangingConfigurations)
        ImageCreator.getInstance().shutdown()
        navigationMap = null
    }

    companion object {
        private const val MAP_INSTANCE_STATE_KEY = "navigation_maplibre_map_instance_state"
        private const val INVALID_STATE = 0
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

}