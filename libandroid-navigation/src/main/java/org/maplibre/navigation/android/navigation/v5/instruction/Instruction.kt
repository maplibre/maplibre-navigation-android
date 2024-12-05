package org.maplibre.navigation.android.navigation.v5.instruction

import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

/**
 * Base Instruction. Subclassed to provide concrete instructions.
 *
 * @since 0.4.0
 */
fun interface Instruction {

    /**
     * Will provide an instruction based on your specifications
     *
     * @return [String] instruction that will be showed or voiced on the client
     * @since 0.4.0
     */
    fun buildInstruction(routeProgress: RouteProgress): String?
}
