package org.maplibre.navigation.core.routeprogress

import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.milestone.MilestoneEventListener
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.models.LegStep
import org.maplibre.navigation.core.models.RouteLeg
import org.maplibre.navigation.core.models.StepIntersection
import kotlin.math.max

/**
 * This is a progress object specific to the current leg the user is on. If there is only one leg
 * in the directions route, much of this information will be identical to the parent
 * [RouteProgress].
 * <p>
 * The latest route leg progress object can be obtained through either the [ProgressChangeListener]
 * or the [MilestoneEventListener] callbacks.
 * Note that the route leg progress object's immutable.
 * </p>
 *
 * @since 0.1.0
 */
data class RouteLegProgress(
    /**
     * Index representing the current step the user is on.
     *
     * @since 0.1.0
     */
    val stepIndex: Int,

    /**
     * Provides the distance remaining in meters tills the user reaches the end of the route.
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
    val currentStepPoints: List<Point>?,

    /**
     * Provides a list of points that represent the upcoming step
     * step geometry.
     *
     * @since 0.12.0
     */
    val upcomingStepPoints: List<Point>?,

    /**
     * Provides the current annotation data for a leg segment determined by
     * the distance traveled along the route.
     * <p>
     * This object will only be present when a [DirectionsRoute] is requested
     * with [DirectionsCriteria#ANNOTATION_DISTANCE].
     * <p>
     * This will be provided by default with [NavigationRoute#builder(context)].
     *
     * @since 0.13.0
     */
    val currentLegAnnotation: CurrentLegAnnotation?,

    val routeLeg: RouteLeg,

    val stepDistanceRemaining: Double,

    val intersections: List<StepIntersection>?,

    val currentIntersection: StepIntersection?,

    val upcomingIntersection: StepIntersection?,

    val intersectionDistancesAlongStep: Map<StepIntersection, Double>?
) {

    /**
     * Total distance traveled in meters along current leg.
     *
     * @since 0.1.0
     */
    val distanceTraveled: Double
        get() = max(0.0, routeLeg.distance - distanceRemaining)

    /**
     * Provides the duration remaining in seconds till the user reaches the end of the current step.
     *
     * @since 0.1.0
     */
    val durationRemaining: Double
        get() = (1 - fractionTraveled) * routeLeg.duration

    /**
     * Get the fraction traveled along the current leg, this is a float value between 0 and 1 and
     * isn't guaranteed to reach 1 before the user reaches the next waypoint.
     *
     * @since 0.1.0
     */
    val fractionTraveled: Float
        get() = if (routeLeg.distance > 0)
                max(0.0, distanceTraveled / routeLeg.distance).toFloat()
            else
                0f

    /**
     * Get the previous step the user traversed along, if the user is still on the first step, this
     * will return null.
     *
     * @since 0.1.0
     */
    val previousStep: LegStep?
        get() = if (stepIndex == 0) {
            null
        } else {
            routeLeg.steps[stepIndex - 1]
        }

    /**
     * Returns the current step the user is traversing along.
     *
     * @since 0.1.0
     */
    val currentStep: LegStep
        get() = routeLeg.steps[stepIndex]

    /**
     * Get the next/upcoming step immediately after the current step. If the user is on the last step
     * on the last leg, this will return null since a next step doesn't exist.
     *
     * @since 0.1.0
     */
    val upComingStep: LegStep?
        get() = routeLeg.steps.getOrNull(stepIndex + 1)

    /**
     * This will return the [LegStep] two steps ahead of the current step the user's on. If the
     * user's current step is within 2 steps of their final destination this will return null.
     *
     * @since 0.5.0
     */
    val followOnStep: LegStep?
        get() = routeLeg.steps.getOrNull(stepIndex + 2)

    /**
     * Gives a [RouteStepProgress] object with information about the particular step the user
     * is currently on.
     *
     * @since 0.1.0
     */
    val currentStepProgress: RouteStepProgress
        get() = RouteStepProgress(
            step = currentStep,
            nextStep = routeLeg.steps.getOrNull(stepIndex + 1),
            distanceRemaining = stepDistanceRemaining,
            intersections = intersections,
            currentIntersection = currentIntersection,
            upcomingIntersection = upcomingIntersection,
            intersectionDistancesAlongStep = intersectionDistancesAlongStep
        )
}
