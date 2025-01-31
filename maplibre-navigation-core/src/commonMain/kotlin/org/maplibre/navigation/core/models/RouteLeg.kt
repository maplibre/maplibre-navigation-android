package org.maplibre.navigation.core.models

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
    val durationTypical: Double? = null,

    /**
     * A short human-readable summary of major roads traversed. Useful to distinguish alternatives.
     *
     * @return String with summary
     * @since 1.0.0
     */
    val summary: String? = null,

    /**
     * An array of objects describing the administrative boundaries the route leg travels through.
     * Use [StepIntersection.adminIndex] on the intersection object
     * to look up the admin for each intersection in this array.
     */
    val admins: List<Admin>? = null,

    /**
     * A list of incidents that occur on this leg.
     *
     * @return a list of [Incident]
     */
    val incidents: List<Incident>? = null,

    /**
     * A [LegAnnotation] that contains additional details about each line segment along the
     * route geometry. If you'd like to receiving this, you must request it inside your Directions
     * request before executing the call.
     *
     * @return a [LegAnnotation] object
     * @since 2.1.0
     */
    val annotation: LegAnnotation? = null,

    /**
     * A list of closures that occur on this leg.
     *
     * @return a list of [Incident]
     */
    val closures: List<Closure>? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `RouteLeg` instance.
     */
    fun toBuilder(): Builder {
        return Builder(
            distance = distance,
            duration = duration,
            steps = steps
        ).apply {
            withDurationTypical(durationTypical)
            withSummary(summary)
            withAdmins(admins)
            withIncidents(incidents)
            withAnnotation(annotation)
            withClosures(closures)
        }
    }

    /**
     * Builder class for creating `RouteLeg` instances.
     * @param distance The distance traveled from one waypoint to another.
     * @param duration The estimated travel time from one waypoint to another.
     * @param steps Gives a List including all the steps to get from one waypoint to another.
     */
    class Builder(
        private var distance: Double,
        private var duration: Double,
        private var steps: List<LegStep>
    ) {
        private var durationTypical: Double? = null
        private var summary: String? = null
        private var admins: List<Admin>? = null
        private var incidents: List<Incident>? = null
        private var annotation: LegAnnotation? = null
        private var closures: List<Closure>? = null

        /**
         * Sets the typical duration.
         *
         * @param durationTypical The typical duration.
         * @return The builder instance.
         */
        fun withDurationTypical(durationTypical: Double?) = apply { this.durationTypical = durationTypical }

        /**
         * Sets the summary.
         *
         * @param summary The summary.
         * @return The builder instance.
         */
        fun withSummary(summary: String?) = apply { this.summary = summary }

        /**
         * Sets the admins.
         *
         * @param admins The admins.
         * @return The builder instance.
         */
        fun withAdmins(admins: List<Admin>?) = apply { this.admins = admins }

        /**
         * Sets the incidents.
         *
         * @param incidents The incidents.
         * @return The builder instance.
         */
        fun withIncidents(incidents: List<Incident>?) = apply { this.incidents = incidents }

        /**
         * Sets the annotation.
         *
         * @param annotation The annotation.
         * @return The builder instance.
         */
        fun withAnnotation(annotation: LegAnnotation?) = apply { this.annotation = annotation }

        /**
         * Sets the closures.
         *
         * @param closures The closures.
         * @return The builder instance.
         */
        fun withClosures(closures: List<Closure>?) = apply { this.closures = closures }

        /**
         * Builds a `RouteLeg` instance with the current builder values.
         *
         * @return A new `RouteLeg` instance.
         */
        fun build(): RouteLeg {
            return RouteLeg(
                distance = distance,
                duration = duration,
                steps = steps,
                durationTypical = durationTypical,
                summary = summary,
                admins = admins,
                incidents = incidents,
                annotation = annotation,
                closures = closures
            )
        }
    }
}