package org.maplibre.navigation.android.navigation.v5.milestone

import org.junit.Assert
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.models.VoiceInstructions
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

class VoiceInstructionMilestoneTest : BaseTest() {
    @Test
    fun sanity() {
        val milestone = buildVoiceInstructionMilestone()

        Assert.assertNotNull(milestone)
    }

    @Test
    @Throws(Exception::class)
    fun onBeginningOfStep_voiceInstructionsShouldTrigger() {
        var routeProgress = buildDefaultTestRouteProgress()
        routeProgress = createBeginningOfStepRouteProgress(routeProgress)
        val milestone = buildVoiceInstructionMilestone()

        val isOccurring = milestone.isOccurring(routeProgress, routeProgress)

        Assert.assertTrue(isOccurring)
    }

    @Test
    @Throws(Exception::class)
    fun onSameInstructionOccurring_milestoneDoesNotTriggerTwice() {
        val routeProgress = buildDefaultTestRouteProgress()
        val firstProgress = createBeginningOfStepRouteProgress(routeProgress)
        val secondProgress: RouteProgress = routeProgress.copy(
            stepDistanceRemaining = routeProgress.currentLegProgress.currentStep.distance - 40,
            stepIndex = 0
        )
        val milestone = buildVoiceInstructionMilestone()

        milestone.isOccurring(firstProgress, firstProgress)
        val shouldNotBeOccurring = milestone.isOccurring(firstProgress, secondProgress)

        Assert.assertFalse(shouldNotBeOccurring)
    }

    @Test
    @Throws(Exception::class)
    fun nullInstructions_doNotGetTriggered() {
        var routeProgress = buildDefaultTestRouteProgress()
        routeProgress = with(createBeginningOfStepRouteProgress(routeProgress)) {
            copy(
                directionsRoute = directionsRoute.copy(
                    legs = directionsRoute.legs.mapIndexed { lIndex, leg ->
                        if (lIndex == legIndex) {
                            leg.copy(steps = leg.steps.mapIndexed { sIndex, step ->
                                if (sIndex == stepIndex) {
                                    step.copy(
                                        voiceInstructions = null
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
        val milestone = buildVoiceInstructionMilestone()

        val isOccurring = milestone.isOccurring(routeProgress, routeProgress)

        Assert.assertFalse(isOccurring)
    }

    @Test
    @Throws(Exception::class)
    fun onOccurringMilestone_voiceSsmlInstructionsAreReturned() {
        var routeProgress = buildDefaultTestRouteProgress()
        routeProgress = createBeginningOfStepRouteProgress(routeProgress)
        val instructions: VoiceInstructions =
            routeProgress.currentLegProgress.currentStep.voiceInstructions!![0]
        val milestone = buildVoiceInstructionMilestone()

        milestone.isOccurring(routeProgress, routeProgress)

        Assert.assertEquals(instructions.ssmlAnnouncement, milestone.ssmlAnnouncement)
    }

    @Test
    @Throws(Exception::class)
    fun onOccurringMilestone_voiceInstructionsAreReturned() {
        var routeProgress = buildDefaultTestRouteProgress()
        routeProgress = createBeginningOfStepRouteProgress(routeProgress)
        val instructions: VoiceInstructions =
            routeProgress.currentLegProgress.currentStep.voiceInstructions!![0]
        val milestone = buildVoiceInstructionMilestone()

        milestone.isOccurring(routeProgress, routeProgress)

        Assert.assertEquals(instructions.announcement, milestone.announcement)
    }

    @Test
    @Throws(Exception::class)
    fun onOccurringMilestone_instructionsAreReturned() {
        var routeProgress = buildDefaultTestRouteProgress()
        routeProgress = createBeginningOfStepRouteProgress(routeProgress)
        val instructions: VoiceInstructions =
            routeProgress.currentLegProgress.currentStep.voiceInstructions!![0]
        val milestone = buildVoiceInstructionMilestone()

        milestone.isOccurring(routeProgress, routeProgress)

        Assert.assertEquals(
            instructions.announcement,
            milestone.getInstruction().buildInstruction(routeProgress)
        )
    }

    @Test
    @Throws(Exception::class)
    fun onNullMilestoneInstructions_emptyInstructionsAreReturned() {
        val milestone = buildVoiceInstructionMilestone()

        Assert.assertEquals("", milestone.announcement)
    }

    @Test
    @Throws(Exception::class)
    fun onNullMilestoneInstructions_emptySsmlInstructionsAreReturned() {
        val milestone = buildVoiceInstructionMilestone()

        Assert.assertEquals("", milestone.ssmlAnnouncement)
    }

    @Test
    @Throws(Exception::class)
    fun onNullMilestoneInstructions_stepNameIsReturnedForInstruction() {
        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val milestone = buildVoiceInstructionMilestone()

        Assert.assertEquals(
            currentStep.name,
            milestone.getInstruction().buildInstruction(routeProgress)
        )
    }

    private fun createBeginningOfStepRouteProgress(routeProgress: RouteProgress): RouteProgress {
        return routeProgress.copy(
            stepDistanceRemaining = routeProgress.currentLegProgress.currentStep.distance,
            stepIndex = 0
        )
    }

    private fun buildVoiceInstructionMilestone(): VoiceInstructionMilestone {
        return VoiceInstructionMilestone(identifier = 1234)
    }
}
