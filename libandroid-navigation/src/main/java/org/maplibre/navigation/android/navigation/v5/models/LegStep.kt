package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Includes one [StepManeuver] object and travel to the following [LegStep].
 *
 * @since 1.0.0
 */
@Suppress("unused")
@Serializable
data class LegStep(

    /**
     * Gives the geometry of the leg step as encoded polyline string.
     *
     * @since 1.0.0
     */
    val geometry: String,

    /**
     * The distance traveled from the maneuver to the next [LegStep] in meters.
     *
     * @since 1.0.0
     */
    val distance: Double,

    /**
     * The estimated travel time from the maneuver to the next [LegStep] in seconds.
     *
     * @since 1.0.0
     */

    val duration: Double,

    /**
     * The typical travel time for traversing this LegStep in seconds. There's a delay along the LegStep
     * if you subtract this durationTypical() value from the LegStep duration() value and
     * the resulting difference is greater than 0. The delay is because of any number
     * of real-world situations (road repair, traffic jam, etc).
     *
     * @since 5.5.0
     */
    @SerialName("duration_typical")
    val durationTypical: Double?,

    /**
     * Speed limit unit as per the locale.
     */
    val speedLimitUnit: SpeedLimit.Unit?,

    /**
     * Speed limit sign type.
     *
     * @see SpeedLimitSign
     */
    val speedLimitSign: SpeedLimitSign?,

    /**
     * String with the name of the way along which the travel proceeds.
     *
     * @since 1.0.0
     */
    val name: String?,

    /**
     * Any road designations associated with the road or path leading from this step&#39;s
     * maneuver to the next step&#39;s maneuver. Optionally included, if data is available.
     * If multiple road designations are associated with the road, they are separated by semicolons.
     * A road designation typically consists of an alphabetic network code (identifying the road type
     * or numbering system), a space or hyphen, and a route number. You should not assume that
     * the network code is globally unique: for example, a network code of &quot;NH&quot; may appear
     * on a &quot;National Highway&quot; or &quot;New Hampshire&quot;. Moreover, a route number may
     * not even uniquely identify a road within a given network.
     *
     * @since 2.0.0
     */
    val ref: String?,

    /**
     * String with the destinations of the way along which the travel proceeds.
     *
     * @since 2.0.0
     */
    val destinations: String?,

    /**
     * indicates the mode of transportation in the step.
     *
     * @since 1.0.0
     */
    val mode: String,

    /**
     * The pronunciation hint of the way name. Will be undefined if no pronunciation is hit.
     *
     * @since 2.0.0
     */
    val pronunciation: String?,

    /**
     * An optional string indicating the name of the rotary. This will only be a nonnull when the
     * maneuver type equals `rotary`.
     *
     * @since 2.0.0
     */
    @SerialName("rotary_name")
    val rotaryName: String?,

    /**
     * An optional string indicating the pronunciation of the name of the rotary. This will only be a
     * nonnull when the maneuver type equals `rotary`.
     *
     * @since 2.0.0
     */
    @SerialName("rotary_pronunciation")
    val rotaryPronunciation: String?,

    /**
     * A [StepManeuver] object that typically represents the first coordinate making up the
     * [LegStep.geometry].
     *
     * @since 1.0.0
     */
    val maneuver: StepManeuver,

    /**
     * The voice instructions object is useful for navigation sessions providing well spoken text
     * instructions along with the distance from the maneuver the instructions should be said.
     *
     * @since 3.0.0
     */
    val voiceInstructions: List<VoiceInstructions>?,

    /**
     * If in your request you set <tt>MapboxDirections.Builder#bannerInstructions()</tt> to true,
     * you'll receive a list of [BannerInstructions] which encompasses all information necessary
     * for creating a visual cue about a given [LegStep].
     *
     * @since 3.0.0
     */
    val bannerInstructions: List<BannerInstructions>?,

    /**
     * The legal driving side at the location for this step. Result will either be `left` or
     * `right`.
     *
     * @since 3.0.0
     */
    @SerialName("driving_side")
    val drivingSide: String?,

    /**
     * Specifies a decimal precision of edge weights, default value 1.
     *
     * @since 2.1.0
     */
    val weight: Double,

    /**
     * Provides a list of all the intersections connected to the current way the user is traveling
     * along.
     *
     * @since 1.3.0
     */
    val intersections: List<StepIntersection>?,

    /**
     * String with the exit numbers or names of the way. Optionally included, if data is available.
     *
     * @since 3.0.0
     */
    val exits: String?
) {

    enum class SpeedLimitSign(val text: String) {
        @SerialName("mutcd")
        MUTCD("mutcd"),

        @SerialName("vienna")
        VIENNA("vienna")
    }
}
