package org.maplibre.navigation.core.location

import org.maplibre.geojson.model.Point

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
     * Altitude of this location, in meters above the WGS84 reference ellipsoid.
     */
    val altitude: Double? = null,

    /**
     * Vertical accuracy of the [altitude], in meters.
     * If not available, it will be `null`.
     */
    val altitudeAccuracyMeters: Float? = null,

    /**
     * Altitude of this location, in meters above the Mean Sea Level
     */
    val mslAltitude: Double? = null,

    /**
     * Vertical accuracy of the [mslAltitude], in meters.
     * If not available, it will be `null`.
     */
    val mslAltitudeAccuracyMeters: Float? = null,

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
    val timeMilliseconds: Long? = null,

    /**
     * Provider that generated this location.
     */
    val provider: String? = null,
) {

    /**
     * Returns a [Point] representation of this location.
     */
    val point: Point
        get() = Point(longitude = longitude, latitude = latitude, altitude = altitude)
}
