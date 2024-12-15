package org.maplibre.navigation.core.location

import android.os.SystemClock
import org.maplibre.geojson.Point

/**
 * A generic model that represents a user location.
 */
data class Location(
    /**
     * Provider that generated this location.
     */
    val provider: String? = null,

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
     * Time of this location fix in milliseconds of elapsed time since system boot.
     */
    //TODO fabi755 systemclock
    val elapsedRealtimeMilliseconds: Long = SystemClock.elapsedRealtime()
) {

    /**
     * Returns a [Point] representation of this location.
     */
    val point: Point
        get() = Point.fromLngLat(longitude, latitude)
}
