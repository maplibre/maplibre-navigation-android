package org.maplibre.navigation.android.navigation.v5.models

import com.google.auto.value.AutoValue
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName

/**
 * Includes both plain text information that can be visualized inside your navigation application
 * along with the text string broken down into [BannerComponents] which may or may not
 * include a image url. To receive this information, your request must have
 * <tt>MapboxDirections.Builder#bannerInstructions()</tt> set to true.
 *
 * @since 3.0.0
 */
data class BannerText(

    /**
     * Plain text with all the [BannerComponents] text combined.
     *
     * @return plain text with all the [BannerComponents] text items combined
     * @since 3.0.0
     */
    val text: String,

    /**
     * A part or element of the [com.mapbox.api.directions.v5.models.BannerInstructions].
     *
     * @return a [BannerComponents] specific to a [LegStep]
     * @since 3.0.0
     */
    val components: List<BannerComponents>?,

    /**
     * This indicates the type of maneuver.
     *
     * @return String with type of maneuver
     * @see StepManeuver.StepManeuverType
     *
     * @since 3.0.0
     */
    val type: StepManeuver.Type?,

    /**
     * This indicates the mode of the maneuver. If type is of turn, the modifier indicates the
     * change in direction accomplished through the turn. If the type is of depart/arrive, the
     * modifier indicates the position of waypoint from the current direction of travel.
     *
     * @return String with modifier
     * @since 3.0.0
     */
    val modifier: ManeuverModifier.Type?,

    /**
     * The degrees at which you will be exiting a roundabout, assuming `180` indicates
     * going straight through the roundabout.
     *
     * @return at which you will be exiting a roundabout
     * @since 3.0.0
     */
    val degrees: Double?,

    /**
     * A string representing which side the of the street people drive on
     * in that location. Can be 'left' or 'right'.
     *
     * @return String either `left` or `right`
     * @since 3.0.0
     */
    @SerializedName("driving_side")
    val drivingSide: String?,
)

//TODO fabi755: json parsing