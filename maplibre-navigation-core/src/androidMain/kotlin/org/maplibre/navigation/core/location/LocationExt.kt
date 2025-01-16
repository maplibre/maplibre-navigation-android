package org.maplibre.navigation.core.location

import android.os.Build
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
        androidLoc.altitude = altitude ?: Double.NaN
        androidLoc.bearing = bearing ?: Float.NaN
        androidLoc.speed = speedMetersPerSeconds ?: Float.NaN
        androidLoc.accuracy = accuracyMeters ?: Float.NaN
        androidLoc.time = time ?: 0L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            androidLoc.verticalAccuracyMeters = altitudeAccuracyMeters ?: Float.NaN
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            androidLoc.mslAltitudeMeters = mslAltitude ?: Double.NaN
            androidLoc.mslAltitudeAccuracyMeters = mslAltitudeAccuracyMeters ?: Float.NaN
        }
    }