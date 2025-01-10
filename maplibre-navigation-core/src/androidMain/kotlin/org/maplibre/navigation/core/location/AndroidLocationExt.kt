package org.maplibre.navigation.core.location

import android.location.Location as AndroidLocation

/**
 * Converts the Android platform location to our generic MapLibre location.
 */
fun AndroidLocation.toLocation() = Location(
    provider = provider,
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    bearing = bearing.takeIf { hasBearing() },
    speedMetersPerSeconds = speed.takeIf { hasSpeed() },
    accuracyMeters = accuracy.takeIf { hasAccuracy() },
    time = time
)