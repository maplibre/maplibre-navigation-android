package org.maplibre.navigation.core.location

import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.Foundation.timeIntervalSince1970

/**
 * Converts the Apple platform location to our generic MapLibre location.
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
fun CLLocation.toLocation() = Location(
    provider = "CLLocationManager",
    latitude = coordinate.useContents { latitude },
    longitude = coordinate.useContents { longitude },
    altitude = coordinate.useContents { altitude },
    bearing = course.toFloat(),
    speedMetersPerSeconds = speed.toFloat(),
    accuracyMeters = horizontalAccuracy.toFloat(),
    timeMilliseconds = timestamp.timeIntervalSince1970.toLong() * 1000L
)