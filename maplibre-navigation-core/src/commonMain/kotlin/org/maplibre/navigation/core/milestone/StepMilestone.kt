package org.maplibre.navigation.core.milestone

import org.maplibre.navigation.core.instruction.Instruction
import org.maplibre.navigation.core.routeprogress.RouteProgress

/**
 * Using a Step Milestone will result in
 * [MilestoneEventListener.onMilestoneEvent]
 * being invoked every step if the condition validation returns true.
 *
 * @since 0.4.0
 */
open class StepMilestone(
    identifier: Int,
    instruction: Instruction? = null,
    trigger: Trigger.Statement? = null,
) : Milestone(identifier, instruction, trigger) {
    private var called = false

    override fun isOccurring(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress
    ): Boolean {
        return trigger?.let { trigger ->
            if (called) {
                // Determine if the step index has changed and set called accordingly. This prevents multiple calls to
                // onMilestoneEvent per Step.
                if (previousRouteProgress?.currentLegProgress?.stepIndex != routeProgress.currentLegProgress.stepIndex
                ) {
                    called = false
                } else {
                    // If milestone's been called already on current step, no need to check triggers.
                    return@let false
                }
            }

            called = trigger.isOccurring(
                TriggerProperty.getSparseArray(
                    previousRouteProgress, routeProgress
                )
            )

            return@let called
        } ?: false
    }
}
