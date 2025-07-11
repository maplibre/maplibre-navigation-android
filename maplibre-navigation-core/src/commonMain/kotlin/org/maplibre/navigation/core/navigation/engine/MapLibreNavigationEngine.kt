package org.maplibre.navigation.core.navigation.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.LocationValidator
import org.maplibre.navigation.core.location.engine.LocationEngine
import org.maplibre.navigation.core.milestone.Milestone
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.navigation.MapLibreNavigation
import org.maplibre.navigation.core.navigation.NavigationEventDispatcher
import org.maplibre.navigation.core.navigation.NavigationHelper.buildSnappedLocation
import org.maplibre.navigation.core.navigation.NavigationHelper.checkMilestones
import org.maplibre.navigation.core.navigation.NavigationHelper.isUserOffRoute
import org.maplibre.navigation.core.navigation.NavigationIndices
import org.maplibre.navigation.core.navigation.NavigationRouteProcessor
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.utils.RouteUtils

/**
 * Default implementation for [NavigationEngine] which is responsible for fetching location updates
 * and processing them to set the current navigation state.
 */
open class MapLibreNavigationEngine(
    private val mapLibreNavigation: MapLibreNavigation,
    private val routeUtils: RouteUtils,
    private val locationValidator: LocationValidator = LocationValidator(mapLibreNavigation.options.locationAcceptableAccuracyInMetersThreshold),
    private val backgroundScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : NavigationEngine {
    private val locationEngine: LocationEngine
        get() = mapLibreNavigation.locationEngine

    private val eventDispatcher: NavigationEventDispatcher
        get() = mapLibreNavigation.eventDispatcher

    private val navigationRouteProcessor = NavigationRouteProcessor(routeUtils)
    private val processingMutex = Mutex()

    private var collectLocationJob: Job? = null

    /**
     * Start navigation for the given route.
     *
     * This call will starting listening to location updates and process this data to update to the current navigation state.
     * This will run until the [stopNavigation] is called.
     */
    override fun startNavigation(route: DirectionsRoute) {
        collectLocationJob?.cancel() // Cancel previous started run

        collectLocationJob = backgroundScope.launch {
            processLocationAndIndexUpdate(
                locationEngine.getLastLocation() ?: routeUtils.createFirstLocationFromRoute(route)
            )

            locationEngine.listenToLocation(
                LocationEngine.Request(
                    minIntervalMilliseconds = LOCATION_ENGINE_INTERVAL,
                    maxIntervalMilliseconds = LOCATION_ENGINE_INTERVAL,
                )
            ).collect(::processLocationAndIndexUpdate)
        }
    }

    /**
     * Stop and cancel the current running navigation.
     *
     * This means listening to the location updates are stopped and not consumed anymore.
     */
    override fun stopNavigation() {
        collectLocationJob?.cancel()
        collectLocationJob = null
    }

    /**
     * Check if the navigation is running
     *
     * @return true if the navigation is running, false otherwise.
     */
    override fun isRunning(): Boolean {
        return collectLocationJob?.isActive == true
    }

    /**
     * Takes a new location model and route indices runs all related engine checks against it
     * (off-route, milestones, snapped location, and faster-route).
     *
     * After running through the engines, all data is submitted to [NavigationEventDispatcher].
     *
     * @param rawLocation hold location, navigation (with options), and distances away from maneuver
     */
     suspend fun processLocationAndIndexUpdate(rawLocation: Location, index: NavigationIndices? = null) {
        processingMutex.withLock {
            // Index is set inside the mutex to avoid race conditions.
            index?.let {
                navigationRouteProcessor.setIndex(mapLibreNavigation, it)
            }

            if (!locationValidator.isValidUpdate(rawLocation)) {
                return
            }

            val routeProgress = navigationRouteProcessor
                .buildNewRouteProgress(mapLibreNavigation, rawLocation)

            val userOffRoute = determineUserOffRoute(mapLibreNavigation, rawLocation, routeProgress)
            val milestones = findTriggeredMilestones(mapLibreNavigation, routeProgress)
            val location = findSnappedLocation(
                mapLibreNavigation,
                rawLocation,
                routeProgress,
                userOffRoute
            )

            val finalRouteProgress = updateRouteProcessorWith(routeProgress)
            dispatchUpdate(userOffRoute, milestones, location, finalRouteProgress)
        }
    }

    protected fun findTriggeredMilestones(
        mapLibreNavigation: MapLibreNavigation,
        routeProgress: RouteProgress
    ): List<Milestone> {
        val previousRouteProgress = navigationRouteProcessor.routeProgress
        return checkMilestones(previousRouteProgress, routeProgress, mapLibreNavigation)
    }

    protected fun findSnappedLocation(
        mapLibreNavigation: MapLibreNavigation,
        rawLocation: Location,
        routeProgress: RouteProgress,
        userOffRoute: Boolean
    ): Location {
        val snapToRouteEnabled = mapLibreNavigation.options.snapToRoute
        return buildSnappedLocation(
            mapLibreNavigation,
            snapToRouteEnabled,
            rawLocation,
            routeProgress,
            userOffRoute
        )
    }

    protected fun determineUserOffRoute(
        mapLibreNavigation: MapLibreNavigation,
        location: Location,
        routeProgress: RouteProgress
    ): Boolean {
        val userOffRoute = isUserOffRoute(
            mapLibreNavigation,
            location,
            routeProgress,
            navigationRouteProcessor
        )
        navigationRouteProcessor.checkIncreaseIndex(mapLibreNavigation)
        return userOffRoute
    }

    protected fun updateRouteProcessorWith(routeProgress: RouteProgress): RouteProgress {
        navigationRouteProcessor.routeProgress = routeProgress
        return routeProgress
    }

    protected fun dispatchUpdate(
        userOffRoute: Boolean,
        milestones: List<Milestone>,
        location: Location,
        routeProgress: RouteProgress
    ) {
        mainScope.launch {
            dispatchRouteProgress(location, routeProgress)
            dispatchTriggeredMilestones(milestones, routeProgress)
            dispatchOffRoute(location, userOffRoute)
        }
    }

    protected fun dispatchRouteProgress(location: Location, routeProgress: RouteProgress) {
        eventDispatcher.onProgressChange(location, routeProgress)
    }

    protected fun dispatchTriggeredMilestones(
        triggeredMilestones: List<Milestone>,
        routeProgress: RouteProgress
    ) {
        for (milestone in triggeredMilestones) {
            val instruction = milestone.getInstruction()?.buildInstruction(routeProgress)
            eventDispatcher.onMilestoneEvent(routeProgress, instruction, milestone)
        }
    }

    protected fun dispatchOffRoute(location: Location, isUSerOffRoute: Boolean) {
        if (isUSerOffRoute) {
            eventDispatcher.onUserOffRoute(location)
        }
    }

    /**
     * Manually triggers a route progress update for the specified leg and step indices.
     * This method is used for waypoint skipping during active navigation.
     *
     * @param legIndex The target leg index to navigate to
     * @param stepIndex The target step index to navigate to
     */
    override fun triggerManualRouteUpdate(legIndex: Int, stepIndex: Int) {
        backgroundScope.launch {
            locationEngine.getLastLocation()?.let { currentLocation ->
                processLocationAndIndexUpdate(currentLocation, index = NavigationIndices(legIndex, stepIndex))
            }
        }
    }

    companion object {
        const val LOCATION_ENGINE_INTERVAL = 1000L
    }
}
