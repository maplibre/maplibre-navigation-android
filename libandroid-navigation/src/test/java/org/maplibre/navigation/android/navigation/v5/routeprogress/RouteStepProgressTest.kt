package org.maplibre.navigation.android.navigation.v5.routeprogress

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.maplibre.geojson.LineString
import org.maplibre.geojson.utils.PolylineUtils
import org.maplibre.navigation.android.json
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.utils.Constants
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement
import org.maplibre.turf.TurfMisc
import java.io.IOException

class RouteStepProgressTest : BaseTest() {
    @Test
    @Throws(Exception::class)
    fun sanityTest() {
        val route = buildTestDirectionsRoute()
        val stepDistanceRemaining = route.legs[0].steps[0].distance
        val legDistanceRemaining = route.legs[0].distance
        val distanceRemaining = route.distance
        val routeProgress = buildTestRouteProgress(
            route, stepDistanceRemaining, legDistanceRemaining,
            distanceRemaining, 0, 0
        )

        Assert.assertNotNull(routeProgress.currentLegProgress.currentStepProgress)
    }

    @Test
    @Throws(Exception::class)
    fun stepDistance_equalsZeroOnOneCoordSteps() {
        val route = loadChipotleTestRoute()
        val stepIndex = route.legs[0].steps.size - 1
        val routeProgress = buildTestRouteProgress(route, 0.0, 0.0, 0.0, stepIndex, 0)
        val routeStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress

        Assert.assertNotNull(routeStepProgress)
        assertEquals(1.0, routeStepProgress.fractionTraveled.toDouble(), DELTA)
        assertEquals(0.0, routeStepProgress.distanceRemaining,DELTA)
        assertEquals(0.0, routeStepProgress.distanceTraveled, DELTA)
        assertEquals(0.0, routeStepProgress.durationRemaining, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun distanceRemaining_equalsStepDistanceAtBeginning() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val lineString = LineString.fromPolyline(
            firstLeg.steps[5].geometry, Constants.PRECISION_6
        )
        val stepDistance = TurfMeasurement.length(lineString, TurfConstants.UNIT_METERS)

        val stepDistanceRemaining = firstLeg.steps[5].distance
        val legDistanceRemaining = firstLeg.distance
        val distanceRemaining = route.distance
        val stepIndex = 4
        val routeProgress = buildTestRouteProgress(
            route, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, stepIndex, 0
        )
        val routeStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress

        assertEquals(
            stepDistance,
            routeStepProgress.distanceRemaining,
            LARGE_DELTA
        )
    }

    @Test
    @Throws(Exception::class)
    fun distanceRemaining_equalsCorrectValueAtIntervals() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val firstStep = route.legs[0].steps[0]
        val lineString = LineString.fromPolyline(
            firstStep.geometry, Constants.PRECISION_6
        )
        val stepDistance = TurfMeasurement.length(lineString, TurfConstants.UNIT_METERS)
        val stepSegments = 5.0

        var i = 0.0
        while (i < stepDistance) {
            val point = TurfMeasurement.along(lineString, i, TurfConstants.UNIT_METERS)

            if (point == route.legs[0].steps[1].maneuver.location) {
                return
            }

            val slicedLine = TurfMisc.lineSlice(
                point,
                route.legs[0].steps[1].maneuver.location, lineString
            )

            val stepDistanceRemaining =
                TurfMeasurement.length(slicedLine, TurfConstants.UNIT_METERS)
            val legDistanceRemaining = firstLeg.distance
            val distanceRemaining = route.distance
            val routeProgress = buildTestRouteProgress(
                route, stepDistanceRemaining,
                legDistanceRemaining, distanceRemaining, 0, 0
            )
            val routeStepProgress: RouteStepProgress =
                routeProgress.currentLegProgress.currentStepProgress

            assertEquals(
                stepDistanceRemaining,
                routeStepProgress.distanceRemaining,
                DELTA
            )
            i += stepSegments
        }
    }

    @Test
    @Throws(Exception::class)
    fun distanceRemaining_equalsZeroAtEndOfStep() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildTestRouteProgress(route, 0.0, 0.0, 0.0, 3, 0)
        val routeStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress

