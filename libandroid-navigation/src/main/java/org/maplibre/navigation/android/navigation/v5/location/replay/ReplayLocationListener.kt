package org.maplibre.navigation.android.navigation.v5.location.replay

import android.location.Location

internal fun interface ReplayLocationListener {
    fun onLocationReplay(location: Location)
}
