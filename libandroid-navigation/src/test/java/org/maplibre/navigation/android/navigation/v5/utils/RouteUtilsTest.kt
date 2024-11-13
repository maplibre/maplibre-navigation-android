package org.maplibre.navigation.android.navigation.v5.utils

import junit.framework.Assert
import org.junit.Test
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.milestone.BannerInstructionMilestone
import org.maplibre.navigation.android.navigation.v5.models.BannerInstructions
import org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.stepDistanceRemaining
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteLegProgress
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.mockito.Mockito

class RouteUtilsTest : BaseTest() {

    @Test
    fun isNewRoute_returnsTrueWhenPreviousGeometriesNull() {
        val defaultRouteProgress = buildDefaultTestRouteProgress()
        val routeUtils = RouteUtils()

        val isNewRoute = routeUtils.isNewRoute(null, defaultRouteProgress!!)

        Assert.assertTrue(isNewRoute)
    }

    @Test
    fun isNewRoute_returnsFalseWhenGeometriesEqualEachOther() {
        val previousRouteProgress = buildDefaultTestRouteProgress()
        val routeUtils = RouteUtils()

        val isNewRoute =
            routeUtils.isNewRoute(previousRouteProgress, previousRouteProgress!!)

        Assert.assertFalse(isNewRoute)
    }

    @Test
    fun isNewRoute_returnsTrueWhenGeometriesDoNotEqual() {
        val aRoute =
            buildTestDirectionsRoute()
        val defaultRouteProgress = buildDefaultTestRouteProgress()
        val previousRouteProgress: RouteProgress = defaultRouteProgress.copy(
            directionsRoute = aRoute!!.copy(geometry = "vfejnqiv")
        )
        val routeUtils = RouteUtils()

        val isNewRoute =
            routeUtils.isNewRoute(previousRouteProgress, defaultRouteProgress)

        Assert.assertTrue(isNewRoute)
    }

    @Test
    fun isArrivalEvent_returnsTrueWhenManeuverTypeIsArrival_andIsLastInstruction() {
        val route = buildTestDirectionsRoute()
        val first = 0
        val lastInstruction = 1
        val routeLeg = route!!.legs!![first]
        val routeSteps = routeLeg.steps
        val currentStepIndex = routeSteps!!.size - 2
        val upcomingStepIndex = routeSteps.size - 1
        val currentStep = routeSteps[currentStepIndex]
        val upcomingStep = routeSteps[upcomingStepIndex]
        val routeProgress = buildRouteProgress(
            first,
            route, currentStep, upcomingStep
        )
        val bannerInstructionMilestone = Mockito.mock(
            BannerInstructionMilestone::class.java
        )
        val currentStepBannerInstructions = currentStep.bannerInstructions
        buildBannerInstruction(
            lastInstruction, bannerInstructionMilestone,
            currentStepBannerInstructions!!
        )

        val routeUtils = RouteUtils()

        val isArrivalEvent =
            routeUtils.isArrivalEvent(routeProgress, bannerInstructionMilestone)

        Assert.assertTrue(isArrivalEvent)
    }

    @Test
    fun isArrivalEvent_returnsFalseWhenManeuverTypeIsArrival_andIsNotLastInstruction() {
        val route = buildTestDirectionsRoute()
        val first = 0
        val routeLeg = route!!.legs!![first]
        val routeSteps = routeLeg.steps
        val currentStepIndex = routeSteps!!.size - 2
        val upcomingStepIndex = routeSteps.size - 1
        val currentStep = routeSteps[currentStepIndex]
        val upcomingStep = routeSteps[upcomingStepIndex]
        val routeProgress = buildRouteProgress(
            first,
            route, currentStep, upcomingStep
        )
        val bannerInstructionMilestone = Mockito.mock(
            BannerInstructionMilestone::class.java
        )
        val currentStepBannerInstructions = currentStep.bannerInstructions
        buildBannerInstruction(
            first, bannerInstructionMilestone,
            currentStepBannerInstructions!!
        )

        val routeUtils = RouteUtils()

        val isArrivalEvent =
            routeUtils.isArrivalEvent(routeProgress, bannerInstructionMilestone)

        Assert.assertFalse(isArrivalEvent)
    }

