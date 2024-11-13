package org.maplibre.navigation.android.navigation.v5.models

import androidx.annotation.StringDef

/**
 * Constants for the [StepManeuver.modifier].
 *
 * @since 5.2.0
 */
object ManeuverModifier {

    enum class Type(text: String) {

        /**
         * Indicates "uturn" maneuver modifier.
         *
         * @since 5.2.0
         */
        UTURN("uturn"),

        /**
         * Indicates "sharp right" maneuver modifier.
         *
         * @since 5.2.0
         */
        SHARP_RIGHT("sharp right"),

        /**
         * Indicates "right" maneuver modifier.
         *
         * @since 5.2.0
         */
        RIGHT("right"),

        /**
         * Indicates "slight right" maneuver modifier.
         *
         * @since 5.2.0
         */
        SLIGHT_RIGHT("slight right"),

        /**
         * Indicates "straight" maneuver modifier.
         *
         * @since 5.2.0
         */
        STRAIGHT("straight"),

        /**
         * Indicates "slight left" maneuver modifier.
         *
         * @since 5.2.0
         */
        SLIGHT_LEFT("slight left"),

        /**
         * Indicates "left" maneuver modifier.
         *
         * @since 5.2.0
         */
        LEFT("left"),

        /**
         * Indicates "sharp left" maneuver modifier.
         *
         * @since 5.2.0
         */
        SHARP_LEFT("sharp left"),
    }
}
