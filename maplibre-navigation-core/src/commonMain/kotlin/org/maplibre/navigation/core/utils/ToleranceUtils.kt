package org.maplibre.navigation.core.utils

import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.measurement.distance
import org.maplibre.spatialk.turf.misc.nearestPointTo
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.jvm.JvmStatic

object ToleranceUtils {

    /**
     * Reduce the offRouteMinimumDistanceMetersBeforeWrongDirection if we are close to an intersection.
     * You can define these values in the navigationOptions
     */
    @JvmStatic
    fun dynamicOffRouteRadiusTolerance(
        snappedPoint: Position,
        routeProgress: RouteProgress,
        navigationOptions: MapLibreNavigationOptions
    ): Double {
        val intersections = routeProgress.currentLegProgress.currentStepProgress.intersections
        if (intersections != null && intersections.size >= 2) {
            val closestIntersectionFeature = intersections.map { pos -> Point(pos.location) }
                .nearestPointTo(Point(snappedPoint))

            val closestIntersection = closestIntersectionFeature.geometry.coordinates
            if (closestIntersection == snappedPoint) {
                return navigationOptions.offRouteThresholdRadiusMeters
            }

            val distanceToNextIntersection = distance(snappedPoint, closestIntersection)
                .inMeters
            if (distanceToNextIntersection <= navigationOptions.maneuverZoneRadius) {
                return navigationOptions.offRouteThresholdRadiusMeters / 2
            }
        }

        return navigationOptions.offRouteThresholdRadiusMeters
    }
}