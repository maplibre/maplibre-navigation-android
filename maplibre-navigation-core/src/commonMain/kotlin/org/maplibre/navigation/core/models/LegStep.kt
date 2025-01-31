package org.maplibre.navigation.core.models

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
    val durationTypical: Double? = null,

    /**
     * Speed limit unit as per the locale.
     */
    val speedLimitUnit: SpeedLimit.Unit? = null,

    /**
     * Speed limit sign type.
     *
     * @see SpeedLimitSign
     */
    val speedLimitSign: SpeedLimitSign? = null,

    /**
     * String with the name of the way along which the travel proceeds.
     *
     * @since 1.0.0
     */
    val name: String? = null,

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
    val ref: String? = null,

    /**
     * String with the destinations of the way along which the travel proceeds.
     *
     * @since 2.0.0
     */
    val destinations: String? = null,

    /**
     * Indicates the mode of transportation in the step.
     *
     * @since 1.0.0
     */
    val mode: String = "driving",

    /**
     * The pronunciation hint of the way name. Will be undefined if no pronunciation is hit.
     *
     * @since 2.0.0
     */
    val pronunciation: String? = null,

    /**
     * An optional string indicating the name of the rotary. This will only be a nonnull when the
     * maneuver type equals `rotary`.
     *
     * @since 2.0.0
     */
    @SerialName("rotary_name")
    val rotaryName: String? = null,

    /**
     * An optional string indicating the pronunciation of the name of the rotary. This will only be a
     * nonnull when the maneuver type equals `rotary`.
     *
     * @since 2.0.0
     */
    @SerialName("rotary_pronunciation")
    val rotaryPronunciation: String? = null,

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
    val voiceInstructions: List<VoiceInstructions>? = null,

    /**
     * If in your request you set <tt>MapboxDirections.Builder#bannerInstructions()</tt> to true,
     * you'll receive a list of [BannerInstructions] which encompasses all information necessary
     * for creating a visual cue about a given [LegStep].
     *
     * @since 3.0.0
     */
    val bannerInstructions: List<BannerInstructions>? = null,

    /**
     * The legal driving side at the location for this step. Result will either be `left` or
     * `right`.
     *
     * @since 3.0.0
     */
    @SerialName("driving_side")
    val drivingSide: String? = null,

    /**
     * Specifies a decimal precision of edge weights, default value 1.
     *
     * @since 2.1.0
     */
    val weight: Double = 1.0,

    /**
     * Provides a list of all the intersections connected to the current way the user is traveling
     * along.
     *
     * @since 1.3.0
     */
    val intersections: List<StepIntersection>? = null,

    /**
     * String with the exit numbers or names of the way. Optionally included, if data is available.
     *
     * @since 3.0.0
     */
    val exits: String? = null
)  {

    /**
     * Creates a builder initialized with the current values of the `LegStep` instance.
     */
    fun toBuilder(): Builder {
        return Builder(
            geometry = geometry,
            distance = distance,
            duration = duration,
            maneuver = maneuver,
        ).apply {
            withMode(mode)
            withWeight(weight)
            withDurationTypical(durationTypical)
            withSpeedLimitUnit(speedLimitUnit)
            withSpeedLimitSign(speedLimitSign)
            withName(name)
            withRef(ref)
            withDestinations(destinations)
            withPronunciation(pronunciation)
            withRotaryName(rotaryName)
            withRotaryPronunciation(rotaryPronunciation)
            withVoiceInstructions(voiceInstructions)
            withBannerInstructions(bannerInstructions)
            withDrivingSide(drivingSide)
            withIntersections(intersections)
            withExits(exits)
        }
    }

    @Serializable
    enum class SpeedLimitSign(val text: String) {
        @SerialName("mutcd")
        MUTCD("mutcd"),

        @SerialName("vienna")
        VIENNA("vienna")
    }

    /**
     * Builder class for creating `LegStep` instances.
     * @param geometry Gives the geometry of the leg step as encoded polyline string.
     * @param distance The distance traveled from the maneuver to the next [LegStep] in meters.
     * @param duration The estimated travel time from the maneuver to the next [LegStep] in seconds.
     * @param mode Indicates the mode of transportation in the step.
     * @param maneuver A [StepManeuver] object that typically represents the first coordinate making up the [LegStep.geometry].
     * @param weight Specifies a decimal precision of edge weights, default value 1.
     */
    class Builder(
        private var geometry: String,
        private var distance: Double,
        private var duration: Double,
        private var maneuver: StepManeuver,
    ) {
        private var weight: Double = 1.0
        private var mode: String = "driving"
        private var durationTypical: Double? = null
        private var speedLimitUnit: SpeedLimit.Unit? = null
        private var speedLimitSign: SpeedLimitSign? = null
        private var name: String? = null
        private var ref: String? = null
        private var destinations: String? = null
        private var pronunciation: String? = null
        private var rotaryName: String? = null
        private var rotaryPronunciation: String? = null
        private var voiceInstructions: List<VoiceInstructions>? = null
        private var bannerInstructions: List<BannerInstructions>? = null
        private var drivingSide: String? = null
        private var intersections: List<StepIntersection>? = null
        private var exits: String? = null

        /**
         * Set the mode of transportation in the step. Default value is `driving`.
         *
         * @param mode The mode of transportation in the step.
         * @return The builder instance.
         */
        fun withMode(mode: String) = apply { this.mode = mode }

        /**
         * Set a decimal precision of edge weights. Default value is `1`.
         *
         * @param weight The decimal precision of edge weights.
         * @return The builder instance.
         */
        fun withWeight(weight: Double) = apply { this.weight = weight }

        /**
         * Sets the typical duration.
         *
         * @param durationTypical The typical duration.
         * @return The builder instance.
         */
        fun withDurationTypical(durationTypical: Double?) = apply { this.durationTypical = durationTypical }

        /**
         * Sets the speed limit unit.
         *
         * @param speedLimitUnit The speed limit unit.
         * @return The builder instance.
         */
        fun withSpeedLimitUnit(speedLimitUnit: SpeedLimit.Unit?) = apply { this.speedLimitUnit = speedLimitUnit }

        /**
         * Sets the speed limit sign.
         *
         * @param speedLimitSign The speed limit sign.
         * @return The builder instance.
         */
        fun withSpeedLimitSign(speedLimitSign: SpeedLimitSign?) = apply { this.speedLimitSign = speedLimitSign }

        /**
         * Sets the name.
         *
         * @param name The name.
         * @return The builder instance.
         */
        fun withName(name: String?) = apply { this.name = name }

        /**
         * Sets the reference.
         *
         * @param ref The reference.
         * @return The builder instance.
         */
        fun withRef(ref: String?) = apply { this.ref = ref }

        /**
         * Sets the destinations.
         *
         * @param destinations The destinations.
         * @return The builder instance.
         */
        fun withDestinations(destinations: String?) = apply { this.destinations = destinations }

        /**
         * Sets the pronunciation.
         *
         * @param pronunciation The pronunciation.
         * @return The builder instance.
         */
        fun withPronunciation(pronunciation: String?) = apply { this.pronunciation = pronunciation }

        /**
         * Sets the rotary name.
         *
         * @param rotaryName The rotary name.
         * @return The builder instance.
         */
        fun withRotaryName(rotaryName: String?) = apply { this.rotaryName = rotaryName }

        /**
         * Sets the rotary pronunciation.
         *
         * @param rotaryPronunciation The rotary pronunciation.
         * @return The builder instance.
         */
        fun withRotaryPronunciation(rotaryPronunciation: String?) = apply { this.rotaryPronunciation = rotaryPronunciation }

        /**
         * Sets the voice instructions.
         *
         * @param voiceInstructions The voice instructions.
         * @return The builder instance.
         */
        fun withVoiceInstructions(voiceInstructions: List<VoiceInstructions>?) = apply { this.voiceInstructions = voiceInstructions }

        /**
         * Sets the banner instructions.
         *
         * @param bannerInstructions The banner instructions.
         * @return The builder instance.
         */
        fun withBannerInstructions(bannerInstructions: List<BannerInstructions>?) = apply { this.bannerInstructions = bannerInstructions }

        /**
         * Sets the driving side.
         *
         * @param drivingSide The driving side.
         * @return The builder instance.
         */
        fun withDrivingSide(drivingSide: String?) = apply { this.drivingSide = drivingSide }

        /**
         * Sets the intersections.
         *
         * @param intersections The intersections.
         * @return The builder instance.
         */
        fun withIntersections(intersections: List<StepIntersection>?) = apply { this.intersections = intersections }

        /**
         * Sets the exits.
         *
         * @param exits The exits.
         * @return The builder instance.
         */
        fun withExits(exits: String?) = apply { this.exits = exits }

        /**
         * Builds a `LegStep` instance with the current builder values.
         *
         * @return A new `LegStep` instance.
         */
        fun build(): LegStep {
            return LegStep(
                geometry = geometry,
                distance = distance,
                duration = duration,
                durationTypical = durationTypical,
                speedLimitUnit = speedLimitUnit,
                speedLimitSign = speedLimitSign,
                name = name,
                ref = ref,
                destinations = destinations,
                mode = mode,
                pronunciation = pronunciation,
                rotaryName = rotaryName,
                rotaryPronunciation = rotaryPronunciation,
                maneuver = maneuver,
                voiceInstructions = voiceInstructions,
                bannerInstructions = bannerInstructions,
                drivingSide = drivingSide,
                weight = weight,
                intersections = intersections,
                exits = exits
            )
        }
    }
}
