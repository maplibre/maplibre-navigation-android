package org.maplibre.navigation.android.navigation.v5.route

import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

/**
 * Will fire when either a successful / failed response is received.
 */
interface RouteListener {
    fun onResponseReceived(response: DirectionsResponse, routeProgress: RouteProgress)

    fun onErrorReceived(throwable: Throwable)
}
