package org.maplibre.navigation.core.location

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.maplibre.geojson.Point

/**
 * A generic model that represents a user location.
 */
data class Location(
    /**
     * Latitude, in degrees.
     */
    val latitude: Double,

    /**
     * Longitude, in degrees.
     */
    val longitude: Double,

    /**
     * Horizontal accuracy of the latitude and longitude, in meters.
     * If not available, it will be `null`.
     */
    val accuracyMeters: Float? = null,

    /**
     * Speed of user in meters per second.
     * If not available, it will be `null`.
     */
    val speedMetersPerSeconds: Float? = null,

    /**
     * Bearing of the user, in degrees.
     * If not available, it will be `null`.
     */
    val bearing: Float? = null,

    /**
     * Date & time of this location fix in UTC zone.
     */
    val time: Instant = Clock.System.now(),

    /**
     * Provider that generated this location.
     */
    val provider: String? = null,
) {

    /**
     * Returns a [Point] representation of this location.
     */
    val point: Point
        get() = Point.fromLngLat(longitude, latitude)
}
