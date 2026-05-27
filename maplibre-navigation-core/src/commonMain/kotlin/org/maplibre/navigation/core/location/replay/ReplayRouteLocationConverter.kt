package org.maplibre.navigation.core.location.replay

import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.utils.Constants
import org.maplibre.navigation.core.utils.getCurrentSystemTimeSeconds
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.polyline.PolylineEncoding
import org.maplibre.spatialk.turf.measurement.bearingTo
import org.maplibre.spatialk.turf.measurement.length
import org.maplibre.spatialk.turf.measurement.locateAlong
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.extensions.inDegrees
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.meters

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

        val distanceMeters = lineString.length().inMeters
        if (distanceMeters <= 0) {
            return emptyList()
        }

        val points: MutableList<Point> = ArrayList()
        var i = 0.0
        while (i < distanceMeters) {
            val point = lineString.locateAlong(i.meters)
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
                    val bearingDegrees = Bearing.North
                        .clockwiseRotationTo(points[i - 1].bearingTo(points[i]))
                        .inDegrees
                        .toFloat()
                    mockedLocation.copy(bearing = bearingDegrees)
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
        val positions = PolylineEncoding.decode(
            encoded = route.legs[currentLeg].steps[currentStep].geometry,
            precision = Constants.PRECISION_6
        )

        increaseIndex()

        return sliceRoute(LineString(positions))
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
