package org.maplibre.navigation.android.navigation.v5.models

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
    val components: List<BannerComponents>?,

    /**
     * This indicates the type of maneuver.
     *
     * @see StepManeuver.Type
     *
     * @since 5.0.0
     */
    val type: StepManeuver.Type?,

    /**
     * This indicates the mode of the maneuver. If type is of turn, the modifier indicates the
     * change in direction accomplished through the turn. If the type is of depart/arrive, the
     * modifier indicates the position of waypoint from the current direction of travel.
     *
     * @since 5.0.0
     */
    val modifier: ManeuverModifier.Type?,
)
