package org.maplibre.navigation.core.utils

import org.maplibre.navigation.geo.LineString
import org.maplibre.navigation.geo.util.PolylineUtils
import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.geo.turf.TurfConstants
import org.maplibre.navigation.geo.turf.TurfMeasurement
import kotlin.test.Test
import kotlin.test.assertEquals

class ToleranceUtilsTest : BaseTest() {

    @Test
    @Throws(Exception::class)
    fun dynamicRerouteDistanceTolerance_userFarAwayFromIntersection() {
        //TODO fabi755
//        val route = buildTestDirectionsRoute()
//        val routeProgress = buildDefaultTestRouteProgress()
//        val stepPoints = PolylineUtils.decode(
//            route.geometry, Constants.PRECISION_6
//        )
//        val midPoint = TurfMeasurement.midpoint(stepPoints[0], stepPoints[1])
//
//        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
//            midPoint,
//            routeProgress,
//            MapLibreNavigationOptions()
//        )
//
//        assertEquals(25.0, tolerance, DELTA)
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

        assertEquals(50.0, tolerance, DELTA)
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

        assertEquals(50.0, tolerance, DELTA)
    }
}