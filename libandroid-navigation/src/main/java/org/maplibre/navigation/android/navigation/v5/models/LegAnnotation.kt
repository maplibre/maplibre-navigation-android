package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An annotations object that contains additional details about each line segment along the route
 * geometry. Each entry in an annotations field corresponds to a coordinate along the route
 * geometry.
 *
 * @since 2.1.0
 */
@Serializable
data class LegAnnotation(

    /**
     * The distance, in meters, between each pair of coordinates.
     *
     * @since 2.1.0
     */
    val distance: List<Double>?,

    /**
     * The speed, in meters per second, between each pair of coordinates.
     *
     * @since 2.1.0
     */
    val duration: List<Double>?,

    /**
     * The speed, in meters per second, between each pair of coordinates.
     *
     * @since 2.1.0
     */
    val speed: List<Double>?,

    /**
     * The posted speed limit, between each pair of coordinates.
     * Maxspeed is only available for the `mapbox/driving` and `mapbox/driving-traffic`
     * profiles, other profiles will return `unknown`s only.
     *
     * @since 3.0.0
     */
    @SerialName("maxspeed")
    val maxSpeed: List<MaxSpeed>?,

    /**
     * The congestion between each pair of coordinates.
     *
     * @since 2.2.0
     */
    val congestion: List<String>?,
)