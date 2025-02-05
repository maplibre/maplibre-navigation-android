package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Includes both plain text information that can be visualized inside your navigation application
 * along with the text string broken down into [BannerComponents] which may or may not
 * include a image url. To receive this information, your request must have
 * <tt>MapboxDirections.Builder#bannerInstructions()</tt> set to true.
 *
 * @since 3.0.0
 */
@Serializable
data class BannerText(

    /**
     * Plain text with all the [BannerComponents] text combined.
     *
     * @return plain text with all the [BannerComponents] text items combined
     * @since 3.0.0
     */
    val text: String,

    /**
     * A part or element of the [BannerInstructions].
     *
     * @return a [BannerComponents] specific to a [LegStep]
     * @since 3.0.0
     */
    val components: List<BannerComponents>? = null,

    /**
     * This indicates the type of maneuver.
     *
     * @return String with type of maneuver
     * @see StepManeuver.Type
     *
     * @since 3.0.0
     */
    val type: StepManeuver.Type? = null,

    /**
     * This indicates the mode of the maneuver. If type is of turn, the modifier indicates the
     * change in direction accomplished through the turn. If the type is of depart/arrive, the
     * modifier indicates the position of waypoint from the current direction of travel.
     *
     * @return String with modifier
     * @since 3.0.0
     */
    val modifier: ManeuverModifier.Type? = null,

    /**
     * The degrees at which you will be exiting a roundabout, assuming `180` indicates
     * going straight through the roundabout.
     *
     * @return at which you will be exiting a roundabout
     * @since 3.0.0
     */
    val degrees: Double? = null,

    /**
     * A string representing which side the of the street people drive on
     * in that location. Can be 'left' or 'right'.
     *
     * @return String either `left` or `right`
     * @since 3.0.0
     */
    @SerialName("driving_side")
    val drivingSide: String? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `BannerText` instance.
     */
    fun toBuilder(): Builder {
        return Builder(
            text = text
        ).apply {
            withComponents(components)
            withType(type)
            withModifier(modifier)
            withDegrees(degrees)
            withDrivingSide(drivingSide)
        }
    }

    /**
     * Builder class for creating `BannerText` instances.
     * @param text Plain text with all the [BannerComponents] text combined.
     */
    class Builder(
        private var text: String
    ) {
        private var components: List<BannerComponents>? = null
        private var type: StepManeuver.Type? = null
        private var modifier: ManeuverModifier.Type? = null
        private var degrees: Double? = null
        private var drivingSide: String? = null

        /**
         * Sets the components.
         *
         * @param components The components.
         * @return The builder instance.
         */
        fun withComponents(components: List<BannerComponents>?) =
            apply { this.components = components }

        /**
         * Sets the type.
         *
         * @param type The type.
         * @return The builder instance.
         */
        fun withType(type: StepManeuver.Type?) = apply { this.type = type }

        /**
         * Sets the modifier.
         *
         * @param modifier The modifier.
         * @return The builder instance.
         */
        fun withModifier(modifier: ManeuverModifier.Type?) = apply { this.modifier = modifier }

        /**
         * Sets the degrees.
         *
         * @param degrees The degrees.
         * @return The builder instance.
         */
        fun withDegrees(degrees: Double?) = apply { this.degrees = degrees }

        /**
         * Sets the driving side.
         *
         * @param drivingSide The driving side.
         * @return The builder instance.
         */
        fun withDrivingSide(drivingSide: String?) = apply { this.drivingSide = drivingSide }

        /**
         * Builds a `BannerText` instance with the current builder values.
         *
         * @return A new `BannerText` instance.
         */
        fun build(): BannerText {
            return BannerText(
                text = text,
                components = components,
                type = type,
                modifier = modifier,
                degrees = degrees,
                drivingSide = drivingSide
            )
        }
    }
}


