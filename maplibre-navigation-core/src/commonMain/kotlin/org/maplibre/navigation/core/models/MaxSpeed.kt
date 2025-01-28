package org.maplibre.navigation.core.models

import kotlinx.serialization.Serializable

/**
 * Object representing max speeds along a route.
 *
 * @since 3.0.0
 */
@Serializable
data class MaxSpeed(
    /**
     * Number indicating the posted speed limit.
     *
     * @since 3.0.0
     */
    val speed: Int? = null,

    /**
     * String indicating the unit of speed, either as `km/h` or `mph`.
     *
     * @since 3.0.0
     */
    val unit: SpeedLimit.Unit? = null,

    /**
     * Boolean is true if the speed limit is not known, otherwise null.
     *
     * @since 3.0.0
     */
    val unknown: Boolean? = null,

    /**
     * Boolean is `true` if the speed limit is unlimited, otherwise null.
     *
     * @since 3.0.0
     */
    val none: Boolean? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `MaxSpeed` instance.
     */
    fun toBuilder(): Builder {
        return Builder()
            .withSpeed(speed)
            .withUnit(unit)
            .withUnknown(unknown)
            .withNone(none)
    }

    /**
     * Builder class for creating `MaxSpeed` instances.
     * @param speed Number indicating the posted speed limit.
     * @param unit String indicating the unit of speed, either as `km/h` or `mph`.
     * @param unknown Boolean is true if the speed limit is not known, otherwise null.
     * @param none Boolean is `true` if the speed limit is unlimited, otherwise null.
     */
    class Builder {
        private var speed: Int? = null
        private var unit: SpeedLimit.Unit? = null
        private var unknown: Boolean? = null
        private var none: Boolean? = null

        /**
         * Sets the speed.
         *
         * @param speed The speed.
         * @return The builder instance.
         */
        fun withSpeed(speed: Int?) = apply { this.speed = speed }

        /**
         * Sets the unit.
         *
         * @param unit The unit.
         * @return The builder instance.
         */
        fun withUnit(unit: SpeedLimit.Unit?) = apply { this.unit = unit }

        /**
         * Sets the unknown status.
         *
         * @param unknown The unknown status.
         * @return The builder instance.
         */
        fun withUnknown(unknown: Boolean?) = apply { this.unknown = unknown }

        /**
         * Sets the none status.
         *
         * @param none The none status.
         * @return The builder instance.
         */
        fun withNone(none: Boolean?) = apply { this.none = none }

        /**
         * Builds a `MaxSpeed` instance with the current builder values.
         *
         * @return A new `MaxSpeed` instance.
         */
        fun build(): MaxSpeed {
            return MaxSpeed(
                speed = speed,
                unit = unit,
                unknown = unknown,
                none = none
            )
        }
    }
}