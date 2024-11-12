package org.maplibre.navigation.android.navigation.v5.milestone

import com.google.gson.GsonBuilder
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.milestone.Trigger.gt
import org.maplibre.navigation.android.navigation.v5.models.DirectionsAdapterFactory
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StepMilestoneTest : BaseTest() {
    @Test
    @Throws(Exception::class)
    fun sanity() {
        val routeProgress = buildStepMilestoneRouteProgress()
        val milestone = StepMilestone(
            identifier = 1,
            instruction = null,
            trigger = gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 100.0),
        )

        Assert.assertNotNull(milestone)
        Assert.assertTrue(milestone.isOccurring(routeProgress, routeProgress))
    }

    @Test
    fun identifier_doesEqualSetValue() {
            val milestone =
                StepMilestone.Builder()
                    .setIdentifier(101)
                    .build()

            Assert.assertEquals(101, milestone.identifier)
        }

    @Throws(Exception::class)
    private fun buildStepMilestoneRouteProgress(): RouteProgress {
        val gson = GsonBuilder()
            .registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create()
        val body = loadJsonFixture(ROUTE_FIXTURE)
        val response = gson.fromJson(
            body,
            DirectionsResponse::class.java
        )
        val route = response.routes()[0]
        val distanceRemaining = route.distance()
        val legDistanceRemaining = route.legs()!![0].distance()!!
        val stepDistanceRemaining = route.legs()!![0].steps()!![0].distance()
        return buildTestRouteProgress(
            route, stepDistanceRemaining,
            legDistanceRemaining, distanceRemaining, 1, 0
        )
    }

    companion object {
        private const val ROUTE_FIXTURE = "directions_v5_precision_6.json"
    }
}
