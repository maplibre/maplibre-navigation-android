package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An annotations object that contains additional details about each line segment along the route
 * geometry. Each entry in an annotations field corresponds to a coordinate along the route
 * geometry.
 *
 * @since 2.1.0
 */
@Serializable
data class LegAnnotation(

    /**
     * The distance, in meters, between each pair of coordinates.
     *
     * @since 2.1.0
     */
    val distance: List<Double>? = null,

    /**
     * The speed, in meters per second, between each pair of coordinates.
     *
     * @since 2.1.0
     */
    val duration: List<Double>? = null,

    /**
     * The speed, in meters per second, between each pair of coordinates.
     *
     * @since 2.1.0
     */
    val speed: List<Double>? = null,

    /**
     * The posted speed limit, between each pair of coordinates.
     * Maxspeed is only available for the `mapbox/driving` and `mapbox/driving-traffic`
     * profiles, other profiles will return `unknown`s only.
     *
     * @since 3.0.0
     */
    @SerialName("maxspeed")
    val maxSpeed: List<MaxSpeed>? = null,

    /**
     * The congestion between each pair of coordinates.
     *
     * @since 2.2.0
     */
    val congestion: List<String>? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `LegAnnotation` instance.
     */
    fun toBuilder(): Builder {
        return Builder()
            .withDistance(distance)
            .withDuration(duration)
            .withSpeed(speed)
            .withMaxSpeed(maxSpeed)
            .withCongestion(congestion)
    }

    /**
     * Builder class for creating `LegAnnotation` instances.
     * @param distance The distance, in meters, between each pair of coordinates.
     * @param duration The speed, in meters per second, between each pair of coordinates.
     * @param speed The speed, in meters per second, between each pair of coordinates.
     * @param maxSpeed The posted speed limit, between each pair of coordinates.
     * @param congestion The congestion between each pair of coordinates.
     */
    class Builder {
        private var distance: List<Double>? = null
        private var duration: List<Double>? = null
        private var speed: List<Double>? = null
        private var maxSpeed: List<MaxSpeed>? = null
        private var congestion: List<String>? = null

        /**
         * Sets the distance.
         *
         * @param distance The distance.
         * @return The builder instance.
         */
        fun withDistance(distance: List<Double>?) = apply { this.distance = distance }

        /**
         * Sets the duration.
         *
         * @param duration The duration.
         * @return The builder instance.
         */
        fun withDuration(duration: List<Double>?) = apply { this.duration = duration }

        /**
         * Sets the speed.
         *
         * @param speed The speed.
         * @return The builder instance.
         */
        fun withSpeed(speed: List<Double>?) = apply { this.speed = speed }

        /**
         * Sets the max speed.
         *
         * @param maxSpeed The max speed.
         * @return The builder instance.
         */
        fun withMaxSpeed(maxSpeed: List<MaxSpeed>?) = apply { this.maxSpeed = maxSpeed }

        /**
         * Sets the congestion.
         *
         * @param congestion The congestion.
         * @return The builder instance.
         */
        fun withCongestion(congestion: List<String>?) = apply { this.congestion = congestion }

        /**
         * Builds a `LegAnnotation` instance with the current builder values.
         *
         * @return A new `LegAnnotation` instance.
         */
        fun build(): LegAnnotation {
            return LegAnnotation(
                distance = distance,
                duration = duration,
                speed = speed,
                maxSpeed = maxSpeed,
                congestion = congestion
            )
        }
    }
}