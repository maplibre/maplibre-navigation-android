package org.maplibre.navigation.core.navigation.camera

import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.navigation.MapLibreNavigation
import org.maplibre.navigation.core.utils.Constants
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.polyline.PolylineEncoding
import org.maplibre.spatialk.turf.measurement.bearingTo
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.extensions.inDegrees

/**
 * The default camera used by [MapLibreNavigation].
 *
 * @since 0.10.0
 */
open class SimpleCamera : Camera {

    private var routeCoordinates: List<Point> = ArrayList()
    private var initialBearing = 0.0
    private var initialRoute: DirectionsRoute? = null

    override fun bearing(routeInformation: RouteInformation): Double {
        return routeInformation.route?.let { route ->
            setupLineStringAndBearing(route)
            initialBearing
        }
            ?: routeInformation.location?.bearing?.toDouble()
            ?: 0.0
    }

    override fun target(routeInformation: RouteInformation): Point? {
        return routeInformation.route?.let { route ->
            setupLineStringAndBearing(route)
            val firstPoint = routeCoordinates.first()
            Point(
                longitude = firstPoint.longitude,
                latitude = firstPoint.latitude,
                altitude = firstPoint.altitude
            )
        } ?: routeInformation.location?.let { location ->
            Point(
                longitude = location.longitude,
                latitude = location.latitude,
                altitude = location.altitude
            )
        }
    }

    override fun tilt(routeInformation: RouteInformation): Double {
        return DEFAULT_TILT.toDouble()
    }

    override fun zoom(routeInformation: RouteInformation): Double {
        return DEFAULT_ZOOM
    }

    override fun overview(routeInformation: RouteInformation): List<Point> {
        if (routeCoordinates.isEmpty()) {
            buildRouteCoordinatesFromRouteData(routeInformation)
        }

        return routeCoordinates
    }

    private fun buildRouteCoordinatesFromRouteData(routeInformation: RouteInformation) {
        routeInformation.route?.let { route ->
            setupLineStringAndBearing(route)
        } ?: routeInformation.routeProgress?.let { routeProgress ->
            setupLineStringAndBearing(routeProgress.directionsRoute)
        }
    }

    private fun setupLineStringAndBearing(route: DirectionsRoute) {
        if (initialRoute != null && route == initialRoute) {
            return // no need to recalculate these values
        }

        initialRoute = route
        routeCoordinates = generateRouteCoordinates(route)
        initialBearing = Bearing.North
                .clockwiseRotationTo(
                    Point(
                        longitude = routeCoordinates.first().longitude,
                        latitude = routeCoordinates.first().latitude,
                        altitude = routeCoordinates.first().altitude
                    ).bearingTo(
                        Point(
                            longitude = routeCoordinates[1].longitude,
                            latitude = routeCoordinates[1].latitude,
                            altitude = routeCoordinates[1].altitude
                        )
                    )
                )
                .inDegrees
    }

    private fun generateRouteCoordinates(route: DirectionsRoute?): List<Point> {
        return route?.let { rte ->
            val lineString =
                PolylineEncoding.decode(encoded = rte.geometry, precision = Constants.PRECISION_6)
            lineString.map(::Point)
        } ?: emptyList()
    }

    protected companion object {
        const val DEFAULT_TILT: Int = 50
        const val DEFAULT_ZOOM: Double = 15.0
    }
}
