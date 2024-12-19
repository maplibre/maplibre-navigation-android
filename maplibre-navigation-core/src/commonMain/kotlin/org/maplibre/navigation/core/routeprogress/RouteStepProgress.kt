package org.maplibre.navigation.core.routeprogress

import org.maplibre.navigation.core.models.LegStep
import org.maplibre.navigation.core.models.StepIntersection
import kotlin.math.max
import org.maplibre.navigation.core.milestone.MilestoneEventListener

/**
 * This is a progress object specific to the current step the user is on.
 *
 * The latest route step progress object can be obtained through either the [ProgressChangeListener]
 * or the [MilestoneEventListener] callbacks.
 * Note that the route step progress object's immutable.
 *
 * @since 0.1.0
 */
data class RouteStepProgress(
    /**
     * Total distance in meters from user to end of step.
     *
     * @since 0.1.0
     */
    val distanceRemaining: Double,

    /**
     * A collection of all the current steps intersections and the next steps maneuver location
     * (if one exist).
     *
     * @since 0.7.0
     */
    val intersections: List<StepIntersection>?,

    /**
     * The current intersection that has been passed along the route.
     *
     *
     * An intersection is considered a current intersection once passed through
     * and will remain so until a different intersection is passed through.
     *
     * @since 0.13.0
     */
    val currentIntersection: StepIntersection?,

    /**
     * The intersection being traveled towards on the route.
     *
     *
     * Will be null if the upcoming step is null (last step of the leg).
     *
     * @since 0.13.0
     */
    val upcomingIntersection: StepIntersection?,

    /**
     * Provides a list of pairs containing two distances, in meters, along the route.
     *
     *
     * The first distance in the pair is the tunnel entrance along the step geometry.
     * The second distance is the tunnel exit along the step geometry.
     *
     * @since 0.13.0
     */
    val intersectionDistancesAlongStep: Map<StepIntersection, Double>?,

    val step: LegStep,

    val nextStep: LegStep?,
) {

    /**
     * Returns distance user has traveled along current step in unit meters.
     *
     * @since 0.1.0
     */
    val distanceTraveled: Double
        get() = max(0.0, step.distance - distanceRemaining)

    /**
     * Get the fraction traveled along the current step, this is a float value between 0 and 1 and
     * isn't guaranteed to reach 1 before the user reaches the next step (if another step exist in route).
     *
     * @since 0.1.0
     */
    val fractionTraveled: Float
        get() = step.distance
            .takeIf { distance -> distance > 0 }
            ?.let { stepDistance ->
                max(0.0, distanceTraveled / stepDistance).toFloat()
            } ?: 1f

    /**
     * Provides the duration remaining in seconds till the user reaches the end of the current step.
     *
     * @since 0.1.0
     */
    val durationRemaining: Double
        get() = (1 - fractionTraveled) * step.duration
}