package org.maplibre.navigation.core.models

import kotlinx.serialization.Serializable
import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.models.serializer.PointSerializer

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
    @Serializable(with = PointSerializer::class)
    val location: Point? = null
)
