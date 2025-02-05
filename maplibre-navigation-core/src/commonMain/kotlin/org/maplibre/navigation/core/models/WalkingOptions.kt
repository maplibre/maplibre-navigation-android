package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class for specifying options for use with the walking profile.
 * @since 4.8.0
 */
@Serializable
data class WalkingOptions(

    /**
     * Walking speed in meters per second. Must be between 0.14 and 6.94 meters per second.
     * Defaults to 1.42 meters per second
     *
     * @since 4.8.0
     */
    @SerialName("walking_speed")
    val walkingSpeed: Double? = null,

    /**
     * A bias which determines whether the route should prefer or avoid the use of roads or paths
     * that are set aside for pedestrian-only use (walkways). The allowed range of values is from
     * -1 to 1, where -1 indicates indicates preference to avoid walkways, 1 indicates preference
     * to favor walkways, and 0 indicates no preference (the default).
     *
     * @since 4.8.0
     */
    @SerialName("walkway_bias")
    val walkwayBias: Double? = null,

    /**
     * A bias which determines whether the route should prefer or avoid the use of alleys. The
     * allowed range of values is from -1 to 1, where -1 indicates indicates preference to avoid
     * alleys, 1 indicates preference to favor alleys, and 0 indicates no preference (the default).
     *
     * @since 4.8.0
     */
    @SerialName("alley_bias")
    val alleyBias: Double? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `WalkingOptions` instance.
     */
    fun toBuilder(): Builder {
        return Builder().apply {
            withWalkingSpeed(walkingSpeed)
            withWalkwayBias(walkwayBias)
            withAlleyBias(alleyBias)
        }
    }

    /**
     * Builder class for creating `WalkingOptions` instances.
     */
    class Builder {
        private var walkingSpeed: Double? = null
        private var walkwayBias: Double? = null
        private var alleyBias: Double? = null

        /**
         * Sets the walking speed.
         *
         * @param walkingSpeed Walking speed in meters per second.
         * @return The builder instance.
         */
        fun withWalkingSpeed(walkingSpeed: Double?) = apply { this.walkingSpeed = walkingSpeed }

        /**
         * Sets the walkway bias.
         *
         * @param walkwayBias Bias for preferring or avoiding walkways.
         * @return The builder instance.
         */
        fun withWalkwayBias(walkwayBias: Double?) = apply { this.walkwayBias = walkwayBias }

        /**
         * Sets the alley bias.
         *
         * @param alleyBias Bias for preferring or avoiding alleys.
         * @return The builder instance.
         */
        fun withAlleyBias(alleyBias: Double?) = apply { this.alleyBias = alleyBias }

        /**
         * Builds a `WalkingOptions` instance with the current builder values.
         *
         * @return A new `WalkingOptions` instance.
         */
        fun build(): WalkingOptions {
            return WalkingOptions(
                walkingSpeed = walkingSpeed,
                walkwayBias = walkwayBias,
                alleyBias = alleyBias
            )
        }
    }
}
