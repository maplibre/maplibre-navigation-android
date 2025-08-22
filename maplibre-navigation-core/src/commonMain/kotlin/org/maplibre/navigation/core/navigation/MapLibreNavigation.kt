package org.maplibre.navigation.core.navigation

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.maplibre.navigation.core.location.engine.LocationEngine
import org.maplibre.navigation.core.milestone.BannerInstructionMilestone
import org.maplibre.navigation.core.milestone.Milestone
import org.maplibre.navigation.core.milestone.MilestoneEventListener
import org.maplibre.navigation.core.milestone.VoiceInstructionMilestone
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.navigation.NavigationConstants.BANNER_INSTRUCTION_MILESTONE_ID
import org.maplibre.navigation.core.navigation.NavigationConstants.VOICE_INSTRUCTION_MILESTONE_ID
import org.maplibre.navigation.core.navigation.camera.Camera
import org.maplibre.navigation.core.navigation.camera.SimpleCamera
import org.maplibre.navigation.core.navigation.engine.MapLibreNavigationEngine
import org.maplibre.navigation.core.navigation.engine.NavigationEngine
import org.maplibre.navigation.core.offroute.OffRoute
import org.maplibre.navigation.core.offroute.OffRouteDetector
import org.maplibre.navigation.core.offroute.OffRouteListener
import org.maplibre.navigation.core.route.FasterRoute
import org.maplibre.navigation.core.route.FasterRouteDetector
import org.maplibre.navigation.core.route.FasterRouteListener
import org.maplibre.navigation.core.routeprogress.ProgressChangeListener
import org.maplibre.navigation.core.snap.Snap
import org.maplibre.navigation.core.snap.SnapToRoute
import org.maplibre.navigation.core.utils.RouteUtils
import org.maplibre.navigation.core.utils.ValidationUtils
import kotlin.jvm.JvmOverloads

/**
 * A MapLibreNavigation class for interacting with and customizing a navigation session.
 *
 * Instance of this class are used to setup, customize, start, and end a navigation session.
 * Building a custom
 * [MapLibreNavigationOptions] object and passing it in allows you to further customize the
 * user experience. Once this class is initialized, the options specified
 * through the options class cannot be modified.
 *
 * @param applicationContext    required in order to create and bind the navigation service. An application context is required here.
 * @param options               a custom built `MapLibreNavigationOptions` class
 * @param locationEngine        a LocationEngine to provide Location updates
 * @param cameraEngine          Navigation uses a camera engine to determine the camera position while routing.
 *  By default, it uses a [SimpleCamera]. If you would like to customize how the camera is
 *  positioned, create a new [Camera] and set it here.
 * @param snapEngine            This parameter is used to pass in a custom implementation of the snapping
 *  logic. A default snap-to-route engine is attached when this class is first initialized;
 *  setting a custom one will replace it with your own implementation.
 * @param offRouteEngine        This param is used to pass in a custom implementation of the off-route
 *  logic, A default off-route detection engine is attached when this class is first initialized;
 *  setting a custom one will replace it with your own implementation.
 * @param fasterRouteEngine     This API is used to pass in a custom implementation of the faster-route
 *  detection logic, A default faster-route detection engine is attached when this class is first
 *  initialized; setting a custom one will replace it with your own implementation.
 * @param routeUtils            core utility class for route related calculations
 *
 * @see MapLibreNavigationOptions
 */