        assertEquals(0.0, routeStepProgress.distanceRemaining, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun distanceTraveled_equalsZeroAtBeginning() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val stepIndex = 5
        val legIndex = 0
        val stepDistanceRemaining = firstLeg.steps[stepIndex].distance
        val legDistanceRemaining = firstLeg.distance
        val distanceRemaining = route.distance
        val routeProgress = buildTestRouteProgress(
            route, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, stepIndex, legIndex
        )
        val routeStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress

        assertEquals(0.0, routeStepProgress.distanceTraveled, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun distanceTraveled_equalsCorrectValueAtIntervals() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val firstStep = route.legs[0].steps[0]
        val lineString = LineString.fromPolyline(
            firstStep.geometry, Constants.PRECISION_6
        )
        val stepSegments = 5.0
        val distances: MutableList<Double> = ArrayList()
        val routeProgressDistancesTraveled: MutableList<Double> = ArrayList()

        var i = 0.0
        while (i < firstStep.distance) {
            val point = TurfMeasurement.along(lineString, i, TurfConstants.UNIT_METERS)
            val slicedLine = TurfMisc.lineSlice(
                point,
                route.legs[0].steps[1].maneuver.location, lineString
            )
            var distance = TurfMeasurement.length(slicedLine, TurfConstants.UNIT_METERS)
            distance = firstStep.distance - distance
            if (distance < 0) {
                distance = 0.0
            }
            val stepIndex = 0
            val legIndex = 0
            val stepDistanceRemaining = firstLeg.steps[0].distance - distance
            val legDistanceRemaining = firstLeg.distance
            val distanceRemaining = route.distance
            val routeProgress = buildTestRouteProgress(
                route, stepDistanceRemaining,
                legDistanceRemaining, distanceRemaining, stepIndex, legIndex
            )
            val routeStepProgress: RouteStepProgress =
                routeProgress.currentLegProgress.currentStepProgress

            distances.add(distance)
            routeProgressDistancesTraveled.add(routeStepProgress.distanceTraveled)
            i += stepSegments
        }

        Assert.assertTrue(distances == routeProgressDistancesTraveled)
    }

    @Test
    @Throws(Exception::class)
    fun distanceTraveled_equalsStepDistanceAtEndOfStep() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val stepIndex = 3
        val legIndex = 0
        val stepDistanceRemaining = 0.0
        val legDistanceRemaining = firstLeg.distance
        val distanceRemaining = route.distance
        val routeProgress = buildTestRouteProgress(
            route, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, stepIndex, legIndex
        )

        val routeStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress

        assertEquals(
            firstLeg.steps[3].distance,
            routeStepProgress.distanceTraveled, DELTA
        )
    }

    @Test
    @Throws(Exception::class)
    fun fractionTraveled_equalsZeroAtBeginning() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val stepIndex = 5
        val legIndex = 0
        val stepDistanceRemaining = firstLeg.steps[stepIndex].distance
        val legDistanceRemaining = firstLeg.distance
        val distanceRemaining = route.distance
        val routeProgress = buildTestRouteProgress(
            route, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, stepIndex, legIndex
        )

        val routeStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress
        assertEquals(0.0, routeStepProgress.fractionTraveled.toDouble(), DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun fractionTraveled_equalsCorrectValueAtIntervals() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val firstStep = route.legs[0].steps[0]
        val lineString = LineString.fromPolyline(
            firstStep.geometry, Constants.PRECISION_6
        )
        val fractionsRemaining: MutableList<Float> = ArrayList()
        val routeProgressFractionsTraveled: MutableList<Float> = ArrayList()
        val stepSegments = 5.0

        var i = 0.0
        while (i < firstStep.distance) {
            val point = TurfMeasurement.along(lineString, i, TurfConstants.UNIT_METERS)
            val slicedLine = TurfMisc.lineSlice(
                point,
                route.legs[0].steps[1].maneuver.location, lineString
            )
            val stepDistanceRemaining =
                TurfMeasurement.length(slicedLine, TurfConstants.UNIT_METERS)
            val stepIndex = 0
            val legIndex = 0
            val legDistanceRemaining = firstLeg.distance
            val distanceRemaining = route.distance
            val routeProgress = buildTestRouteProgress(
                route, stepDistanceRemaining,
                legDistanceRemaining, distanceRemaining, stepIndex, legIndex
            )
            val routeStepProgress: RouteStepProgress =
                routeProgress.currentLegProgress.currentStepProgress
            var fractionRemaining =
                ((firstStep.distance - stepDistanceRemaining) / firstStep.distance).toFloat()
            if (fractionRemaining < 0) {
                fractionRemaining = 0f
            }
            fractionsRemaining.add(fractionRemaining)
            routeProgressFractionsTraveled.add(routeStepProgress.fractionTraveled)
            i += stepSegments
        }

        Assert.assertTrue(fractionsRemaining == routeProgressFractionsTraveled)
    }

    @Test
    @Throws(Exception::class)
    fun fractionTraveled_equalsOneAtEndOfStep() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val stepIndex = 3
        val legIndex = 0
        val stepDistanceRemaining = 0.0
        val legDistanceRemaining = firstLeg.distance
        val distanceRemaining = route.distance
        val routeProgress = buildTestRouteProgress(
            route, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, stepIndex, legIndex
        )
        val routeStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress

