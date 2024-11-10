package org.maplibre.navigation.android.navigation.v5.milestone

import org.maplibre.navigation.android.navigation.v5.exception.NavigationException
import org.maplibre.navigation.android.navigation.v5.instruction.Instruction
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.VoiceInstructions
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.utils.RouteUtils

/**
 * A default milestone that is added to [MapLibreNavigation]
 * when default milestones are enabled.
 *
 *
 * Please note, this milestone has a custom trigger based on location progress along a route.  If you
 * set custom triggers, they will be ignored in favor of this logic.
 */
class VoiceInstructionMilestone(
    identifier: Int,
    instruction: Instruction?,
    trigger: Trigger.Statement?
) : Milestone(identifier, instruction, trigger) {

    @Deprecated("Use constructor with named arguments.", replaceWith = ReplaceWith("VoiceInstructionMilestone(identifier, instruction, trigger)"))
    constructor(builder: Builder) : this(
        builder.identifier,
        builder.instruction,
        builder.trigger
    )

    private var instructions: VoiceInstructions? = null
    private var currentRoute: DirectionsRoute? = null
    private val routeUtils = RouteUtils()

    override fun isOccurring(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress
    ): Boolean {
        val currentStep = routeProgress.currentLegProgress().currentStep()
        val stepDistanceRemaining =
            routeProgress.currentLegProgress().currentStepProgress().distanceRemaining()
        val instructions =
            routeUtils.findCurrentVoiceInstructions(currentStep, stepDistanceRemaining)
        if (shouldBeVoiced(instructions, stepDistanceRemaining)) {
            return updateInstructions(routeProgress, instructions)
        }
        return false
    }

    //TODO fabi755, keep this or change param/function name?!
    override val instruction: Instruction
        get() = object :
            Instruction() {
            override fun buildInstruction(routeProgress: RouteProgress): String {
                if (instructions == null) {
                    return routeProgress.currentLegProgress().currentStep().name()!!
                }
                return instructions!!.announcement()!!
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
            return instructions!!.ssmlAnnouncement()
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
            return instructions!!.announcement()
        }

    /**
     * Looks to see if we have a new route.
     *
     * @param routeProgress provides updated route information
     * @return true if new route, false if not
     */
    private fun isNewRoute(routeProgress: RouteProgress): Boolean {
        val newRoute = currentRoute == null || currentRoute != routeProgress.directionsRoute()
        currentRoute = routeProgress.directionsRoute()
        return newRoute
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
        return isValidNewInstruction && instructions!!.distanceAlongGeometry()!! >= stepDistanceRemaining
    }

    private fun updateInstructions(
        routeProgress: RouteProgress,
        instructions: VoiceInstructions?
    ): Boolean {
        this.instructions = instructions
        return true
    }

    @Deprecated("Use RouteMilestone constructor with named arguments to create instance.", replaceWith = ReplaceWith("VoiceInstructionMilestone(identifier, instruction, trigger)"))
    class Builder : Milestone.Builder() {

        @Throws(NavigationException::class)
        override fun build(): VoiceInstructionMilestone {
            return VoiceInstructionMilestone(this)
        }
    }

    companion object {
        private const val EMPTY_STRING = ""
    }
}