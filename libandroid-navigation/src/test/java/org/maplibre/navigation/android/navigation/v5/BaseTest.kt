package org.maplibre.navigation.android.navigation.v5

import android.location.Location
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import java.io.IOException

open class BaseTest {
    private val routeBuilder =
        TestRouteBuilder()
    private val routeProgressBuilder =
        TestRouteProgressBuilder()
    private val locationBuilder = MockLocationBuilder()

    @Throws(IOException::class)
    protected fun loadJsonFixture(filename: String): String {
        return routeBuilder.loadJsonFixture(filename)
    }

    @Throws(IOException::class)
    protected fun buildTestDirectionsRoute(): DirectionsRoute {
        return routeBuilder.buildTestDirectionsRoute(null)
    }

    @Throws(IOException::class)
    protected fun buildTestDirectionsRoute(fixtureName: String?): DirectionsRoute {
        return routeBuilder.buildTestDirectionsRoute(fixtureName)
    }

    @Throws(Exception::class)
    protected fun buildDefaultTestRouteProgress(): RouteProgress {
        val testRoute = routeBuilder.buildTestDirectionsRoute(null)
        return routeProgressBuilder.buildDefaultTestRouteProgress(testRoute)
    }

    @Throws(Exception::class)
    protected fun buildDefaultTestRouteProgress(testRoute: DirectionsRoute): RouteProgress {
        return routeProgressBuilder.buildDefaultTestRouteProgress(testRoute)
    }

    @Throws(Exception::class)
    protected fun buildTestRouteProgress(
        route: DirectionsRoute,
        stepDistanceRemaining: Double,
        legDistanceRemaining: Double,
        distanceRemaining: Double,
        stepIndex: Int,
        legIndex: Int
    ): RouteProgress {
        return routeProgressBuilder.buildTestRouteProgress(
            route,
            stepDistanceRemaining,
            legDistanceRemaining,
            distanceRemaining,
            stepIndex,
            legIndex
        )
    }

    protected fun buildDefaultLocationUpdate(lng: Double, lat: Double): Location {
        return locationBuilder.buildDefaultMockLocationUpdate(lng, lat)
    }

    protected fun buildPointAwayFromLocation(location: Location, distanceAway: Double): Point {
        return locationBuilder.buildPointAwayFromLocation(location, distanceAway)
    }

    protected fun buildPointAwayFromPoint(
        point: Point,
        distanceAway: Double,
        bearing: Double
    ): Point {
        return locationBuilder.buildPointAwayFromPoint(point, distanceAway, bearing)
    }

    protected fun createCoordinatesFromCurrentStep(progress: RouteProgress): List<Point> {
        return locationBuilder.createCoordinatesFromCurrentStep(progress)
    }

    companion object {
        const val DELTA: Double = 1E-10
        const val LARGE_DELTA: Double = 0.1
        const val ACCESS_TOKEN: String = "pk.XXX"
    }
}
