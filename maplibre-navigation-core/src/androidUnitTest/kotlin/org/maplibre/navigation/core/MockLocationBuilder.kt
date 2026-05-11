package org.maplibre.navigation.core

import org.maplibre.android.annotations.Polyline
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.utils.Constants
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.polyline.PolylineEncoding
import org.maplibre.spatialk.turf.measurement.bearingTo
import org.maplibre.spatialk.turf.measurement.offset
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.Rotation
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.meters

internal class MockLocationBuilder {
    fun buildDefaultMockLocationUpdate(lng: Double, lat: Double): Location {
        return buildMockLocationUpdate(lng, lat, 30f, 10f, System.currentTimeMillis())
    }

    fun buildPointAwayFromLocation(location: Location, distanceAway: Double): Point {
        val fromLocation = Point(
            location.longitude, location.latitude, location.altitude
        )

        return fromLocation.offset(distanceAway.meters, Bearing.North + 90.degrees)
            .let(::Point)
    }

    fun buildPointAwayFromPoint(point: Point, distanceAway: Double, bearing: Double): Point {
        return point.offset(distanceAway.meters, Bearing.North + bearing.degrees)
            .let(::Point)
    }

    fun createCoordinatesFromCurrentStep(progress: RouteProgress): List<Point> {
        val currentStep = progress.currentLegProgress.currentStep
        return PolylineEncoding.decode(currentStep.geometry, Constants.PRECISION_6)
            .map(::Point)
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
            timeMilliseconds = timeValue
        )
    }
}
