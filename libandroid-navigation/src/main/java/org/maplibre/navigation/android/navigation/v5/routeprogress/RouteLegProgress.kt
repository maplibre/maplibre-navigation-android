package org.maplibre.navigation.android.navigation.v5.routeprogress

import android.util.Pair
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.milestone.MilestoneEventListener
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.models.RouteLeg
import org.maplibre.navigation.android.navigation.v5.models.StepIntersection
import java.lang.Double.max

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
    val distanceTraveled: Double?
        get() = routeLeg.distance?.let { distance ->
            max(0.0, distance - distanceRemaining)
        }

    /**
     * Provides the duration remaining in seconds till the user reaches the end of the current step.
     *
     * @since 0.1.0
     */
    val durationRemaining: Double?
        get() = routeLeg.duration?.let { routeDuration ->
            fractionTraveled?.let { fractionTraveled ->
                (1 - fractionTraveled) * routeDuration
            }
        }

    /**
     * Get the fraction traveled along the current leg, this is a float value between 0 and 1 and
     * isn't guaranteed to reach 1 before the user reaches the next waypoint.
     *
     * @since 0.1.0
     */
    val fractionTraveled: Float?
        get() = routeLeg.distance
            ?.takeIf { distance -> distance > 0 }
            ?.let { routeDistance ->
                distanceTraveled?.let { distanceTraveled ->
                    max(0.0, distanceTraveled / routeDistance)
                }
            }?.toFloat()

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
            routeLeg.steps?.get(stepIndex - 1)
        }

    /**
     * Returns the current step the user is traversing along.
     *
     * @since 0.1.0
     */
    val currentStep: LegStep?
        get() = routeLeg.steps?.get(stepIndex)

    /**
     * Get the next/upcoming step immediately after the current step. If the user is on the last step
     * on the last leg, this will return null since a next step doesn't exist.
     *
     * @since 0.1.0
     */
    val upComingStep: LegStep?
        get() = if (((routeLeg.steps?.size ?: 0) - 1) > stepIndex) {
            routeLeg.steps?.get(stepIndex + 1)
        } else {
            null
        }

    /**
     * This will return the [LegStep] two steps ahead of the current step the user's on. If the
     * user's current step is within 2 steps of their final destination this will return null.
     *
     * @since 0.5.0
     */
    val followOnStep: LegStep?
        get() = if (((routeLeg.steps?.size ?: 0) - 2) > stepIndex) {
            routeLeg.steps?.get(stepIndex + 2)
        } else {
            null
        }

    /**
     * Gives a [RouteStepProgress] object with information about the particular step the user
     * is currently on.
     *
     * @since 0.1.0
     */
    val currentStepProgress: RouteStepProgress?
        get() = routeLeg.steps?.get(stepIndex)?.let { currentStep ->
            RouteStepProgress(
                step = currentStep,
                nextStep = routeLeg.steps?.getOrNull(stepIndex + 1),
                distanceRemaining = stepDistanceRemaining,
                intersections = intersections,
                currentIntersection = currentIntersection,
                upcomingIntersection = upcomingIntersection,
                intersectionDistancesAlongStep = intersectionDistancesAlongStep
            )
        }
}
