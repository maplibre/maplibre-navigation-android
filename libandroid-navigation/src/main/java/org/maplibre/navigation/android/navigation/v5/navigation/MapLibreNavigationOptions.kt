package org.maplibre.navigation.android.navigation.v5.navigation

import org.maplibre.navigation.android.navigation.v5.milestone.BannerInstructionMilestone
import org.maplibre.navigation.android.navigation.v5.milestone.VoiceInstructionMilestone
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationConstants.RoundingIncrement
import org.maplibre.navigation.android.navigation.v5.navigation.notification.NavigationNotification


/**
 * Immutable and can't be changed after passing into [MapLibreNavigation].
 */
data class MapLibreNavigationOptions(
    /**
     * Threshold user must be within to count as completing a step. One of two heuristics used to know
     * when a user completes a step, see [.maneuverZoneRadius]. The users heading and the
     * finalHeading are compared. If this number is within this defined value, the user has
     * completed the step.
     */
    val maxTurnCompletionOffset: Double = Defaults.MAX_TURN_COMPLETION_OFFSET,

    /**
     * Radius in meters the user must enter to count as completing a step. One of two heuristics used
     * to know when a user completes a step, see [.maxTurnCompletionOffset].
     */
    val maneuverZoneRadius: Double = Defaults.MANEUVER_ZONE_RADIUS,

    /**
     * When calculating whether or not the user is on the route, we look where the user will be given
     * their speed and this variable.
     */
    val deadReckoningTimeInterval: Double = Defaults.DEAD_RECKONING_TIME_INTERVAL,

    /**
     * Maximum angle the user puck will be rotated when snapping the user's course to the route line.
     */
    val maxManipulatedCourseAngle: Double = Defaults.MAX_MANIPULATED_COURSE_ANGLE,

    /**
     * Accepted deviation excluding horizontal accuracy before the user is considered to be off route.
     */
    val userLocationSnapDistance: Double = Defaults.USER_LOCATION_SNAPPING_DISTANCE,

    /**
     * Seconds used before a reroute occurs.
     */
    val secondsBeforeReroute: Int = Defaults.SECONDS_BEFORE_REROUTE,

    /**
     * If enabled, the default milestones [VoiceInstructionMilestone] and
     * [BannerInstructionMilestone] are added and used by default.
     */
    val defaultMilestonesEnabled: Boolean = Defaults.DEFAULT_MILESTONES_ENABLED,

    val snapToRoute: Boolean = Defaults.SNAP_TO_ROUTE,

    val enableOffRouteDetection: Boolean = Defaults.ENABLE_OFF_ROUTE_DETECTION,

    val enableFasterRouteDetection: Boolean = Defaults.ENABLE_FASTER_ROUTE_DETECTION,

    val manuallyEndNavigationUponCompletion: Boolean = Defaults.MANUALLY_END_NAVIGATION_UPON_COMPLETION,

    /**
     * Meter radius which the user must be inside for an arrival milestone to be triggered and
     * navigation to end.
     */
    val metersRemainingTillArrival: Double = Defaults.METERS_REMAINING_TILL_ARRIVAL,

    /**
     * Minimum distance in meters that the user must travel in the wrong direction before the
     * off-route logic recognizes the user is moving away from upcoming maneuver
     */
    val offRouteMinimumDistanceMetersBeforeWrongDirection: Double = Defaults.OFF_ROUTE_MINIMUM_DISTANCE_METERS_BEFORE_WRONG_DIRECTION,

    /**
     * Minimum distance in meters that the user must travel in the correct direction before the
     * off-route logic recognizes the user is back on the right direction
     */
    val offRouteMinimumDistanceMetersBeforeRightDirection: Double = Defaults.OFF_ROUTE_MINIMUM_DISTANCE_METERS_BEFORE_RIGHT_DIRECTION,

    /**
     * If true, the SDK will print debug logs.
     */
    val isDebugLoggingEnabled: Boolean = Defaults.IS_DEBUG_LOGGING_ENABLED,

    val navigationNotification: NavigationNotification? = null,

    @RoundingIncrement
    val roundingIncrement: Int = Defaults.ROUNDING_INCREMENT,

    val timeFormatType: TimeFormat = TimeFormat.NONE_SPECIFIED,

    /**
     * Default location acceptable accuracy threshold
     * used in {@link LocationValidator}.
     * <p>
     * If a new {@link android.location.Location} update is received from the LocationEngine that has
     * an accuracy less than this threshold, the update will be considered valid and all other validation
     * is not considered.
     */
    val locationAcceptableAccuracyInMetersThreshold: Int = Defaults.LOCATION_ACCEPTABLE_ACCURACY_IN_METERS_THRESHOLD,
) {
    enum class TimeFormat(val id: Int) {
        NONE_SPECIFIED(-1),
        TWELVE_HOURS(0),
        TWENTY_FOUR_HOURS(1)
    }

    object Defaults {
        const val MAX_TURN_COMPLETION_OFFSET = 30.0
        const val MANEUVER_ZONE_RADIUS = 40.0
        const val DEAD_RECKONING_TIME_INTERVAL = 1.0
        const val MAX_MANIPULATED_COURSE_ANGLE = 25.0
        const val USER_LOCATION_SNAPPING_DISTANCE = 10.0
        const val SECONDS_BEFORE_REROUTE = 3
        const val DEFAULT_MILESTONES_ENABLED = true
        const val SNAP_TO_ROUTE = true
        const val ENABLE_OFF_ROUTE_DETECTION = true
        const val ENABLE_FASTER_ROUTE_DETECTION = false
        const val MANUALLY_END_NAVIGATION_UPON_COMPLETION = false
        const val METERS_REMAINING_TILL_ARRIVAL = 40.0
        const val OFF_ROUTE_MINIMUM_DISTANCE_METERS_BEFORE_WRONG_DIRECTION = 50.0
        const val OFF_ROUTE_MINIMUM_DISTANCE_METERS_BEFORE_RIGHT_DIRECTION = 20.0
        const val IS_DEBUG_LOGGING_ENABLED = false
        const val ROUNDING_INCREMENT = NavigationConstants.ROUNDING_INCREMENT_FIFTY
        const val LOCATION_ACCEPTABLE_ACCURACY_IN_METERS_THRESHOLD = 100
    }
}
