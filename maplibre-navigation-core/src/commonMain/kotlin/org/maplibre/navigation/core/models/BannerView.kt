package org.maplibre.navigation.core.models

import kotlinx.serialization.Serializable

/**
 * Includes both plain text information that can be visualized inside your navigation application
 * along with the text string broken down into [BannerComponents] which may or may not
 * include a image url. To receive this information, your request must have
 * <tt>MapboxDirections.Builder#bannerInstructions()</tt> set to true.
 *
 * @since 5.0.0
 */
@Serializable
data class BannerView(
    /**
     * Plain text with all the [BannerComponents] text combined.
     *
     * @since 5.0.0
     */
    val text: String,

    /**
     * A part or element of the [BannerInstructions].
     *
     * @since 5.0.0
     */
    val components: List<BannerComponents>? = null,

    /**
     * This indicates the type of maneuver.
     *
     * @see StepManeuver.Type
     *
     * @since 5.0.0
     */
    val type: StepManeuver.Type? = null,

    /**
     * This indicates the mode of the maneuver. If type is of turn, the modifier indicates the
     * change in direction accomplished through the turn. If the type is of depart/arrive, the
     * modifier indicates the position of waypoint from the current direction of travel.
     *
     * @since 5.0.0
     */
    val modifier: ManeuverModifier.Type? = null,
){

    /**
     * Creates a builder initialized with the current values of the `BannerView` instance.
     */
    fun toBuilder(): Builder {
        return Builder(
            text = text
        ).apply {
            withComponents(components)
            withType(type)
            withModifier(modifier)
        }
    }

    /**
     * Builder class for creating `BannerView` instances.
     * @param text Plain text with all the [BannerComponents] text combined.
     */
    class Builder(
        private var text: String
    ) {
        private var components: List<BannerComponents>? = null
        private var type: StepManeuver.Type? = null
        private var modifier: ManeuverModifier.Type? = null

        /**
         * Sets the components.
         *
         * @param components The components.
         * @return The builder instance.
         */
        fun withComponents(components: List<BannerComponents>?) = apply { this.components = components }

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
         * Builds a `BannerView` instance with the current builder values.
         *
         * @return A new `BannerView` instance.
         */
        fun build(): BannerView {
            return BannerView(
                text = text,
                components = components,
                type = type,
                modifier = modifier
            )
        }
    }
}
