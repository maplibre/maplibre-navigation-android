package org.maplibre.navigation.android.navigation.v5.navigation

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.geojson.Point
import org.maplibre.geojson.utils.PolylineUtils
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.milestone.StepMilestone
import org.maplibre.navigation.android.navigation.v5.milestone.Trigger.eq
import org.maplibre.navigation.android.navigation.v5.milestone.TriggerProperty
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.LegAnnotation
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.models.RouteLeg
import org.maplibre.navigation.android.navigation.v5.models.StepIntersection
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.checkMilestones
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.createCurrentAnnotation
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.createDistancesToIntersections
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.createIntersectionsList
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.findCurrentIntersection
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.findUpcomingIntersection
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.increaseIndex
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.isUserOffRoute
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.nextManeuverPosition
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationHelper.stepDistanceRemaining
import org.maplibre.navigation.android.navigation.v5.routeprogress.CurrentLegAnnotation
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteLegProgress
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteStepProgress
import org.maplibre.navigation.android.navigation.v5.utils.Constants
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class NavigationHelperTest : BaseTest() {
    @Test
    @Throws(Exception::class)
    fun increaseIndex_increasesStepByOne() {
        val routeProgress = buildMultiLegRouteProgress()
        val previousIndices = NavigationIndices(0, 0)

        val newIndices = increaseIndex(routeProgress, previousIndices)

        Assert.assertEquals(0, newIndices.legIndex)
        Assert.assertEquals(1, newIndices.stepIndex)
    }

    @Test
    @Throws(Exception::class)
    fun increaseIndex_increasesLegIndex() {
        val multiLegRouteProgress = buildMultiLegRouteProgress()
        val routeProgress: RouteProgress = multiLegRouteProgress.copy(
            legIndex = 0,
            stepIndex = 21
        )
        val previousIndices = NavigationIndices(0, 21)

        val newIndices = increaseIndex(routeProgress, previousIndices)

        Assert.assertEquals(1, newIndices.legIndex)
    }

    @Test
    @Throws(Exception::class)
    fun increaseIndex_stepIndexResetsOnLegIndexIncrease() {
        val multiLegRouteProgress = buildMultiLegRouteProgress()
        val routeProgress: RouteProgress = multiLegRouteProgress.copy(
            legIndex = 0,
            stepIndex = 21
        )
        val previousIndices = NavigationIndices(0, 21)

        val newIndices = increaseIndex(routeProgress, previousIndices)

        Assert.assertEquals(0, newIndices.stepIndex)
    }

    @Test
    @Throws(Exception::class)
    fun checkMilestones_onlyTriggeredMilestonesGetReturned() {
        val routeProgress = buildMultiLegRouteProgress()
        val options = MapLibreNavigationOptions(defaultMilestonesEnabled = false)
        val context = mockk<Context>(relaxed = true) {
            every { applicationContext } returns this
        }
        val mapLibreNavigation = MapLibreNavigation(context, options, mockk())
        mapLibreNavigation.addMilestone(
            StepMilestone(identifier = 1001, trigger = eq(TriggerProperty.STEP_INDEX, 0))
        )
        mapLibreNavigation.addMilestone(
            StepMilestone(identifier = 1002, trigger = eq(TriggerProperty.STEP_INDEX, 4))
        )

        val triggeredMilestones = checkMilestones(
            routeProgress,
            routeProgress, mapLibreNavigation
        )

        Assert.assertEquals(1, triggeredMilestones.size)
        Assert.assertEquals(1001, triggeredMilestones[0].identifier)
        Assert.assertNotSame(1002, triggeredMilestones[0].identifier)
    }

    @Test
    @Throws(Exception::class)
    fun offRouteDetectionDisabled_isOffRouteReturnsFalse() {
        val options = MapLibreNavigationOptions(enableOffRouteDetection = false)
        val context = mockk<Context>(relaxed = true) {
            every { applicationContext } returns this
        }
        val mapLibreNavigation = MapLibreNavigation(context, options, mockk())
        val model = NavigationLocationUpdate(mockk(), mapLibreNavigation)

        val userOffRoute = isUserOffRoute(model, mockk(), mockk())

        Assert.assertFalse(userOffRoute)
    }

    @Test
    @Throws(Exception::class)
    fun stepDistanceRemaining_returnsZeroWhenPositionsEqualEachOther() {
        val route = buildMultiLegRoute()
        val location = buildDefaultLocationUpdate(-77.062996, 38.798405)
        val coordinates = PolylineUtils.decode(
            route.legs[0].steps[1].geometry, Constants.PRECISION_6
        )

        val distance = stepDistanceRemaining(location, 0, 1, route, coordinates)

        Assert.assertEquals(0.0, distance, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun stepDistanceRemaining_returnsFullLengthForLargeDistance() {
        val route = buildMultiLegRoute()
        val location = buildDefaultLocationUpdate(0.0, 0.0)
        val coordinates = PolylineUtils.decode(
            route.legs[0].steps[1].geometry, Constants.PRECISION_6
        )

        val distance = stepDistanceRemaining(location, 0, 1, route, coordinates)

        Assert.assertEquals(25.0, distance, 1.0)
    }

    @Test
    @Throws(Exception::class)
    fun nextManeuverPosition_correctlyReturnsNextManeuverPosition() {
        val route = buildMultiLegRoute()
        val coordinates = PolylineUtils.decode(
            route.legs[0].steps[0].geometry, Constants.PRECISION_6
        )

        val nextManeuver = nextManeuverPosition(
            0,
            route.legs[0].steps, coordinates
        )

        Assert.assertTrue(nextManeuver == route.legs[0].steps[1].maneuver.location)
    }

    @Test
    @Throws(Exception::class)
    fun nextManeuverPosition_correctlyReturnsNextManeuverPositionInNextLeg() {
        val route = buildMultiLegRoute()
        val stepIndex = route.legs[0].steps.size - 1
        val coordinates = PolylineUtils.decode(
            route.legs[0].steps[stepIndex].geometry, Constants.PRECISION_6
        )

        val nextManeuver = nextManeuverPosition(
            stepIndex,
            route.legs[0].steps, coordinates
        )

        Assert.assertTrue(nextManeuver == route.legs[1].steps[0].maneuver.location)
    }

    @Test
    @Throws(Exception::class)
    fun createIntersectionList_returnsCompleteIntersectionList() {
        val routeProgress = buildMultiLegRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val upcomingStep: LegStep = routeProgress.currentLegProgress.upComingStep!!

        val intersections = createIntersectionsList(currentStep, upcomingStep)
        val correctListSize = currentStep.intersections!!.size + 1

        Assert.assertTrue(correctListSize == intersections.size)
    }

    @Test
    @Throws(Exception::class)
    fun createIntersectionList_upcomingStepNull_returnsCurrentStepIntersectionList() {
        val routeProgress = buildMultiLegRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val upcomingStep: LegStep? = null

        val intersections = createIntersectionsList(currentStep, upcomingStep)
        val correctListSize = currentStep.intersections!!.size + 1

        Assert.assertFalse(correctListSize == intersections.size)
    }

    @Test
    @Throws(Exception::class)
    fun createIntersectionDistanceList_samePointsForDistanceCalculationsEqualZero() {
        val routeProgress = buildMultiLegRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val currentStepPoints = PolylineUtils.decode(
            currentStep.geometry, Constants.PRECISION_6
        )
        val currentStepIntersections = currentStep.intersections

        val intersectionDistances = createDistancesToIntersections(
            currentStepPoints, currentStepIntersections!!
        )

        Assert.assertTrue(intersectionDistances.toList()[0].second == 0.0)
    }

    @Test
    @Throws(Exception::class)
    fun createIntersectionDistanceList_intersectionListSizeEqualsDistanceListSize() {
        val routeProgress = buildMultiLegRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val currentStepPoints = PolylineUtils.decode(
            currentStep.geometry, Constants.PRECISION_6
        )
        val currentStepIntersections = currentStep.intersections

        val intersectionDistances = createDistancesToIntersections(
            currentStepPoints, currentStepIntersections!!
        )

        Assert.assertTrue(currentStepIntersections.size == intersectionDistances.size)
    }

    @Test
    @Throws(Exception::class)
    fun createIntersectionDistanceList_emptyStepPointsReturnsEmptyList() {
        val routeProgress = buildMultiLegRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val currentStepPoints: List<Point> = ArrayList()
        val currentStepIntersections = currentStep.intersections

        val intersectionDistances = createDistancesToIntersections(
            currentStepPoints, currentStepIntersections!!
        )

        Assert.assertTrue(intersectionDistances.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun createIntersectionDistanceList_oneStepPointReturnsEmptyList() {
        val routeProgress = buildMultiLegRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val currentStepPoints: MutableList<Point> = ArrayList()
        currentStepPoints.add(Point.fromLngLat(1.0, 1.0))
        val currentStepIntersections = currentStep.intersections

        val intersectionDistances = createDistancesToIntersections(
            currentStepPoints, currentStepIntersections!!
        )

        Assert.assertTrue(intersectionDistances.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun createIntersectionDistanceList_emptyStepIntersectionsReturnsEmptyList() {
        val routeProgress = buildMultiLegRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val currentStepPoints = PolylineUtils.decode(
            currentStep.geometry, Constants.PRECISION_6
        )
        val currentStepIntersections: List<StepIntersection> = ArrayList()

        val intersectionDistances = createDistancesToIntersections(
            currentStepPoints, currentStepIntersections
        )

        Assert.assertTrue(intersectionDistances.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentIntersection_beginningOfStepReturnsFirstIntersection() {
        val routeProgress = buildMultiLegRouteProgress()
        val legProgress: RouteLegProgress = routeProgress.currentLegProgress
        val stepProgress: RouteStepProgress = legProgress.currentStepProgress
        val intersections: List<StepIntersection> = stepProgress.intersections!!
        val intersectionDistances = stepProgress.intersectionDistancesAlongStep!!

        val currentIntersection = findCurrentIntersection(
            intersections, intersectionDistances, 0.0
        )

        Assert.assertTrue(currentIntersection == intersections[0])
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentIntersection_endOfStepReturnsLastIntersection() {
        val routeProgress = buildMultiLegRouteProgress()
        val legProgress: RouteLegProgress = routeProgress.currentLegProgress
        val stepProgress: RouteStepProgress = legProgress.currentStepProgress
        val intersections: List<StepIntersection> = stepProgress.intersections!!
        val intersectionDistances = stepProgress.intersectionDistancesAlongStep!!

        val currentIntersection = findCurrentIntersection(
            intersections, intersectionDistances, legProgress.currentStep.distance
        )

        Assert.assertEquals(currentIntersection, intersections[intersections.size - 1])
    }

    @Test
    @Throws(Exception::class)
    fun findCurrentIntersection_middleOfStepReturnsCorrectIntersection() {
        val routeProgress = buildMultiLegRouteProgress(100.0, 0.0, 0.0, 2, 0)
        val legProgress: RouteLegProgress = routeProgress.currentLegProgress
        val stepProgress: RouteStepProgress = legProgress.currentStepProgress
        val intersections: List<StepIntersection> = stepProgress.intersections!!
        val intersectionDistances = stepProgress.intersectionDistancesAlongStep!!

        val currentIntersection = findCurrentIntersection(
            intersections, intersectionDistances, 130.0
        )

        Assert.assertEquals(currentIntersection, intersections[1])
    }

    @Test
    @Throws(Exception::class)
    fun findUpcomingIntersection_beginningOfStepReturnsSecondIntersection() {
        val routeProgress = buildMultiLegRouteProgress()
        val legProgress: RouteLegProgress = routeProgress.currentLegProgress
        val stepProgress: RouteStepProgress = legProgress.currentStepProgress
        val intersections = stepProgress.intersections!!

        val upcomingIntersection = findUpcomingIntersection(
            intersections, legProgress.upComingStep!!, stepProgress.currentIntersection!!
        )

        Assert.assertTrue(upcomingIntersection == intersections[1])
    }

    @Test
    @Throws(Exception::class)
    fun findUpcomingIntersection_endOfStepReturnsUpcomingStepFirstIntersection() {
        val routeProgress = buildMultiLegRouteProgress()
        val legProgress: RouteLegProgress = routeProgress.currentLegProgress
        val stepProgress: RouteStepProgress = legProgress.currentStepProgress
        val intersections: List<StepIntersection> = stepProgress.intersections!!
        val intersectionDistances = stepProgress.intersectionDistancesAlongStep!!
        val currentIntersection = findCurrentIntersection(
            intersections, intersectionDistances, legProgress.currentStep.distance
        )!!

        val upcomingIntersection = findUpcomingIntersection(
            intersections, legProgress.upComingStep!!, currentIntersection
        )

        assertEquals(legProgress.upComingStep!!.intersections!![0], upcomingIntersection)
    }

    @Test
    @Throws(Exception::class)
    fun findUpcomingIntersection_endOfLegReturnsNullIntersection() {
        val stepIndex = buildMultiLegRoute().legs[1].steps.size - 1
        val routeProgress = buildMultiLegRouteProgress(0.0, 0.0, 0.0, stepIndex, 1)
        val legProgress: RouteLegProgress = routeProgress.currentLegProgress
        val stepProgress: RouteStepProgress = legProgress.currentStepProgress
        val intersections: List<StepIntersection> = stepProgress.intersections!!
        val intersectionDistances =
            stepProgress.intersectionDistancesAlongStep!!
        val currentIntersection = findCurrentIntersection(
            intersections, intersectionDistances, legProgress.currentStep.distance
        )!!

        val upcomingIntersection = findUpcomingIntersection(
            intersections, legProgress.upComingStep, currentIntersection
        )

        Assert.assertEquals(null, upcomingIntersection)
    }

    @Test
    @Throws(Exception::class)
    fun createCurrentAnnotation_nullAnnotationReturnsNull() {
        val currentLegAnnotation = createCurrentAnnotation(
            null, mockk(relaxed = true), 0.0
        )

        Assert.assertEquals(null, currentLegAnnotation)
    }

    @Test
    @Throws(Exception::class)
    fun createCurrentAnnotation_emptyDistanceArrayReturnsNull() {
        val currentLegAnnotation = buildCurrentAnnotation()
        val routeLeg = buildRouteLegWithAnnotation()

        val newLegAnnotation = createCurrentAnnotation(
            currentLegAnnotation, routeLeg, 0.0
        )

        Assert.assertEquals(null, newLegAnnotation)
    }

    @Test
    @Throws(Exception::class)
    fun createCurrentAnnotation_beginningOfStep_correctAnnotationIsReturned() {
        val routeProgress = buildDistanceCongestionAnnotationRouteProgress(0.0, 0.0, 0.0, 0, 0)
        val legDistanceRemaining: Double = routeProgress.currentLeg.distance

        val newLegAnnotation = createCurrentAnnotation(
            null, routeProgress.currentLeg, legDistanceRemaining
        )

        Assert.assertEquals("moderate", newLegAnnotation!!.congestion)
    }

    @Test
    @Throws(Exception::class)
    fun createCurrentAnnotation_midStep_correctAnnotationIsReturned() {
        val routeProgress = buildDistanceCongestionAnnotationRouteProgress(0.0, 0.0, 0.0, 0, 0)
        val legDistanceRemaining: Double = routeProgress.currentLeg.distance / 2

        val newLegAnnotation = createCurrentAnnotation(
            null, routeProgress.currentLeg, legDistanceRemaining
        )

        Assert.assertTrue(newLegAnnotation!!.distanceToAnnotation < legDistanceRemaining)
        Assert.assertEquals("heavy", newLegAnnotation.congestion)
    }

    @Test
    @Throws(Exception::class)
    fun createCurrentAnnotation_usesCurrentLegAnnotationForPriorDistanceTraveled() {
        val routeProgress = buildDistanceCongestionAnnotationRouteProgress(0.0, 0.0, 0.0, 0, 0)
        val legDistanceRemaining: Double = routeProgress.currentLeg.distance / 2
        val previousAnnotationDistance: Double = routeProgress.currentLeg.distance / 3
        val currentLegAnnotation = CurrentLegAnnotation(
            distance = 100.0,
            distanceToAnnotation = previousAnnotationDistance,
            index = 0,
            congestion = null,
            maxSpeed = null,
            speed = null,
            duration = null
        )

        val newLegAnnotation = createCurrentAnnotation(
            currentLegAnnotation, routeProgress.currentLeg, legDistanceRemaining
        )

        assertEquals(11, newLegAnnotation!!.index)
    }

    @Throws(Exception::class)
    private fun buildMultiLegRouteProgress(
        stepDistanceRemaining: Double, legDistanceRemaining: Double,
        distanceRemaining: Double, stepIndex: Int, legIndex: Int
    ): RouteProgress {
        val multiLegRoute = buildMultiLegRoute()
        return buildTestRouteProgress(
            multiLegRoute, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, stepIndex, legIndex
        )
    }

    @Throws(Exception::class)
    private fun buildDistanceCongestionAnnotationRouteProgress(
        stepDistanceRemaining: Double,
        legDistanceRemaining: Double,
        distanceRemaining: Double,
        stepIndex: Int,
        legIndex: Int
    ): RouteProgress {
        val annotatedRoute = buildDistanceCongestionAnnotationRoute()
        return buildTestRouteProgress(
            annotatedRoute, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, stepIndex, legIndex
        )
    }

    @Throws(Exception::class)
    private fun buildMultiLegRouteProgress(): RouteProgress {
        val multiLegRoute = buildMultiLegRoute()
        return buildTestRouteProgress(multiLegRoute, 1000.0, 1000.0, 1000.0, 0, 0)
    }

    @Throws(IOException::class)
    private fun buildMultiLegRoute(): DirectionsRoute {
        val body = loadJsonFixture(MULTI_LEG_ROUTE_FIXTURE)
        val response = DirectionsResponse.fromJson(body)
        return response.routes[0]
    }

    @Throws(IOException::class)
    private fun buildDistanceCongestionAnnotationRoute(): DirectionsRoute {
        val body = loadJsonFixture(ANNOTATED_DISTANCE_CONGESTION_ROUTE_FIXTURE)
        val response = DirectionsResponse.fromJson(body)
        return response.routes[0]
    }

    private fun buildCurrentAnnotation(): CurrentLegAnnotation {
        return CurrentLegAnnotation(
            distance = 54.0,
            distanceToAnnotation = 100.0,
            index = 1,
            congestion = "severe",
            maxSpeed = null,
            speed = null,
            duration = null
        )
    }

    private fun buildRouteLegWithAnnotation(): RouteLeg {
        val legAnnotation = LegAnnotation(
            distance = listOf(),
            duration = null,
            speed = null,
            maxSpeed = null,
            congestion = null
        )
        val routeLeg = mockk<RouteLeg>(relaxed = true) {
            every { annotation } returns legAnnotation
        }
        return routeLeg
    }

    companion object {
        private const val MULTI_LEG_ROUTE_FIXTURE = "directions_two_leg_route.json"
        private const val ANNOTATED_DISTANCE_CONGESTION_ROUTE_FIXTURE =
            "directions_distance_congestion_annotation.json"
    }
}