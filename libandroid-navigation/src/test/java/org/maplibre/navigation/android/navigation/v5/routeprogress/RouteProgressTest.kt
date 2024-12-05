package org.maplibre.navigation.android.navigation.v5.routeprogress

import org.junit.Assert
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.maplibre.navigation.android.json
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.models.RouteLeg

class RouteProgressTest : BaseTest() {
    @Test
    @Throws(Exception::class)
    fun sanityTest() {
        val route = buildTestDirectionsRoute()
        val beginningRouteProgress = buildBeginningOfLegRouteProgress(route)

        Assert.assertNotNull(beginningRouteProgress)
    }

    @Test
    @Throws(Exception::class)
    fun directionsRoute_returnsDirectionsRoute() {
        val route = buildTestDirectionsRoute()
        val beginningRouteProgress = buildBeginningOfLegRouteProgress(route)

        assertEquals(route, beginningRouteProgress.directionsRoute)
    }

    @Test
    @Throws(Exception::class)
    fun distanceRemaining_equalsRouteDistanceAtBeginning() {
        val route = buildTestDirectionsRoute()
        val beginningRouteProgress = buildBeginningOfLegRouteProgress(route)

        assertEquals(
            route.distance,
            beginningRouteProgress.distanceRemaining,
            LARGE_DELTA
        )
    }

    @Test
    @Throws(Exception::class)
    fun distanceRemaining_equalsZeroAtEndOfRoute() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val lastRouteProgress = buildLastRouteProgress(route, firstLeg)

