package org.maplibre.navigation.core.utils

import org.maplibre.geojson.model.LineString
import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.milestone.BannerInstructionMilestone
import org.maplibre.navigation.core.milestone.Milestone
import org.maplibre.navigation.core.models.BannerInstructions
import org.maplibre.navigation.core.models.BannerText
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.models.LegStep
import org.maplibre.navigation.core.models.VoiceInstructions
import org.maplibre.navigation.core.navigation.NavigationConstants
import org.maplibre.navigation.core.routeprogress.RouteProgress


open class RouteUtils {

    /**
     * Compares a new routeProgress geometry to a previousRouteProgress geometry to determine if the
     * user is traversing along a new route. If the route geometries do not match, this returns true.
     *
     * @param previousRouteProgress the past route progress with the directions route included
     * @param routeProgress         the route progress with the directions route included
     * @return true if the direction route geometries do not match up, otherwise, false
     * @since 0.7.0
     */
    fun isNewRoute(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress
    ): Boolean {
        return isNewRoute(previousRouteProgress, routeProgress.directionsRoute)
    }

    /**
     * Compares a new routeProgress geometry to a previousRouteProgress geometry to determine if the
     * user is traversing along a new route. If the route geometries do not match, this returns true.
     *
     * @param previousRouteProgress the past route progress with the directions route included
     * @param directionsRoute       the current directions route
     * @return true if the direction route geometries do not match up, otherwise, false
     * @since 0.7.0
     */
    fun isNewRoute(
        previousRouteProgress: RouteProgress?,
        directionsRoute: DirectionsRoute
    ): Boolean {
        return previousRouteProgress == null || (previousRouteProgress.directionsRoute.geometry
                != directionsRoute.geometry)
    }

    /**
     * Looks at the current [RouteProgress] maneuverType for type "arrival", then
     * checks if the arrival meter threshold has been hit.
     *
     * @param routeProgress the current route progress
     * @param milestone     the current milestone from the MilestoneEventListener
     * @return true if in arrival state, false if not
     * @since 0.8.0
     */
    fun isArrivalEvent(routeProgress: RouteProgress, milestone: Milestone): Boolean {
        return (milestone as? BannerInstructionMilestone)?.let { bannerMilestone ->
            val isValidArrivalManeuverType =
                upcomingStepIsArrivalManeuverType(routeProgress) || currentStepIsArrivalManeuverType(
                    routeProgress
                )
            if (isValidArrivalManeuverType) {
                val currentStep = routeProgress.currentLegProgress.currentStep
                val currentInstructions = bannerMilestone.bannerInstructions
                val bannerInstructions = currentStep.bannerInstructions ?: emptyList()
                if (bannerInstructions.isNotEmpty() && currentInstructions != null) {
                    val lastInstructionIndex = bannerInstructions.size - 1
                    val lastInstructions = bannerInstructions[lastInstructionIndex]
                    return@let currentInstructions == lastInstructions
                }
            }

            return@let false
        } ?: false
    }

    /**
     * Given a [RouteProgress], this method will calculate the remaining coordinates
     * along the given route based on total route coordinates and the progress remaining waypoints.
     *
     *
     * If the coordinate size is less than the remaining waypoints, this method
     * will return null.
     *
     * @param routeProgress for route coordinates and remaining waypoints
     * @return list of remaining waypoints as [Point]s
     * @since 0.10.0
     */
    fun calculateRemainingWaypoints(routeProgress: RouteProgress): List<Point>? {
        return routeProgress.directionsRoute.routeOptions?.let { options ->
            val coordinatesSize = options.coordinates.size
            if (coordinatesSize < routeProgress.remainingWaypoints) {
                return null
            }

            options.coordinates.subList(
                coordinatesSize - routeProgress.remainingWaypoints,
                coordinatesSize
            )
        }
    }

    /**
     * Given a [RouteProgress], this method will calculate the remaining waypoint names
     * along the given route based on route option waypoint names and the progress remaining coordinates.
     *
     * If the waypoint names are empty, this method will return null.
     *
     * @param routeProgress for route waypoint names and remaining coordinates
     * @return String array including the origin waypoint name and the remaining ones
     * @since 0.19.0
     */
    fun calculateRemainingWaypointNames(routeProgress: RouteProgress): List<String>? {
        return routeProgress.directionsRoute.routeOptions?.let { routeOptions ->
            routeOptions.waypointNames
                ?.takeIf { allWaypointNames -> allWaypointNames.isNotEmpty() }
                ?.let { allWaypointNames ->
                    val wayPointNames = allWaypointNames.split(";")

                    val coordinatesSize = routeOptions.coordinates.size
                    listOf(wayPointNames.first()) +
                            wayPointNames.subList(
                                coordinatesSize - routeProgress.remainingWaypoints,
                                coordinatesSize
                            )
                }
        }
    }

