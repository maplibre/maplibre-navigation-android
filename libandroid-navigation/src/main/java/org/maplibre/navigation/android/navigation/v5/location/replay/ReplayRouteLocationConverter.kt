package org.maplibre.navigation.android.navigation.v5.location.replay

import android.location.Location
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.utils.Constants
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement

open class ReplayRouteLocationConverter(
    private val route: DirectionsRoute,
    private var speed: Int,
    private var delay: Int
) {
    private var distance = calculateDistancePerSec()
    private var currentLeg = 0
    private var currentStep = 0
    private var time: Long = 0

    val isMultiLegRoute: Boolean
        get() = route.legs.size > 1


    fun updateSpeed(customSpeedInKmPerHour: Int) {
        this.speed = customSpeedInKmPerHour
    }

    fun updateDelay(customDelayInSeconds: Int) {
        this.delay = customDelayInSeconds
    }

    fun toLocations(): MutableList<Location> {
        val stepPoints = calculateStepPoints()
        val mockedLocations = calculateMockLocations(stepPoints)

        return mockedLocations
    }

    fun initializeTime() {
        time = System.currentTimeMillis()
    }

    /**
     * Interpolates the route into even points along the route and adds these to the points list.
     *
     * @param lineString our route geometry.
     * @return list of sliced [Point]s.
     */
    fun sliceRoute(lineString: LineString): List<Point> {
        if (lineString.coordinates().isEmpty()) {
            return emptyList()
        }

        val distanceMeters = TurfMeasurement.length(lineString, TurfConstants.UNIT_METERS)
        if (distanceMeters <= 0) {
            return emptyList()
        }

        val points: MutableList<Point> = ArrayList()
        var i = 0.0
        while (i < distanceMeters) {
            val point = TurfMeasurement.along(lineString, i, TurfConstants.UNIT_METERS)
            points.add(point)
            i += distance
        }
        return points
    }

    fun calculateMockLocations(points: List<Point>): MutableList<Location> {
        val mockedLocations: MutableList<Location> = ArrayList()
        for (i in points.indices) {
            val mockedLocation = createMockLocationFrom(points[i])

            if (i - 1 >= 0) {
                val bearing = TurfMeasurement.bearing(points[i - 1], points[i])
                mockedLocation.bearing = bearing.toFloat()
            } else {
                mockedLocation.bearing = 0f
            }
            time += (delay * ONE_SECOND_IN_MILLISECONDS).toLong()
            mockedLocations.add(mockedLocation)
        }

        return mockedLocations
    }

    /**
     * Converts the speed value to m/s and delay to seconds. Then the distance is calculated and returned.
     *
     * @return a double value representing the distance given a speed and time.
     */
    private fun calculateDistancePerSec(): Double {
        val distance = (speed * ONE_KM_IN_METERS * delay) / ONE_HOUR_IN_SECONDS
        return distance
    }

    private fun calculateStepPoints(): List<Point> {
        val line = LineString.fromPolyline(
            route.legs[currentLeg].steps[currentStep].geometry,
            Constants.PRECISION_6
        )

        increaseIndex()

        return sliceRoute(line)
    }

    private fun increaseIndex() {
        if (currentStep < route.legs[currentLeg].steps.size - 1) {
            currentStep++
        } else if (currentLeg < route.legs.size - 1) {
            currentLeg++
            currentStep = 0
        }
    }

    private fun createMockLocationFrom(point: Point): Location {
        val location = Location(REPLAY_ROUTE)
        location.latitude = point.latitude()
        location.longitude = point.longitude()
        location.speed = ((speed * ONE_KM_IN_METERS) / ONE_HOUR_IN_SECONDS).toFloat()
        location.accuracy = 3f
        location.time = time
        return location
    }

    companion object {
        private const val ONE_SECOND_IN_MILLISECONDS = 1000
        private const val ONE_KM_IN_METERS = 1000.0
        private const val ONE_HOUR_IN_SECONDS = 3600
        private const val REPLAY_ROUTE = "ReplayRouteLocation"
    }
}
