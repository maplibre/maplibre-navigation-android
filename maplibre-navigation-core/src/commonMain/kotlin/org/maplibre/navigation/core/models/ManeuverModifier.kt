package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Constants for the [StepManeuver.modifier].
 *
 * @since 5.2.0
 */
object ManeuverModifier {

    @Serializable
    enum class Type(val text: String) {

        /**
         * Indicates "uturn" maneuver modifier.
         *
         * @since 5.2.0
         */
        @SerialName("uturn")
        UTURN("uturn"),

        /**
         * Indicates "sharp right" maneuver modifier.
         *
         * @since 5.2.0
         */
        @SerialName("sharp right")
        SHARP_RIGHT("sharp right"),

        /**
         * Indicates "right" maneuver modifier.
         *
         * @since 5.2.0
         */
        @SerialName("right")
        RIGHT("right"),

        /**
         * Indicates "slight right" maneuver modifier.
         *
         * @since 5.2.0
         */
        @SerialName("slight right")
        SLIGHT_RIGHT("slight right"),

        /**
         * Indicates "straight" maneuver modifier.
         *
         * @since 5.2.0
         */
        @SerialName("straight")
        STRAIGHT("straight"),

        /**
         * Indicates "slight left" maneuver modifier.
         *
         * @since 5.2.0
         */
        @SerialName("slight left")
        SLIGHT_LEFT("slight left"),

        /**
         * Indicates "left" maneuver modifier.
         *
         * @since 5.2.0
         */
        @SerialName("left")
        LEFT("left"),

        /**
         * Indicates "sharp left" maneuver modifier.
         *
         * @since 5.2.0
         */
        @SerialName("sharp left")
        SHARP_LEFT("sharp left"),
    }
}
