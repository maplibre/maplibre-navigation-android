package org.maplibre.navigation.core

import org.maplibre.navigation.geo.LineString
import org.maplibre.navigation.geo.Point
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.utils.Constants
import org.maplibre.navigation.geo.turf.TurfConstants
import org.maplibre.navigation.geo.turf.TurfMeasurement

internal class MockLocationBuilder {
    fun buildDefaultMockLocationUpdate(lng: Double, lat: Double): Location {
        return buildMockLocationUpdate(lng, lat, 30f, 10f, System.currentTimeMillis())
    }

    fun buildPointAwayFromLocation(location: Location, distanceAway: Double): Point {
        val fromLocation = Point(
            location.longitude, location.latitude, location.altitude
        )
        return TurfMeasurement.destination(
            fromLocation,
            distanceAway,
            90.0,
            TurfConstants.UNIT_METERS
        )
    }

    fun buildPointAwayFromPoint(point: Point, distanceAway: Double, bearing: Double): Point {
        return TurfMeasurement.destination(point, distanceAway, bearing, TurfConstants.UNIT_METERS)
    }

    fun createCoordinatesFromCurrentStep(progress: RouteProgress): List<Point> {
        val currentStep = progress.currentLegProgress.currentStep
        val lineString = LineString.fromPolyline(currentStep.geometry, Constants.PRECISION_6)
        return lineString.points
    }

    private fun buildMockLocationUpdate(
        lngValue: Double,
        latValue: Double,
        speedValue: Float,
        horizontalAccuracyValue: Float,
        timeValue: Long
    ): Location {
        return Location(
            provider = "test",
            latitude = latValue,
            longitude = lngValue,
            speedMetersPerSeconds = speedValue,
            accuracyMeters = horizontalAccuracyValue,
            time = timeValue
        )
    }
}