    @Test
    fun isArrivalEvent_returnsFalseWhenManeuverTypeIsNotArrival() {
        val route = buildTestDirectionsRoute()
        val first = 0
        val routeLeg = route!!.legs!![first]
        val routeSteps = routeLeg.steps
        val currentStep = routeSteps!![first]
        val upcomingStep = routeSteps[first + 1]
        val routeProgress = buildRouteProgress(
            first,
            route, currentStep, upcomingStep
        )
        val bannerInstructionMilestone = Mockito.mock(
            BannerInstructionMilestone::class.java
        )
        val currentStepBannerInstructions = currentStep.bannerInstructions
        buildBannerInstruction(
            first, bannerInstructionMilestone,
            currentStepBannerInstructions!!
        )

        val routeUtils = RouteUtils()

        val isArrivalEvent =
            routeUtils.isArrivalEvent(routeProgress, bannerInstructionMilestone)

        Assert.assertFalse(isArrivalEvent)
    }

    @Test
    fun isValidRouteProfile_returnsTrueWithDrivingTrafficProfile() {
        val routeProfileDrivingTraffic =
            DirectionsCriteria.PROFILE_DRIVING_TRAFFIC
        val routeUtils = RouteUtils()

        val isValidProfile =
            routeUtils.isValidRouteProfile(routeProfileDrivingTraffic)

        Assert.assertTrue(isValidProfile)
    }

    @Test
    fun isValidRouteProfile_returnsTrueWithDrivingProfile() {
        val routeProfileDriving =
            DirectionsCriteria.PROFILE_DRIVING
        val routeUtils = RouteUtils()

        val isValidProfile = routeUtils.isValidRouteProfile(routeProfileDriving)

        Assert.assertTrue(isValidProfile)
    }

    @Test
    fun isValidRouteProfile_returnsTrueWithCyclingProfile() {
        val routeProfileCycling =
            DirectionsCriteria.PROFILE_CYCLING
        val routeUtils = RouteUtils()

        val isValidProfile = routeUtils.isValidRouteProfile(routeProfileCycling)

        Assert.assertTrue(isValidProfile)
    }

    @Test
    fun isValidRouteProfile_returnsTrueWithWalkingProfile() {
        val routeProfileWalking =
            DirectionsCriteria.PROFILE_WALKING
        val routeUtils = RouteUtils()

        val isValidProfile = routeUtils.isValidRouteProfile(routeProfileWalking)

        Assert.assertTrue(isValidProfile)
    }

    @Test
    fun isValidRouteProfile_returnsFalseWithInvalidProfile() {
        val invalidProfile = "invalid_profile"
        val routeUtils = RouteUtils()

        val isValidProfile = routeUtils.isValidRouteProfile(invalidProfile)

        Assert.assertFalse(isValidProfile)
    }

    @Test
    fun isValidRouteProfile_returnsFalseWithNullProfile() {
        val nullProfile: String? = null
        val routeUtils = RouteUtils()

        val isValidProfile = routeUtils.isValidRouteProfile(nullProfile)

        Assert.assertFalse(isValidProfile)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentBannerInstructions_returnsNullWithNullCurrentStep() {
        val currentStep: LegStep? = null
        val stepDistanceRemaining = 0.0
        val routeUtils = RouteUtils()

        val currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertNull(currentBannerInstructions)
    }

    //TODO fabi755 fix this
//    @Test
//    @Throws(Exception::class)
//    fun findCurrentBannerInstructions_returnsNullWithCurrentStepEmptyInstructions() {
//        val routeProgress = buildDefaultTestRouteProgress()
//        val currentStep: LegStep = routeProgress!!.currentLegProgress!!.currentStep!!
//        val stepDistanceRemaining: Double =
//            routeProgress.currentLegProgress!!.currentStepProgress!!.distanceRemaining
//        val currentInstructions = currentStep.bannerInstructions
//        currentInstructions!!.clear()
//        val routeUtils = RouteUtils()
//
//        val currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
//            currentStep, stepDistanceRemaining
//        )
//
//        Assert.assertNull(currentBannerInstructions)
//    }

    @Test
    @Throws(Exception::class)
    fun findCurrentBannerInstructions_returnsCorrectCurrentInstruction() {
        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep: LegStep = routeProgress!!.currentLegProgress!!.currentStep!!
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress!!.currentStepProgress!!.distanceRemaining
        val routeUtils = RouteUtils()

        val currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertEquals(currentStep.bannerInstructions!![0], currentBannerInstructions)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentBannerInstructions_adjustedDistanceRemainingReturnsCorrectInstruction() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 1,
            stepDistanceRemaining = 50.0
        )
        val currentStep: LegStep = routeProgress.currentLegProgress!!.currentStep!!
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress!!.currentStepProgress!!.distanceRemaining
        val routeUtils = RouteUtils()

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
        val currentStep: LegStep = routeProgress.currentLegProgress!!.currentStep!!
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress!!.currentStepProgress!!.distanceRemaining
        val routeUtils = RouteUtils()

        val currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertEquals(currentStep.bannerInstructions!![0], currentBannerInstructions)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentBannerText_returnsCorrectPrimaryBannerText() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 1,
            stepDistanceRemaining = 50.0
        )
        val currentStep: LegStep = routeProgress.currentLegProgress!!.currentStep!!
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress!!.currentStepProgress!!.distanceRemaining
        val routeUtils = RouteUtils()

