package org.maplibre.navigation.android.navigation.v5.models

import androidx.annotation.StringDef

/**
 * Constants and properties used to customize the directions request.
 *
 * @since 1.0.0
 */

@Suppress("unused")
object DirectionsCriteria {
    /**
     * Mapbox default username.
     *
     * @since 1.0.0
     */
    const val PROFILE_DEFAULT_USER: String = "mapbox"

    /**
     * For car and motorcycle routing. This profile factors in current and historic traffic
     * conditions to avoid slowdowns.
     *
     * @since 2.0.0
     */
    const val PROFILE_DRIVING_TRAFFIC: String = "driving-traffic"

    /**
     * For car and motorcycle routing. This profile shows the fastest routes by preferring
     * high-speed roads like highways.
     *
     * @since 1.0.0
     */
    const val PROFILE_DRIVING: String = "driving"

    /**
     * For pedestrian and hiking routing. This profile shows the shortest path by using sidewalks
     * and trails.
     *
     * @since 1.0.0
     */
    const val PROFILE_WALKING: String = "walking"

    /**
     * For bicycle routing. This profile shows routes that are short and safe for cyclist, avoiding
     * highways and preferring streets with bike lanes.
     *
     * @since 1.0.0
     */
    const val PROFILE_CYCLING: String = "cycling"

    /**
     * Format to return route geometry will be an encoded polyline.
     *
     * @since 1.0.0
     */
    const val GEOMETRY_POLYLINE: String = "polyline"

    /**
     * Format to return route geometry will be an encoded polyline with precision 6.
     *
     * @since 2.0.0
     */
    const val GEOMETRY_POLYLINE6: String = "polyline6"

    /**
     * A simplified version of the [.OVERVIEW_FULL] geometry. If not specified simplified is
     * the default.
     *
     * @since 1.0.0
     */
    const val OVERVIEW_SIMPLIFIED: String = "simplified"

    /**
     * The most detailed geometry available.
     *
     * @since 1.0.0
     */
    const val OVERVIEW_FULL: String = "full"

    /**
     * No overview geometry.
     *
     * @since 1.0.0
     */
    const val OVERVIEW_FALSE: String = "false"

    /**
     * The duration, in seconds, between each pair of coordinates.
     *
     * @since 2.1.0
     */
    const val ANNOTATION_DURATION: String = "duration"

    /**
     * The distance, in meters, between each pair of coordinates.
     *
     * @since 2.1.0
     */
    const val ANNOTATION_DISTANCE: String = "distance"

    /**
     * The speed, in km/h, between each pair of coordinates.
     *
     * @since 2.1.0
     */
    const val ANNOTATION_SPEED: String = "speed"

    /**
     * The congestion, provided as a String, between each pair of coordinates.
     *
     * @since 2.2.0
     */
    const val ANNOTATION_CONGESTION: String = "congestion"

    /**
     * The posted speed limit, between each pair of coordinates.
     *
     * @since 2.1.0
     */
    const val ANNOTATION_MAXSPEED: String = "maxspeed"

    /**
     * The closure of sections of a route.
     */
    const val ANNOTATION_CLOSURE: String = "closure"

    /**
     * Exclude all tolls along the returned directions route.
     *
     * @since 3.0.0
     */
    const val EXCLUDE_TOLL: String = "toll"

    /**
     * Exclude all motorways along the returned directions route.
     *
     * @since 3.0.0
     */
    const val EXCLUDE_MOTORWAY: String = "motorway"

    /**
     * Exclude all ferries along the returned directions route.
     *
     * @since 3.0.0
     */
    const val EXCLUDE_FERRY: String = "ferry"

    /**
     * Exclude all tunnels along the returned directions route.
     *
     * @since 3.0.0
     */
    const val EXCLUDE_TUNNEL: String = "tunnel"

    /**
     * Exclude all roads with access restrictions along the returned directions route.
     *
     * @since 3.0.0
     */
    const val EXCLUDE_RESTRICTED: String = "restricted"

