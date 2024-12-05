package org.maplibre.navigation.android.navigation.v5.utils

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.milestone.BannerInstructionMilestone
import org.maplibre.navigation.android.navigation.v5.models.BannerInstructions
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress


class RouteUtilsTest : BaseTest() {

    private val routeUtils = RouteUtils()

    @Test
    fun isNewRoute_returnsTrueWhenPreviousGeometriesNull() {
        val defaultRouteProgress = buildDefaultTestRouteProgress()

        val isNewRoute = routeUtils.isNewRoute(null, defaultRouteProgress)

        Assert.assertTrue(isNewRoute)
    }

    @Test
    fun isNewRoute_returnsFalseWhenGeometriesEqualEachOther() {
        val previousRouteProgress = buildDefaultTestRouteProgress()

        val isNewRoute = routeUtils.isNewRoute(previousRouteProgress, previousRouteProgress)

        Assert.assertFalse(isNewRoute)
    }

    @Test
    fun isNewRoute_returnsTrueWhenGeometriesDoNotEqual() {
        val aRoute =
            buildTestDirectionsRoute()
        val defaultRouteProgress = buildDefaultTestRouteProgress()
        val previousRouteProgress: RouteProgress = defaultRouteProgress.copy(
            directionsRoute = aRoute.copy(geometry = "vfejnqiv")
        )

        val isNewRoute = routeUtils.isNewRoute(previousRouteProgress, defaultRouteProgress)

        Assert.assertTrue(isNewRoute)
    }

    @Test
    fun isArrivalEvent_returnsTrueWhenManeuverTypeIsArrival_andIsLastInstruction() {
        val route = buildTestDirectionsRoute()
        val first = 0
        val lastInstruction = 1
        val routeLeg = route.legs.first()
        val routeSteps = routeLeg.steps
        val currentStepIndex = routeSteps.size - 2
        val upcomingStepIndex = routeSteps.size - 1
        val currentStep = routeSteps[currentStepIndex]
        val upcomingStep = routeSteps[upcomingStepIndex]
        val routeProgress = buildRouteProgress(
            first,
            route, currentStep, upcomingStep
        )
        val bannerInstructionMilestone = mockk<BannerInstructionMilestone>()
        val currentStepBannerInstructions = currentStep.bannerInstructions
        buildBannerInstruction(
            lastInstruction,
            bannerInstructionMilestone,
            currentStepBannerInstructions!!
        )


        val isArrivalEvent = routeUtils.isArrivalEvent(routeProgress, bannerInstructionMilestone)

        Assert.assertTrue(isArrivalEvent)
    }

    @Test
    fun isArrivalEvent_returnsFalseWhenManeuverTypeIsArrival_andIsNotLastInstruction() {
        val route = buildTestDirectionsRoute()
        val first = 0
        val routeLeg = route.legs.first()
        val routeSteps = routeLeg.steps
        val currentStepIndex = routeSteps.size - 2
        val upcomingStepIndex = routeSteps.size - 1
        val currentStep = routeSteps[currentStepIndex]
        val upcomingStep = routeSteps[upcomingStepIndex]
        val routeProgress = buildRouteProgress(
            first,
            route, currentStep, upcomingStep
        )
        val bannerInstructionMilestone = mockk<BannerInstructionMilestone>()
        val currentStepBannerInstructions = currentStep.bannerInstructions
        buildBannerInstruction(
            first, bannerInstructionMilestone,
            currentStepBannerInstructions!!
        )

        val isArrivalEvent = routeUtils.isArrivalEvent(routeProgress, bannerInstructionMilestone)

        Assert.assertFalse(isArrivalEvent)
    }