        val currentBannerText = routeUtils.findCurrentBannerText(
            currentStep, stepDistanceRemaining, true
        )

        Assert.assertEquals(currentStep.bannerInstructions!![1].primary, currentBannerText)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentBannerText_returnsCorrectSecondaryBannerText() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 1,
            stepDistanceRemaining = 50.0
        )
        val currentStep: LegStep = routeProgress.currentLegProgress!!.currentStep!!
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress!!.currentStepProgress!!.distanceRemaining
        val routeUtils = RouteUtils()

        val currentBannerText = routeUtils.findCurrentBannerText(
            currentStep, stepDistanceRemaining, false
        )

        Assert.assertEquals(currentStep.bannerInstructions!![1].secondary, currentBannerText)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentBannerText_returnsNullWithNullCurrentStep() {
        val currentStep: LegStep? = null
        val stepDistanceRemaining = 0.0
        val routeUtils = RouteUtils()

        val currentBannerText = routeUtils.findCurrentBannerText(
            currentStep, stepDistanceRemaining, false
        )

        Assert.assertNull(currentBannerText)
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentVoiceInstructions_returnsNullWithNullCurrentStep() {
        val currentStep: LegStep? = null
        val stepDistanceRemaining = 0.0
        val routeUtils = RouteUtils()

        val currentVoiceInstructions = routeUtils.findCurrentVoiceInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertNull(currentVoiceInstructions)
    }

    //TODO fabi755
//    @Test
//    @Throws(Exception::class)
//    fun findCurrentVoiceInstructions_returnsNullWithCurrentStepEmptyInstructions() {
//        val routeProgress = buildDefaultTestRouteProgress()
//        val currentStep: LegStep = routeProgress.currentLegProgress!!.currentStep!!
//        val stepDistanceRemaining: Double =
//            routeProgress.currentLegProgress!!.currentStepProgress!!.distanceRemaining
//        val currentInstructions = currentStep.voiceInstructions
//        currentInstructions!!.clear()
//        val routeUtils = RouteUtils()
//
//        val voiceInstructions = routeUtils.findCurrentVoiceInstructions(
//            currentStep, stepDistanceRemaining
//        )
//
//        Assert.assertNull(voiceInstructions)
//    }

    @Test
    @Throws(Exception::class)
    fun findCurrentVoiceInstructions_returnsCorrectInstructionsBeginningOfStepDistanceRemaining() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            stepIndex = 1,
            stepDistanceRemaining = 300.0
        )
        val currentStep: LegStep = routeProgress.currentLegProgress!!.currentStep!!
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress!!.currentStepProgress!!.distanceRemaining
        val routeUtils = RouteUtils()

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
            stepDistanceRemaining = routeProgress.currentLegProgress!!.currentStep!!.distance
        )
        val currentStep: LegStep = routeProgress.currentLegProgress!!.currentStep!!
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress!!.currentStepProgress!!.distanceRemaining
        val routeUtils = RouteUtils()

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
        val currentStep: LegStep = routeProgress.currentLegProgress!!.currentStep!!
        val stepDistanceRemaining: Double =
            routeProgress.currentLegProgress!!.currentStepProgress!!.distanceRemaining
        val routeUtils = RouteUtils()

        val currentVoiceInstructions = routeUtils.findCurrentVoiceInstructions(
            currentStep, stepDistanceRemaining
        )

        Assert.assertEquals(currentStep.voiceInstructions!![2], currentVoiceInstructions)
    }

    @Test
    fun calculateRemainingWaypoints() {
        val route = Mockito.mock(
            DirectionsRoute::class.java
        )
        val routeOptions = Mockito.mock(
            RouteOptions::class.java
        )
        Mockito.`when`(routeOptions.coordinates).thenReturn(buildCoordinateList())
        Mockito.`when`(route.routeOptions).thenReturn(routeOptions)
        val routeProgress = Mockito.mock(RouteProgress::class.java)
        Mockito.`when`(routeProgress.remainingWaypoints).thenReturn(2)
        Mockito.`when`(routeProgress.directionsRoute).thenReturn(route)
        val routeUtils = RouteUtils()

        val remainingWaypoints = routeUtils.calculateRemainingWaypoints(routeProgress)

        Assert.assertEquals(2, remainingWaypoints!!.size)
        Assert.assertEquals(
            Point.fromLngLat(7.890, 1.234),
            remainingWaypoints[0]
        )
        Assert.assertEquals(
            Point.fromLngLat(5.678, 9.012),
            remainingWaypoints[1]
        )
    }

    @Test
    fun calculateRemainingWaypoints_handlesNullOptions() {
        val route = Mockito.mock(
            DirectionsRoute::class.java
        )
        Mockito.`when`(route.routeOptions).thenReturn(null)
        val routeProgress = Mockito.mock(RouteProgress::class.java)
        Mockito.`when`(routeProgress.remainingWaypoints).thenReturn(2)
        Mockito.`when`(routeProgress.directionsRoute).thenReturn(route)
        val routeUtils = RouteUtils()

        val remainingWaypoints = routeUtils.calculateRemainingWaypoints(routeProgress)

        Assert.assertNull(remainingWaypoints)
    }

    @Test
    fun calculateRemainingWaypointNames() {
        val route = Mockito.mock(
            DirectionsRoute::class.java
        )
        val routeOptions = Mockito.mock(
            RouteOptions::class.java
        )
        Mockito.`when`(routeOptions.coordinates).thenReturn(buildCoordinateList())
        Mockito.`when`(routeOptions.waypointNames).thenReturn("first;second;third;fourth")
        Mockito.`when`(route.routeOptions).thenReturn(routeOptions)
        val routeProgress = Mockito.mock(RouteProgress::class.java)
        Mockito.`when`(routeProgress.remainingWaypoints).thenReturn(2)
        Mockito.`when`(routeProgress.directionsRoute).thenReturn(route)
        val routeUtils = RouteUtils()

        val remainingWaypointNames = routeUtils.calculateRemainingWaypointNames(routeProgress)

        Assert.assertEquals(3, remainingWaypointNames!!.size)
        Assert.assertEquals("first", remainingWaypointNames[0])
        Assert.assertEquals("third", remainingWaypointNames[1])
        Assert.assertEquals("fourth", remainingWaypointNames[2])
    }

    @Test
    fun calculateRemainingWaypointNames_handlesNullOptions() {
        val route = Mockito.mock(
            DirectionsRoute::class.java
        )
        Mockito.`when`(route.routeOptions).thenReturn(null)
        val routeProgress = Mockito.mock(RouteProgress::class.java)
        Mockito.`when`(routeProgress.remainingWaypoints).thenReturn(2)
        Mockito.`when`(routeProgress.directionsRoute).thenReturn(route)
        val routeUtils = RouteUtils()

        val remainingWaypointNames = routeUtils.calculateRemainingWaypointNames(routeProgress)

        Assert.assertNull(remainingWaypointNames)
    }

    private fun buildRouteProgress(
        first: Int, route: DirectionsRoute, currentStep: LegStep,
        upcomingStep: LegStep
    ): RouteProgress {
        val routeProgress = Mockito.mock(RouteProgress::class.java)
        val legProgress = Mockito.mock(RouteLegProgress::class.java)
        Mockito.`when`(legProgress.currentStep).thenReturn(currentStep)
        Mockito.`when`(legProgress.upComingStep).thenReturn(upcomingStep)
        Mockito.`when`(routeProgress.currentLegProgress).thenReturn(legProgress)
        Mockito.`when`(routeProgress.directionsRoute).thenReturn(route)
        Mockito.`when`(routeProgress.currentLeg).thenReturn(route.legs!![first])
        return routeProgress
    }

    private fun buildBannerInstruction(
        first: Int, bannerInstructionMilestone: BannerInstructionMilestone,
        currentStepBannerInstructions: List<BannerInstructions>
    ) {
        val bannerInstructions = currentStepBannerInstructions[first]
        Mockito.`when`(bannerInstructionMilestone.bannerInstructions).thenReturn(bannerInstructions)
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