package org.maplibre.navigation.core.utils

import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.geojson.turf.TurfMeasurement
import org.maplibre.geojson.turf.TurfMisc
import org.maplibre.geojson.turf.TurfUnit
import kotlin.jvm.JvmStatic

object ToleranceUtils {

    /**
     * Reduce the offRouteMinimumDistanceMetersBeforeWrongDirection if we are close to an intersection.
     * You can define these values in the navigationOptions
     */
    @JvmStatic
    fun dynamicOffRouteRadiusTolerance(
        snappedPoint: Point,
        routeProgress: RouteProgress,
        navigationOptions: MapLibreNavigationOptions
    ): Double {
        val intersections = routeProgress.currentLegProgress.currentStepProgress.intersections
        if (!intersections.isNullOrEmpty()) {
            val intersectionsPoints: MutableList<Point> = ArrayList()
            for (intersection in intersections) {
                intersectionsPoints.add(intersection.location)
            }

            val closestIntersectionFeature = TurfMisc.nearestPointOnLine(snappedPoint, intersectionsPoints)
            val closestIntersection = closestIntersectionFeature.geometry as Point
            if (closestIntersection == snappedPoint) {
                return navigationOptions.offRouteThresholdRadiusMeters
            }

            val distanceToNextIntersection = TurfMeasurement.distance(
                snappedPoint,
                closestIntersection,
                TurfUnit.METERS
            )

            if (distanceToNextIntersection <= navigationOptions.maneuverZoneRadius) {
                return navigationOptions.offRouteThresholdRadiusMeters / 2
            }
        }

        return navigationOptions.offRouteThresholdRadiusMeters
    }
}