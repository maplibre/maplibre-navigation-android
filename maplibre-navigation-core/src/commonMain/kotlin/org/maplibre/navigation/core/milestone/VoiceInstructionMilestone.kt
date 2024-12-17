package org.maplibre.navigation.core.milestone

import org.maplibre.navigation.core.instruction.Instruction
import org.maplibre.navigation.core.models.VoiceInstructions
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.utils.RouteUtils
import org.maplibre.navigation.core.navigation.MapLibreNavigation

/**
 * A default milestone that is added to [MapLibreNavigation] when default milestones are enabled.
 *
 * Please note, this milestone has a custom trigger based on location progress along a route.  If you
 * set custom triggers, they will be ignored in favor of this logic.
 */
open class VoiceInstructionMilestone(
    identifier: Int,
    instruction: Instruction? = null,
    trigger: Trigger.Statement? = null,
    private val routeUtils: RouteUtils = RouteUtils()
) : Milestone(identifier, instruction, trigger) {

    private var instructions: VoiceInstructions? = null

    override fun isOccurring(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress
    ): Boolean {
        val stepDistanceRemaining = routeProgress.currentLegProgress
            .currentStepProgress
            .distanceRemaining
        val instructions = routeUtils.findCurrentVoiceInstructions(
            routeProgress.currentLegProgress.currentStep,
            stepDistanceRemaining
        )

        return if (shouldBeVoiced(instructions, stepDistanceRemaining)) {
            this.instructions = instructions
            true
        } else {
            false
        }
    }

    override fun getInstruction(): Instruction {
        return Instruction { routeProgress ->
            instructions?.announcement
                ?: routeProgress.currentLegProgress.currentStep.name
        }
    }

    val ssmlAnnouncement: String?
        /**
         * Provide the SSML instruction that can be used with Mapbox's API Voice.
         *
         *
         * This String will provide special markup denoting how certain portions of the announcement
         * should be pronounced.
         *
         * @return announcement with SSML markup
         * @since 0.8.0
         */
        get() {
            if (instructions == null) {
                return EMPTY_STRING
            }
            return instructions!!.ssmlAnnouncement
        }

    val announcement: String?
        /**
         * Provide the instruction that can be used with Android's TextToSpeech.
         *
         *
         * This string will be in plain text.
         *
         * @return announcement in plain text
         * @since 0.12.0
         */
        get() {
            if (instructions == null) {
                return EMPTY_STRING
            }
            return instructions!!.announcement
        }

    /**
     * Checks if the current instructions are different from the instructions
     * determined by the step distance remaining.
     *
     * @param instructions          the current voice instructions from the list of step instructions
     * @param stepDistanceRemaining the current step distance remaining
     * @return true if time to voice the announcement, false if not
     */
    private fun shouldBeVoiced(
        instructions: VoiceInstructions?,
        stepDistanceRemaining: Double
    ): Boolean {
        val isNewInstruction = this.instructions == null || this.instructions != instructions
        val isValidNewInstruction = instructions != null && isNewInstruction
        return isValidNewInstruction && instructions!!.distanceAlongGeometry >= stepDistanceRemaining
    }

    companion object {
        private const val EMPTY_STRING = ""
    }
}