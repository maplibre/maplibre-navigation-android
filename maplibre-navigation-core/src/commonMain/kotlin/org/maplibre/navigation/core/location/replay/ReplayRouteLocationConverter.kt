package org.maplibre.navigation.core.location.replay

import org.maplibre.geojson.model.LineString
import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.utils.Constants
import org.maplibre.geojson.turf.TurfMeasurement
import org.maplibre.geojson.turf.TurfUnit
import org.maplibre.navigation.core.utils.getCurrentSystemTimeSeconds

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
        time = getCurrentSystemTimeSeconds() * ONE_SECOND_IN_MILLISECONDS
    }

    /**
     * Interpolates the route into even points along the route and adds these to the points list.
     *
     * @param lineString our route geometry.
     * @return list of sliced [Point]s.
     */
    fun sliceRoute(lineString: LineString): List<Point> {
        if (lineString.coordinates.isEmpty()) {
            return emptyList()
        }

        val distanceMeters = TurfMeasurement.length(lineString, TurfUnit.METERS)
        if (distanceMeters <= 0) {
            return emptyList()
        }

        val points: MutableList<Point> = ArrayList()
        var i = 0.0
        while (i < distanceMeters) {
            val point = TurfMeasurement.along(lineString, i, TurfUnit.METERS)
            points.add(point)
            i += distance
        }
        return points
    }

    fun calculateMockLocations(points: List<Point>): MutableList<Location> {
        val mockedLocations: MutableList<Location> = ArrayList()
        for (i in points.indices) {
            val mockedLocation = createMockLocationFrom(points[i])

            mockedLocations.add(
                if (i - 1 >= 0) {
                    val bearing = TurfMeasurement.bearing(points[i - 1], points[i])
                    mockedLocation.copy(bearing = bearing.toFloat())
                } else {
                    mockedLocation.copy(bearing = 0f)
                }
            )

            time += (delay * ONE_SECOND_IN_MILLISECONDS).toLong()
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
        val line = LineString(
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
        return Location(
            provider = PROVIDER_NAME,
            latitude = point.latitude,
            longitude = point.longitude,
            altitude = point.altitude,
            speedMetersPerSeconds = ((speed * ONE_KM_IN_METERS) / ONE_HOUR_IN_SECONDS).toFloat(),
            accuracyMeters = 3f,
            timeMilliseconds = time
        )
    }

    companion object {
        private const val ONE_SECOND_IN_MILLISECONDS = 1000
        private const val ONE_KM_IN_METERS = 1000.0
        private const val ONE_HOUR_IN_SECONDS = 3600
        const val PROVIDER_NAME = "ReplayRouteLocation"
    }
}
