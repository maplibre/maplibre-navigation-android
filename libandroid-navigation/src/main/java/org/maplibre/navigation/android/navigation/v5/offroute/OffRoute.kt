package org.maplibre.navigation.android.navigation.v5.offroute

import org.maplibre.navigation.android.navigation.v5.location.Location
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

fun interface OffRoute {

    fun isUserOffRoute(location: Location, routeProgress: RouteProgress, options: MapLibreNavigationOptions): Boolean
}
