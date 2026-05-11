package org.maplibre.navigation.core.utils

import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.models.StepIntersection
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.polyline.PolylineEncoding
import org.maplibre.spatialk.turf.measurement.locateAlong
import org.maplibre.spatialk.turf.measurement.midpoint
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test
import kotlin.test.assertEquals

class ToleranceUtilsTest : BaseTest() {

    @Test
    fun dynamicRerouteDistanceTolerance_userFarAwayFromIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val stepPoints = PolylineEncoding.decode(
            route.geometry, Constants.PRECISION_6
        )

        val midPoint = midpoint(stepPoints[0], stepPoints[1])

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            Point(midPoint),
            routeProgress,
            MapLibreNavigationOptions()
        )

        assertEquals(25.0, tolerance, DELTA)
    }

    @Test
    fun dynamicRerouteDistanceTolerance_userCloseToIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val distanceToIntersection = route.distance - 39
        val positions = PolylineEncoding.decode(route.geometry, Constants.PRECISION_6)
        val closePoint = LineString(positions).locateAlong(distanceToIntersection.meters)

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            closePoint,
            routeProgress,
            MapLibreNavigationOptions()
        )

        assertEquals(50.0, tolerance, DELTA)
    }

    @Test
    fun dynamicRerouteDistanceTolerance_userJustPastTheIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val distanceToIntersection = route.distance
        val positions = PolylineEncoding.decode(route.geometry, Constants.PRECISION_6)
        val closePoint = LineString(positions).locateAlong(distanceToIntersection.meters)

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            closePoint,
            routeProgress,
            MapLibreNavigationOptions()
        )

        assertEquals(50.0, tolerance, DELTA)
    }

    @Test
    fun dynamicRerouteDistanceTolerance_noIntersections() {
        val routeProgress = buildDefaultTestRouteProgress().copy(intersections = listOf())

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            routeProgress.currentStepPoints[0],
            routeProgress,
            MapLibreNavigationOptions()
        )

        assertEquals(50.0, tolerance, DELTA)
    }

    @Test
    fun dynamicRerouteDistanceTolerance_singleIntersection() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            intersections = listOf(StepIntersection(location = Point(-122.416686, 37.783425)))
        )

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            routeProgress.currentStepPoints[0],
            routeProgress,
            MapLibreNavigationOptions()
        )

        assertEquals(50.0, tolerance, DELTA)
    }
}