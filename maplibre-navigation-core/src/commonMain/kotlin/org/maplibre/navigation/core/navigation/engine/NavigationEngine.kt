package org.maplibre.navigation.core.navigation.engine

import org.maplibre.navigation.core.models.DirectionsRoute

/**
 * Base interface for running logic of navigation.
 *
 * Call [startNavigation] should start listening to location updates, and process this data to
 * update to the current navigation state.
 *
 * Default implementation is [MapLibreNavigationEngine].
 */
interface NavigationEngine {

    /**
     * Start a navigation for the given route.
     *
     * @param route that is going to be navigated.
     */
    fun startNavigation(route: DirectionsRoute)

    /**
     * Stop current running navigation
     */
    fun stopNavigation()

    /**
     * Check if the navigation is running
     *
     * @return true if the navigation is running, false otherwise.
     */
    fun isRunning(): Boolean

    fun triggerManualRouteUpdate(legIndex: Int, stepIndex: Int)
}