    /**
     * Change the units to imperial for voice and visual information. Note that this won't change
     * other results such as raw distance measurements which will always be returned in meters.
     *
     * @since 3.0.0
     */
    const val IMPERIAL: String = "imperial"

    /**
     * Change the units to metric for voice and visual information. Note that this won't change
     * other results such as raw distance measurements which will always be returned in meters.
     *
     * @since 3.0.0
     */
    const val METRIC: String = "metric"

    /**
     * Returned route starts at the first provided coordinate in the list. Used specifically for the
     * Optimization API.
     *
     * @since 2.1.0
     */
    const val SOURCE_FIRST: String = "first"

    /**
     * Returned route starts at any of the provided coordinate in the list. Used specifically for the
     * Optimization API.
     *
     * @since 2.1.0
     */
    const val SOURCE_ANY: String = "any"


    /**
     * Returned route ends at any of the provided coordinate in the list. Used specifically for the
     * Optimization API.
     *
     * @since 3.0.0
     */
    const val DESTINATION_ANY: String = "any"

    /**
     * Returned route ends at the last provided coordinate in the list. Used specifically for the
     * Optimization API.
     *
     * @since 3.0.0
     */
    const val DESTINATION_LAST: String = "last"

    /**
     * The routes can approach waypoints from either side of the road.
     *
     *
     *
     * Used in MapMatching and Directions API.
     *
     * @since 3.2.0
     */
    const val APPROACH_UNRESTRICTED: String = "unrestricted"

    /**
     * The route will be returned so that on arrival,
     * the waypoint will be found on the side that corresponds with the  driving_side of
     * the region in which the returned route is located.
     *
     *
     *
     * Used in MapMatching and Directions API.
     *
     * @since 3.2.0
     */
    const val APPROACH_CURB: String = "curb"

    /**
     * Retention policy for the various direction profiles.
     *
     * @since 3.0.0
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(PROFILE_DRIVING_TRAFFIC, PROFILE_DRIVING, PROFILE_WALKING, PROFILE_CYCLING)
    annotation class ProfileCriteria

    /**
     * Retention policy for the various direction geometries.
     *
     * @since 3.0.0
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(GEOMETRY_POLYLINE, GEOMETRY_POLYLINE6)
    annotation class GeometriesCriteria

    /**
     * Retention policy for the various direction overviews.
     *
     * @since 3.0.0
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(OVERVIEW_FALSE, OVERVIEW_FULL, OVERVIEW_SIMPLIFIED)
    annotation class OverviewCriteria

    /**
     * Retention policy for the various direction annotations.
     *
     * @since 3.0.0
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(ANNOTATION_CONGESTION, ANNOTATION_DISTANCE, ANNOTATION_DURATION, ANNOTATION_SPEED, ANNOTATION_MAXSPEED)
    annotation class AnnotationCriteria

    /**
     * Retention policy for the various direction exclusions.
     *
     * @since 3.0.0
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(EXCLUDE_FERRY, EXCLUDE_MOTORWAY, EXCLUDE_TOLL, EXCLUDE_TUNNEL, EXCLUDE_RESTRICTED)
    annotation class ExcludeCriteria

    /**
     * Retention policy for the various units of measurements.
     *
     * @since 0.3.0
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(IMPERIAL, METRIC)
    annotation class VoiceUnitCriteria

    /**
     * Retention policy for the source parameter in the Optimization API.
     *
     * @since 3.0.0
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(SOURCE_ANY, SOURCE_FIRST)
    annotation class SourceCriteria

    /**
     * Retention policy for the destination parameter in the Optimization API.
     *
     * @since 3.0.0
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(DESTINATION_ANY, DESTINATION_LAST)
    annotation class DestinationCriteria

    /**
     * Retention policy for the approaches parameter in the MapMatching and Directions API.
     *
     * @since 3.2.0
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(APPROACH_UNRESTRICTED, APPROACH_CURB)
    annotation class ApproachesCriteria
}
