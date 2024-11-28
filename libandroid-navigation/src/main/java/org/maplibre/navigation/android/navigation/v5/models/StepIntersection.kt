package org.maplibre.navigation.android.navigation.v5.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.models.serializer.PointSerializer

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
    val bearings: List<Int>?,

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
    val classes: List<String>?,

    /**
     * A list of entry flags, corresponding in a 1:1 relationship to the bearings. A value of true
     * indicates that the respective road could be entered on a valid route. false indicates that the
     * turn onto the respective road would violate a restriction.
     *
     * @since 1.3.0
     */
    val entry: List<Boolean>?,

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
    val inIndex: Int?,

    /**
     * Index out of the bearings/entry array. Used to extract the bearing after the turn. Namely, The
     * clockwise angle from true north to the direction of travel after the maneuver/passing the
     * intersection. The value is not supplied for arrive maneuvers.
     *
     * @since 1.3.0
     */
    @SerialName("out")
    val outIndex: Int?,

    /**
     * Array of lane objects that represent the available turn lanes at the intersection. If no lane
     * information is available for an intersection, the lanes property will not be present. Lanes are
     * provided in their order on the street, from left to right.
     *
     * @since 2.0.0
     */
    val lanes: List<IntersectionLanes>?,

    /**
     * The zero-based index for the intersection.
     * This value can be used to apply the duration annotation that corresponds with the intersection.
     * Only available on the driving profile.
     */
    @SerialName("geometry_index")
    val geometryIndex: Int?,

    @get:SerializedName("is_urban")
    val isUrban: Boolean?,

    /**
     * The zero-based index into the admin list on the route leg for this intersection.
     * Use this field to look up the ISO-3166-1 country code for this point on the route.
     * Only available on the `driving` profile.
     *
     * @see RouteLeg.admins
     */
    @SerialName("admin_index")
    val adminIndex: Int?,

    /**
     * An object containing information about passing rest stops along the route.
     * Only available on the `driving` profile.
     */
    @SerialName("rest_stop")
    val restStop: RestStop?,

    /**
     * An object containing information about a toll collection point along the route.
     * This is a payment booth or overhead electronic gantry
     * [payment booth or overhead electronic gantry](https://wiki.openstreetmap.org/wiki/Tag:barrier%3Dtoll_booth)
     * where toll charge is collected.
     * Only available on the [DirectionsCriteria.PROFILE_DRIVING] profile.
     */
    @SerialName("toll_collection")
    val tollCollection: TollCollection?,

    /**
     * Name of the tunnel. Value may be present if [.classes] contains "tunnel".
     */
    @SerialName("tunnel_name")
    val tunnelName: String?,
)
