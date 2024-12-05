package org.maplibre.navigation.android.navigation.v5.offroute

import org.maplibre.navigation.android.navigation.v5.location.Location

fun interface OffRouteListener {
    fun userOffRoute(location: Location)
}
