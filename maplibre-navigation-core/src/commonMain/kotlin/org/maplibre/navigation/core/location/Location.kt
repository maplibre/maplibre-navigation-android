package org.maplibre.navigation.core.location

import org.maplibre.navigation.geo.Point

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
     * Altitude, in degrees.
     */
    val altitude: Double = null,

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
     * Date time of this location fix. This value is in milliseconds
     * since epoch (1970-01-01T00:00:00Z) in UTC.
     */
    val time: Long? = null,

    /**
     * Provider that generated this location.
     */
    val provider: String? = null,
) {

    /**
     * Returns a [Point] representation of this location.
     */
    val point: Point
        get() = Point(longitude = longitude, latitude = latitude)
}
