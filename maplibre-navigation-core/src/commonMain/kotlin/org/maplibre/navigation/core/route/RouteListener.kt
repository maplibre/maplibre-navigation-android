package org.maplibre.navigation.core.route

import org.maplibre.navigation.core.models.DirectionsResponse
import org.maplibre.navigation.core.routeprogress.RouteProgress

/**
 * Will fire when either a successful / failed response is received.
 */
interface RouteListener {
    fun onResponseReceived(response: DirectionsResponse, routeProgress: RouteProgress)

    fun onErrorReceived(throwable: Throwable)
}
