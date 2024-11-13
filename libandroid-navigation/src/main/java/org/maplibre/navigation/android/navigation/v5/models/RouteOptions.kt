package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.models.serializer.PointSerializer

/**
 * Provides information connected to your request that help when a new directions request is needing
 * using the identical parameters as the original request.
 *
 *
 * For example, if I request a driving (profile) with alternatives and continueStraight set to true.
 * I make the request but loose reference and information which built the original request. Thus, If
 * I only want to change a single variable such as the destination coordinate, i'd have to have all
 * the other route information stores so the request was made identical to the previous but only now
 * using this new destination point.
 *
 *
 * Using this class can provide you wth the information used when the [DirectionsRoute] was
 * made.
 *
 * @since 3.0.0
 */
@Serializable
data class RouteOptions(
    /**
     * The same base URL which was used during the request that resulted in this root directions
     * response.
     *
     * @since 3.0.0
     */
    val baseUrl: String,

    /**
     * The same user which was used during the request that resulted in this root directions response.
     *
     * @since 3.0.0
     */
    val user: String,

    /**
     * The routing profile to use. Possible values are
     * [DirectionsCriteria.PROFILE_DRIVING_TRAFFIC], [DirectionsCriteria.PROFILE_DRIVING],
     * [DirectionsCriteria.PROFILE_WALKING], or [DirectionsCriteria.PROFILE_CYCLING].
     * The same profile which was used during the request that resulted in this root directions
     * response. <tt>MapboxDirections.Builder</tt> ensures that a profile is always set even if the
     * <tt>MapboxDirections</tt> requesting object doesn't specifically set a profile.
     *
     * @since 3.0.0
     */
    val profile: String,

    /**
     * A list of Points to visit in order.
     * There can be between two and 25 coordinates for most requests, or up to three coordinates for
     * [DirectionsCriteria.PROFILE_DRIVING_TRAFFIC] requests.
     * Note that these coordinates are different than the direction responses
     * [DirectionsWaypoint]s that these are the non-snapped coordinates.
     *
     * @since 3.0.0
     */
    val coordinates: List<@Serializable(with = PointSerializer::class) Point>,

    /**
     * Whether to try to return alternative routes (true) or not (false, default). An alternative
     * route is a route that is significantly different than the fastest route, but also still
     * reasonably fast. Such a route does not exist in all circumstances. Up to two alternatives may
     * be returned. This is available for [DirectionsCriteria.PROFILE_DRIVING_TRAFFIC],
     * [DirectionsCriteria.PROFILE_DRIVING], [DirectionsCriteria.PROFILE_CYCLING].
     *
     * @since 3.0.0
     */
    val alternatives: Boolean?,

    /**
     * The language of returned turn-by-turn text instructions. The default is en (English).
     * Must be used in conjunction with [Builder.steps].
     *
     * @since 3.0.0
     */
    val language: String?,

    /**
     * The maximum distance a coordinate can be moved to snap to the road network in meters. There
     * must be as many radiuses as there are coordinates in the request, each separated by ;.
     * Values can be any number greater than 0, the string unlimited or empty string.
     *
     * @since 3.0.0
     */
    val radiuses: String?,

///**
// * The maximum distance a coordinate can be moved to snap to the road network in meters. There
// * must be as many radiuses as there are coordinates in the request.
// * Values can be any number greater than 0, the string unlimited, or null.
// *
// * @return a list of radiuses
// */
//fun radiusesList(): List<Double?>? {
//    return ParseUtils.parseToDoubles(radiuses())
//}

    /**
     * Influences the direction in which a route starts from a waypoint. Used to filter the road
     * segment the waypoint will be placed on by direction. This is useful for making sure the new
     * routes of rerouted vehicles continue traveling in their current direction. A request that does
     * this would provide bearing and radius values for the first waypoint and leave the remaining
     * values empty. Returns two comma-separated values per waypoint: an angle clockwise from true
     * north between 0 and 360, and the range of degrees by which the angle can deviate (recommended
     * value is 45° or 90°), formatted as {angle, degrees}. If provided, the list of bearings must be
     * the same length as the list of coordinates.
     *
     * @return a string representing the bearings with the ; separator. Angle and degrees for every
     * bearing value are comma-separated.
     * @since 3.0.0
     */
    val bearings: String?,

///**
// * Influences the direction in which a route starts from a waypoint. Used to filter the road
// * segment the waypoint will be placed on by direction. This is useful for making sure the new
// * routes of rerouted vehicles continue traveling in their current direction. A request that does
// * this would provide bearing and radius values for the first waypoint and leave the remaining
// * values empty. Returns a list of values, each value is a list of an angle clockwise from true
// * north between 0 and 360, and the range of degrees by which the angle can deviate (recommended
// * value is 45° or 90°).
// * If provided, the list of bearings must be the same length as the list of coordinates.
// *
// * @return a List of list of doubles representing the bearings used in the original request.
// * The first value in the list is the angle, the second one is the degrees.
// */
//fun bearingsList(): List<List<Double?>?>? {
//    return ParseUtils.parseToListOfListOfDoubles(bearings())
//}

    /**
     * The allowed direction of travel when departing intermediate waypoints. If true, the route
     * will continue in the same direction of travel. If false, the route may continue in the opposite
     * direction of travel. Defaults to true for [DirectionsCriteria.PROFILE_DRIVING] and false
     * for [DirectionsCriteria.PROFILE_WALKING] and [DirectionsCriteria.PROFILE_CYCLING].
     *
     * @since 3.0.0
     */
    @SerialName("continue_straight")
    val continueStraight: Boolean?,

    /**
     * Whether to emit instructions at roundabout exits (true) or not (false, default). Without
     * this parameter, roundabout maneuvers are given as a single instruction that includes both
     * entering and exiting the roundabout. With roundabout_exits=true, this maneuver becomes two
     * instructions, one for entering the roundabout and one for exiting it. Must be used in
     * conjunction with [RouteOptions.steps]=true.
     *
     * @since 3.1.0
     */
    @SerialName("roundabout_exits")
    val roundaboutExits: Boolean?,

    /**
     * The format of the returned geometry. Allowed values are:
     * [DirectionsCriteria.GEOMETRY_POLYLINE] (default, a polyline with a precision of five
     * decimal places), [DirectionsCriteria.GEOMETRY_POLYLINE6] (a polyline with a precision
     * of six decimal places).
     *
     * @since 3.1.0
     */
    val geometries: String?,

    /**
     * Displays the requested type of overview geometry. Can be
     * [DirectionsCriteria.OVERVIEW_FULL] (the most detailed geometry
     * available), [DirectionsCriteria.OVERVIEW_SIMPLIFIED] (default, a simplified version of
     * the full geometry), or [DirectionsCriteria.OVERVIEW_FALSE] (no overview geometry).
     *
     * @since 3.1.0
     */
    val overview: String?,

    /**
     * Whether to return steps and turn-by-turn instructions (true) or not (false, default).
     * If steps is set to true, the following guidance-related parameters will be available:
     * [RouteOptions.bannerInstructions], [RouteOptions.language],
     * [RouteOptions.roundaboutExits], [RouteOptions.voiceInstructions],
     * [RouteOptions.voiceUnits], [RouteOptions.waypointNamesList],
     * [RouteOptions.waypointTargetsList], waypoints from [RouteOptions.coordinates]
     *
     * @since 3.1.0
     */
    val steps: Boolean?,

    /**
     * A comma-separated list of annotations. Defines whether to return additional metadata along the
     * route. Possible values are:
     * [DirectionsCriteria.ANNOTATION_DURATION]
     * [DirectionsCriteria.ANNOTATION_DISTANCE]
     * [DirectionsCriteria.ANNOTATION_SPEED]
     * [DirectionsCriteria.ANNOTATION_CONGESTION]
     * [DirectionsCriteria.ANNOTATION_MAXSPEED]
     * See the [RouteLeg] object for more details on what is included with annotations.
     * Must be used in conjunction with overview=full.
     *
     * @since 3.0.0
     */
    val annotations: String?,

///**
// * A list of annotations. Defines whether to return additional metadata along the
// * route. Possible values are:
// * [DirectionsCriteria.ANNOTATION_DURATION]
// * [DirectionsCriteria.ANNOTATION_DISTANCE]
// * [DirectionsCriteria.ANNOTATION_SPEED]
// * [DirectionsCriteria.ANNOTATION_CONGESTION]
// * [DirectionsCriteria.ANNOTATION_MAXSPEED]
// * See the [RouteLeg] object for more details on what is included with annotations.
// * Must be used in conjunction with overview=full.
// *
// * @return a list of annotations that were used during the request
// */
//fun annotationsList(): List<String?>? {
//    return ParseUtils.parseToStrings(annotations(), ",")
//}

    /**
     * Exclude certain road types from routing. The default is to not exclude anything from the
     * profile selected. The following exclude flags are available for each profile:
     *
     * [DirectionsCriteria.PROFILE_DRIVING]: One of [DirectionsCriteria.EXCLUDE_TOLL],
     * [DirectionsCriteria.EXCLUDE_MOTORWAY], or [DirectionsCriteria.EXCLUDE_FERRY].
     *
     * [DirectionsCriteria.PROFILE_DRIVING_TRAFFIC]: One of
     * [DirectionsCriteria.EXCLUDE_TOLL], [DirectionsCriteria.EXCLUDE_MOTORWAY], or
     * [DirectionsCriteria.EXCLUDE_FERRY].
     *
     * [DirectionsCriteria.PROFILE_WALKING]: No excludes supported
     *
     * [DirectionsCriteria.PROFILE_CYCLING]: [DirectionsCriteria.EXCLUDE_FERRY]
     *
     * @since 3.0.0
     */
    val exclude: String?,

    /**
     * Whether to return SSML marked-up text for voice guidance along the route (true) or not
     * (false, default).
     * Must be used in conjunction with [RouteOptions.steps]=true.
     *
     * @since 3.0.0
     */
    @SerialName("voice_instructions")
    val voiceInstructions: Boolean?,

    /**
     * Whether to return banner objects associated with the route steps (true) or not
     * (false, default). Must be used in conjunction with [RouteOptions.steps]=true
     *
     * @since 3.0.0
     */
    @SerialName("banner_instructions")
    val bannerInstructions: Boolean?,

    /**
     * A type of units to return in the text for voice instructions.
     * Can be [DirectionsCriteria.IMPERIAL] (default) or [DirectionsCriteria.METRIC].
     * Must be used in conjunction with [RouteOptions.steps]=true and
     * [RouteOptions.voiceInstructions] ()}=true.
     *
     * @since 3.0.0
     */
    @SerialName("voice_units")
    val voiceUnits: String?,

    /**
     * A valid Mapbox access token used to making the request.
     *
     * @since 3.0.0
     */
    @SerialName("access_token")
    val accessToken: String,

    /**
     * A universally unique identifier (UUID) for identifying and executing a similar specific route
     * in the future. <tt>MapboxDirections</tt> always waits for the response object which ensures
     * this value will never be null.
     *
     * @since 3.0.0
     */
    @SerialName("uuid")
    val requestUuid: String,

    /**
     * Indicates from which side of the road to approach a waypoint.
     * Accepts  [DirectionsCriteria.APPROACH_UNRESTRICTED] (default) or
     * [DirectionsCriteria.APPROACH_CURB] .
     * If set to [DirectionsCriteria.APPROACH_UNRESTRICTED], the route can approach waypoints
     * from either side of the road.
     * If set to [DirectionsCriteria.APPROACH_CURB], the route will be returned so that on
     * arrival, the waypoint will be found on the side that corresponds with the driving_side of the
     * region in which the returned route is located.
     * If provided, the list of approaches must be the same length as the list of waypoints.
     *
     * @since 3.2.0
     */
    val approaches: String?,

///**
// * Indicates from which side of the road to approach a waypoint.
// * Accepts  [DirectionsCriteria.APPROACH_UNRESTRICTED] (default) or
// * [DirectionsCriteria.APPROACH_CURB] .
// * If set to [DirectionsCriteria.APPROACH_UNRESTRICTED], the route can approach waypoints
// * from either side of the road.
// * If set to [DirectionsCriteria.APPROACH_CURB], the route will be returned so that on
// * arrival, the waypoint will be found on the side that corresponds with the driving_side of the
// * region in which the returned route is located.
// * If provided, the list of approaches must be the same length as the list of waypoints.
// *
// * @return a list of strings representing approaches for each waypoint
// */
//fun approachesList(): List<String>? {
//    return parseToStrings(approaches())
//}

    /**
     * Indicates which input coordinates should be treated as waypoints.
     *
     *
     * Most useful in combination with  steps=true and requests based on traces
     * with high sample rates. Can be an index corresponding to any of the input coordinates,
     * but must contain the first ( 0 ) and last coordinates' index separated by  ; .
     * [.steps]
     *
     * @since 4.4.0
     */
    @SerialName("waypoints")
    val waypointIndices: String?,

///**
// * Indicates which input coordinates should be treated as waypoints.
// *
// *
// * Most useful in combination with  steps=true and requests based on traces
// * with high sample rates. Can be an index corresponding to any of the input coordinates,
// * but must contain the first ( 0 ) and last coordinates' index.
// * [.steps]
// *
// *
// * @return a List of Integers representing indices to be used as waypoints
// */
//fun waypointIndicesList(): List<Int?>? {
//    return ParseUtils.parseToIntegers(waypointIndices())
//}

    /**
     * A semicolon-separated list of custom names for entries in the list of
     * [RouteOptions.coordinates], used for the arrival instruction in banners and voice
     * instructions. Values can be any string, and the total number of all characters cannot exceed
     * 500. If provided, the list of waypoint_names must be the same length as the list of
     * coordinates. The first value in the list corresponds to the route origin, not the first
     * destination.
     * Must be used in conjunction with [RouteOptions.steps] = true.
     *
     * @since 3.3.0
     */
    @SerialName("waypoint_names")
    val waypointNames: String?,

///**
// * A semicolon-separated list of custom names for entries in the list of
// * [RouteOptions.coordinates], used for the arrival instruction in banners and voice
// * instructions. Values can be any string, and the total number of all characters cannot exceed
// * 500. If provided, the list of waypoint_names must be the same length as the list of
// * coordinates. The first value in the list corresponds to the route origin, not the first
// * destination.
// * Must be used in conjunction with [RouteOptions.steps] = true.
// *
// * @return  a list of strings representing names for each waypoint
// */
//fun waypointNamesList(): List<String>? {
//    return parseToStrings(waypointNames())
//}

    /**
     * A semicolon-separated list of coordinate pairs used to specify drop-off
     * locations that are distinct from the locations specified in coordinates.
     * If this parameter is provided, the Directions API will compute the side of the street,
     * left or right, for each target based on the waypoint_targets and the driving direction.
     * The maneuver.modifier, banner and voice instructions will be updated with the computed
     * side of street. The number of waypoint targets must be the same as the number of coordinates.
     * Must be used with [RouteOptions.steps] = true.
     *
     * @since 4.3.0
     */
    @SerialName("waypoint_targets")
    val waypointTargets: String?,

///**
// * A list of points used to specify drop-off
// * locations that are distinct from the locations specified in coordinates.
// * If this parameter is provided, the Directions API will compute the side of the street,
// * left or right, for each target based on the waypoint_targets and the driving direction.
// * The maneuver.modifier, banner and voice instructions will be updated with the computed
// * side of street. The number of waypoint targets must be the same as the number of coordinates.
// * Must be used with [RouteOptions.steps] = true.
// * @return  a list of Points representing coordinate pairs for drop-off locations
// */
//fun waypointTargetsList(): List<Point?>? {
//    return ParseUtils.parseToPoints(waypointTargets())
//}

    /**
     * To be used to specify settings for use with the walking profile.
     *
     * @since 4.8.0
     */
    val walkingOptions: WalkingOptions?,

    /**
     * A semicolon-separated list of booleans affecting snapping of waypoint locations to road
     * segments.
     * If true, road segments closed due to live-traffic closures will be considered for snapping.
     * If false, they will not be considered for snapping.
     * If provided, the number of snappingClosures must be the same as the number of
     * coordinates.
     * Must be used with [DirectionsCriteria.PROFILE_DRIVING_TRAFFIC]
     *
     * @return a String representing a list of booleans
     */
    @SerialName("snapping_closures")
    val snappingClosures: String?,

///**
// * A list of booleans affecting snapping of waypoint locations to road segments.
// * If true, road segments closed due to live-traffic closures will be considered for snapping.
// * If false, they will not be considered for snapping.
// * If provided, the number of snappingClosures must be the same as the number of
// * coordinates.
// * Must be used with [DirectionsCriteria.PROFILE_DRIVING_TRAFFIC]
// *
// * @return a list of booleans
// */
//fun snappingClosuresList(): List<Boolean?>? {
//    return ParseUtils.parseToBooleans(snappingClosures())
//}
)

//TODO fabi755 check options, which can be removed or be optional?
//TODO fabi755 json parsing
