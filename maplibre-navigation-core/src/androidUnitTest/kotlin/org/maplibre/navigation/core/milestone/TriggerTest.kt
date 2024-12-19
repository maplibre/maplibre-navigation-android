package org.maplibre.navigation.core.milestone

import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.milestone.Trigger.all
import org.maplibre.navigation.core.milestone.Trigger.any
import org.maplibre.navigation.core.milestone.Trigger.eq
import org.maplibre.navigation.core.milestone.Trigger.gt
import org.maplibre.navigation.core.milestone.Trigger.gte
import org.maplibre.navigation.core.milestone.Trigger.lt
import org.maplibre.navigation.core.milestone.Trigger.lte
import org.maplibre.navigation.core.milestone.Trigger.neq
import org.maplibre.navigation.core.milestone.Trigger.none
import org.maplibre.navigation.core.models.DirectionsResponse
import org.maplibre.navigation.core.routeprogress.RouteProgress
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TriggerTest : BaseTest() {

    @Test
    @Throws(Exception::class)
    fun triggerAll_noStatementsProvidedResultsInTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = all())

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun triggerAll_validatesAllStatements() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = all(
            gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0),
            eq(TriggerProperty.STEP_INDEX, 1)
        ))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun triggerAll_oneConditionsFalse() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = all(
            gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0),
            eq(TriggerProperty.NEW_STEP, TriggerProperty.FALSE)
        ))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun triggerAny_noConditionsAreTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = any(
            gt(TriggerProperty.STEP_DURATION_REMAINING_SECONDS, 200.0),
            lt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0)
        ))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun triggerAny_validatesAllStatementsTillOnesTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = any(
            eq(TriggerProperty.STEP_INDEX, 1),
            gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0),
            eq(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0)
        ))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun triggerAny_oneConditionsTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = any(
            gt(TriggerProperty.STEP_DURATION_REMAINING_SECONDS, 100.0),
            gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0)
        ))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }


    @Test
    @Throws(Exception::class)
    fun triggerNone_noConditionsAreTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger =none(
            gt(TriggerProperty.STEP_DURATION_REMAINING_SECONDS, 200.0),
            lt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0)
        ))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun triggerNone_validatesAllStatementsTillOnesTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = none(
            neq(TriggerProperty.STEP_INDEX, 1),
            lt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0)
        ))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun triggerNone_onoConditionsTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger =none(
            gt(TriggerProperty.STEP_DURATION_REMAINING_SECONDS, 100.0),
            gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0)
        ) )

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun greaterThan_validatesToTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun greaterThan_validatesToFalse() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 10000.0))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun greaterThanEqual_validatesToTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = gte(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun greaterThanEqual_equalStillValidatesToTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = gte(
            TriggerProperty.STEP_DISTANCE_TOTAL_METERS,
            routeProgress.currentLegProgress.currentStep.distance
        ))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun greaterThanEqual_validatesToFalse() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = gte(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 10000.0))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun lessThan_validatesToTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = lt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 10000.0))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun lessThan_validatesToFalse() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = lt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun lessThanEqual_validatesToTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = lte(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 10000.0))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun lessThanEqual_equalStillValidatesToTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = lte(
            TriggerProperty.STEP_DISTANCE_TOTAL_METERS,
            routeProgress.currentLegProgress.currentStep.distance
        ))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun lessThanEqual_validatesToFalse() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger =  lte(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun equal_validatesToFalse() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = eq(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun equal_validatesToTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger =eq(
            TriggerProperty.STEP_DISTANCE_TOTAL_METERS,
            routeProgress.currentLegProgress.currentStep.distance
        ) )

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun notEqual_validatesToFalse() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = neq(
            TriggerProperty.STEP_DISTANCE_TOTAL_METERS,
            routeProgress.currentLegProgress.currentStep.distance
        ))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun notEqual_validatesToTrue() {
        val routeProgress = buildTriggerRouteProgress()
        val milestone = StepMilestone(identifier = 42, trigger = neq(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0))

        val result = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(result)
    }

    @Throws(Exception::class)
    private fun buildTriggerRouteProgress(): RouteProgress {
        val body = loadJsonFixture(ROUTE_FIXTURE)
        val response = DirectionsResponse.fromJson(body)
        val route = response.routes[0]
        val stepDistanceRemaining = route.legs[0].steps[0].distance.toInt()
        val legDistanceRemaining = route.legs[0].distance.toInt()
        val routeDistance = route.distance.toInt()
        return buildTestRouteProgress(
            route, stepDistanceRemaining.toDouble(), legDistanceRemaining.toDouble(),
            routeDistance.toDouble(), 1, 0
        )
    }

    companion object {
        private const val ROUTE_FIXTURE = "directions_v5_precision_6.json"
    }
}
