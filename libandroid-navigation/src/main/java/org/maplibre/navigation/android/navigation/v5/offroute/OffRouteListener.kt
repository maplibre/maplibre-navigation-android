package org.maplibre.navigation.android.navigation.v5.offroute

import android.location.Location

fun interface OffRouteListener {
    fun userOffRoute(location: Location)
}
