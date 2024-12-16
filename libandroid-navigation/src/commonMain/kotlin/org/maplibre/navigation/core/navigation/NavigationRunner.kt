package org.maplibre.navigation.core.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.LocationValidator
import org.maplibre.navigation.core.location.engine.LocationEngine
import org.maplibre.navigation.core.milestone.Milestone
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.navigation.NavigationHelper.buildSnappedLocation
import org.maplibre.navigation.core.navigation.NavigationHelper.checkMilestones
import org.maplibre.navigation.core.navigation.NavigationHelper.isUserOffRoute
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.utils.RouteUtils

//TODO fabi755: discuss and find perfect name
// - NavigationProcessor
// - NavigationEngine
// - NavigationRouteProcessor
// - NavigationRouteEngine
// - NavigationRunner
// - NavigationRouteRunner
//TODO: fabi755 add interface and allow to customize (open/inject/...)
/**
 *
 */
class NavigationRunner(
    private val mapLibreNavigation: MapLibreNavigation,
    private val routeUtils: RouteUtils,
    private val locationValidator: LocationValidator = LocationValidator(mapLibreNavigation.options.locationAcceptableAccuracyInMetersThreshold),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    ) {
    private val locationEngine: LocationEngine
        get() = mapLibreNavigation.locationEngine

    private val eventDispatcher: NavigationEventDispatcher
        get() = mapLibreNavigation.eventDispatcher

    private val navigationRouteProcessor = NavigationRouteProcessor(routeUtils)

    private var collectLocationJob: Job? = null

    fun startNavigation(route: DirectionsRoute) {
        collectLocationJob?.cancel() // Cancel previous started run

        collectLocationJob = coroutineScope.launch {
            processLocationUpdate(
                locationEngine.getLastLocation() ?: routeUtils.createFirstLocationFromRoute(route)
            )
            locationEngine.listenToLocation(
                LocationEngineRequest.Builder(LOCATION_ENGINE_INTERVAL)
                    .setFastestInterval(LOCATION_ENGINE_INTERVAL)
                    .build(),
            ).collect(::processLocationUpdate)
        }
    }

    fun stopNavigation() {
        collectLocationJob?.cancel()
        collectLocationJob = null
    }

    fun isRunning(): Boolean {
        return collectLocationJob?.isActive == true
    }

    /**
     * Takes a new location model and runs all related engine checks against it
     * (off-route, milestones, snapped location, and faster-route).
     *
     *
     * After running through the engines, all data is submitted to [NavigationService] via
     * [RouteProcessorBackgroundThread.Listener].
     *
     * @param update hold location, navigation (with options), and distances away from maneuver
     */
    private fun processLocationUpdate(rawLocation: Location) {
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

    private fun findTriggeredMilestones(
        mapLibreNavigation: MapLibreNavigation,
        routeProgress: RouteProgress
    ): List<Milestone> {
        val previousRouteProgress = navigationRouteProcessor.routeProgress
        return checkMilestones(previousRouteProgress, routeProgress, mapLibreNavigation)
    }

    private fun findSnappedLocation(
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

    private fun determineUserOffRoute(
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

    private fun updateRouteProcessorWith(routeProgress: RouteProgress): RouteProgress {
        navigationRouteProcessor.routeProgress = routeProgress
        return routeProgress
    }

    private fun dispatchUpdate(
        userOffRoute: Boolean,
        milestones: List<Milestone>,
        location: Location,
        routeProgress: RouteProgress
    ) {
        coroutineScope.launch(Dispatchers.Main) {
            dispatchRouteProgress(location, routeProgress)
            dispatchTriggeredMilestones(milestones, routeProgress)
            dispatchOffRoute(location, userOffRoute)
        }
    }

    private fun dispatchRouteProgress(location: Location, routeProgress: RouteProgress) {
        eventDispatcher.onProgressChange(location, routeProgress)
    }

    private fun dispatchTriggeredMilestones(
        triggeredMilestones: List<Milestone>,
        routeProgress: RouteProgress
    ) {
        for (milestone in triggeredMilestones) {
            val instruction = milestone.getInstruction()?.buildInstruction(routeProgress)
            eventDispatcher.onMilestoneEvent(routeProgress, instruction, milestone)
        }
    }

    private fun dispatchOffRoute(location: Location, isUSerOffRoute: Boolean) {
        if (isUSerOffRoute) {
            eventDispatcher.onUserOffRoute(location)
        }
    }

    companion object {
        private const val LOCATION_ENGINE_INTERVAL = 1000L
    }
}