    @Test
    fun isArrivalEvent_returnsFalseWhenManeuverTypeIsNotArrival() {
        val route = buildTestDirectionsRoute()
        val routeLeg = route.legs.first()
        val routeSteps = routeLeg.steps
        val currentStep = routeSteps.first()
        val upcomingStep = routeSteps[1]
        val routeProgress = buildRouteProgress(
            0,
            route, currentStep, upcomingStep
        )
        val bannerInstructionMilestone = mockk<BannerInstructionMilestone>()
        val currentStepBannerInstructions = currentStep.bannerInstructions
        buildBannerInstruction(
            0,
            bannerInstructionMilestone,
            currentStepBannerInstructions!!
        )

        val isArrivalEvent = routeUtils.isArrivalEvent(routeProgress, bannerInstructionMilestone)

        Assert.assertFalse(isArrivalEvent)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentBannerInstructions_returnsNullWithCurrentStepEmptyInstructions() {
        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep = routeProgress.currentLegProgress.currentStep.copy(
            bannerInstructions = emptyList()
        )
        val stepDistanceRemaining =
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining

        val currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertNull(currentBannerInstructions)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentBannerInstructions_returnsCorrectCurrentInstruction() {
        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining

        val currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertEquals(currentStep.bannerInstructions?.first(), currentBannerInstructions)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentBannerInstructions_adjustedDistanceRemainingReturnsCorrectInstruction() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 1,
            stepDistanceRemaining = 50.0
        )
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining

        val currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertEquals(currentStep.bannerInstructions!![1], currentBannerInstructions)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentBannerInstructions_adjustedDistanceRemainingRemovesCorrectInstructions() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 1,
            stepDistanceRemaining = 500.0
        )
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining

        val currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertEquals(currentStep.bannerInstructions?.first(), currentBannerInstructions)
    }


