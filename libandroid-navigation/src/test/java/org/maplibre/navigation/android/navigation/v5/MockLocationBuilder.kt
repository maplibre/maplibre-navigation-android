package org.maplibre.navigation.android.navigation.v5

import android.location.Location
import io.mockk.every
import io.mockk.mockk
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.utils.Constants
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement

internal class MockLocationBuilder {
    fun buildDefaultMockLocationUpdate(lng: Double, lat: Double): Location {
        return buildMockLocationUpdate(lng, lat, 30f, 10f, System.currentTimeMillis())
    }

    fun buildPointAwayFromLocation(location: Location, distanceAway: Double): Point {
        val fromLocation = Point.fromLngLat(
            location.longitude, location.latitude
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
        return lineString.coordinates()
    }

    private fun buildMockLocationUpdate(
        lngValue: Double,
        latValue: Double,
        speedValue: Float,
        horizontalAccuracyValue: Float,
        timeValue: Long
    ): Location {
        return mockk(relaxed = true) {
            every { longitude } returns lngValue
            every { latitude } returns latValue
            every { speed } returns speedValue
            every { accuracy } returns horizontalAccuracyValue
            every { time } returns timeValue
        }
    }
}
