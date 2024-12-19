package org.maplibre.navigation.core.offroute

import org.maplibre.navigation.core.location.Location

fun interface OffRouteListener {
    fun userOffRoute(location: Location)
}