    @Test
    @Throws(java.lang.Exception::class)
    fun findCurrentBannerText_returnsCorrectPrimaryBannerText() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 1,
            stepDistanceRemaining = 50.0
        )
        val currentStep = routeProgress.currentLegProgress.currentStep
        val stepDistanceRemaining = routeProgress.currentLegProgress.currentStepProgress.distanceRemaining
        val routeUtils = RouteUtils()

        val currentBannerText = routeUtils.findCurrentBannerText(
            currentStep, stepDistanceRemaining, true
        )

        Assert.assertEquals(currentStep.bannerInstructions!![1].primary, currentBannerText)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun findCurrentBannerText_returnsCorrectSecondaryBannerText() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 1,
            stepDistanceRemaining = 50.0
        )
        val currentStep = routeProgress.currentLegProgress.currentStep
        val stepDistanceRemaining =
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining
        val routeUtils = RouteUtils()

        val currentBannerText = routeUtils.findCurrentBannerText(
            currentStep, stepDistanceRemaining, false
        )

        Assert.assertEquals(currentStep.bannerInstructions!!.get(1).secondary, currentBannerText)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentVoiceInstructions_returnsNullWithCurrentStepEmptyInstructions() {
        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep.copy(
            voiceInstructions = emptyList()
        )
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining

        val voiceInstructions = routeUtils.findCurrentVoiceInstructions(
            currentStep,
            stepDistanceRemaining
        )

        Assert.assertNull(voiceInstructions)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentVoiceInstructions_returnsCorrectInstructionsBeginningOfStepDistanceRemaining() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 1,
            stepDistanceRemaining = 300.0
        )
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining

        val currentVoiceInstructions = routeUtils.findCurrentVoiceInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertEquals(currentStep.voiceInstructions!![1], currentVoiceInstructions)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentVoiceInstructions_returnsCorrectInstructionsNoDistanceTraveled() {
        var routeProgress = buildDefaultTestRouteProgress()
        routeProgress = routeProgress.copy(
            stepIndex = 0,
            stepDistanceRemaining = routeProgress.currentLegProgress.currentStep.distance
        )
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining

        val currentVoiceInstructions = routeUtils.findCurrentVoiceInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertEquals(currentStep.voiceInstructions!![0], currentVoiceInstructions)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentVoiceInstructions_returnsCorrectInstructionsEndOfStepDistanceRemaining() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 1,
            stepDistanceRemaining = 50.0
        )
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining

        val currentVoiceInstructions = routeUtils.findCurrentVoiceInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertEquals(currentStep.voiceInstructions!![2], currentVoiceInstructions)
    }

    @Test
    fun calculateRemainingWaypoints() {
        val options = mockk<RouteOptions> {
            every { coordinates } returns buildCoordinateList()
        }
        val route = mockk<DirectionsRoute> {
            every { routeOptions } returns options
        }

        val routeProgress = mockk<RouteProgress> {
            every { remainingWaypoints } returns 2
            every { directionsRoute } answers { route }
        }

        val remainingWaypoints = routeUtils.calculateRemainingWaypoints(routeProgress)

        Assert.assertEquals(2, remainingWaypoints?.size)
        Assert.assertEquals(
            Point.fromLngLat(7.890, 1.234),
            remainingWaypoints?.first()
        )
        Assert.assertEquals(
            Point.fromLngLat(5.678, 9.012),
            remainingWaypoints?.get(1)
        )
    }

    @Test
    fun calculateRemainingWaypoints_handlesNullOptions() {
        val route = mockk<DirectionsRoute> {
            every { routeOptions } returns null
        }
        val routeProgress = mockk<RouteProgress> {
            every { remainingWaypoints } returns 2
            every { directionsRoute } returns route
        }

        val remainingWaypoints = routeUtils.calculateRemainingWaypoints(routeProgress)

        Assert.assertNull(remainingWaypoints)
    }

    @Test
    fun calculateRemainingWaypointNames() {
        val route = mockk<DirectionsRoute> {
            every { routeOptions } returns mockk {
                every { coordinates } returns buildCoordinateList()
                every { waypointNames } returns "first;second;third;fourth"
            }
        }
        val routeProgress = mockk<RouteProgress> {
            every { remainingWaypoints } returns 2
            every { directionsRoute } returns route
        }

        val remainingWaypointNames = routeUtils.calculateRemainingWaypointNames(routeProgress)

        Assert.assertEquals(3, remainingWaypointNames?.size)
        Assert.assertEquals("first", remainingWaypointNames?.first())
        Assert.assertEquals("third", remainingWaypointNames?.get(1))
        Assert.assertEquals("fourth", remainingWaypointNames?.get(2))
    }

    @Test
    fun calculateRemainingWaypointNames_handlesNullOptions() {
        val route = mockk<DirectionsRoute> {
            every { routeOptions } returns null
        }
        val routeProgress = mockk<RouteProgress> {
            every { remainingWaypoints } returns 2
            every { directionsRoute } returns route
        }

        val remainingWaypointNames = routeUtils.calculateRemainingWaypointNames(routeProgress)

        Assert.assertNull(remainingWaypointNames)
    }

    fun buildRouteProgress(
        first: Int,
        routeValue: DirectionsRoute,
        currentStepValue: LegStep,
        upcomingStepValue: LegStep
    ): RouteProgress {
        return mockk {
            every { directionsRoute } returns routeValue
            every { currentLeg } returns routeValue.legs[first]
            every { currentLegProgress } returns mockk {
                every { currentStep } returns currentStepValue
                every { upComingStep } returns upcomingStepValue
            }
        }
    }

    private fun buildBannerInstruction(
        first: Int,
        bannerInstructionMilestone: BannerInstructionMilestone,
        currentStepBannerInstructions: List<BannerInstructions>
    ) {
        val bannerInstructions = currentStepBannerInstructions[first]
        every { bannerInstructionMilestone.bannerInstructions } returns bannerInstructions
    }

    private fun buildCoordinateList(): List<Point> {
        val coordinates: MutableList<Point> = ArrayList()
        coordinates.add(Point.fromLngLat(1.234, 5.678))
        coordinates.add(Point.fromLngLat(9.012, 3.456))
        coordinates.add(Point.fromLngLat(7.890, 1.234))
        coordinates.add(Point.fromLngLat(5.678, 9.012))
        return coordinates
    }
}