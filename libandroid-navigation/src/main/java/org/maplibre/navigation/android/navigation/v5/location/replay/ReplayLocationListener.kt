package org.maplibre.navigation.android.navigation.v5.location.replay

import org.maplibre.navigation.android.navigation.v5.location.Location

fun interface ReplayLocationListener {
    fun onLocationReplay(location: Location)
}