        assertEquals(1.0, routeStepProgress.fractionTraveled.toDouble(), DELTA)
    }

    @Test
    fun durationRemaining_equalsStepDurationAtBeginning() {
            val route =
                buildTestDirectionsRoute()
            val firstLeg =
                route.legs[0]
            val fourthStep =
                firstLeg.steps[5]
            val stepDuration = fourthStep.duration
            val stepIndex = 5
            val legIndex = 0
            val stepDistanceRemaining = firstLeg.steps[stepIndex].distance
            val legDistanceRemaining = firstLeg.distance
            val distanceRemaining = route.distance
            val routeProgress = buildTestRouteProgress(
                route, stepDistanceRemaining,
                legDistanceRemaining, distanceRemaining, stepIndex, legIndex
            )

            val routeStepProgress: RouteStepProgress =
                routeProgress.currentLegProgress.currentStepProgress
            val durationRemaining: Double = routeStepProgress.durationRemaining

            assertEquals(
                stepDuration,
                durationRemaining,
                DELTA
            )
        }

    @Test
    @Throws(Exception::class)
    fun durationRemaining_equalsCorrectValueAtIntervals() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val firstStep = route.legs[0].steps[0]
        val lineString = LineString.fromPolyline(
            firstStep.geometry, Constants.PRECISION_6
        )
        val stepSegments = 5.0
        val fractionsRemaining: MutableList<Double> = ArrayList()
        val routeProgressDurationsTraveled: MutableList<Double> = ArrayList()

        var i = 0.0
        while (i < firstStep.distance) {
            val point = TurfMeasurement.along(lineString, i, TurfConstants.UNIT_METERS)
            val slicedLine = TurfMisc.lineSlice(
                point,
                route.legs[0].steps[1].maneuver.location, lineString
            )
            val stepIndex = 0
            val legIndex = 0
            val stepDistanceRemaining =
                TurfMeasurement.length(slicedLine, TurfConstants.UNIT_METERS)
            val legDistanceRemaining = firstLeg.distance
            val distanceRemaining = route.distance
            val routeProgress = buildTestRouteProgress(
                route, stepDistanceRemaining,
                legDistanceRemaining, distanceRemaining, stepIndex, legIndex
            )
            val routeStepProgress: RouteStepProgress =
                routeProgress.currentLegProgress.currentStepProgress
            val fractionRemaining = (firstStep.distance - stepDistanceRemaining) / firstStep.distance

            val expectedFractionRemaining = (1.0 - fractionRemaining) * firstStep.duration
            fractionsRemaining.add(Math.round(expectedFractionRemaining * 100.0) / 100.0)
            routeProgressDurationsTraveled.add(Math.round(routeStepProgress.durationRemaining * 100.0) / 100.0)
            i += stepSegments
        }
        val fractionRemaining = fractionsRemaining[fractionsRemaining.size - 1]
        val routeProgressDuration =
            routeProgressDurationsTraveled[routeProgressDurationsTraveled.size - 1]

        assertEquals(fractionRemaining, routeProgressDuration, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun durationRemaining_equalsZeroAtEndOfStep() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val stepIndex = 3
        val legIndex = 0
        val stepDistanceRemaining = 0.0
        val legDistanceRemaining = firstLeg.distance
        val distanceRemaining = route.distance
        val routeProgress = buildTestRouteProgress(
            route, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, stepIndex, legIndex
        )
        val routeStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress

        assertEquals(0.0, routeStepProgress.durationRemaining, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun stepIntersections_includesAllStepIntersectionsAndNextManeuver() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val stepIndex = 3
        val legIndex = 0
        val stepDistanceRemaining = 0.0
        val legDistanceRemaining = firstLeg.distance
        val distanceRemaining = route.distance
        val routeProgress = buildTestRouteProgress(
            route, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, stepIndex, legIndex
        )
        val routeStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress

        val stepIntersections = route.legs[0].steps[3]
            .intersections!!.size
        val intersectionSize = stepIntersections + 1

        assertEquals(intersectionSize, routeStepProgress.intersections!!.size)
    }

    @Test
    @Throws(Exception::class)
    fun stepIntersections_handlesNullNextManeuverCorrectly() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val stepIndex = (route.legs[0].steps.size - 1)
        val legIndex = 0
        val stepDistanceRemaining = 0.0
        val legDistanceRemaining = firstLeg.distance
        val distanceRemaining = route.distance
        val routeProgress = buildTestRouteProgress(
            route, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, stepIndex, legIndex
        )
        val routeStepProgress: RouteStepProgress =
            routeProgress.currentLegProgress.currentStepProgress
        val currentStepTotal = route.legs[0].steps[stepIndex]
            .intersections!!.size
        val lastStepLocation = PolylineUtils.decode(
            route.legs[0].steps[stepIndex].geometry, Constants.PRECISION_6
        )

        assertEquals(currentStepTotal, routeStepProgress.intersections!!.size)
        assertEquals(
            routeStepProgress.intersections!![0].location.latitude(),
            lastStepLocation[0].latitude(),
            DELTA
        )
        assertEquals(
            routeStepProgress.intersections!![0].location.longitude(),
            lastStepLocation[0].longitude(),
            DELTA
        )
    }

    @Throws(IOException::class)
    private fun loadChipotleTestRoute(): DirectionsRoute {
        val fixtureJsonString = loadJsonFixture(DCMAPBOX_CHIPOLTLE_FIXTURE)
        val response = json.decodeFromString<DirectionsResponse>(fixtureJsonString)
        return response.routes[0]
    }

    companion object {
        private const val DCMAPBOX_CHIPOLTLE_FIXTURE = "dcmapbox_chipoltle.json"
    }
}