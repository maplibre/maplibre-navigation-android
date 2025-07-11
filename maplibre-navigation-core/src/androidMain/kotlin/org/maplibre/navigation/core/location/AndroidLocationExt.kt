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
    accuracyMeters = if (hasAccuracy()) accuracy else null,
    altitude = if (hasAltitude()) altitude else null,
    bearing = if (hasBearing()) bearing else null,
    speedMetersPerSeconds = if (hasSpeed()) speed else null,
    timeMilliseconds = time,
    altitudeAccuracyMeters = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        if (hasVerticalAccuracy()) verticalAccuracyMeters else null
    else
        null,
    mslAltitude = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        if (hasMslAltitude()) mslAltitudeMeters else null
    else
        null,
    mslAltitudeAccuracyMeters = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        if (hasMslAltitudeAccuracy()) mslAltitudeAccuracyMeters else null
    else
        null,
)