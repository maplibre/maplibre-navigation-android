package org.maplibre.navigation.core.utils

import org.maplibre.geojson.model.LineString
import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.geojson.turf.TurfMeasurement
import org.maplibre.geojson.turf.TurfUnit
import org.maplibre.geojson.utils.PolylineUtils
import org.maplibre.navigation.core.models.StepIntersection
import kotlin.test.Test
import kotlin.test.assertEquals

class ToleranceUtilsTest : BaseTest() {

    @Test
    fun dynamicRerouteDistanceTolerance_userFarAwayFromIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val stepPoints = PolylineUtils.decode(
            route.geometry, Constants.PRECISION_6
        )
        val midPoint = TurfMeasurement.midpoint(stepPoints[0], stepPoints[1])

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            midPoint,
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
        val lineString = LineString(route.geometry, Constants.PRECISION_6)
        val closePoint =
            TurfMeasurement.along(lineString, distanceToIntersection, TurfUnit.METERS)

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
        val lineString = LineString(route.geometry, Constants.PRECISION_6)
        val closePoint =
            TurfMeasurement.along(lineString, distanceToIntersection, TurfUnit.METERS)

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