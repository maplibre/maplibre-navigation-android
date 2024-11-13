package org.maplibre.navigation.android.navigation.v5.milestone

import org.maplibre.navigation.android.navigation.v5.exception.NavigationException
import org.maplibre.navigation.android.navigation.v5.instruction.Instruction
import org.maplibre.navigation.android.navigation.v5.models.BannerInstructions
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
class BannerInstructionMilestone(
    identifier: Int,
    instruction: Instruction?,
    trigger: Trigger.Statement?
) : Milestone(identifier, instruction, trigger) {

    @Deprecated(
        "Use constructor with named arguments.",
        replaceWith = ReplaceWith("BannerInstructionMilestone(identifier, instruction, trigger)")
    )
    constructor(builder: Builder) : this(
        builder.identifier,
        builder.instruction,
        builder.trigger
    )

    /**
     * Returns the given [BannerInstructions] for the time that the milestone is triggered.
     *
     * @return current banner instructions based on distance along the current step
     * @since 0.13.0
     */
    var bannerInstructions: BannerInstructions? = null
        private set

    private val routeUtils = RouteUtils()

    override fun isOccurring(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress
    ): Boolean {
        return routeProgress.currentLegProgress?.let { legProgress ->
            legProgress.currentStepProgress?.distanceRemaining?.let currentStepLet@{ stepDistanceRemaining ->
                val instructions = routeUtils.findCurrentBannerInstructions(
                    legProgress.currentStep,
                    stepDistanceRemaining
                )

                return@currentStepLet if (shouldBeShown(instructions, stepDistanceRemaining)) {
                    this.bannerInstructions = instructions
                    true
                } else {
                    false
                }
            } ?: false
        } ?: false
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

    /**
     * Build a new [BannerInstructionMilestone]
     *
     * @since 0.4.0
     */
    @Deprecated(
        "Use BannerInstructionMilestone constructor with named arguments to create instance.",
        replaceWith = ReplaceWith("BannerInstructionMilestone(identifier, instruction, trigger)")
    )
    class Builder : Milestone.Builder() {

        @Throws(NavigationException::class)
        override fun build(): BannerInstructionMilestone {
            return BannerInstructionMilestone(this)
        }
    }
}