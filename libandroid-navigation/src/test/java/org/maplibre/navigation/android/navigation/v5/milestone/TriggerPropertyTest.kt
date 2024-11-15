package org.maplibre.navigation.android.navigation.v5.milestone

import com.google.gson.GsonBuilder
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.milestone.Trigger.eq
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.robolectric.RobolectricTestRunner
import kotlin.math.abs

//TODO fabi755, fix tests, after updated JSON parsing
//@RunWith(RobolectricTestRunner::class)
//class TriggerPropertyTest : BaseTest() {
//    @Test
//    @Throws(Exception::class)
//    fun stepDurationRemainingProperty_onlyPassesValidationWhenEqual() {
//        val routeProgress = buildTestRouteProgressForTrigger()
//        val stepDuration: Double =
//            routeProgress!!.currentLegProgress!!.currentStepProgress!!.durationRemaining!!
//
//        for (i in 10 downTo 1) {
//            val milestone = StepMilestone.Builder()
//                .setTrigger(
//                    eq(TriggerProperty.STEP_DURATION_REMAINING_SECONDS, (stepDuration / i))
//                ).build()
//
//            val result = milestone.isOccurring(routeProgress, routeProgress)
//            if ((stepDuration / i) == stepDuration) {
//                Assert.assertTrue(result)
//            } else {
//                Assert.assertFalse(result)
//            }
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun stepDistanceRemainingProperty_onlyPassesValidationWhenEqual() {
//        val routeProgress = buildTestRouteProgressForTrigger()
//        val stepDistance: Double =
//            routeProgress!!.currentLegProgress!!.currentStepProgress!!.distanceRemaining!!
//
//        for (i in 10 downTo 1) {
//            val milestone = StepMilestone.Builder()
//                .setTrigger(
//                    eq(TriggerProperty.STEP_DISTANCE_REMAINING_METERS, (stepDistance / i))
//                ).build()
//
//            val result = milestone.isOccurring(routeProgress, routeProgress)
//            if ((stepDistance / i) == stepDistance) {
//                Assert.assertTrue(result)
//            } else {
//                Assert.assertFalse(result)
//            }
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun stepDistanceTotalProperty_onlyPassesValidationWhenEqual() {
//        val routeProgress = buildTestRouteProgressForTrigger()
//        val stepDistanceTotal: Double =
//            routeProgress!!.currentLegProgress!!.currentStep!!.distance
//
//        for (i in 10 downTo 1) {
//            val milestone = StepMilestone.Builder()
//                .setTrigger(
//                    eq(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, (stepDistanceTotal / i))
//                ).build()
//
//            val result = milestone.isOccurring(routeProgress, routeProgress)
//            if ((stepDistanceTotal / i) == stepDistanceTotal) {
//                Assert.assertTrue(result)
//            } else {
//                Assert.assertFalse(result)
//            }
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun stepDurationTotalProperty_onlyPassesValidationWhenEqual() {
//        val routeProgress = buildTestRouteProgressForTrigger()
//        val stepDurationTotal: Double =
//            routeProgress!!.currentLegProgress!!.currentStep!!.duration
//
//        for (i in 10 downTo 1) {
//            val milestone = StepMilestone.Builder()
//                .setTrigger(
//                    eq(TriggerProperty.STEP_DURATION_TOTAL_SECONDS, (stepDurationTotal / i))
//                ).build()
//
//            val result = milestone.isOccurring(routeProgress, routeProgress)
//            if ((stepDurationTotal / i) == stepDurationTotal) {
//                Assert.assertTrue(result)
//            } else {
//                Assert.assertFalse(result)
//            }
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun stepIndexProperty_onlyPassesValidationWhenEqual() {
//        val routeProgress = buildTestRouteProgressForTrigger()
//        val stepIndex: Int = routeProgress!!.currentLegProgress!!.stepIndex
//
//        for (i in 10 downTo 1) {
//            val milestone = StepMilestone(
//                identifier = 1,
//                instruction = null,
//                trigger = eq(TriggerProperty.STEP_INDEX, abs((stepIndex - i)))
//            )
//
//            val result = milestone.isOccurring(routeProgress, routeProgress)
//            if (abs((stepIndex - i)) == stepIndex) {
//                Assert.assertTrue(result)
//            } else {
//                Assert.assertFalse(result)
//            }
//        }
//    }
//
//    @Throws(Exception::class)
//    private fun buildTestRouteProgressForTrigger(): RouteProgress {
//        val gson = GsonBuilder()
//            .registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create()
//        val body = loadJsonFixture(ROUTE_FIXTURE)
//        val response = gson.fromJson(
//            body,
//            DirectionsResponse::class.java
//        )
//
//        val route = response.routes()[0]
//        val distanceRemaining = route.distance()
//        val legDistanceRemaining = route.legs()!![0].distance()!!
//        val stepDistanceRemaining = route.legs()!![0].steps()!![0].distance()
//        return buildTestRouteProgress(
//            route, stepDistanceRemaining,
//            legDistanceRemaining, distanceRemaining, 1, 0
//        )
//    }
//
//    companion object {
//        private const val ROUTE_FIXTURE = "directions_v5_precision_6.json"
//    }
//}