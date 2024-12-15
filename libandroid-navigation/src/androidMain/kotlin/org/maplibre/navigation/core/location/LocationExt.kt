package org.maplibre.navigation.core.location

import android.location.Location as AndroidLocation

fun Location.toAndroidLocation() = AndroidLocation(provider)
    .also { androidLoc ->
        androidLoc.latitude = latitude
        androidLoc.longitude = longitude
        androidLoc.bearing = bearing ?: 0f
        androidLoc.accuracy = accuracyMeters ?: 0.0f
    }