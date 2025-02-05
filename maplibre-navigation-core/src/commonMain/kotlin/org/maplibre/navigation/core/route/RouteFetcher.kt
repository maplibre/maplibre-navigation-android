package org.maplibre.navigation.core.route

import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.models.RouteOptions
import kotlin.jvm.JvmField

/**
 * You can extend this to fetch a route. When a route was successfully fetched, you should notify the routeListeners about the new route
 */
abstract class RouteFetcher {

    @JvmField
    protected val routeListeners: MutableList<RouteListener> = mutableListOf()

    fun addRouteListener(listener: RouteListener) {
        if (!routeListeners.contains(listener)) {
            routeListeners.add(listener)
        }
    }

    fun clearListeners() {
        routeListeners.clear()
    }

    /**
     * Calculates a new [DirectionsRoute] given
     * the current [Location] and [RouteProgress] along the route.
     *
     *
     * Uses [RouteOptions.coordinates] and [RouteProgress.remainingWaypoints]
     * to determine the amount of remaining waypoints there are along the given route.
     *
     * @param location      current location of the device
     * @param routeProgress for remaining waypoints along the route
     * @since 0.13.0
     */
    abstract fun findRouteFromRouteProgress(location: Location, routeProgress: RouteProgress)

    /**
     * Cancels the Directions API call if it has not been executed yet.
     */
    abstract fun cancelRouteCall()
}
