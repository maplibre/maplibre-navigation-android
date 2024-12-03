package org.maplibre.navigation.android.navigation.v5.milestone

import org.maplibre.navigation.android.navigation.v5.instruction.Instruction
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigation
import org.maplibre.navigation.android.navigation.v5.exception.NavigationException

/**
 * Base Milestone statement. Subclassed to provide concrete statements.
 *
 * @since 0.4.0
 */
abstract class Milestone(
    val identifier: Int,
    instruction: Instruction? = null,
    val trigger: Trigger.Statement? = null
) {

    private val internalInstruction = instruction

    /**
     * A milestone can either be passed in to the
     * [MapLibreNavigation] object
     * (recommended) or validated directly inside your activity.
     *
     * @param previousRouteProgress last locations generated [RouteProgress] object used to
     *  determine certain [TriggerProperty]s
     * @param routeProgress         used to determine certain [TriggerProperty]s
     * @return true if the milestone trigger's valid, else false
     * @since 0.4.0
     */
    abstract fun isOccurring(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress
    ): Boolean

    open fun getInstruction(): Instruction? {
        return internalInstruction
    }
}
