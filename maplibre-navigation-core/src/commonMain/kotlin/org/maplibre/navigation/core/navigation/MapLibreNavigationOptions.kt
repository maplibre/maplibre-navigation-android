package org.maplibre.navigation.core.navigation

import org.maplibre.navigation.core.milestone.BannerInstructionMilestone
import org.maplibre.navigation.core.milestone.VoiceInstructionMilestone
import org.maplibre.navigation.core.route.FasterRouteDetector


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
     * Minimum distance in meters that the user must travel after a re-routing was done.
     */
    val offRouteMinimumDistanceMetersAfterReroute: Double = Defaults.OFF_ROUTE_MINIMUM_DISTANCE_METERS_AFTER_REROUTE,

    /**
     * Threshold for off-route detection. If the user is outside of this defined radius,
     * the user always is off route. Radius defined in meters.
     */
    val offRouteThresholdRadiusMeters: Double = Defaults.OFF_ROUTE_THRESHOLD_RADIUS_METERS,

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

    /**
     * The increment used for rounding the user's speed and the remaining duration of the route.
     */
    val roundingIncrement: RoundingIncrement = Defaults.roundingIncrement,

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

    /**
     * In seconds, how quickly [FasterRouteDetector]
     * will tell [RouteProcessorBackgroundThread] to check
     * for a faster [org.maplibre.navigation.core.models.DirectionsRoute].
     */
    val fasterRouteCheckIntervalSeconds: Int = Defaults.FASTER_ROUTE_CHECK_INTERVAL_SECONDS
) {
    fun toBuilder(): Builder {
        return Builder()
            .withMaxTurnCompletionOffset(maxTurnCompletionOffset)
            .withManeuverZoneRadius(maneuverZoneRadius)
            .withDeadReckoningTimeInterval(deadReckoningTimeInterval)
            .withMaxManipulatedCourseAngle(maxManipulatedCourseAngle)
            .withUserLocationSnapDistance(userLocationSnapDistance)
            .withSecondsBeforeReroute(secondsBeforeReroute)
            .withDefaultMilestonesEnabled(defaultMilestonesEnabled)
            .withSnapToRoute(snapToRoute)
            .withEnableOffRouteDetection(enableOffRouteDetection)
            .withEnableFasterRouteDetection(enableFasterRouteDetection)
            .withManuallyEndNavigationUponCompletion(manuallyEndNavigationUponCompletion)
            .withMetersRemainingTillArrival(metersRemainingTillArrival)
            .withOffRouteMinimumDistanceMetersAfterReroute(offRouteMinimumDistanceMetersAfterReroute)
            .withOffRouteThresholdRadiusMeters(offRouteThresholdRadiusMeters)
            .withOffRouteMinimumDistanceMetersBeforeWrongDirection(offRouteMinimumDistanceMetersBeforeWrongDirection)
            .withOffRouteMinimumDistanceMetersBeforeRightDirection(offRouteMinimumDistanceMetersBeforeRightDirection)
            .withIsDebugLoggingEnabled(isDebugLoggingEnabled)
            .withRoundingIncrement(roundingIncrement)
            .withTimeFormatType(timeFormatType)
            .withLocationAcceptableAccuracyInMetersThreshold(locationAcceptableAccuracyInMetersThreshold)
            .withFasterRouteCheckIntervalSeconds(fasterRouteCheckIntervalSeconds)
    }

    enum class TimeFormat(val id: Int) {
        NONE_SPECIFIED(-1),
        TWELVE_HOURS(0),
        TWENTY_FOUR_HOURS(1)
    }

    enum class RoundingIncrement(val increment: Int) {
        ROUNDING_INCREMENT_FIVE(5),
        ROUNDING_INCREMENT_TEN(10),
        ROUNDING_INCREMENT_TWENTY_FIVE(25),
        ROUNDING_INCREMENT_FIFTY(50),
        ROUNDING_INCREMENT_ONE_HUNDRED(100)
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
        const val OFF_ROUTE_MINIMUM_DISTANCE_METERS_AFTER_REROUTE = 50.0
        const val OFF_ROUTE_THRESHOLD_RADIUS_METERS = 50.0
        const val OFF_ROUTE_MINIMUM_DISTANCE_METERS_BEFORE_WRONG_DIRECTION = 50.0
        const val OFF_ROUTE_MINIMUM_DISTANCE_METERS_BEFORE_RIGHT_DIRECTION = 20.0
        const val IS_DEBUG_LOGGING_ENABLED = false
        const val LOCATION_ACCEPTABLE_ACCURACY_IN_METERS_THRESHOLD = 100
        const val FASTER_ROUTE_CHECK_INTERVAL_SECONDS = 120
        val roundingIncrement = RoundingIncrement.ROUNDING_INCREMENT_FIFTY
    }

    class Builder {
        private var maxTurnCompletionOffset: Double = Defaults.MAX_TURN_COMPLETION_OFFSET
        private var maneuverZoneRadius: Double = Defaults.MANEUVER_ZONE_RADIUS
        private var deadReckoningTimeInterval: Double = Defaults.DEAD_RECKONING_TIME_INTERVAL
        private var maxManipulatedCourseAngle: Double = Defaults.MAX_MANIPULATED_COURSE_ANGLE
        private var userLocationSnapDistance: Double = Defaults.USER_LOCATION_SNAPPING_DISTANCE
        private var secondsBeforeReroute: Int = Defaults.SECONDS_BEFORE_REROUTE
        private var defaultMilestonesEnabled: Boolean = Defaults.DEFAULT_MILESTONES_ENABLED
        private var snapToRoute: Boolean = Defaults.SNAP_TO_ROUTE
        private var enableOffRouteDetection: Boolean = Defaults.ENABLE_OFF_ROUTE_DETECTION
        private var enableFasterRouteDetection: Boolean = Defaults.ENABLE_FASTER_ROUTE_DETECTION
        private var manuallyEndNavigationUponCompletion: Boolean =
            Defaults.MANUALLY_END_NAVIGATION_UPON_COMPLETION
        private var metersRemainingTillArrival: Double = Defaults.METERS_REMAINING_TILL_ARRIVAL
        private var offRouteMinimumDistanceMetersAfterReroute: Double =
            Defaults.OFF_ROUTE_MINIMUM_DISTANCE_METERS_AFTER_REROUTE
        private var offRouteThresholdRadiusMeters: Double =
            Defaults.OFF_ROUTE_THRESHOLD_RADIUS_METERS
        private var offRouteMinimumDistanceMetersBeforeWrongDirection: Double =
            Defaults.OFF_ROUTE_MINIMUM_DISTANCE_METERS_BEFORE_WRONG_DIRECTION
        private var offRouteMinimumDistanceMetersBeforeRightDirection: Double =
            Defaults.OFF_ROUTE_MINIMUM_DISTANCE_METERS_BEFORE_RIGHT_DIRECTION
        private var isDebugLoggingEnabled: Boolean = Defaults.IS_DEBUG_LOGGING_ENABLED
        private var roundingIncrement: RoundingIncrement = Defaults.roundingIncrement
        private var timeFormatType: TimeFormat = TimeFormat.NONE_SPECIFIED
        private var locationAcceptableAccuracyInMetersThreshold: Int =
            Defaults.LOCATION_ACCEPTABLE_ACCURACY_IN_METERS_THRESHOLD
        private var fasterRouteCheckIntervalSeconds: Int =
            Defaults.FASTER_ROUTE_CHECK_INTERVAL_SECONDS

        fun withMaxTurnCompletionOffset(maxTurnCompletionOffset: Double) = apply { this.maxTurnCompletionOffset = maxTurnCompletionOffset }
        fun withManeuverZoneRadius(maneuverZoneRadius: Double) = apply { this.maneuverZoneRadius = maneuverZoneRadius }
        fun withDeadReckoningTimeInterval(deadReckoningTimeInterval: Double) = apply { this.deadReckoningTimeInterval = deadReckoningTimeInterval }
        fun withMaxManipulatedCourseAngle(maxManipulatedCourseAngle: Double) = apply { this.maxManipulatedCourseAngle = maxManipulatedCourseAngle }
        fun withUserLocationSnapDistance(userLocationSnapDistance: Double) = apply { this.userLocationSnapDistance = userLocationSnapDistance }
        fun withSecondsBeforeReroute(secondsBeforeReroute: Int) = apply { this.secondsBeforeReroute = secondsBeforeReroute }
        fun withDefaultMilestonesEnabled(defaultMilestonesEnabled: Boolean) = apply { this.defaultMilestonesEnabled = defaultMilestonesEnabled }
        fun withSnapToRoute(snapToRoute: Boolean) = apply { this.snapToRoute = snapToRoute }
        fun withEnableOffRouteDetection(enableOffRouteDetection: Boolean) = apply { this.enableOffRouteDetection = enableOffRouteDetection }
        fun withEnableFasterRouteDetection(enableFasterRouteDetection: Boolean) = apply { this.enableFasterRouteDetection = enableFasterRouteDetection }
        fun withManuallyEndNavigationUponCompletion(manuallyEndNavigationUponCompletion: Boolean) = apply { this.manuallyEndNavigationUponCompletion = manuallyEndNavigationUponCompletion }
        fun withMetersRemainingTillArrival(metersRemainingTillArrival: Double) = apply { this.metersRemainingTillArrival = metersRemainingTillArrival }
        fun withOffRouteMinimumDistanceMetersAfterReroute(offRouteMinimumDistanceMetersAfterReroute: Double) = apply { this.offRouteMinimumDistanceMetersAfterReroute = offRouteMinimumDistanceMetersAfterReroute }
        fun withOffRouteThresholdRadiusMeters(offRouteThresholdRadiusMeters: Double) = apply { this.offRouteThresholdRadiusMeters = offRouteThresholdRadiusMeters }
        fun withOffRouteMinimumDistanceMetersBeforeWrongDirection(offRouteMinimumDistanceMetersBeforeWrongDirection: Double) = apply { this.offRouteMinimumDistanceMetersBeforeWrongDirection = offRouteMinimumDistanceMetersBeforeWrongDirection }
        fun withOffRouteMinimumDistanceMetersBeforeRightDirection(offRouteMinimumDistanceMetersBeforeRightDirection: Double) = apply { this.offRouteMinimumDistanceMetersBeforeRightDirection = offRouteMinimumDistanceMetersBeforeRightDirection }
        fun withIsDebugLoggingEnabled(isDebugLoggingEnabled: Boolean) = apply { this.isDebugLoggingEnabled = isDebugLoggingEnabled }
        fun withRoundingIncrement(roundingIncrement: RoundingIncrement) = apply { this.roundingIncrement = roundingIncrement }
        fun withTimeFormatType(timeFormatType: TimeFormat) = apply { this.timeFormatType = timeFormatType }
        fun withLocationAcceptableAccuracyInMetersThreshold(locationAcceptableAccuracyInMetersThreshold: Int) = apply { this.locationAcceptableAccuracyInMetersThreshold = locationAcceptableAccuracyInMetersThreshold }
        fun withFasterRouteCheckIntervalSeconds(fasterRouteCheckIntervalSeconds: Int) = apply { this.fasterRouteCheckIntervalSeconds = fasterRouteCheckIntervalSeconds }

        fun build(): MapLibreNavigationOptions {
            return MapLibreNavigationOptions(
                maxTurnCompletionOffset,
                maneuverZoneRadius,
                deadReckoningTimeInterval,
                maxManipulatedCourseAngle,
                userLocationSnapDistance,
                secondsBeforeReroute,
                defaultMilestonesEnabled,
                snapToRoute,
                enableOffRouteDetection,
                enableFasterRouteDetection,
                manuallyEndNavigationUponCompletion,
                metersRemainingTillArrival,
                offRouteMinimumDistanceMetersAfterReroute,
                offRouteThresholdRadiusMeters,
                offRouteMinimumDistanceMetersBeforeWrongDirection,
                offRouteMinimumDistanceMetersBeforeRightDirection,
                isDebugLoggingEnabled,
                roundingIncrement,
                timeFormatType,
                locationAcceptableAccuracyInMetersThreshold,
                fasterRouteCheckIntervalSeconds
            )
        }
    }
}
