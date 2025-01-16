package org.maplibre.navigation.core.location

import android.os.Build
import android.location.Location as AndroidLocation

/**
 * Converts the Android platform location to our generic MapLibre location.
 */
fun AndroidLocation.toLocation() = Location(
    provider = provider,
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracy.takeIf { hasAccuracy() },
    altitude = altitude,
    bearing = bearing.takeIf { hasBearing() },
    speedMetersPerSeconds = speed.takeIf { hasSpeed() },
    time = time,
    altitudeAccuracyMeters = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        verticalAccuracyMeters.takeIf { hasVerticalAccuracy() }
    else
        null,
    mslAltitude = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        mslAltitudeMeters.takeIf { hasMslAltitude() }
    else
        null,
    mslAltitudeAccuracyMeters = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        mslAltitudeAccuracyMeters.takeIf { hasMslAltitudeAccuracy() }
    else
        null,
)