package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.maplibre.navigation.android.json

/**
 * This is the root Mapbox Directions API response. Inside this class are several nested classes
 * chained together to make up a similar structure to the original APIs JSON response.
 *
 * @see [Direction
 * Response Object](https://www.mapbox.com/api-documentation/navigation/.directions-response-object)
 *
 * @since 1.0.0
 */
@Serializable
data class DirectionsResponse(
    /**
     * String indicating the state of the response. This is a separate code than the HTTP status code.
     * On normal valid responses, the value will be Ok. The possible responses are listed below:
     *
     *  * **Ok**:200 Normal success case
     *  * **NoRoute**: 200 There was no route found for the given coordinates. Check
     * for impossible routes (e.g. routes over oceans without ferry connections).
     *  * **NoSegment**: 200 No road segment could be matched for coordinates. Check for
     * coordinates too far away from a road.
     *  * **ProfileNotFound**: 404 Use a valid profile as described above
     *  * **InvalidInput**: 422
     *
     * @since 1.0.0
     */
    val code: String,

    /**
     * Optionally shows up in a directions response if an error or something unexpected occurred.
     *
     * @since 3.0.0
     */
    val message: String?,

    /**
     * List of [DirectionsWaypoint] objects. Each `waypoint` is an input coordinate
     * snapped to the road and path network. The `waypoint` appear in the list in the order of
     * the input coordinates.
     *
     * @since 1.0.0
     */
    val waypoints: List<DirectionsWaypoint>?,

    /**
     * List containing all the different route options. It's ordered by descending recommendation
     * rank. In other words, object 0 in the List is the highest recommended route. if you don't
     * setAlternatives to true (default is false) in your builder this should always be a List of
     * size 1. At most this will return 2 [DirectionsRoute] objects.
     *
     * @since 1.0.0
     */
    val routes: List<DirectionsRoute>,

    /**
     * A universally unique identifier (UUID) for identifying and executing a similar specific route
     * in the future.
     *
     * @since 3.0.0
     */
    val uuid: String?,
) {

    fun toJson(): String = json.encodeToString(this)

    companion object {

        @JvmStatic
        fun fromJson(jsonString: String): DirectionsResponse = json.decodeFromString(jsonString)
    }
}