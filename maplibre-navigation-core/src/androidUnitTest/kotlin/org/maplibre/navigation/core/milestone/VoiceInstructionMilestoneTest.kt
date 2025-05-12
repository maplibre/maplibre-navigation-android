package org.maplibre.navigation.core.milestone

import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.models.LegStep
import org.maplibre.navigation.core.models.VoiceInstructions
import org.maplibre.navigation.core.routeprogress.RouteProgress
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class VoiceInstructionMilestoneTest : BaseTest() {

    @Test
    fun sanity() {
        val milestone = buildVoiceInstructionMilestone()

        assertNotNull(milestone)
    }

    @Test
    @Throws(Exception::class)
    fun onBeginningOfStep_voiceInstructionsShouldTrigger() {
        var routeProgress = buildDefaultTestRouteProgress()
        routeProgress = createBeginningOfStepRouteProgress(routeProgress)
        val milestone = buildVoiceInstructionMilestone()

        val isOccurring = milestone.isOccurring(routeProgress, routeProgress)

        assertTrue(isOccurring)
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

        assertFalse(shouldNotBeOccurring)
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

        assertFalse(isOccurring)
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

        assertEquals(instructions.ssmlAnnouncement, milestone.ssmlAnnouncement)
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

        assertEquals(instructions.announcement, milestone.announcement)
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

        assertEquals(
            instructions.announcement,
            milestone.getInstruction().buildInstruction(routeProgress)
        )
    }

    @Test
    @Throws(Exception::class)
    fun onNullMilestoneInstructions_emptyInstructionsAreReturned() {
        val milestone = buildVoiceInstructionMilestone()

        assertEquals("", milestone.announcement)
    }

    @Test
    @Throws(Exception::class)
    fun onNullMilestoneInstructions_emptySsmlInstructionsAreReturned() {
        val milestone = buildVoiceInstructionMilestone()

        assertEquals("", milestone.ssmlAnnouncement)
    }

    @Test
    @Throws(Exception::class)
    fun onNullMilestoneInstructions_stepNameIsReturnedForInstruction() {
        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val milestone = buildVoiceInstructionMilestone()

        assertEquals(
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
