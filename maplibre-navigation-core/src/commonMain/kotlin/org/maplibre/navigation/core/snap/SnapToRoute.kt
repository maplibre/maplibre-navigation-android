package org.maplibre.navigation.core.snap

import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.routeprogress.RouteLegProgress
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.utils.Constants
import org.maplibre.navigation.core.utils.MathUtils.wrap
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.polyline.PolylineEncoding
import org.maplibre.spatialk.turf.measurement.bearingTo
import org.maplibre.spatialk.turf.measurement.locateAlong
import org.maplibre.spatialk.turf.misc.nearestPointTo
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.extensions.inDegrees
import org.maplibre.spatialk.units.extensions.meters


/**
 * This attempts to snap the user to the closest position along the route. Prior to snapping the
 * user, their location's checked to ensure that the user didn't veer off-route. If your application
 * uses the MapLibre Map SDK, querying the map and snapping the user to the road grid might be a
 * better solution.
 *
 * @since 0.4.0
 */
open class SnapToRoute : Snap() {
    /**
     * Last calculated snapped bearing. This will be re-used if bearing can not calculated.
     * Is NULL if no bearing was calculated yet.
     */
    private var lastSnappedBearing: Float? = null

    /**
     * Calculate a snapped location along the route. Latitude, longitude and bearing are provided.
     *
     * @param location Current raw user location
     * @param routeProgress Current route progress
     * @return Snapped location along route
     */
    override fun getSnappedLocation(location: Location, routeProgress: RouteProgress): Location {
        val snappedLocation = snapLocationLatLng(location, routeProgress.currentStepPoints)
        return snappedLocation.copy(bearing = snapLocationBearing(location, routeProgress))
    }

    /**
     * Creates a snapped bearing for the snapped [Location].
     *
     *
     * This is done by measuring 1 meter ahead of the current step distance traveled and
     * creating a [Point] with this distance.
     *
     *
     * If the step distance remaining is zero, the distance ahead is the first point of upcoming leg.
     * This way, an accurate bearing is upheld transitioning between legs.
     *
     * @param location Current raw user location
     * @param routeProgress Current route progress
     * @return Float bearing snapped to route
     */
    private fun snapLocationBearing(location: Location, routeProgress: RouteProgress): Float? {
        return getCurrentPoint(routeProgress)?.let { currentPoint ->
            getFuturePoint(routeProgress)?.let { futurePoint ->
                currentPoint.bearingTo(futurePoint)
                    .let { bearing -> Bearing.North.clockwiseRotationTo(bearing) }
                    .inDegrees
                    .toFloat()
                    .also { bearing -> lastSnappedBearing = bearing }
            }
        }
            ?: lastSnappedBearing
            ?: location.bearing
    }

    /**
     * Snap coordinates of user's location to the closest position along the current step.
     *
     * @param location        the raw location
     * @param stepCoordinates the list of step geometry coordinates
     * @return the altered user location
     * @since 0.4.0
     */
    private fun snapLocationLatLng(location: Location, stepCoordinates: List<Point>): Location {
        // Uses Turf's pointOnLine, which takes a Point and a LineString to calculate the closest
        // Point on the LineString.
        return if (stepCoordinates.size > 1) {
            val pointFeature = stepCoordinates.nearestPointTo(location.point)
            val point = pointFeature.geometry
            location.copy(
                latitude = point.latitude,
                longitude = point.longitude
            )
        } else {
            location.copy()
        }
    }

    /**
     * Current step point. If no current leg process is available, null is returned.
     *
     * @param routeProgress Current route progress
     * @return Current step point or null if no current leg process is available
     */
    private fun getCurrentPoint(routeProgress: RouteProgress): Point? {
        return getCurrentStepPoint(routeProgress.currentLegProgress, 0.0)
    }

    /**
     * Get future point. This might be the upcoming step or the following leg. If none of them are
     * available, null is returned.
     *
     * @param routeProgress Current route progress
     * @return Future point or null if no following point is available
     */
    private fun getFuturePoint(routeProgress: RouteProgress): Point? {
        return if (routeProgress.currentLegProgress.distanceRemaining > 1) {
            // User has not reaching the end of current leg. Use traveled distance + 1 meter for future point
            getCurrentStepPoint(routeProgress.currentLegProgress, 1.0)
        } else {
            // User has reached the end of steps. Use upcoming leg for future point if available.
            getUpcomingLegPoint(routeProgress)
        }
    }

    /**
     * Current step point plus additional distance value. If no current leg process is available,
     * null is returned.
     *
     * @param currentLegProgress Current leg process
     * @param additionalDistance Additional distance to add to current step point
     * @return Current step point + additional distance or null if no current leg process is available
     */
    private fun getCurrentStepPoint(
        currentLegProgress: RouteLegProgress,
        additionalDistance: Double
    ): Point? {
        val currentStepPositions = currentLegProgress.currentStep
            .geometry
            .let { geometry ->
                PolylineEncoding.decode(encoded = geometry, precision = Constants.PRECISION_6)
            }

        return if (currentStepPositions.size >= 2) {
            currentLegProgress.currentStepProgress.distanceTraveled.let { distanceTraveled ->
                val currentStepLineString = LineString(currentStepPositions)
                currentStepLineString.locateAlong((distanceTraveled + additionalDistance).meters)
            }
        } else {
            currentStepPositions.firstOrNull()?.let(::Point)
        }
    }

    /**
     * Get next leg's start point. The second step of next leg is used as start point to avoid
     * returning the same coordinates as the end point of the leg before. If no next leg is available,
     * null is returned.
     *
     * @param routeProgress Current route progress
     * @return Next leg's start point or null if no next leg is available
     */
    private fun getUpcomingLegPoint(routeProgress: RouteProgress): Point? {
        return routeProgress.directionsRoute
            .legs
            .getOrNull(routeProgress.legIndex + 1)
            ?.steps
            // While first step is the same point as the last point of the current step, use the second one.
            ?.getOrNull(1)
            ?.let { firstStep ->
                val currentStepPositions =
                    PolylineEncoding.decode(
                        encoded = firstStep.geometry,
                        precision = Constants.PRECISION_6
                    )
                if (currentStepPositions.size < 2) {
                    return@let null
                }

                val currentStepLineString = LineString(currentStepPositions)
                currentStepLineString.locateAlong(1.0.meters)
            }
    }
}