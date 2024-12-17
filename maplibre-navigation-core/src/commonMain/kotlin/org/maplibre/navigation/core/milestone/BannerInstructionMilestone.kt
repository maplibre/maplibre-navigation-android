package org.maplibre.navigation.core.milestone

import org.maplibre.navigation.core.instruction.Instruction
import org.maplibre.navigation.core.models.BannerInstructions
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.utils.RouteUtils
import org.maplibre.navigation.core.navigation.MapLibreNavigation

/**
 * A default milestone that is added to [MapLibreNavigation]
 * when default milestones are enabled.
 *
 *
 * Please note, this milestone has a custom trigger based on location progress along a route.  If you
 * set custom triggers, they will be ignored in favor of this logic.
 */
open class BannerInstructionMilestone(
    identifier: Int,
    instruction: Instruction? = null,
    trigger: Trigger.Statement? = null,
    private val routeUtils: RouteUtils = RouteUtils()
) : Milestone(identifier, instruction, trigger) {

    /**
     * Returns the given [BannerInstructions] for the time that the milestone is triggered.
     *
     * @return current banner instructions based on distance along the current step
     * @since 0.13.0
     */
    var bannerInstructions: BannerInstructions? = null
        private set

    override fun isOccurring(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress
    ): Boolean {
        val stepDistanceRemaining =
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining
        val instructions = routeUtils.findCurrentBannerInstructions(
            routeProgress.currentLegProgress.currentStep,
            stepDistanceRemaining
        )

        return if (shouldBeShown(instructions, stepDistanceRemaining)) {
            this.bannerInstructions = instructions
            true
        } else {
            false
        }
    }

    /**
     * Uses the current step distance remaining to check against banner instructions distance.
     *
     * @param instructions          given banner instructions from the list of step instructions
     * @param stepDistanceRemaining distance remaining along the current step
     * @return true if time to show the instructions, false if not
     */
    private fun shouldBeShown(
        instructions: BannerInstructions?,
        stepDistanceRemaining: Double
    ): Boolean {
        val isNewInstruction =
            this.bannerInstructions == null || this.bannerInstructions != instructions
        val isValidNewInstruction = instructions != null && isNewInstruction
        val withinDistanceAlongGeometry = isValidNewInstruction
                && instructions!!.distanceAlongGeometry >= stepDistanceRemaining
        val isFirstInstruction = this.bannerInstructions == null && instructions != null
        return isFirstInstruction || withinDistanceAlongGeometry
    }
}