package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A route between only two [DirectionsWaypoint].
 *
 * @since 1.0.0
 */
@Serializable
data class RouteLeg(

    /**
     * The distance traveled from one waypoint to another.
     *
     * @return a double number with unit meters
     * @since 1.0.0
     */
    val distance: Double,

    /**
     * The estimated travel time from one waypoint to another.
     *
     * @return a double number with unit seconds
     * @since 1.0.0
     */
    val duration: Double,

    /**
     * Gives a List including all the steps to get from one waypoint to another.
     *
     * @return List of [LegStep]
     * @since 1.0.0
     */
    val steps: List<LegStep>,

    /**
     * The typical travel time for traversing this RouteLeg. There's a delay along the RouteLeg
     * if you subtract this durationTypical() value from the RouteLeg duration() value and
     * the resulting difference is greater than 0. The delay is because of any number
     * of real-world situations (road repair, traffic jam, etc).
     *
     * @return a double number with unit seconds
     * @since 5.5.0
     */
    @SerialName("duration_typical")
    val durationTypical: Double?,

    /**
     * A short human-readable summary of major roads traversed. Useful to distinguish alternatives.
     *
     * @return String with summary
     * @since 1.0.0
     */
    val summary: String?,

    /**
     * An array of objects describing the administrative boundaries the route leg travels through.
     * Use [StepIntersection.adminIndex] on the intersection object
     * to look up the admin for each intersection in this array.
     */
    val admins: List<Admin>?,

    /**
     * A list of incidents that occur on this leg.
     *
     * @return a list of [Incident]
     */
    val incidents: List<Incident>?,

    /**
     * A [LegAnnotation] that contains additional details about each line segment along the
     * route geometry. If you'd like to receiving this, you must request it inside your Directions
     * request before executing the call.
     *
     * @return a [LegAnnotation] object
     * @since 2.1.0
     */
    val annotation: LegAnnotation?,

    /**
     * A list of closures that occur on this leg.
     *
     * @return a list of [Incident]
     */
    val closures: List<Closure>?,
)