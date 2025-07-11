package org.maplibre.navigation.core.route

import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.models.DirectionsResponse
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.models.LegStep
import org.maplibre.navigation.core.models.RouteLeg
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.core.navigation.NavigationConstants
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.routeprogress.RouteStepProgress

open class FasterRouteDetector(
    private val navigationOptions: MapLibreNavigationOptions
) : FasterRoute() {
    private var lastCheckedLocation: Location? = null

    override fun shouldCheckFasterRoute(location: Location, routeProgress: RouteProgress): Boolean {
        // On first pass through detector, last checked location will be null
        if (lastCheckedLocation == null) {
            lastCheckedLocation = location
        }

        // Check if the faster route time interval has been exceeded
        if (secondsSinceLastCheck(location) >= navigationOptions.fasterRouteCheckIntervalSeconds) {
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
                val routeLeg = newRoute.legs.first()
                if (hasAtLeastTwoSteps(routeLeg)) {
                    // Extract the first two steps
                    val firstStep = routeLeg.steps.first()
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
        return newRoute.legs.isNotEmpty()
    }

    private fun hasAtLeastTwoSteps(routeLeg: RouteLeg): Boolean {
        return routeLeg.steps.size > 2
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
        return routeProgress.currentLegProgress.upComingStep != null
                && routeProgress.currentLegProgress.upComingStep == secondStep
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
    private fun validRouteResponse(response: DirectionsResponse): Boolean {
        return response.routes.isNotEmpty()
    }

    private fun validRouteDurationRemaining(routeProgress: RouteProgress): Boolean {
        // Total route duration remaining in seconds
        val routeDurationRemaining = routeProgress.durationRemaining.toInt()
        return routeDurationRemaining > VALID_ROUTE_DURATION_REMAINING
    }

    private fun validStepDurationRemaining(routeProgress: RouteProgress): Boolean {
        val currentStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress
        // Current step duration remaining in seconds
        val currentStepDurationRemaining = currentStepProgress.durationRemaining.toInt()
        return currentStepDurationRemaining > NavigationConstants.NAVIGATION_MEDIUM_ALERT_DURATION
    }

    private fun secondsSinceLastCheck(location: Location): Long {
        return lastCheckedLocation?.let { lastCheckedLocation ->
            (location.timeMilliseconds ?: 0) - (lastCheckedLocation.timeMilliseconds ?: 0) / 1000
        } ?: -1
    }

    companion object {
        private const val VALID_ROUTE_DURATION_REMAINING = 600
    }
}
