package org.maplibre.navigation.android.navigation.v5.route

import android.location.Location
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.models.RouteLeg
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationConstants
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteStepProgress
import java.util.Date
import java.util.concurrent.TimeUnit

class FasterRouteDetector : FasterRoute() {
    private var lastCheckedLocation: Location? = null

    override fun shouldCheckFasterRoute(
        location: Location?,
        routeProgress: RouteProgress?
    ): Boolean {
        if (location == null || routeProgress == null) {
            return false
        }
        // On first pass through detector, last checked location will be null
        if (lastCheckedLocation == null) {
            lastCheckedLocation = location
        }
        // Check if the faster route time interval has been exceeded
        if (secondsSinceLastCheck(location) >= NavigationConstants.NAVIGATION_CHECK_FASTER_ROUTE_INTERVAL) {
            lastCheckedLocation = location
            // Check for both valid route and step durations remaining
            if (validRouteDurationRemaining(routeProgress) && validStepDurationRemaining(
                    routeProgress
                )
            ) {
                return true
            }
        }
        return false
    }

    override fun isFasterRoute(
        response: DirectionsResponse,
        routeProgress: RouteProgress
    ): Boolean {
        if (validRouteResponse(response)) {
            val currentDurationRemaining = routeProgress.durationRemaining
            val newRoute = response.routes[0]

            if (hasLegs(newRoute)) {
                // Extract the first leg
                val routeLeg = newRoute.legs!![0]
                if (hasAtLeastTwoSteps(routeLeg)) {
                    // Extract the first two steps
                    val firstStep = routeLeg.steps!![0]
                    val secondStep = routeLeg.steps[1]
                    // Check for valid first and second steps of the new route
                    if (!validFirstStep(firstStep) || !validSecondStep(secondStep, routeProgress)) {
                        return false
                    }
                }
            }
            // New route must be at least 10% faster
            if (newRoute.duration <= (0.9 * currentDurationRemaining)) {
                return true
            }
        }
        return false
    }

    private fun hasLegs(newRoute: DirectionsRoute): Boolean {
        return newRoute.legs != null && !newRoute.legs!!.isEmpty()
    }

    private fun hasAtLeastTwoSteps(routeLeg: RouteLeg): Boolean {
        return routeLeg.steps != null && routeLeg.steps!!.size > 2
    }

    /**
     * The second step of the new route is valid if
     * it equals the current route upcoming step.
     *
     * @param secondStep of the new route
     * @param routeProgress current route progress
     * @return true if valid, false if not
     */
    private fun validSecondStep(secondStep: LegStep, routeProgress: RouteProgress): Boolean {
        return routeProgress.currentLegProgress!!.upComingStep != null
                && routeProgress.currentLegProgress!!.upComingStep!!.equals(secondStep)
    }

    /**
     * First step is valid if it is greater than
     * [NavigationConstants.NAVIGATION_MEDIUM_ALERT_DURATION].
     *
     * @param firstStep of the new route
     * @return true if valid, false if not
     */
    private fun validFirstStep(firstStep: LegStep): Boolean {
        return firstStep.duration > NavigationConstants.NAVIGATION_MEDIUM_ALERT_DURATION
    }

    /**
     * Checks if we have at least one [DirectionsRoute] in the given
     * [DirectionsResponse].
     *
     * @param response to be checked
     * @return true if valid, false if not
     */
    private fun validRouteResponse(response: DirectionsResponse?): Boolean {
        return response != null
                && !response.routes.isEmpty()
    }

    private fun validRouteDurationRemaining(routeProgress: RouteProgress): Boolean {
        // Total route duration remaining in seconds
        val routeDurationRemaining = routeProgress.durationRemaining as Int
        return routeDurationRemaining > VALID_ROUTE_DURATION_REMAINING
    }

    private fun validStepDurationRemaining(routeProgress: RouteProgress): Boolean {
        val currentStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress!!.currentStepProgress!!
        // Current step duration remaining in seconds
        val currentStepDurationRemaining = currentStepProgress.durationRemaining as Int
        return currentStepDurationRemaining > NavigationConstants.NAVIGATION_MEDIUM_ALERT_DURATION
    }

    private fun secondsSinceLastCheck(location: Location): Long {
        return dateDiff(Date(lastCheckedLocation!!.time), Date(location.time), TimeUnit.SECONDS)
    }

    private fun dateDiff(firstDate: Date, secondDate: Date, timeUnit: TimeUnit): Long {
        val diffInMillis = secondDate.time - firstDate.time
        return timeUnit.convert(diffInMillis, TimeUnit.MILLISECONDS)
    }

    companion object {
        private const val VALID_ROUTE_DURATION_REMAINING = 600
    }
}
