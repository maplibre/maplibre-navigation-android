package org.maplibre.navigation.core.location.replay

import org.maplibre.navigation.core.location.Location

fun interface ReplayLocationListener {
    fun onLocationReplay(location: Location)
}
