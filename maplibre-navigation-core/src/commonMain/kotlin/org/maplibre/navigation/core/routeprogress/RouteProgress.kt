package org.maplibre.navigation.core.routeprogress

import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.models.RouteLeg
import org.maplibre.navigation.core.models.StepIntersection
import org.maplibre.navigation.core.navigation.MapLibreNavigation
import org.maplibre.navigation.core.milestone.MilestoneEventListener
import kotlin.math.max

/**
 * This class contains all progress information at any given time during a navigation session. This
 * progress includes information for the current route, leg and step the user is traversing along.
 * With every new valid location update, a new route progress will be generated using the latest
 * information.
 *
 * The latest route progress object can be obtained through either the [ProgressChangeListener]
 * or the [MilestoneEventListener] callbacks.
 * Note that the route progress object's immutable.
 *
 * @since 0.1.0
 */
data class RouteProgress(
    /**
     * Get the route the navigation session is currently using. When a reroute occurs and a new
     * directions route gets obtained, with the next location update this directions route should
     * reflect the new route. All direction route get passed in through
     * [MapLibreNavigation.startNavigation].
     *
     * @since 0.1.0
     */
    val directionsRoute: DirectionsRoute,

    /**
     * Index representing the current leg the user is on. If the directions route currently in use
     * contains more then two waypoints, the route is likely to have multiple legs representing the
     * distance between the two points.
     *
     * @since 0.1.0
     */
    val legIndex: Int,

    /**
     * Provides the distance remaining in meters till the user reaches the end of the route.
     *
     * @since 0.1.0
     */
    val distanceRemaining: Double,

    /**
     * Provides a list of points that represent the current step
     * step geometry.
     *
     * @since 0.12.0
     */
    val currentStepPoints: List<Point>,

    /**
     * Provides a list of points that represent the upcoming step
     * step geometry.
     *
     * @since 0.12.0
     */
    val upcomingStepPoints: List<Point>?,

    val stepIndex: Int,

    val legDistanceRemaining: Double,

    val stepDistanceRemaining: Double,

    val intersections: List<StepIntersection>?,

    val currentIntersection: StepIntersection?,

    val upcomingIntersection: StepIntersection?,

    val currentLegAnnotation: CurrentLegAnnotation?,

    val intersectionDistancesAlongStep: Map<StepIntersection, Double>?,
) {

    /**
     * Provides the current [RouteLeg] the user is on.
     *
     * @since 0.1.0
     */
    val currentLeg: RouteLeg
        get() = directionsRoute.legs[legIndex]

    /**
     * Total distance traveled in meters along route.
     *
     * @since 0.1.0
     */
    val distanceTraveled: Double
        get() = max(0.0, directionsRoute.distance - distanceRemaining)

    /**
     * Provides the duration remaining in seconds till the user reaches the end of the route.
     *
     * @since 0.1.0
     */
    val durationRemaining: Double
        get() = (1 - fractionTraveled) * directionsRoute.duration

    /**
     * Get the fraction traveled along the current route, this is a float value between 0 and 1 and
     * isn't guaranteed to reach 1 before the user reaches the end of the route.
     *
     * @since 0.1.0
     */
    val fractionTraveled: Float
        get() = directionsRoute.distance
            .takeIf { distance -> distance > 0 }
            ?.let { routeDistance ->
                max(0.0, distanceTraveled / routeDistance).toFloat()
            } ?: 1f

    /**
     * Number of waypoints remaining on the current route.
     *
     * @since 0.5.0
     */
    val remainingWaypoints: Int
        get() = directionsRoute.legs.size.minus(legIndex)

    /**
     * Gives a [RouteLegProgress] object with information about the particular leg the user is
     * currently on.
     *
     * @since 0.1.0
     */
    val currentLegProgress: RouteLegProgress
        get() = RouteLegProgress(
            routeLeg = directionsRoute.legs[legIndex],
            stepIndex = stepIndex,
            distanceRemaining = legDistanceRemaining,
            stepDistanceRemaining = stepDistanceRemaining,
            currentStepPoints = currentStepPoints,
            upcomingStepPoints = upcomingStepPoints,
            intersections = intersections,
            currentIntersection = currentIntersection,
            upcomingIntersection = upcomingIntersection,
            intersectionDistancesAlongStep = intersectionDistancesAlongStep,
            currentLegAnnotation = currentLegAnnotation,
        )



    fun toBuilder(): Builder {
        return Builder(
            directionsRoute = directionsRoute,
            legIndex = legIndex,
            distanceRemaining = distanceRemaining,
            currentStepPoints = currentStepPoints,
            stepIndex = stepIndex,
            legDistanceRemaining = legDistanceRemaining,
            stepDistanceRemaining = stepDistanceRemaining
        ).apply {
            withUpcomingStepPoints(upcomingStepPoints)
            withIntersections(intersections)
            withCurrentIntersection(currentIntersection)
            withUpcomingIntersection(upcomingIntersection)
            withCurrentLegAnnotation(currentLegAnnotation)
            withIntersectionDistancesAlongStep(intersectionDistancesAlongStep)
        }
    }

    class Builder(
        private var directionsRoute: DirectionsRoute,
        private var legIndex: Int,
        private var distanceRemaining: Double,
        private var currentStepPoints: List<Point>,
        private var stepIndex: Int,
        private var legDistanceRemaining: Double,
        private var stepDistanceRemaining: Double
    ) {
        private var upcomingStepPoints: List<Point>? = null
        private var intersections: List<StepIntersection>? = null
        private var currentIntersection: StepIntersection? = null
        private var upcomingIntersection: StepIntersection? = null
        private var currentLegAnnotation: CurrentLegAnnotation? = null
        private var intersectionDistancesAlongStep: Map<StepIntersection, Double>? = null

        fun withUpcomingStepPoints(upcomingStepPoints: List<Point>?) = apply { this.upcomingStepPoints = upcomingStepPoints }
        fun withIntersections(intersections: List<StepIntersection>?) = apply { this.intersections = intersections }
        fun withCurrentIntersection(currentIntersection: StepIntersection?) = apply { this.currentIntersection = currentIntersection }
        fun withUpcomingIntersection(upcomingIntersection: StepIntersection?) = apply { this.upcomingIntersection = upcomingIntersection }
        fun withCurrentLegAnnotation(currentLegAnnotation: CurrentLegAnnotation?) = apply { this.currentLegAnnotation = currentLegAnnotation }
        fun withIntersectionDistancesAlongStep(intersectionDistancesAlongStep: Map<StepIntersection, Double>?) = apply { this.intersectionDistancesAlongStep = intersectionDistancesAlongStep }

        fun build(): RouteProgress {
            return RouteProgress(
                directionsRoute = directionsRoute,
                legIndex = legIndex,
                distanceRemaining = distanceRemaining,
                currentStepPoints = currentStepPoints,
                upcomingStepPoints = upcomingStepPoints,
                stepIndex = stepIndex,
                legDistanceRemaining = legDistanceRemaining,
                stepDistanceRemaining = stepDistanceRemaining,
                intersections = intersections,
                currentIntersection = currentIntersection,
                upcomingIntersection = upcomingIntersection,
                currentLegAnnotation = currentLegAnnotation,
                intersectionDistancesAlongStep = intersectionDistancesAlongStep
            )
        }
    }
}