        assertEquals(0.0, lastRouteProgress.distanceRemaining, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun fractionTraveled_equalsZeroAtBeginning() {
        val route = buildTestDirectionsRoute()
        val beginningRouteProgress = buildBeginningOfLegRouteProgress(route)

        assertEquals(0.0, beginningRouteProgress.fractionTraveled.toDouble(), LARGE_DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun fractionTraveled_equalsCorrectValueAtIntervals() {
        val route = buildTestDirectionsRoute()
        val multiLegRoute = buildMultipleLegRoute()
        val fractionsRemaining: MutableList<Float> = ArrayList()
        val routeProgressFractionsTraveled: MutableList<Float> = ArrayList()

        for (stepIndex in route.legs[0].steps.indices) {
            val stepDistanceRemaining = getFirstStep(multiLegRoute).distance
            val legDistanceRemaining = multiLegRoute.legs[0].distance
            val distanceRemaining = multiLegRoute.distance
            val routeProgress = buildTestRouteProgress(
                multiLegRoute, stepDistanceRemaining,
                legDistanceRemaining, distanceRemaining, stepIndex, 0
            )
            val fractionRemaining = (routeProgress.distanceTraveled / route.distance).toFloat()

            fractionsRemaining.add(fractionRemaining)
            routeProgressFractionsTraveled.add(routeProgress.fractionTraveled)
        }

        Assert.assertTrue(fractionsRemaining == routeProgressFractionsTraveled)
    }

    @Test
    @Throws(Exception::class)
    fun fractionTraveled_equalsOneAtEndOfRoute() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val lastRouteProgress = buildLastRouteProgress(route, firstLeg)

        assertEquals(1.0, lastRouteProgress.fractionTraveled.toDouble(), DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun durationRemaining_equalsRouteDurationAtBeginning() {
        val route = buildTestDirectionsRoute()
        val beginningRouteProgress = buildBeginningOfLegRouteProgress(route)

        val durationRemaining = route.duration
        val progressDurationRemaining: Double = beginningRouteProgress.durationRemaining

        Assert.assertEquals(durationRemaining, progressDurationRemaining, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun durationRemaining_equalsZeroAtEndOfRoute() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val lastRouteProgress = buildLastRouteProgress(route, firstLeg)

        assertEquals(0.0, lastRouteProgress.durationRemaining, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun distanceTraveled_equalsZeroAtBeginning() {
        val route = buildTestDirectionsRoute()
        val beginningRouteProgress = buildBeginningOfLegRouteProgress(route)

        assertEquals(0.0, beginningRouteProgress.distanceTraveled, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun distanceTraveled_equalsRouteDistanceAtEndOfRoute() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val lastRouteProgress = buildLastRouteProgress(route, firstLeg)

        assertEquals(
            route.distance,
            lastRouteProgress.distanceTraveled,
            DELTA
        )
    }

    @Test
    @Throws(Exception::class)
    fun currentLeg_returnsCurrentLeg() {
        val route = buildTestDirectionsRoute()
        val beginningRouteProgress = buildBeginningOfLegRouteProgress(route)

        assertEquals(route.legs[0], beginningRouteProgress.currentLeg)
    }

    @Test
    @Throws(Exception::class)
    fun legIndex_returnsCurrentLegIndex() {
        val route = buildTestDirectionsRoute()
        val beginningRouteProgress = buildBeginningOfLegRouteProgress(route)

        assertEquals(0, beginningRouteProgress.legIndex)
    }

    @Test
    @Throws(Exception::class)
    fun multiLeg_distanceRemaining_equalsRouteDistanceAtBeginning() {
        val multiLegRoute = buildMultipleLegRoute()
        val routeProgress = buildBeginningOfLegRouteProgress(multiLegRoute)

        assertEquals(
            multiLegRoute.distance,
            routeProgress.distanceRemaining,
            LARGE_DELTA
        )
    }

    @Test
    @Throws(Exception::class)
    fun multiLeg_distanceRemaining_equalsZeroAtEndOfRoute() {
        val routeProgress = buildEndOfMultiRouteProgress()

        assertEquals(0.0, routeProgress.distanceRemaining, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun multiLeg_fractionTraveled_equalsZeroAtBeginning() {
        val multiLegRoute = buildMultipleLegRoute()
        val routeProgress = buildBeginningOfLegRouteProgress(multiLegRoute)

        assertEquals(0.0, routeProgress.fractionTraveled.toDouble(), LARGE_DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun multiLeg_getFractionTraveled_equalsCorrectValueAtIntervals() {
        val multiLegRoute = buildMultipleLegRoute()
        val fractionsRemaining: MutableList<Float> = ArrayList()
        val routeProgressFractionsTraveled: MutableList<Float> = ArrayList()

        for (leg in multiLegRoute.legs) {
            for (stepIndex in leg.steps.indices) {
                val stepDistanceRemaining = getFirstStep(multiLegRoute).distance
                val legDistanceRemaining = multiLegRoute.legs[0].distance
                val distanceRemaining = multiLegRoute.distance
                val routeProgress = buildTestRouteProgress(
                    multiLegRoute, stepDistanceRemaining,
                    legDistanceRemaining, distanceRemaining, stepIndex, 0
                )
                val fractionRemaining =
                    (routeProgress.distanceTraveled / multiLegRoute.distance).toFloat()

                fractionsRemaining.add(fractionRemaining)
                routeProgressFractionsTraveled.add(routeProgress.fractionTraveled)
            }
        }

        Assert.assertTrue(fractionsRemaining == routeProgressFractionsTraveled)
    }

    @Test
    @Throws(Exception::class)
    fun multiLeg_getFractionTraveled_equalsOneAtEndOfRoute() {
        val routeProgress = buildEndOfMultiRouteProgress()

        assertEquals(1.0, routeProgress.fractionTraveled.toDouble(), DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun multiLeg_getDurationRemaining_equalsRouteDurationAtBeginning() {
        val multiLegRoute = buildMultipleLegRoute()
        val routeProgress = buildBeginningOfLegRouteProgress(multiLegRoute)

        assertEquals(2858.1, routeProgress.durationRemaining, LARGE_DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun multiLeg_getDurationRemaining_equalsZeroAtEndOfRoute() {
        val routeProgress = buildEndOfMultiRouteProgress()

        assertEquals(0.0, routeProgress.durationRemaining, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun multiLeg_getDistanceTraveled_equalsZeroAtBeginning() {
        val multiLegRoute = buildMultipleLegRoute()
        val routeProgress = buildBeginningOfLegRouteProgress(multiLegRoute)

        assertEquals(0.0, routeProgress.distanceTraveled, LARGE_DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun multiLeg_getDistanceTraveled_equalsRouteDistanceAtEndOfRoute() {
        val multiLegRoute = buildMultipleLegRoute()
        val routeProgress = buildEndOfMultiRouteProgress()

        assertEquals(
            multiLegRoute.distance,
            routeProgress.distanceTraveled,
            DELTA
        )
    }

    @Test
    @Throws(Exception::class)
    fun multiLeg_getLegIndex_returnsCurrentLegIndex() {
        val multiLegRoute = buildMultipleLegRoute()
        val routeProgress = buildBeginningOfLegRouteProgress(multiLegRoute)
            .copy(legIndex = 1)

        assertEquals(1, routeProgress.legIndex)
    }

    @Test
    @Throws(Exception::class)
    fun remainingWaypoints_firstLegReturnsTwoWaypoints() {
        val multiLegRoute = buildMultipleLegRoute()
        val routeProgress = buildBeginningOfLegRouteProgress(multiLegRoute)

        assertEquals(2, routeProgress.remainingWaypoints)
    }

    @Throws(Exception::class)
    private fun buildMultipleLegRoute(): DirectionsRoute {
        val fixtureJsonString = loadJsonFixture(MULTI_LEG_ROUTE_FIXTURE)
        val response = json.decodeFromString<DirectionsResponse>(fixtureJsonString)
        return response.routes[0]
    }

    @Throws(Exception::class)
    private fun buildLastRouteProgress(route: DirectionsRoute, firstLeg: RouteLeg): RouteProgress {
        val stepIndex = firstLeg.steps.size - 1
        val step = route.legs[0].steps[stepIndex]
        val legIndex = route.legs.size - 1
        val legDistanceRemaining = route.legs[0].distance
        val stepDistanceRemaining = step.distance

        return buildTestRouteProgress(
            route, stepDistanceRemaining,
            legDistanceRemaining, 0.0, stepIndex, legIndex
        )
    }

    private fun getFirstStep(route: DirectionsRoute): LegStep {
        return route.legs[0].steps[0]
    }

    @Throws(Exception::class)
    private fun buildBeginningOfLegRouteProgress(route: DirectionsRoute): RouteProgress {
        val step = getFirstStep(route)
        val stepDistanceRemaining = step.distance
        val legDistanceRemaining = route.legs[0].distance
        val distanceRemaining = route.distance
        return buildTestRouteProgress(
            route, stepDistanceRemaining, legDistanceRemaining,
            distanceRemaining, 0, 0
        )
    }

    @Throws(Exception::class)
    private fun buildEndOfMultiRouteProgress(): RouteProgress {
        val multiLegRoute = buildMultipleLegRoute()

        val legIndex = multiLegRoute.legs.size - 1
        val stepIndex = multiLegRoute.legs[legIndex].steps.size - 1
        val stepDistanceRemaining = multiLegRoute.legs[0].steps[stepIndex].distance
        val legDistanceRemaining = multiLegRoute.legs[0].distance
        return buildTestRouteProgress(
            multiLegRoute, stepDistanceRemaining,
            legDistanceRemaining, 0.0, stepIndex, legIndex
        )
    }

    companion object {
        private const val MULTI_LEG_ROUTE_FIXTURE = "directions_two_leg_route.json"
    }
}