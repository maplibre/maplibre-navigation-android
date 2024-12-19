package org.maplibre.navigation.core.routeprogress

import org.maplibre.navigation.core.location.Location

fun interface ProgressChangeListener {
    fun onProgressChange(location: Location, routeProgress: RouteProgress)
}
