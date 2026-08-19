package org.maplibre.navigation.core.models

import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/**
 * An input coordinate snapped to the roads network.
 *
 * @since 1.0.0
 */
@Serializable
data class DirectionsWaypoint(

    /**
     * Provides the way name which the waypoint's coordinate is snapped to.
     *
     * @since 1.0.0
     */
    val name: String? = null,

    /**
     * A [Point] representing this waypoint location.
     *
     * @since 3.0.0
     */
    val location: Position? = null
)
