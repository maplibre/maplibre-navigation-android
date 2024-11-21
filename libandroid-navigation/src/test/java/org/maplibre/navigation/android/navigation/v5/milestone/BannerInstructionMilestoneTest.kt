package org.maplibre.navigation.android.navigation.v5.milestone

import org.junit.Assert
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.BannerInstructions
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress


class BannerInstructionMilestoneTest : BaseTest() {
    @Test
    fun sanity() {
        val milestone = buildBannerInstructionMilestone()

        Assert.assertNotNull(milestone)
    }

    @Test
    @Throws(Exception::class)
    fun onBeginningOfStep_bannerInstructionsShouldTrigger() {
        var routeProgress = buildDefaultTestRouteProgress()
        routeProgress = createBeginningOfStepRouteProgress(routeProgress)
        val milestone = buildBannerInstructionMilestone()

        val isOccurring = milestone.isOccurring(routeProgress, routeProgress)

        Assert.assertTrue(isOccurring)
    }

    @Test
    @Throws(Exception::class)
    fun onSameInstructionOccurring_milestoneDoesNotTriggerTwice() {
        val routeProgress = buildDefaultTestRouteProgress()
        val firstProgress = createBeginningOfStepRouteProgress(routeProgress)
        val fortyMetersIntoStep: Double =
            routeProgress.currentLegProgress.currentStep.distance - 40
        val secondProgress: RouteProgress = routeProgress.copy(
            stepDistanceRemaining = fortyMetersIntoStep,
            stepIndex = 0
        )

        val milestone = buildBannerInstructionMilestone()

        milestone.isOccurring(firstProgress, firstProgress)
        val shouldNotBeOccurring = milestone.isOccurring(firstProgress, secondProgress)

        Assert.assertFalse(shouldNotBeOccurring)
    }

    @Test
    @Throws(Exception::class)
    fun nullInstructions_MilestoneDoesNotGetTriggered() {
        var routeProgress = buildDefaultTestRouteProgress()
        routeProgress = with(createBeginningOfStepRouteProgress(routeProgress)) {
            copy(
                directionsRoute = directionsRoute.copy(
                    legs = directionsRoute.legs.mapIndexed { lIndex, leg ->
                        if (lIndex == legIndex) {
                            leg.copy(steps = leg.steps.mapIndexed { sIndex, step ->
                                if (sIndex == stepIndex) {
                                    step.copy(
                                        bannerInstructions = null
                                    )
                                } else {
                                    step
                                }
                            })
                        } else {
                            leg
                        }
                    }
                )
            )
        }
        val milestone = buildBannerInstructionMilestone()

        val isOccurring = milestone.isOccurring(routeProgress, routeProgress)

        Assert.assertFalse(isOccurring)
    }

    @Test
    @Throws(Exception::class)
    fun onOccurringMilestone_beginningOfStep_bannerInstructionsAreReturned() {
        var routeProgress = buildDefaultTestRouteProgress()
        routeProgress = routeProgress.copy(
            stepDistanceRemaining = routeProgress.currentLegProgress.currentStep.distance,
            stepIndex = 1
        )
        val instructions: BannerInstructions =
            routeProgress.currentLegProgress.currentStep.bannerInstructions!![0]
        val milestone = buildBannerInstructionMilestone()

        milestone.isOccurring(routeProgress, routeProgress)

        Assert.assertEquals(instructions, milestone.bannerInstructions)
    }

    @Test
    @Throws(Exception::class)
    fun onOccurringMilestone_endOfStep_bannerInstructionsAreReturned() {
        var routeProgress = buildDefaultTestRouteProgress()
        val tenMetersRemainingInStep = 10.0
        routeProgress = routeProgress.copy(
            stepDistanceRemaining = tenMetersRemainingInStep,
            stepIndex = 1
        )

        val bannerInstructions: List<BannerInstructions> =
            routeProgress.currentLegProgress.currentStep.bannerInstructions!!.toList()
        val instructions = bannerInstructions[bannerInstructions.size - 1]
        val milestone = buildBannerInstructionMilestone()

        milestone.isOccurring(routeProgress, routeProgress)

        Assert.assertEquals(instructions, milestone.bannerInstructions)
    }

    private fun createBeginningOfStepRouteProgress(routeProgress: RouteProgress): RouteProgress {
        return routeProgress.copy(
            stepDistanceRemaining = routeProgress.currentLegProgress.currentStep.distance,
            stepIndex = 0
        )
    }

    private fun buildBannerInstructionMilestone(): BannerInstructionMilestone {
        return BannerInstructionMilestone(identifier = 1234)
    }
}
