package org.maplibre.navigation.core.milestone

import org.maplibre.navigation.core.routeprogress.RouteProgress

fun interface MilestoneEventListener {
    fun onMilestoneEvent(routeProgress: RouteProgress, instruction: String?, milestone: Milestone)
}
