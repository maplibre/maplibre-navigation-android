package org.maplibre.navigation.android.navigation.v5.navigation

import android.location.Location
import android.os.Handler
import android.os.Message
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.buildSnappedLocation
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.checkMilestones
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.isUserOffRoute
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

open class RouteProcessorHandlerCallback(
    private val routeProcessor: NavigationRouteProcessor,
    private val responseHandler: Handler,
    private val listener: RouteProcessorBackgroundThread.Listener
) : Handler.Callback {

    override fun handleMessage(msg: Message): Boolean {
        return (msg.obj as? NavigationLocationUpdate)?.let { update ->
            handleRequest(update)
            true
        } ?: false
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
    private fun handleRequest(update: NavigationLocationUpdate) {
        val mapLibreNavigation = update.mapLibreNavigation
        val rawLocation = update.location
        val routeProgress = routeProcessor.buildNewRouteProgress(mapLibreNavigation, rawLocation)

        val userOffRoute = determineUserOffRoute(update, mapLibreNavigation, routeProgress)
        val milestones = findTriggeredMilestones(mapLibreNavigation, routeProgress)
        val location = findSnappedLocation(
                mapLibreNavigation,
                rawLocation,
                routeProgress,
                userOffRoute
            )

        val finalRouteProgress = updateRouteProcessorWith(routeProgress)
        sendUpdateToListener(userOffRoute, milestones, location, finalRouteProgress)
    }

    private fun findTriggeredMilestones(
        mapLibreNavigation: MapLibreNavigation,
        routeProgress: RouteProgress
    ): List<Milestone> {
        val previousRouteProgress = routeProcessor.routeProgress
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
        navigationLocationUpdate: NavigationLocationUpdate,
        mapLibreNavigation: MapLibreNavigation,
        routeProgress: RouteProgress
    ): Boolean {
        val userOffRoute = isUserOffRoute(
            navigationLocationUpdate, routeProgress,
            routeProcessor
        )
        routeProcessor.checkIncreaseIndex(mapLibreNavigation)
        return userOffRoute
    }

    private fun updateRouteProcessorWith(routeProgress: RouteProgress): RouteProgress {
        routeProcessor.routeProgress = routeProgress
        return routeProgress
    }

    private fun sendUpdateToListener(
        userOffRoute: Boolean,
        milestones: List<Milestone>,
        location: Location,
        finalRouteProgress: RouteProgress
    ) {
        responseHandler.post {
            listener.onNewRouteProgress(location, finalRouteProgress)
            listener.onMilestoneTrigger(milestones, finalRouteProgress)
            listener.onUserOffRoute(location, userOffRoute)
        }
    }
}
