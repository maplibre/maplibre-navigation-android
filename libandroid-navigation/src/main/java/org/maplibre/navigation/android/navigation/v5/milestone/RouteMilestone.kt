package org.maplibre.navigation.android.navigation.v5.milestone

import org.maplibre.navigation.android.navigation.v5.instruction.Instruction
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.utils.RouteUtils

/**
 * Using a Route Milestone will result in
 * [MilestoneEventListener.onMilestoneEvent] being invoked only
 * once during a navigation session.
 *
 * @since 0.4.0
 */
open class RouteMilestone(
    identifier: Int,
    instruction: Instruction?,
    trigger: Trigger.Statement?,
) : Milestone(identifier, instruction, trigger) {
    private var called = false

    override fun isOccurring(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress
    ): Boolean {
        if (called) {
            return false
        }

        return trigger?.let { trigger ->
            this@RouteMilestone.called = trigger.isOccurring(
                TriggerProperty.getSparseArray(previousRouteProgress, routeProgress)
            )
            called
        } ?: false
    }
}
