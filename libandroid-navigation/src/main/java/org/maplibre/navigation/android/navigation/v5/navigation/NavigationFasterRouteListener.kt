package org.maplibre.navigation.android.navigation.v5.navigation

import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.route.FasterRoute
import org.maplibre.navigation.android.navigation.v5.route.RouteListener
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import timber.log.Timber

open class NavigationFasterRouteListener(
    private val eventDispatcher: NavigationEventDispatcher,
    private val fasterRouteEngine: FasterRoute
) : RouteListener {

    override fun onResponseReceived(response: DirectionsResponse, routeProgress: RouteProgress) {
        if (fasterRouteEngine.isFasterRoute(response, routeProgress)) {
            eventDispatcher.onFasterRouteEvent(response.routes.first())
        }
    }

    override fun onErrorReceived(throwable: Throwable) {
        Timber.e(throwable)
    }
}
