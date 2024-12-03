package org.maplibre.navigation.android.navigation.v5.navigation

import android.location.Location
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

open class RouteProcessorThreadListener(
    private val eventDispatcher: NavigationEventDispatcher,
    private val notificationProvider: NavigationNotificationProvider
) : RouteProcessorBackgroundThread.Listener {

    /**
     * Corresponds to ProgressChangeListener object, updating the notification and passing information
     * to the navigation event dispatcher.
     */
    override fun onNewRouteProgress(location: Location, routeProgress: RouteProgress) {
        notificationProvider.updateNavigationNotification(routeProgress)
        eventDispatcher.onProgressChange(location, routeProgress)
    }

    /**
     * With each valid and successful rawLocation update, this will get called once the work on the
     * navigation engine thread has finished. Depending on whether or not a milestone gets triggered
     * or not, the navigation event dispatcher will be called to notify the developer.
     */
    override fun onMilestoneTrigger(
        triggeredMilestones: List<Milestone>,
        routeProgress: RouteProgress
    ) {
        for (milestone in triggeredMilestones) {
            val instruction = milestone.getInstruction()?.buildInstruction(routeProgress)
            eventDispatcher.onMilestoneEvent(routeProgress, instruction, milestone)
        }
    }

    /**
     * With each valid and successful rawLocation update, this callback gets invoked and depending on
     * whether or not the user is off route, the event dispatcher gets called.
     */
    override fun onUserOffRoute(location: Location, userOffRoute: Boolean) {
        if (userOffRoute) {
            eventDispatcher.onUserOffRoute(location)
        }
    }
}
