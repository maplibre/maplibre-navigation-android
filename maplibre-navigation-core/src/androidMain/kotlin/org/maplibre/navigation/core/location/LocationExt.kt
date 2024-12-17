package org.maplibre.navigation.core.location

import android.location.Location as AndroidLocation

/**
 * Converts our generic MapLibre location to an Android platform location.
 */
fun Location.toAndroidLocation() = AndroidLocation(provider)
    .also { androidLoc ->
        androidLoc.provider = provider
        androidLoc.latitude = latitude
        androidLoc.longitude = longitude
        androidLoc.bearing = bearing ?: 0f
        androidLoc.speed = speedMetersPerSeconds ?: 0f
        androidLoc.accuracy = accuracyMeters ?: 0.0f
        androidLoc.time = time.toEpochMilliseconds()
    }