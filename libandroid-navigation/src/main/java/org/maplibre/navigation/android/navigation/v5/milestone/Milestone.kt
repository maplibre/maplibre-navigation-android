package org.maplibre.navigation.android.navigation.v5.milestone

import org.maplibre.navigation.android.navigation.v5.instruction.Instruction
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigation

/**
 * Base Milestone statement. Subclassed to provide concrete statements.
 *
 * @since 0.4.0
 */
abstract class Milestone(val identifier: Int, open val instruction: Instruction? = null, val trigger: Trigger.Statement? = null) {

    @Deprecated("Use constructor with named arguments.", replaceWith = ReplaceWith("Milestone(identifier, instruction, trigger)"))
    constructor(builder: Builder) : this(builder.identifier, builder.instruction, builder.trigger)

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

    /**
     * Build a new [Milestone]
     *
     * @since 0.4.0
     */
    @Deprecated("Use Milestone constructor with named arguments to create instance.", replaceWith = ReplaceWith("Milestone(identifier, instruction, trigger)"))
    abstract class Builder {
        /**
         * Milestone specific identifier as an `int` value, useful for deciphering which milestone
         * invoked [MilestoneEventListener.onMilestoneEvent].
         *
         * @return `int` representing the identifier
         * @since 0.4.0
         */
        var identifier: Int = 0
            private set

        /**
         * Milestone specific [Instruction], which can be used to build a [String]
         * instruction specified by the superclass
         *
         * @return this builder
         * @since 0.4.0
         */
        var instruction: Instruction? = null
            private set

        var trigger: Trigger.Statement? = null
            private set

        /**
         * Milestone specific identifier as an `int` value, useful for deciphering which milestone
         * invoked [MilestoneEventListener.onMilestoneEvent].
         *
         * @param identifier an `int` used to identify this milestone instance
         * @return this builder
         * @since 0.4.0
         */
        fun setIdentifier(identifier: Int) = apply {
            this.identifier = identifier
        }

        fun setInstruction(instruction: Instruction?) = apply {
            this.instruction = instruction
        }

        /**
         * The list of triggers that are used to determine whether this milestone should invoke
         * [MilestoneEventListener.onMilestoneEvent]
         *
         * @param trigger a single simple statement or compound statement found in [Trigger]
         * @return this builder
         * @since 0.4.0
         */
        fun setTrigger(trigger: Trigger.Statement) = apply {
            this.trigger = trigger
        }

        /**
         * Build a new milestone
         *
         * @return A new [Milestone] object
         * @throws NavigationException if an invalid value has been set on the milestone
         * @since 0.4.0
         */
        abstract fun build(): Milestone
    }
}
