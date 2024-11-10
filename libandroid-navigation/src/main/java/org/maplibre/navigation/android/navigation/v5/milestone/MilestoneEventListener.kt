package org.maplibre.navigation.android.navigation.v5.milestone

import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

interface MilestoneEventListener {
    //TODO fabi755, check for nullable params
    fun onMilestoneEvent(routeProgress: RouteProgress?, instruction: String?, milestone: Milestone?)
}