open class MapLibreNavigation @JvmOverloads constructor(
    val options: MapLibreNavigationOptions = MapLibreNavigationOptions(),
    /**
     * Navigation needs an instance of location engine in order to acquire user location information
     * and handle events based off of the current information. By default, a LOST location engine is
     * created with the optimal navigation settings.
     *
     * Although it is not required to set your location engine to these parameters, these values are
     * what we found works best. Note that this also depends on which underlying location service you
     * are using. Reference the corresponding location service documentation for more information and
     * way's you could improve the performance.
     *
     * An ideal conditions, the Navigation SDK will receive location updates once every second with
     * mild to high horizontal accuracy. The location update must also contain all information an
     * Android location object would expect including bearing, speed, timestamp, and
     * latitude/longitude.
     *
     * Listed below are the ideal conditions for both a LOST location engine and a Google Play
     * Services Location engine.
     *
     * - Set the location priority to `HIGH_ACCURACY`.
     * - The fastest interval should be set around 1 second (1000ms). Note that the interval isn't
     *   a guaranteed to match this value exactly and is only an estimate.
     * - Setting the location engine interval to 0 will result in location updates occurring as
     *   quickly as possible within the fastest interval limit placed on it.
     */
    locationEngine: LocationEngine,
    var cameraEngine: Camera = SimpleCamera(),
    var snapEngine: Snap = SnapToRoute(),
    var offRouteEngine: OffRoute = OffRouteDetector(),
    var fasterRouteEngine: FasterRoute = FasterRouteDetector(options),
    val routeUtils: RouteUtils = RouteUtils(),
) {
    /**
     * Convenience constructor for tests. Enables injection of navigation engine mock.
     */
    internal constructor(
        options: MapLibreNavigationOptions = MapLibreNavigationOptions(),
        locationEngine: LocationEngine,
        cameraEngine: Camera = SimpleCamera(),
        snapEngine: Snap = SnapToRoute(),
        offRouteEngine: OffRoute = OffRouteDetector(),
        fasterRouteEngine: FasterRoute = FasterRouteDetector(options),
        routeUtils: RouteUtils = RouteUtils(),
        mapLibreNavigationEngine: MapLibreNavigationEngine
    ) : this(
        options,
        locationEngine,
        cameraEngine,
        snapEngine,
        offRouteEngine,
        fasterRouteEngine,
        routeUtils,
    ) {
        this.mapLibreNavigationEngine = mapLibreNavigationEngine
    }

    private val navigationRunnerJob = Job()
    private var mapLibreNavigationEngine: NavigationEngine? = null
        set(value) {
            // Stop previous started navigation session to avoid leaks
            field?.stopNavigation()
            field = value
        }

    /**
     * Navigation needs an instance of location engine in order to acquire user location information
     * and handle events based off of the current information. By default, a LOST location engine is
     * created with the optimal navigation settings.
     *
     * Although it is not required to set your location engine to these parameters, these values are
     * what we found works best. Note that this also depends on which underlying location service you
     * are using. Reference the corresponding location service documentation for more information and
     * way's you could improve the performance.
     *
     * An ideal conditions, the Navigation SDK will receive location updates once every second with
     * mild to high horizontal accuracy. The location update must also contain all information an
     * Android location object would expect including bearing, speed, timestamp, and
     * latitude/longitude.
     *
     * Listed below are the ideal conditions for both a LOST location engine and a Google Play
     * Services Location engine.
     *
     * - Set the location priority to `HIGH_ACCURACY`.
     * - The fastest interval should be set around 1 second (1000ms). Note that the interval isn't
     *   a guaranteed to match this value exactly and is only an estimate.
     * - Setting the location engine interval to 0 will result in location updates occurring as
     *   quickly as possible within the fastest interval limit placed on it.
     */
    var locationEngine: LocationEngine = locationEngine
        set(value) {
            field = value

            // Restart current running navigation session to apply the new location engine
            route?.let {route ->
                if (getNavigationEngineInternal().isRunning()) {
                    getNavigationEngineInternal().stopNavigation()
                    getNavigationEngineInternal().startNavigation(route)
                }
            }
        }

    private val mutableMilestones: MutableSet<Milestone> = mutableSetOf<Milestone>()
        .apply {
            if (options.defaultMilestonesEnabled) {
                add(VoiceInstructionMilestone(identifier = VOICE_INSTRUCTION_MILESTONE_ID))
                add(BannerInstructionMilestone(identifier = BANNER_INSTRUCTION_MILESTONE_ID))
            }
        }

    val eventDispatcher: NavigationEventDispatcher = NavigationEventDispatcher()

    val milestones: Set<Milestone>
        get() = mutableMilestones

    var route: DirectionsRoute? = null
        private set

    // Public APIs

    /**
     * Critical to place inside your navigation activity so that when your application gets destroyed
     * the navigation service unbinds and gets destroyed, preventing any memory leaks. Calling this
     * also removes all listeners that have been attached.
     */
    fun onDestroy() {
        stopNavigation()
        removeOffRouteListener(null)
        removeProgressChangeListener(null)
        removeMilestoneEventListener(null)
        removeNavigationEventListener(null)
    }

    /**
     * Navigation [Milestone]s provide a powerful way to give your user instructions at custom
     * defined locations along their route. Default milestones are automatically added unless
     * [MapLibreNavigationOptions.defaultMilestonesEnabled] is set to false but they can also
     * be individually removed using the [.removeMilestone] API. Once a custom
     * milestone is built, it will need to be passed into the navigation SDK through this method.
     *
     * Milestones can only be added once and must be removed and added back if any changes are
     * desired.
     *
     * @param milestone a custom built milestone
     * @since 0.4.0
     */
    fun addMilestone(milestone: Milestone) {
        val milestoneAdded = mutableMilestones.add(milestone)
        if (!milestoneAdded) {
            Logger.w { "Milestone has already been added to the stack." }
        }
    }

    /**
     * Adds the given list of [Milestone] to be triggered during navigation.
     *
     *
     * Milestones can only be added once and must be removed and added back if any changes are
     * desired.
     *
     *
     * @param milestones a list of custom built milestone
     * @since 0.14.0
     */
    fun addMilestones(milestones: List<Milestone>) {
        val milestonesAdded = this.mutableMilestones.addAll(milestones)
        if (!milestonesAdded) {
            Logger.w { "These milestones have already been added to the stack." }
        }
    }

    /**
     * Remove a specific milestone by passing in the instance of it. Removal of all the milestones can
     * be achieved by passing in null rather than a specific milestone.
     *
     * @param milestone a milestone you'd like to have removed or null if you'd like to remove all
     * milestones
     * @since 0.4.0
     */
    fun removeMilestone(milestone: Milestone?) {
        if (milestone == null) {
            mutableMilestones.clear()
        } else if (!mutableMilestones.remove(milestone)) {
            Logger.w { "Milestone attempting to remove does not exist in stack." }
        }
    }

    /**
     * Remove a specific milestone by passing in the identifier associated with the milestone you'd
     * like to remove. If the identifier passed in does not match one of the milestones in the list,
     * a warning will return in the log.
     *
     * @param milestoneIdentifier identifier matching one of the milestones
     * @since 0.5.0
     */
    fun removeMilestone(milestoneIdentifier: Int) {
        milestones.firstOrNull { m -> m.identifier == milestoneIdentifier }
            ?.let { removeMilestone(it) }
            ?: run { Logger.w { "No milestone found with the specified identifier." } }
    }

    /**
     * Calling This begins a new navigation session using the provided directions route. this API is
     * also intended to be used when a reroute occurs passing in the updated directions route.
     *
     *
     * On initial start of the navigation session, the navigation services gets created and bound to
     * your activity. Unless disabled, a notification will be displayed to the user and will remain
     * until the service stops running in the background.
     *
     *
     * @param directionsRoute a [DirectionsRoute] that makes up the path your user should
     * traverse along
     * @since 0.1.0
     */
    fun startNavigation(directionsRoute: DirectionsRoute) {
        ValidationUtils.validDirectionsRoute(directionsRoute, options.defaultMilestonesEnabled)
        this.route = directionsRoute
        Logger.d { "MapLibreNavigation startNavigation called." }

        getNavigationEngineInternal().startNavigation(directionsRoute)
        eventDispatcher.onNavigationEvent(true)
    }

    /**
     * Call this when the navigation session needs to end before the user reaches their final
     * destination. There isn't a need to manually end the navigation session using this API when the
     * user arrives unless you set [MapLibreNavigationOptions.manuallyEndNavigationUponCompletion]
     * to true.
     *
     *
     * Ending the navigation session ends and unbinds the navigation service meaning any milestone,
     * progress change, or off-route listeners will not be invoked anymore. A call returning false
     * will occur to [NavigationEventListener.onRunning] to notify you when the service
     * ends.
     *
     *
     * @since 0.1.0
     */
    fun stopNavigation() {
        Logger.d { "MapLibreNavigation stopNavigation called" }

        getNavigationEngineInternal().stopNavigation()
        eventDispatcher.onNavigationEvent(false)
    }

    /**
     * Manually advances navigation to the specified leg and step indices.
     * This method is intended for waypoint skipping during active turn-by-turn navigation.
     *
     * @param legIndex The target leg index (0-based)
     * @param stepIndex The target step index within the leg (0-based)
     * @throws IllegalStateException if navigation is not currently active
     * @throws IllegalArgumentException if indices are invalid for the current route
     * 
     * @since 5.0.0
     */
    fun setIndex(legIndex: Int, stepIndex: Int) {
        val currentRoute = route
        require(currentRoute != null) { "Cannot set index: no route is currently active" }
        require(getNavigationEngineInternal().isRunning()) { 
            "Cannot set index: navigation is not currently running" 
        }

        require(legIndex >= 0 && legIndex < currentRoute.legs.size) {
            "Invalid leg index: $legIndex. Route has ${currentRoute.legs.size} legs"
        }
        require(stepIndex >= 0 && stepIndex < currentRoute.legs[legIndex].steps.size) {
            "Invalid step index: $stepIndex. Leg $legIndex has ${currentRoute.legs[legIndex].steps.size} steps"
        }
        
        Logger.d { "Manual waypoint update: advancing to leg $legIndex, step $stepIndex" }
        getNavigationEngineInternal().triggerManualRouteUpdate(legIndex, stepIndex)
    }

    /**
     * Get custom set navigation engine or create default instance if not set.
     * The use of this inconvenient method is necessary because we can not pass
     * `this` MapLibreNavigation instance by the constructor. This would cause memory leaks.
     * Additionally, we want to allow the injection of [NavigationEngine].
     *
     * @return current instance of navigation engine
     */
    private fun getNavigationEngineInternal(): NavigationEngine {
        if (mapLibreNavigationEngine == null) {
            mapLibreNavigationEngine = MapLibreNavigationEngine(
                mapLibreNavigation = this,
                routeUtils = routeUtils,
                backgroundScope = CoroutineScope(Dispatchers.Default + navigationRunnerJob)
            )
        }

        return mapLibreNavigationEngine!!

    }

    // Listeners

    /**
     * This adds a new milestone event listener which is invoked when a milestone gets triggered. If
     * more then one milestone gets triggered on a location update, each milestone event listener will
     * be invoked for each of those milestones. This is important to consider if you are using voice
     * instructions since this would cause multiple instructions to be said at once. Ideally the
     * milestones setup should avoid triggering too close to each other.
     *
     * It is not possible to add the same listener implementation more then once and a warning will be
     * printed in the log if attempted.
     *
     * @param milestoneEventListener an implementation of `MilestoneEventListener` which hasn't
     * already been added
     * @see MilestoneEventListener
     *
     * @since 0.4.0
     */
    fun addMilestoneEventListener(milestoneEventListener: MilestoneEventListener) {
        eventDispatcher.addMilestoneEventListener(milestoneEventListener)
    }

    /**
     * This removes a specific milestone event listener by passing in the instance of it or you can
     * pass in null to remove all the listeners. When [.onDestroy] is called, all listeners
     * get removed automatically, removing the requirement for developers to manually handle this.
     *
     * If the listener you are trying to remove does not exist in the list, a warning will be printed
     * in the log.
     *
     * @param milestoneEventListener an implementation of `MilestoneEventListener` which
     * currently exist in the milestoneEventListener list
     * @see MilestoneEventListener
     *
     * @since 0.4.0
     */
    // Public exposed for usage outside SDK
    fun removeMilestoneEventListener(milestoneEventListener: MilestoneEventListener?) {
        eventDispatcher.removeMilestoneEventListener(milestoneEventListener)
    }

    /**
     * This adds a new progress change listener which is invoked when a location change occurs and the
     * navigation engine successfully runs it's calculations on it.
     *
     * It is not possible to add the same listener implementation more then once and a warning will be
     * printed in the log if attempted.
     *
     * @param progressChangeListener an implementation of `ProgressChangeListener` which hasn't
     * already been added
     * @see ProgressChangeListener
     *
     * @since 0.1.0
     */
    fun addProgressChangeListener(progressChangeListener: ProgressChangeListener) {
        eventDispatcher.addProgressChangeListener(progressChangeListener)
    }

    /**
     * This removes a specific progress change listener by passing in the instance of it or you can
     * pass in null to remove all the listeners. When [.onDestroy] is called, all listeners
     * get removed automatically, removing the requirement for developers to manually handle this.
     *
     * If the listener you are trying to remove does not exist in the list, a warning will be printed
     * in the log.
     *
     * @param progressChangeListener an implementation of `ProgressChangeListener` which
     * currently exist in the progressChangeListener list
     * @see ProgressChangeListener
     *
     * @since 0.1.0
     */
    fun removeProgressChangeListener(progressChangeListener: ProgressChangeListener?) {
        eventDispatcher.removeProgressChangeListener(progressChangeListener)
    }

    /**
     * This adds a new off route listener which is invoked when the devices location veers off the
     * route and the specified criteria's in [MapLibreNavigationOptions] have been met.
     *
     *
     * The behavior that causes this listeners callback to get invoked vary depending on whether a
     * custom off route engine has been set using [.setOffRouteEngine].
     *
     *
     * It is not possible to add the same listener implementation more then once and a warning will be
     * printed in the log if attempted.
     *
     *
     * @param offRouteListener an implementation of `OffRouteListener` which hasn't already been
     * added
     * @see OffRouteListener
     *
     * @since 0.2.0
     */
    fun addOffRouteListener(offRouteListener: OffRouteListener) {
        eventDispatcher.addOffRouteListener(offRouteListener)
    }

    /**
     * This removes a specific off route listener by passing in the instance of it or you can pass in
     * null to remove all the listeners. When [.onDestroy] is called, all listeners
     * get removed automatically, removing the requirement for developers to manually handle this.
     *
     *
     * If the listener you are trying to remove does not exist in the list, a warning will be printed
     * in the log.
     *
     *
     * @param offRouteListener an implementation of `OffRouteListener` which currently exist in
     * the offRouteListener list
     * @see OffRouteListener
     *
     * @since 0.2.0
     */
    fun removeOffRouteListener(offRouteListener: OffRouteListener?) {
        eventDispatcher.removeOffRouteListener(offRouteListener)
    }

    /**
     * This adds a new navigation event listener which is invoked when navigation service begins
     * running in the background and again when the service gets destroyed.
     *
     * It is not possible to add the same listener implementation more then once and a warning will be
     * printed in the log if attempted.
     *
     * @param navigationEventListener an implementation of `NavigationEventListener` which
     * hasn't already been added
     * @see NavigationEventListener
     *
     * @since 0.1.0
     */
    fun addNavigationEventListener(navigationEventListener: NavigationEventListener) {
        eventDispatcher.addNavigationEventListener(navigationEventListener)
    }

    /**
     * This removes a specific navigation event listener by passing in the instance of it or you can
     * pass in null to remove all the listeners. When [.onDestroy] is called, all listeners
     * get removed automatically, removing the requirement for developers to manually handle this.
     *
     *
     * If the listener you are trying to remove does not exist in the list, a warning will be printed
     * in the log.
     *
     *
     * @param navigationEventListener an implementation of `NavigationEventListener` which
     * currently exist in the navigationEventListener list
     * @see NavigationEventListener
     *
     * @since 0.1.0
     */
    fun removeNavigationEventListener(navigationEventListener: NavigationEventListener?) {
        eventDispatcher.removeNavigationEventListener(navigationEventListener)
    }

    /**
     * This adds a new faster route listener which is invoked when a new, faster [DirectionsRoute]
     * has been retrieved by the specified criteria in [FasterRoute].
     *
     *
     * The behavior that causes this listeners callback to get invoked vary depending on whether a
     * custom faster route engine has been set using [.setFasterRouteEngine].
     *
     *
     * It is not possible to add the same listener implementation more then once and a warning will be
     * printed in the log if attempted.
     *
     *
     * @param fasterRouteListener an implementation of `FasterRouteListener`
     * @see FasterRouteListener
     *
     * @since 0.9.0
     */
    fun addFasterRouteListener(fasterRouteListener: FasterRouteListener) {
        eventDispatcher.addFasterRouteListener(fasterRouteListener)
    }

    /**
     * This removes a specific faster route listener by passing in the instance of it or you can pass in
     * null to remove all the listeners. When [.onDestroy] is called, all listeners
     * get removed automatically, removing the requirement for developers to manually handle this.
     *
     *
     * If the listener you are trying to remove does not exist in the list, a warning will be printed
     * in the log.
     *
     *
     * @param fasterRouteListener an implementation of `FasterRouteListener` which currently exist in
     * the fasterRouteListeners list
     * @see FasterRouteListener
     *
     * @since 0.9.0
     */
    @Suppress("unused")
    fun removeFasterRouteListener(fasterRouteListener: FasterRouteListener?) {
        eventDispatcher.removeFasterRouteListener(fasterRouteListener)
    }
}
