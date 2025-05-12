package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.models.serializer.PointSerializer

/**
 * Gives maneuver information about one [LegStep].
 *
 * @since 1.0.0
 */
@Suppress("unused")
@Serializable
data class StepManeuver(

    /**
     * A [Point] representing this intersection location.
     *
     * @since 3.0.0
     */
    @Serializable(with = PointSerializer::class)
    val location: Point,

    /**
     * Number between 0 and 360 indicating the clockwise angle from true north to the direction of
     * travel right before the maneuver.
     *
     * @since 1.0.0
     */
    @SerialName("bearing_before")
    val bearingBefore: Double,

    /**
     * Number between 0 and 360 indicating the clockwise angle from true north to the direction of
     * travel right after the maneuver.
     *
     * @since 1.0.0
     */
    @SerialName("bearing_after")
    val bearingAfter: Double,

    /**
     * A human-readable instruction of how to execute the returned maneuver. This String is built
     * using OSRM-Text-Instructions and can be further customized inside either the Mapbox Navigation
     * SDK for Android or using the OSRM-Text-Instructions.java project in Project-OSRM.
     *
     * @see [Navigation SDK](https://github.com/mapbox/mapbox-navigation-android)
     * @see [OSRM-Text-Instructions.java](https://github.com/Project-OSRM/osrm-text-instructions.java)
     *
     * @since 1.0.0
     */
    val instruction: String? = null,

    /**
     * This indicates the type of maneuver.
     *
     * @since 1.0.0
     */
    val type: Type? = null,

    /**
     * This indicates the mode of the maneuver. If type is of turn, the modifier indicates the
     * change in direction accomplished through the turn. If the type is of depart/arrive, the
     * modifier indicates the position of waypoint from the current direction of travel.
     *
     * @since 1.0.0
     */
    val modifier: ManeuverModifier.Type? = null,

    /**
     * An optional integer indicating number of the exit to take. If exit is undefined the destination
     * is on the roundabout. The property exists for the following type properties:
     *
     *
     * else - indicates the number of intersections passed until the turn.
     * roundabout - traverse roundabout
     * rotary - a traffic circle
     *
     *
     * @since 2.0.0
     */
    val exit: Int? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `StepManeuver` instance.
     */
    fun toBuilder(): Builder {
        return Builder(
            location = location,
            bearingBefore = bearingBefore,
            bearingAfter = bearingAfter
        ).apply {
            withInstruction(instruction)
            withType(type)
            withModifier(modifier)
            withExit(exit)
        }
    }

    @Serializable
    enum class Type(val text: String) {
        /**
         * A basic turn in the direction of the modifier.
         *
         * @since 4.1.0
         */
        @SerialName("turn")
        TURN("turn"),

        /**
         * The road name changes (after a mandatory turn).
         *
         * @since 4.1.0
         */
        @SerialName("new name")
        NEW_NAME("new name"),

        /**
         * Indicates departure from a leg.
         * The  modifier value indicates the position of the departure point
         * in relation to the current direction of travel.
         *
         * @since 4.1.0
         */
        @SerialName("depart")
        DEPART("depart"),

        /**
         * Indicates arrival to a destination of a leg.
         * The modifier value indicates the position of the arrival point
         * in relation to the current direction of travel.
         *
         * @since 4.1.0
         */
        @SerialName("arrive")
        ARRIVE("arrive"),

        /**
         * Merge onto a street.
         *
         * @since 4.1.0
         */
        @SerialName("merge")
        MERGE("merge"),

        /**
         * Take a ramp to enter a highway.
         * @since 4.1.0
         */
        @SerialName("on ramp")
        ON_RAMP("on ramp"),

        /**
         * Take a ramp to exit a highway.
         *
         * @since 4.1.0
         */
        @SerialName("off ramp")
        OFF_RAMP("off ramp"),

        /**
         * Take the left or right side of a fork.
         *
         * @since 4.1.0
         */
        @SerialName("fork")
        FORK("fork"),

        /**
         * Road ends in a T intersection.
         *
         * @since 4.1.0
         */
        @SerialName("end of road")
        END_OF_ROAD("end of road"),

        /**
         * Continue on a street after a turn.
         *
         * @since 4.1.0
         */
        @SerialName("continue")
        CONTINUE("continue"),

        /**
         * Traverse roundabout.
         * Has an additional property  exit in the route step that contains
         * the exit number. The  modifier specifies the direction of entering the roundabout.
         *
         * @since 4.1.0
         */
        @SerialName("roundabout")
        ROUNDABOUT("roundabout"),

        /**
         * A traffic circle. While very similar to a larger version of a roundabout,
         * it does not necessarily follow roundabout rules for right of way.
         * It can offer [LegStep.rotaryName]  parameters,
         * [LegStep.rotaryPronunciation] ()}  parameters, or both,
         * in addition to the [.exit] property.
         *
         * @since 4.1.0
         */
        @SerialName("rotary")
        ROTARY("rotary"),

        /**
         * A small roundabout that is treated as an intersection.
         *
         * @since 4.1.0
         */
        @SerialName("roundabout turn")
        ROUNDABOUT_TURN("roundabout turn"),

        /**
         * Indicates a change of driving conditions, for example changing the  mode
         * from driving to ferry.
         *
         * @since 4.1.0
         */
        @SerialName("notification")
        NOTIFICATION("notification"),

        /**
         * Indicates the exit maneuver from a roundabout.
         * Will not appear in results unless you supply true to the [.exit] query
         * parameter in the request.
         *
         * @since 4.1.0
         */
        @SerialName("exit roundabout")
        EXIT_ROUNDABOUT("exit roundabout"),

        /**
         * Indicates the exit maneuver from a rotary.
         * Will not appear in results unless you supply true
         * to the <tt>MapboxDirections.Builder#roundaboutExits()</tt> query parameter in the request.
         *
         * @since 4.1.0
         */
        @SerialName("exit rotary")
        EXIT_ROTARY("exit rotary"),

        @SerialName("use lane")
        USE_LANE("use lane"),
    }

    /**
     * Builder class for creating `StepManeuver` instances.
     * @param location A [Point] representing this intersection location.
     * @param bearingBefore Number between 0 and 360 indicating the clockwise angle from true north to the direction of travel right before the maneuver.
     * @param bearingAfter Number between 0 and 360 indicating the clockwise angle from true north to the direction of travel right after the maneuver.
     */
    class Builder(
        private var location: Point,
        private var bearingBefore: Double,
        private var bearingAfter: Double
    ) {
        private var instruction: String? = null
        private var type: Type? = null
        private var modifier: ManeuverModifier.Type? = null
        private var exit: Int? = null

        /**
         * Sets the instruction.
         *
         * @param instruction A human-readable instruction of how to execute the returned maneuver.
         * @return The builder instance.
         */
        fun withInstruction(instruction: String?) = apply { this.instruction = instruction }

        /**
         * Sets the type.
         *
         * @param type This indicates the type of maneuver.
         * @return The builder instance.
         */
        fun withType(type: Type?) = apply { this.type = type }

        /**
         * Sets the modifier.
         *
         * @param modifier This indicates the mode of the maneuver.
         * @return The builder instance.
         */
        fun withModifier(modifier: ManeuverModifier.Type?) = apply { this.modifier = modifier }

        /**
         * Sets the exit.
         *
         * @param exit An optional integer indicating number of the exit to take.
         * @return The builder instance.
         */
        fun withExit(exit: Int?) = apply { this.exit = exit }

        /**
         * Builds a `StepManeuver` instance with the current builder values.
         *
         * @return A new `StepManeuver` instance.
         */
        fun build(): StepManeuver {
            return StepManeuver(
                location = location,
                bearingBefore = bearingBefore,
                bearingAfter = bearingAfter,
                instruction = instruction,
                type = type,
                modifier = modifier,
                exit = exit
            )
        }
    }
}
