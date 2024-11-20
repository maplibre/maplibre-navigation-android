package org.maplibre.navigation.android.navigation.v5.routeprogress

import org.junit.Assert
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.models.RouteLeg

class RouteLegProgressTest : BaseTest() {
    @Test
    @Throws(Exception::class)
    fun sanityTest() {
        val routeProgress = buildDefaultTestRouteProgress()

        Assert.assertNotNull(routeProgress.currentLegProgress)
    }

    @Test
    @Throws(Exception::class)
    fun upComingStep_returnsNextStepInLeg() {
        val stepIndex = 5
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = stepIndex
        )
        val steps: List<LegStep> = routeProgress.currentLeg.steps

        val upComingStep: LegStep = routeProgress.currentLegProgress.upComingStep!!
        val upComingStepIndex = steps.indexOf(upComingStep)

        Assert.assertEquals(stepIndex + 1, upComingStepIndex)
    }

    @Test
    @Throws(Exception::class)
    fun upComingStep_returnsNull() {
        val routeProgress = buildDefaultTestRouteProgress()
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]

        val upComingStep = findUpcomingStep(
            routeProgress, firstLeg
        )

        Assert.assertNull(upComingStep)
    }

    @Test
    @Throws(Exception::class)
    fun currentStep_returnsCurrentStep() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 5
        )
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]


        assertEquals(
            firstLeg.steps[5].geometry,
            routeProgress.currentLegProgress.currentStep.geometry
        )
        Assert.assertNotSame(
            firstLeg.steps[6].geometry,
            routeProgress.currentLegProgress.currentStep.geometry
        )
    }

    @Test
    @Throws(Exception::class)
    fun previousStep_returnsPreviousStep() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 5
        )
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]


        assertEquals(
            firstLeg.steps[4].geometry,
            routeProgress.currentLegProgress.previousStep!!.geometry
        )
        Assert.assertNotSame(
            firstLeg.steps[5].geometry,
            routeProgress.currentLegProgress.previousStep!!.geometry
        )
    }

    @Test
    @Throws(Exception::class)
    fun stepIndex_returnsCurrentStepIndex() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 3
        )

        assertEquals(3.0, routeProgress.currentLegProgress.stepIndex.toDouble(), DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun fractionTraveled_equalsZeroAtBeginning() {
        val routeProgress = buildBeginningOfLegRouteProgress()

        assertEquals(
            0.0,
            routeProgress.currentLegProgress.fractionTraveled.toDouble(),
            DELTA
        )
    }

    @Test
    @Throws(Exception::class)
    fun fractionTraveled_equalsCorrectValueAtIntervals() {
        val routeProgress = buildDefaultTestRouteProgress()
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val stepSegmentsInMeters = 5000.0
        val fractionsRemaining: MutableList<Float> = ArrayList()
        val routeProgressFractionsTraveled: MutableList<Float> = ArrayList()

        var i = 0.0
        while (i < firstLeg.distance) {
            val fractionRemaining = (routeProgress.currentLegProgress
                .distanceTraveled / firstLeg.distance).toFloat()
            fractionsRemaining.add(fractionRemaining)
            routeProgressFractionsTraveled.add(
                routeProgress.currentLegProgress.fractionTraveled
            )
            i += stepSegmentsInMeters
        }

        Assert.assertTrue(fractionsRemaining == routeProgressFractionsTraveled)
    }

    @Test
    @Throws(Exception::class)
    fun fractionTraveled_equalsOneAtEndOfLeg() {
        val routeProgress = buildEndOfLegRouteProgress()

        assertEquals(
            1.0,
            routeProgress.currentLegProgress.fractionTraveled.toDouble(),
            DELTA
        )
    }

    @Test
    @Throws(Exception::class)
    fun distanceRemaining_equalsLegDistanceAtBeginning() {
        val routeProgress = buildBeginningOfLegRouteProgress()
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]

        assertEquals(
            firstLeg.distance, routeProgress.currentLegProgress.distanceRemaining,
            LARGE_DELTA
        )
    }

    @Test
    @Throws(Exception::class)
    fun distanceRemaining_equalsZeroAtEndOfLeg() {
        val routeProgress = buildEndOfLegRouteProgress()

        assertEquals(
            0.0,
            routeProgress.currentLegProgress.distanceRemaining,
            DELTA
        )
    }

    @Test
    @Throws(Exception::class)
    fun distanceTraveled_equalsZeroAtBeginning() {
        val routeProgress = buildBeginningOfLegRouteProgress()

        assertEquals(
            0.0,
            routeProgress.currentLegProgress.distanceTraveled,
            DELTA
        )
    }

    @Test
    fun distanceTraveled_equalsLegDistanceAtEndOfLeg() {
            val routeProgress = buildEndOfLegRouteProgress()
            val route =
                buildTestDirectionsRoute()
            val firstLeg =
                route.legs[0]

            val firstLegDistance = firstLeg.distance
            val distanceTraveled: Double =
                routeProgress.currentLegProgress.distanceTraveled

            Assert.assertEquals(
                firstLegDistance,
                distanceTraveled,
                DELTA
            )
        }

    @Test
    fun durationRemaining_equalsLegDurationAtBeginning() {
            val routeProgress = buildBeginningOfLegRouteProgress()
            val route: DirectionsRoute =
                routeProgress.directionsRoute
            val firstLeg =
                route.legs[0]

            val firstLegDuration = firstLeg.duration
            val currentLegDurationRemaining: Double =
                routeProgress.currentLegProgress.durationRemaining

            Assert.assertEquals(
                firstLegDuration,
                currentLegDurationRemaining,
                DELTA
            )
        }

    @Test
    fun durationRemaining_equalsZeroAtEndOfLeg() {
            val routeProgress = buildEndOfLegRouteProgress()

            assertEquals(
                0.0,
                routeProgress.currentLegProgress.durationRemaining,
                DELTA
            )
        }

    @Test
    @Throws(Exception::class)
    fun followOnStep_doesReturnTwoStepsAheadOfCurrent() {
        val stepIndex = 5
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = stepIndex
        )
        val steps: List<LegStep> = routeProgress.directionsRoute.legs[0].steps

        val followOnStep: LegStep = routeProgress.currentLegProgress.followOnStep!!
        val followOnIndex = steps.indexOf(followOnStep)

        Assert.assertEquals(stepIndex + 2, followOnIndex)
    }

    @Test
    @Throws(Exception::class)
    fun followOnStep_returnsNull() {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val lastStepIndex = firstLeg.steps.size - 1
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = lastStepIndex
        )

        Assert.assertNull(routeProgress.currentLegProgress.followOnStep)
    }

    @Throws(Exception::class)
    private fun buildBeginningOfLegRouteProgress(): RouteProgress {
        val route = buildTestDirectionsRoute()
        val stepDistanceRemaining = route.legs[0].steps[0].distance
        val legDistanceRemaining = route.legs[0].distance
        val routeDistance = route.distance
        return buildTestRouteProgress(
            route, stepDistanceRemaining, legDistanceRemaining,
            routeDistance, 0, 0
        )
    }

    @Throws(Exception::class)
    private fun buildEndOfLegRouteProgress(): RouteProgress {
        val route = buildTestDirectionsRoute()
        val firstLeg = route.legs[0]
        val lastStepIndex = firstLeg.steps.size - 1
        return buildTestRouteProgress(route, 0.0, 0.0, 0.0, lastStepIndex, 0)
    }

    private fun findUpcomingStep(routeProgress: RouteProgress, firstLeg: RouteLeg): LegStep? {
        val lastStepIndex = firstLeg.steps.size - 1
        return routeProgress.copy(stepIndex = lastStepIndex)
            .currentLegProgress.upComingStep
    }
}
