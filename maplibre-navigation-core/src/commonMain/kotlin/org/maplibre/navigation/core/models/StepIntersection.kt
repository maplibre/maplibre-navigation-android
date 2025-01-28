package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.models.serializer.PointSerializer

/**
 * Object representing an intersection along the step.
 *
 * @since 1.3.0
 */
@Serializable
data class StepIntersection(

    /**
     * A [Point] representing this intersection location.
     *
     * @since 3.0.0
     */
    @Serializable(with = PointSerializer::class)
    val location: Point,

    /**
     * An integer list of bearing values available at the step intersection.
     *
     * @since 1.3.0
     */
    val bearings: List<Int>? = null,

    /**
     * A list of strings signifying the classes of the road exiting the intersection. Possible
     * values:
     *
     *  * **toll**: the road continues on a toll road
     *  * **ferry**: the road continues on a ferry
     *  * **restricted**: the road continues on with access restrictions
     *  * **motorway**: the road continues on a motorway
     *  * **tunnel**: the road continues on a tunnel
     *
     *
     * @since 3.0.0
     */
    val classes: List<String>? = null,

    /**
     * A list of entry flags, corresponding in a 1:1 relationship to the bearings. A value of true
     * indicates that the respective road could be entered on a valid route. false indicates that the
     * turn onto the respective road would violate a restriction.
     *
     * @since 1.3.0
     */
    val entry: List<Boolean>? = null,

    /**
     * Index into bearings/entry array. Used to calculate the bearing before the turn. Namely, the
     * clockwise angle from true north to the direction of travel before the maneuver/passing the
     * intersection. To get the bearing in the direction of driving, the bearing has to be rotated by
     * a value of 180. The value is not supplied for departure
     * maneuvers.
     *
     * @since 1.3.0
     */
    @SerialName("in")
    val inIndex: Int? = null,

    /**
     * Index out of the bearings/entry array. Used to extract the bearing after the turn. Namely, The
     * clockwise angle from true north to the direction of travel after the maneuver/passing the
     * intersection. The value is not supplied for arrive maneuvers.
     *
     * @since 1.3.0
     */
    @SerialName("out")
    val outIndex: Int? = null,

    /**
     * Array of lane objects that represent the available turn lanes at the intersection. If no lane
     * information is available for an intersection, the lanes property will not be present. Lanes are
     * provided in their order on the street, from left to right.
     *
     * @since 2.0.0
     */
    val lanes: List<IntersectionLanes>? = null,

    /**
     * The zero-based index for the intersection.
     * This value can be used to apply the duration annotation that corresponds with the intersection.
     * Only available on the driving profile.
     */
    @SerialName("geometry_index")
    val geometryIndex: Int? = null,

    @SerialName("is_urban")
    val isUrban: Boolean? = null,

    /**
     * The zero-based index into the admin list on the route leg for this intersection.
     * Use this field to look up the ISO-3166-1 country code for this point on the route.
     * Only available on the `driving` profile.
     *
     * @see RouteLeg.admins
     */
    @SerialName("admin_index")
    val adminIndex: Int? = null,

    /**
     * An object containing information about passing rest stops along the route.
     * Only available on the `driving` profile.
     */
    @SerialName("rest_stop")
    val restStop: RestStop? = null,

    /**
     * An object containing information about a toll collection point along the route.
     * This is a payment booth or overhead electronic gantry
     * [payment booth or overhead electronic gantry](https://wiki.openstreetmap.org/wiki/Tag:barrier%3Dtoll_booth)
     * where toll charge is collected.
     * Only available on the [DirectionsCriteria.PROFILE_DRIVING] profile.
     */
    @SerialName("toll_collection")
    val tollCollection: TollCollection? = null,

    /**
     * Name of the tunnel. Value may be present if [.classes] contains "tunnel".
     */
    @SerialName("tunnel_name")
    val tunnelName: String? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `StepIntersection` instance.
     */
    fun toBuilder(): Builder {
        return Builder(
            location = location
        ).apply {
            withBearings(bearings)
            withClasses(classes)
            withEntry(entry)
            withInIndex(inIndex)
            withOutIndex(outIndex)
            withLanes(lanes)
            withGeometryIndex(geometryIndex)
            withIsUrban(isUrban)
            withAdminIndex(adminIndex)
            withRestStop(restStop)
            withTollCollection(tollCollection)
            withTunnelName(tunnelName)
        }
    }

    /**
     * Builder class for creating `StepIntersection` instances.
     * @param location A [Point] representing this intersection location.
     */
    class Builder(
        private var location: Point
    ) {
        private var bearings: List<Int>? = null
        private var classes: List<String>? = null
        private var entry: List<Boolean>? = null
        private var inIndex: Int? = null
        private var outIndex: Int? = null
        private var lanes: List<IntersectionLanes>? = null
        private var geometryIndex: Int? = null
        private var isUrban: Boolean? = null
        private var adminIndex: Int? = null
        private var restStop: RestStop? = null
        private var tollCollection: TollCollection? = null
        private var tunnelName: String? = null

        /**
         * Sets the bearings.
         *
         * @param bearings An integer list of bearing values available at the step intersection.
         * @return The builder instance.
         */
        fun withBearings(bearings: List<Int>?) = apply { this.bearings = bearings }

        /**
         * Sets the classes.
         *
         * @param classes A list of strings signifying the classes of the road exiting the intersection.
         * @return The builder instance.
         */
        fun withClasses(classes: List<String>?) = apply { this.classes = classes }

        /**
         * Sets the entry flags.
         *
         * @param entry A list of entry flags, corresponding in a 1:1 relationship to the bearings.
         * @return The builder instance.
         */
        fun withEntry(entry: List<Boolean>?) = apply { this.entry = entry }

        /**
         * Sets the inIndex.
         *
         * @param inIndex Index into bearings/entry array.
         * @return The builder instance.
         */
        fun withInIndex(inIndex: Int?) = apply { this.inIndex = inIndex }

        /**
         * Sets the outIndex.
         *
         * @param outIndex Index out of the bearings/entry array.
         * @return The builder instance.
         */
        fun withOutIndex(outIndex: Int?) = apply { this.outIndex = outIndex }

        /**
         * Sets the lanes.
         *
         * @param lanes Array of lane objects that represent the available turn lanes at the intersection.
         * @return The builder instance.
         */
        fun withLanes(lanes: List<IntersectionLanes>?) = apply { this.lanes = lanes }

        /**
         * Sets the geometryIndex.
         *
         * @param geometryIndex The zero-based index for the intersection.
         * @return The builder instance.
         */
        fun withGeometryIndex(geometryIndex: Int?) = apply { this.geometryIndex = geometryIndex }

        /**
         * Sets the isUrban flag.
         *
         * @param isUrban Whether the intersection is in an urban area.
         * @return The builder instance.
         */
        fun withIsUrban(isUrban: Boolean?) = apply { this.isUrban = isUrban }

        /**
         * Sets the adminIndex.
         *
         * @param adminIndex The zero-based index into the admin list on the route leg for this intersection.
         * @return The builder instance.
         */
        fun withAdminIndex(adminIndex: Int?) = apply { this.adminIndex = adminIndex }

        /**
         * Sets the restStop.
         *
         * @param restStop An object containing information about passing rest stops along the route.
         * @return The builder instance.
         */
        fun withRestStop(restStop: RestStop?) = apply { this.restStop = restStop }

        /**
         * Sets the tollCollection.
         *
         * @param tollCollection An object containing information about a toll collection point along the route.
         * @return The builder instance.
         */
        fun withTollCollection(tollCollection: TollCollection?) =
            apply { this.tollCollection = tollCollection }

        /**
         * Sets the tunnelName.
         *
         * @param tunnelName Name of the tunnel.
         * @return The builder instance.
         */
        fun withTunnelName(tunnelName: String?) = apply { this.tunnelName = tunnelName }

        /**
         * Builds a `StepIntersection` instance with the current builder values.
         *
         * @return A new `StepIntersection` instance.
         */
        fun build(): StepIntersection {
            return StepIntersection(
                location = location,
                bearings = bearings,
                classes = classes,
                entry = entry,
                inIndex = inIndex,
                outIndex = outIndex,
                lanes = lanes,
                geometryIndex = geometryIndex,
                isUrban = isUrban,
                adminIndex = adminIndex,
                restStop = restStop,
                tollCollection = tollCollection,
                tunnelName = tunnelName
            )
        }
    }
}
