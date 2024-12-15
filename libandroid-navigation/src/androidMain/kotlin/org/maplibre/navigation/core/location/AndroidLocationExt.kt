package org.maplibre.navigation.core.location

import android.location.Location as AndroidLocation

fun AndroidLocation.toLocation() = Location(
    provider = provider,
    latitude = latitude,
    longitude = longitude,
    bearing = bearing.takeIf { hasBearing() },
    speedMetersPerSeconds = speed.takeIf { hasSpeed() },
    accuracyMeters = accuracy.takeIf { hasAccuracy() }
)