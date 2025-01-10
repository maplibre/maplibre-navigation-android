package org.maplibre.navigation.core.offroute

import org.maplibre.navigation.core.utils.RingBuffer
import org.maplibre.geojson.model.LineString
import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.models.LegStep
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.utils.MeasurementUtils.userTrueDistanceFromStep
import org.maplibre.navigation.core.utils.ToleranceUtils.dynamicOffRouteRadiusTolerance
import org.maplibre.geojson.turf.TurfMeasurement
import org.maplibre.geojson.turf.TurfMisc
import org.maplibre.geojson.turf.TurfUnit
import kotlin.jvm.JvmStatic
import kotlin.math.max

/**
 * @param callback a callback that is fired for different off-route scenarios.
 */
open class OffRouteDetector(
    /**
     * A callback that is fired for different off-route scenarios.
     *
     * Right now, the only scenario is when the step index should be increased with
     * [OffRouteCallback.onShouldIncreaseIndex].
     */
    var callback: OffRouteCallback? = null
) : OffRoute {

    private var lastReroutePoint: Point? = null
    private val distancesAwayFromManeuver = RingBuffer<Int>(3)

    /**
     * Method in charge of running a series of test based on the device current location
     * and the user progress along the route.
     *
     *
     * Test #1:
     * Distance remaining.  If the route distance remaining is 0, then return true immediately.  In the
     * route processor this will prompt the snap-to-route logic to return the raw Location.  If there isn't any
     * distance remaining, the user will always be off-route.
     *
     *
     * Test #2:
     * Valid or invalid off-route. An off-route check can only continue if the device has received
     * at least 1 location update (for comparison) and the user has traveled passed
     * the [MapLibreNavigationOptions.offRouteMinimumDistanceMetersAfterReroute] checked against the last re-route location.
     *
     *
     * Test #3:
     * Distance from the step. This test is checked against the max of the dynamic rerouting tolerance or the
     * accuracy based tolerance. If this test passes, this method then also checks if there have been &gt;= 3
     * location updates moving away from the maneuver point. If false, this method will return false early.
     *
     *
     * Test #4:
     * Checks if the user is close the upcoming step.  At this point, the user is considered off-route.
     * But, if the location update is within the [MapLibreNavigationOptions.maneuverZoneRadius] of the
     * upcoming step, this method will return false as well as send fire [OffRouteCallback.onShouldIncreaseIndex]
     * to let the <tt>NavigationEngine</tt> know that the
     * step index should be increased on the next location update.
     *
     * @return true if the users off-route, else false.
     * @since 0.2.0
     */
    override fun isUserOffRoute(
        location: Location,
        routeProgress: RouteProgress,
        options: MapLibreNavigationOptions
    ): Boolean {
        if (checkDistanceRemaining(routeProgress)) {
            return true
        }

        if (!validOffRoute(location, options)) {
            return false
        }
        val currentPoint = Point(longitude = location.longitude, latitude = location.latitude, altitude = location.altitude)
        val isOffRoute = checkOffRouteRadius(location, routeProgress, options, currentPoint)

        if (!isOffRoute) {
            return isMovingAwayFromManeuver(
                location,
                routeProgress,
                distancesAwayFromManeuver,
                currentPoint,
                options
            )
        }

        callback?.let { callback ->
            routeProgress.currentLegProgress.upComingStep?.let { upComingStep ->
                if (closeToUpcomingStep(options, callback, currentPoint, upComingStep)) {
                    return false
                }
            }
        }

        // All checks have run, return true
        updateLastReroutePoint(location)
        return true
    }

    /**
     * Clears the [RingBuffer] used for tracking our recent
     * distances away from the maneuver that is being driven towards.
     *
     * @since 0.11.0
     */
    fun clearDistancesAwayFromManeuver() {
        distancesAwayFromManeuver.clear()
    }

    private fun checkDistanceRemaining(routeProgress: RouteProgress): Boolean {
        return routeProgress.distanceRemaining == 0.0
    }

    /**
     * Method to check if the user has passed either the set (in [MapLibreNavigationOptions.offRouteMinimumDistanceMetersAfterReroute])
     * minimum amount of seconds or minimum amount of meters since the last reroute.
     *
     * If the user is above both thresholds, then the off-route can proceed.  Otherwise, ignore.
     *
     * @param location current location from engine
     * @param options  for second (default 3) / distance (default 50m) minimums
     * @return true if valid, false if not
     */
    private fun validOffRoute(location: Location, options: MapLibreNavigationOptions): Boolean {
        return lastReroutePoint?.let { lastReroutePoint ->
            val currentPoint = Point(longitude = location.longitude, latitude = location.latitude, location.altitude)

            // Check if minimum amount of distance has been passed since last reroute
            val distanceFromLastReroute =
                TurfMeasurement.distance(lastReroutePoint, currentPoint, TurfUnit.METERS)
            distanceFromLastReroute > options.offRouteMinimumDistanceMetersAfterReroute
        } ?: run {
            // This is our first update - set the last reroute point to the given location
            updateLastReroutePoint(location)
            false
        }
    }

    private fun checkOffRouteRadius(
        location: Location,
        routeProgress: RouteProgress,
        options: MapLibreNavigationOptions,
        currentPoint: Point
    ): Boolean {
        val currentStep = routeProgress.currentLegProgress.currentStep
        val distanceFromCurrentStep = userTrueDistanceFromStep(
            currentPoint,
            currentStep
        )

        val offRouteRadius = createOffRouteRadius(location, routeProgress, options, currentPoint)
        return distanceFromCurrentStep > offRouteRadius
    }

    private fun createOffRouteRadius(
        location: Location,
        routeProgress: RouteProgress,
        options: MapLibreNavigationOptions,
        currentPoint: Point
    ): Double {
        val dynamicTolerance = dynamicOffRouteRadiusTolerance(currentPoint, routeProgress, options)
        val accuracyTolerance = (location.accuracyMeters ?: 0f) * options.deadReckoningTimeInterval
        return max(dynamicTolerance, accuracyTolerance)
    }

    private fun isMovingAwayFromManeuver(
        location: Location,
        routeProgress: RouteProgress,
        distancesAwayFromManeuver: RingBuffer<Int>,
        currentPoint: Point,
        options: MapLibreNavigationOptions
    ): Boolean {
        if (movingAwayFromManeuver(
                routeProgress,
                distancesAwayFromManeuver,
                routeProgress.currentStepPoints,
                currentPoint,
                options
            )
        ) {
            updateLastReroutePoint(location)
            return true
        }
        return false
    }

    private fun updateLastReroutePoint(location: Location) {
        lastReroutePoint = Point(longitude = location.longitude, latitude = location.latitude, location.altitude)
    }

    /**
     * If the upcoming step is not null, detect if the current point
     * is within the maneuver radius.
     *
     *
     * If it is, fire [OffRouteCallback.onShouldIncreaseIndex] to increase the step
     * index in the <tt>NavigationEngine</tt> and return true.
     *
     * @param options      for maneuver zone radius
     * @param callback     to increase step index
     * @param currentPoint for distance from upcoming step
     * @param upComingStep for distance from current point
     * @return true if close to upcoming step, false if not
     */
    private fun closeToUpcomingStep(
        options: MapLibreNavigationOptions,
        callback: OffRouteCallback,
        currentPoint: Point,
        upComingStep: LegStep
    ): Boolean {
        val distanceFromUpcomingStep = userTrueDistanceFromStep(currentPoint, upComingStep)
        val maneuverZoneRadius = options.maneuverZoneRadius
        return if (distanceFromUpcomingStep < maneuverZoneRadius) {
            // Callback to the NavigationEngine to increase the step index
            callback.onShouldIncreaseIndex()
            true
        } else {
            false
        }
    }

    /**
     * Checks to see if the current point is moving away from the maneuver.
     *
     *
     * Minimum three location updates and minimum of 50 meters away from the maneuver are required
     * to fire an off-route event. This parameters be considered that the user is no longer going in the right direction.
     *
     * @param routeProgress             for the upcoming step maneuver
     * @param distancesAwayFromManeuver current stack of distances away
     * @param stepPoints                current step points being traveled along
     * @param currentPoint              to determine if moving away or not
     * @return true if moving away from maneuver, false if not
     */
    private fun movingAwayFromManeuver(
        routeProgress: RouteProgress,
        distancesAwayFromManeuver: RingBuffer<Int>,
        stepPoints: List<Point>,
        currentPoint: Point,
        options: MapLibreNavigationOptions
    ): Boolean {
        val invalidUpcomingStep = routeProgress.currentLegProgress.upComingStep == null
        val invalidStepPointSize = stepPoints.size < TWO_POINTS
        if (invalidUpcomingStep || invalidStepPointSize) {
            return false
        }

        val stepLineString = LineString(stepPoints)
        val maneuverPoint = stepPoints[stepPoints.size - 1]

        val userPointOnStepFeature = TurfMisc.nearestPointOnLine(currentPoint, stepPoints)
        val userPointOnStep = userPointOnStepFeature.geometry as Point
        if (maneuverPoint == userPointOnStep) {
            return false
        }

        val remainingStepLineString =
            TurfMisc.lineSlice(userPointOnStep, maneuverPoint, stepLineString)
        val userDistanceToManeuver =
            TurfMeasurement.length(remainingStepLineString, TurfUnit.METERS)
                .toInt()

        if (distancesAwayFromManeuver.isEmpty()) {
            // No move-away positions before, add the current one to history stack
            distancesAwayFromManeuver.addLast(userDistanceToManeuver)
        } else if (userDistanceToManeuver > distancesAwayFromManeuver.last()) {
            // If distance to maneuver increased (wrong way), add new position to history stack

            if (distancesAwayFromManeuver.size >= 3) {
                // Replace the latest position with newest one, for keeping first position
                distancesAwayFromManeuver.removeLast()
            }
            distancesAwayFromManeuver.addLast(userDistanceToManeuver)
        } else if ((distancesAwayFromManeuver.last() - userDistanceToManeuver) > options.offRouteMinimumDistanceMetersBeforeRightDirection) {
            // If distance to maneuver decreased (right way) clean history
            distancesAwayFromManeuver.clear()
        }

        // Minimum 3 position updates in the wrong way are required before an off-route can occur
        if (distancesAwayFromManeuver.size >= 3) {
            // Check for minimum distance traveled
            return (distancesAwayFromManeuver.last() - distancesAwayFromManeuver.first()) > options.offRouteMinimumDistanceMetersBeforeWrongDirection
        }

        return false
    }

    companion object {
        @JvmStatic
        protected val TWO_POINTS = 2
    }
}
