package org.maplibre.navigation.android.navigation.v5

import android.location.Location
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.utils.Constants
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement
import org.mockito.Mockito

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
        val currentStep: LegStep = progress.currentLegProgress!!.currentStep!! //TODO fabi755
        val lineString = LineString.fromPolyline(
            currentStep.geometry()!!, Constants.PRECISION_6
        )
        return lineString.coordinates()
    }

    private fun buildMockLocationUpdate(
        lng: Double,
        lat: Double,
        speed: Float,
        horizontalAccuracy: Float,
        time: Long
    ): Location {
        val location = Mockito.mock(Location::class.java)
        Mockito.`when`(location.longitude).thenReturn(lng)
        Mockito.`when`(location.latitude).thenReturn(lat)
        Mockito.`when`(location.speed).thenReturn(speed)
        Mockito.`when`(location.accuracy).thenReturn(horizontalAccuracy)
        Mockito.`when`(location.time).thenReturn(time)
        return location
    }
}
