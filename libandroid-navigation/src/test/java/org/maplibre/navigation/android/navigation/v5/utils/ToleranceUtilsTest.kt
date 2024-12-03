package org.maplibre.navigation.android.navigation.v5.utils

import org.junit.Assert
import org.junit.Test
import org.maplibre.geojson.LineString
import org.maplibre.geojson.utils.PolylineUtils
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement

class ToleranceUtilsTest : BaseTest() {
    @Test
    @Throws(Exception::class)
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

        Assert.assertEquals(25.0, tolerance, DELTA)
    }


    @Test
    @Throws(Exception::class)
    fun dynamicRerouteDistanceTolerance_userCloseToIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val distanceToIntersection = route.distance - 39
        val lineString = LineString.fromPolyline(
            route.geometry, Constants.PRECISION_6
        )
        val closePoint =
            TurfMeasurement.along(lineString, distanceToIntersection, TurfConstants.UNIT_METERS)

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            closePoint,
            routeProgress,
            MapLibreNavigationOptions()
        )

        Assert.assertEquals(50.0, tolerance, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun dynamicRerouteDistanceTolerance_userJustPastTheIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val distanceToIntersection = route.distance
        val lineString = LineString.fromPolyline(
            route.geometry, Constants.PRECISION_6
        )
        val closePoint =
            TurfMeasurement.along(lineString, distanceToIntersection, TurfConstants.UNIT_METERS)

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            closePoint,
            routeProgress,
            MapLibreNavigationOptions()
        )

        Assert.assertEquals(50.0, tolerance, DELTA)
    }
}