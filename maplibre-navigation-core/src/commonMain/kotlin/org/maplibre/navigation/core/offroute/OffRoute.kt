package org.maplibre.navigation.core.offroute

import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.core.routeprogress.RouteProgress

fun interface OffRoute {

    fun isUserOffRoute(location: Location, routeProgress: RouteProgress, options: MapLibreNavigationOptions): Boolean
}
