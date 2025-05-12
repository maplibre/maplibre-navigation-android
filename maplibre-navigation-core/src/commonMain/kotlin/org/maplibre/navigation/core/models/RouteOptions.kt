package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.models.serializer.PointSerializer

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
     * [NavigationRoute.PROFILE_DRIVING_TRAFFIC], [NavigationRoute.PROFILE_DRIVING],
     * [NavigationRoute.PROFILE_WALKING], or [NavigationRoute.PROFILE_CYCLING].
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
     * [NavigationRoute.PROFILE_DRIVING_TRAFFIC] requests.
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
     * be returned. This is available for [NavigationRoute.PROFILE_DRIVING_TRAFFIC],
     * [NavigationRoute.PROFILE_DRIVING], [NavigationRoute.PROFILE_CYCLING].
     *
     * @since 3.0.0
     */
    val alternatives: Boolean? = null,

    /**
     * The language of returned turn-by-turn text instructions. The default is en (English).
     *
     * @since 3.0.0
     */
    val language: String? = null,

    /**
     * The maximum distance a coordinate can be moved to snap to the road network in meters. There
     * must be as many radiuses as there are coordinates in the request, each separated by ;.
     * Values can be any number greater than 0, the string unlimited or empty string.
     *
     * @since 3.0.0
     */
    val radiuses: String? = null,

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
    val bearings: String? = null,

    /**
     * The allowed direction of travel when departing intermediate waypoints. If true, the route
     * will continue in the same direction of travel. If false, the route may continue in the opposite
     * direction of travel. Defaults to true for [NavigationRoute.PROFILE_DRIVING] and false
     * for [NavigationRoute.PROFILE_WALKING] and [NavigationRoute.PROFILE_CYCLING].
     *
     * @since 3.0.0
     */
    @SerialName("continue_straight")
    val continueStraight: Boolean? = null,

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
    val roundaboutExits: Boolean? = null,

    /**
     * The format of the returned geometry. Allowed values are:
     * [NavigationRoute.GEOMETRY_POLYLINE] (default, a polyline with a precision of five
     * decimal places), [NavigationRoute.GEOMETRY_POLYLINE6] (a polyline with a precision
     * of six decimal places).
     *
     * @since 3.1.0
     */
    val geometries: String? = null,

    /**
     * Displays the requested type of overview geometry. Can be
     * [NavigationRoute.OVERVIEW_FULL] (the most detailed geometry
     * available), [NavigationRoute.OVERVIEW_SIMPLIFIED] (default, a simplified version of
     * the full geometry), or [NavigationRoute.OVERVIEW_FALSE] (no overview geometry).
     *
     * @since 3.1.0
     */
    val overview: String? = null,

    /**
     * Whether to return steps and turn-by-turn instructions (true) or not (false, default).
     * If steps is set to true, the following guidance-related parameters will be available:
     * [RouteOptions.bannerInstructions], [RouteOptions.language],
     * [RouteOptions.roundaboutExits], [RouteOptions.voiceInstructions],
     * [RouteOptions.voiceUnits], [RouteOptions.waypointNames],
     * [RouteOptions.waypointTargets], waypoints from [RouteOptions.coordinates]
     *
     * @since 3.1.0
     */
    val steps: Boolean? = null,

    /**
     * A comma-separated list of annotations. Defines whether to return additional metadata along the
     * route. Possible values are:
     * [NavigationRoute.ANNOTATION_DURATION]
     * [NavigationRoute.ANNOTATION_DISTANCE]
     * [NavigationRoute.ANNOTATION_SPEED]
     * [NavigationRoute.ANNOTATION_CONGESTION]
     * [NavigationRoute.ANNOTATION_MAXSPEED]
     * See the [RouteLeg] object for more details on what is included with annotations.
     * Must be used in conjunction with overview=full.
     *
     * @since 3.0.0
     */
    val annotations: String? = null,

    /**
     * Exclude certain road types from routing. The default is to not exclude anything from the
     * profile selected. The following exclude flags are available for each profile:
     *
     * [NavigationRoute.PROFILE_DRIVING]: One of [NavigationRoute.EXCLUDE_TOLL],
     * [NavigationRoute.EXCLUDE_MOTORWAY], or [NavigationRoute.EXCLUDE_FERRY].
     *
     * [NavigationRoute.PROFILE_DRIVING_TRAFFIC]: One of
     * [NavigationRoute.EXCLUDE_TOLL], [NavigationRoute.EXCLUDE_MOTORWAY], or
     * [NavigationRoute.EXCLUDE_FERRY].
     *
     * [NavigationRoute.PROFILE_WALKING]: No excludes supported
     *
     * [NavigationRoute.PROFILE_CYCLING]: [NavigationRoute.EXCLUDE_FERRY]
     *
     * @since 3.0.0
     */
    val exclude: String? = null,

    /**
     * Whether to return SSML marked-up text for voice guidance along the route (true) or not
     * (false, default).
     * Must be used in conjunction with [RouteOptions.steps]=true.
     *
     * @since 3.0.0
     */
    @SerialName("voice_instructions")
    val voiceInstructions: Boolean? = null,

    /**
     * Whether to return banner objects associated with the route steps (true) or not
     * (false, default). Must be used in conjunction with [RouteOptions.steps]=true
     *
     * @since 3.0.0
     */
    @SerialName("banner_instructions")
    val bannerInstructions: Boolean? = null,

    /**
     * A type of units to return in the text for voice instructions.
     * Can be [UnitType.IMPERIAL] (default) or [UnitType.METRIC].
     * Must be used in conjunction with [RouteOptions.steps]=true and
     * [RouteOptions.voiceInstructions] ()}=true.
     *
     * @since 3.0.0
     */
    @SerialName("voice_units")
    val voiceUnits: UnitType? = null,

    /**
     * A valid access token that will included to the request.
     *
     * @since 3.0.0
     */
    @SerialName("access_token")
    val accessToken: String? = null,

    /**
     * A universally unique identifier (UUID) for identifying and executing a similar specific route
     * in the future.
     *
     * @since 3.0.0
     */
    @SerialName("uuid")
    val requestUuid: String? = null,

    /**
     * Indicates from which side of the road to approach a waypoint.
     * Accepts  [NavigationRoute.APPROACH_UNRESTRICTED] (default) or
     * [NavigationRoute.APPROACH_CURB] .
     * If set to [NavigationRoute.APPROACH_UNRESTRICTED], the route can approach waypoints
     * from either side of the road.
     * If set to [NavigationRoute.APPROACH_CURB], the route will be returned so that on
     * arrival, the waypoint will be found on the side that corresponds with the driving_side of the
     * region in which the returned route is located.
     * If provided, the list of approaches must be the same length as the list of waypoints.
     *
     * @since 3.2.0
     */
    val approaches: String? = null,

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
    val waypointIndices: String? = null,

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
    val waypointNames: String? = null,

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
    val waypointTargets: String? = null,

    /**
     * To be used to specify settings for use with the walking profile.
     *
     * @since 4.8.0
     */
    val walkingOptions: WalkingOptions? = null,

    /**
     * A semicolon-separated list of booleans affecting snapping of waypoint locations to road
     * segments.
     * If true, road segments closed due to live-traffic closures will be considered for snapping.
     * If false, they will not be considered for snapping.
     * If provided, the number of snappingClosures must be the same as the number of
     * coordinates.
     * Must be used with [NavigationRoute.PROFILE_DRIVING_TRAFFIC]
     *
     * @return a String representing a list of booleans
     */
    @SerialName("snapping_closures")
    val snappingClosures: String? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `RouteOptions` instance.
     */
    fun toBuilder(): Builder {
        return Builder(
            baseUrl = baseUrl,
            user = user,
            profile = profile,
            coordinates = coordinates,
        ).apply {
            withAccessToken(accessToken)
            withRequestUuid(requestUuid)
            withAlternatives(alternatives)
            withLanguage(language)
            withRadiuses(radiuses)
            withBearings(bearings)
            withContinueStraight(continueStraight)
            withRoundaboutExits(roundaboutExits)
            withGeometries(geometries)
            withOverview(overview)
            withSteps(steps)
            withAnnotations(annotations)
            withExclude(exclude)
            withVoiceInstructions(voiceInstructions)
            withBannerInstructions(bannerInstructions)
            withVoiceUnits(voiceUnits)
            withApproaches(approaches)
            withWaypointIndices(waypointIndices)
            withWaypointNames(waypointNames)
            withWaypointTargets(waypointTargets)
            withWalkingOptions(walkingOptions)
            withSnappingClosures(snappingClosures)
        }
    }

    /**
     * Builder class for creating `RouteOptions` instances.
     * @param baseUrl The same base URL which was used during the request that resulted in this root directions response.
     * @param user The same user which was used during the request that resulted in this root directions response.
     * @param profile The routing profile to use.
     * @param coordinates A list of Points to visit in order.
     * @param requestUuid A universally unique identifier (UUID) for identifying and executing a similar specific route in the future.
     */
    class Builder(
        private var baseUrl: String,
        private var user: String,
        private var profile: String,
        private var coordinates: List<@Serializable(with = PointSerializer::class) Point>,
    ) {
        private var accessToken: String? = null
        private var requestUuid: String? = null
        private var alternatives: Boolean? = null
        private var language: String? = null
        private var radiuses: String? = null
        private var bearings: String? = null
        private var continueStraight: Boolean? = null
        private var roundaboutExits: Boolean? = null
        private var geometries: String? = null
        private var overview: String? = null
        private var steps: Boolean? = null
        private var annotations: String? = null
        private var exclude: String? = null
        private var voiceInstructions: Boolean? = null
        private var bannerInstructions: Boolean? = null
        private var voiceUnits: UnitType? = null
        private var approaches: String? = null
        private var waypointIndices: String? = null
        private var waypointNames: String? = null
        private var waypointTargets: String? = null
        private var walkingOptions: WalkingOptions? = null
        private var snappingClosures: String? = null

        /**
         * Sets the access token.
         *
         * @param accessToken A valid access token used to making the request.
         * @return The builder instance.
         */
        fun withAccessToken(accessToken: String?) = apply { this.accessToken = accessToken }

        /**
         * Sets the UUID for requests.
         *
         * @param requestUuid A valid access token used to making the request.
         * @return The builder instance.
         */
        fun withRequestUuid(requestUuid: String?) = apply { this.requestUuid = requestUuid }

        /**
         * Sets the alternatives.
         *
         * @param alternatives Whether to try to return alternative routes.
         * @return The builder instance.
         */
        fun withAlternatives(alternatives: Boolean?) = apply { this.alternatives = alternatives }

        /**
         * Sets the language.
         *
         * @param language The language of returned turn-by-turn text instructions.
         * @return The builder instance.
         */
        fun withLanguage(language: String?) = apply { this.language = language }

        /**
         * Sets the radiuses.
         *
         * @param radiuses The maximum distance a coordinate can be moved to snap to the road network in meters.
         * @return The builder instance.
         */
        fun withRadiuses(radiuses: String?) = apply { this.radiuses = radiuses }

        /**
         * Sets the bearings.
         *
         * @param bearings Influences the direction in which a route starts from a waypoint.
         * @return The builder instance.
         */
        fun withBearings(bearings: String?) = apply { this.bearings = bearings }

        /**
         * Sets the continue straight option.
         *
         * @param continueStraight The allowed direction of travel when departing intermediate waypoints.
         * @return The builder instance.
         */
        fun withContinueStraight(continueStraight: Boolean?) =
            apply { this.continueStraight = continueStraight }

        /**
         * Sets the roundabout exits option.
         *
         * @param roundaboutExits Whether to emit instructions at roundabout exits.
         * @return The builder instance.
         */
        fun withRoundaboutExits(roundaboutExits: Boolean?) =
            apply { this.roundaboutExits = roundaboutExits }

        /**
         * Sets the geometries.
         *
         * @param geometries The format of the returned geometry.
         * @return The builder instance.
         */
        fun withGeometries(geometries: String?) = apply { this.geometries = geometries }

        /**
         * Sets the overview.
         *
         * @param overview Displays the requested type of overview geometry.
         * @return The builder instance.
         */
        fun withOverview(overview: String?) = apply { this.overview = overview }

        /**
         * Sets the steps option.
         *
         * @param steps Whether to return steps and turn-by-turn instructions.
         * @return The builder instance.
         */
        fun withSteps(steps: Boolean?) = apply { this.steps = steps }

        /**
         * Sets the annotations.
         *
         * @param annotations A comma-separated list of annotations.
         * @return The builder instance.
         */
        fun withAnnotations(annotations: String?) = apply { this.annotations = annotations }

        /**
         * Sets the exclude option.
         *
         * @param exclude Exclude certain road types from routing.
         * @return The builder instance.
         */
        fun withExclude(exclude: String?) = apply { this.exclude = exclude }

        /**
         * Sets the voice instructions option.
         *
         * @param voiceInstructions Whether to return SSML marked-up text for voice guidance along the route.
         * @return The builder instance.
         */
        fun withVoiceInstructions(voiceInstructions: Boolean?) =
            apply { this.voiceInstructions = voiceInstructions }

        /**
         * Sets the banner instructions option.
         *
         * @param bannerInstructions Whether to return banner objects associated with the route steps.
         * @return The builder instance.
         */
        fun withBannerInstructions(bannerInstructions: Boolean?) =
            apply { this.bannerInstructions = bannerInstructions }

        /**
         * Sets the voice units.
         *
         * @param voiceUnits A type of units to return in the text for voice instructions.
         * @return The builder instance.
         */
        fun withVoiceUnits(voiceUnits: UnitType?) = apply { this.voiceUnits = voiceUnits }

        /**
         * Sets the approaches.
         *
         * @param approaches Indicates from which side of the road to approach a waypoint.
         * @return The builder instance.
         */
        fun withApproaches(approaches: String?) = apply { this.approaches = approaches }

        /**
         * Sets the waypoint indices.
         *
         * @param waypointIndices Indicates which input coordinates should be treated as waypoints.
         * @return The builder instance.
         */
        fun withWaypointIndices(waypointIndices: String?) =
            apply { this.waypointIndices = waypointIndices }

        /**
         * Sets the waypoint names.
         *
         * @param waypointNames A semicolon-separated list of custom names for entries in the list of coordinates.
         * @return The builder instance.
         */
        fun withWaypointNames(waypointNames: String?) = apply { this.waypointNames = waypointNames }

        /**
         * Sets the waypoint targets.
         *
         * @param waypointTargets A semicolon-separated list of coordinate pairs used to specify drop-off locations.
         * @return The builder instance.
         */
        fun withWaypointTargets(waypointTargets: String?) =
            apply { this.waypointTargets = waypointTargets }

        /**
         * Sets the walking options.
         *
         * @param walkingOptions To be used to specify settings for use with the walking profile.
         * @return The builder instance.
         */
        fun withWalkingOptions(walkingOptions: WalkingOptions?) =
            apply { this.walkingOptions = walkingOptions }

        /**
         * Sets the snapping closures.
         *
         * @param snappingClosures A semicolon-separated list of booleans affecting snapping of waypoint locations to road segments.
         * @return The builder instance.
         */
        fun withSnappingClosures(snappingClosures: String?) =
            apply { this.snappingClosures = snappingClosures }

        /**
         * Builds a `RouteOptions` instance with the current builder values.
         *
         * @return A new `RouteOptions` instance.
         */
        fun build(): RouteOptions {
            return RouteOptions(
                baseUrl = baseUrl,
                user = user,
                profile = profile,
                coordinates = coordinates,
                alternatives = alternatives,
                language = language,
                radiuses = radiuses,
                bearings = bearings,
                continueStraight = continueStraight,
                roundaboutExits = roundaboutExits,
                geometries = geometries,
                overview = overview,
                steps = steps,
                annotations = annotations,
                exclude = exclude,
                voiceInstructions = voiceInstructions,
                bannerInstructions = bannerInstructions,
                voiceUnits = voiceUnits,
                accessToken = accessToken,
                requestUuid = requestUuid,
                approaches = approaches,
                waypointIndices = waypointIndices,
                waypointNames = waypointNames,
                waypointTargets = waypointTargets,
                walkingOptions = walkingOptions,
                snappingClosures = snappingClosures
            )
        }
    }
}