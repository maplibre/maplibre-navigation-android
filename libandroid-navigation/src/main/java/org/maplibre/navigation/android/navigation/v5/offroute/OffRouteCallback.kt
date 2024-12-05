package org.maplibre.navigation.android.navigation.v5.offroute

import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

interface OffRouteCallback {

    /**
     * This callback will fire when the [OffRouteDetector] determines that the user
     * location is close enough to the upcoming [org.maplibre.navigation.android.navigation.v5.models.LegStep].
     *
     * In this case, the step index needs to be increased for the next [RouteProgress] generation.
     */
    fun onShouldIncreaseIndex()

    /**
     * This callback will fire when the [OffRouteDetector] determines that the user
     * location is close enough to a [org.maplibre.navigation.android.navigation.v5.models.LegStep].
     *
     * This allows to the OffRouteDetector to either go steps back or multple steps forward.
     *
     * You can use this for advanced navigation scenarios, by default you probably don't need this.
     */
    fun onShouldUpdateToIndex(legIndex: Int, stepIndex: Int)
}
