package org.maplibre.navigation.android.navigation.v5.milestone

import org.maplibre.navigation.android.navigation.v5.exception.NavigationException
import org.maplibre.navigation.android.navigation.v5.instruction.Instruction
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

/**
 * Using a Route Milestone will result in
 * [MilestoneEventListener.onMilestoneEvent] being invoked only
 * once during a navigation session.
 *
 * @since 0.4.0
 */
class RouteMilestone(
    identifier: Int,
    instruction: Instruction?,
    trigger: Trigger.Statement?
) : Milestone(identifier, instruction, trigger) {
    private var called = false

    @Deprecated(
        "Use constructor with named arguments.",
        replaceWith = ReplaceWith("RouteMilestone(identifier, instruction, trigger)")
    )
    constructor(builder: Builder) : this(
        builder.identifier,
        builder.instruction,
        builder.trigger
    )

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

    /**
     * Build a new [RouteMilestone]
     *
     * @since 0.4.0
     */
    @Deprecated(
        "Use RouteMilestone constructor with named arguments to create instance.",
        replaceWith = ReplaceWith("RouteMilestone(identifier, instruction, trigger)")
    )
    class Builder : Milestone.Builder() {

        @Throws(NavigationException::class)
        override fun build(): RouteMilestone {
            return RouteMilestone(this)
        }
    }
}