    /**
     * If navigation begins, a location update is sometimes needed to force a
     * progress change update as soon as navigation is started.
     *
     * This method creates a location update from the first coordinate (origin) that created
     * the route.
     *
     * @param route with list of coordinates
     * @return [Location] from first coordinate
     * @since 0.10.0
     */
    fun createFirstLocationFromRoute(route: DirectionsRoute): Location {
        val lineString = LineString(
            route.legs.firstOrNull()?.steps?.firstOrNull()?.geometry ?: route.geometry,
            Constants.PRECISION_6
        )
        val firstRoutePoint = lineString.coordinates.first()
        return Location(
            provider = FORCED_LOCATION,
            latitude = firstRoutePoint.latitude,
            longitude = firstRoutePoint.longitude,
            altitude = firstRoutePoint.altitude,
        )
    }

    /**
     * Given the current step / current step distance remaining, this function will
     * find the current instructions to be shown.
     *
     * @param currentStep           holding the current banner instructions
     * @param stepDistanceRemaining to determine progress along the currentStep
     * @return the current banner instructions based on the current distance along the step
     * @since 0.13.0
     */
    fun findCurrentBannerInstructions(
        currentStep: LegStep,
        stepDistanceRemaining: Double
    ): BannerInstructions? {
        return currentStep.bannerInstructions
            ?.takeIf { instructions -> instructions.isNotEmpty() }
            ?.let { bannerInstructions ->
                val instructions =
                    bannerInstructions.sortedBy(BannerInstructions::distanceAlongGeometry)
                for (instruction in instructions) {
                    val distanceAlongGeometry = instruction.distanceAlongGeometry
                    if (distanceAlongGeometry >= stepDistanceRemaining) {
                        return@let instruction
                    }
                }
                instructions.firstOrNull()
            }
    }

    /**
     * This method returns the current [BannerText] based on the currentStep distance
     * remaining.
     *
     *
     * When called, this is the banner text that should be shown at the given point along the route.
     *
     * @param currentStep           holding the current banner instructions
     * @param stepDistanceRemaining to determine progress along the currentStep
     * @param findPrimary           if the primary or secondary BannerText should be retrieved
     * @return current BannerText based on currentStep distance remaining
     * @since 0.13.0
     */
    fun findCurrentBannerText(
        currentStep: LegStep,
        stepDistanceRemaining: Double,
        findPrimary: Boolean
    ): BannerText? {
        return findCurrentBannerInstructions(currentStep, stepDistanceRemaining)
            ?.let { instructions ->
            retrievePrimaryOrSecondaryBannerText(findPrimary, instructions)
        }
    }

    private fun retrievePrimaryOrSecondaryBannerText(
        findPrimary: Boolean,
        instruction: BannerInstructions
    ): BannerText? {
        return if (findPrimary) instruction.primary else instruction.secondary
    }

    /**
     * This method returns the current [VoiceInstructions] based on the step distance
     * remaining.
     *
     * @param currentStep           holding the current banner instructions
     * @param stepDistanceRemaining to determine progress along the step
     * @return current voice instructions based on step distance remaining
     * @since 0.13.0
     */
    fun findCurrentVoiceInstructions(
        currentStep: LegStep,
        stepDistanceRemaining: Double
    ): VoiceInstructions? {
        return currentStep.voiceInstructions
            ?.takeIf { instructions -> instructions.isNotEmpty() }
            ?.let { voiceInstructions ->
                val instructions =
                    voiceInstructions.sortedBy(VoiceInstructions::distanceAlongGeometry)
                for (instruction in instructions) {
                    val distanceAlongGeometry = instruction.distanceAlongGeometry
                    if (distanceAlongGeometry >= stepDistanceRemaining) {
                        return@let instruction
                    }
                }
                instructions.firstOrNull()
            }
    }

    private fun upcomingStepIsArrivalManeuverType(routeProgress: RouteProgress): Boolean {
        return routeProgress.currentLegProgress.upComingStep?.maneuver?.type?.text?.contains(
            NavigationConstants.STEP_MANEUVER_TYPE_ARRIVE
        ) ?: false
    }

    private fun currentStepIsArrivalManeuverType(routeProgress: RouteProgress): Boolean {
        return routeProgress.currentLegProgress.currentStep.maneuver.type?.text?.contains(
            NavigationConstants.STEP_MANEUVER_TYPE_ARRIVE
        ) ?: false
    }

    companion object {
        const val FORCED_LOCATION = "Forced Location"
    }
}
