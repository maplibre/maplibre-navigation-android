package org.maplibre.navigation.android.navigation.v5.models

import com.google.auto.value.AutoValue
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import org.maplibre.geojson.Point

/**
 * An input coordinate snapped to the roads network.
 *
 * @since 1.0.0
 */
data class DirectionsWaypoint(

    /**
     * Provides the way name which the waypoint's coordinate is snapped to.
     *
     * @since 1.0.0
     */
    val name: String?,

    /**
     * A [Point] representing this waypoint location.
     *
     * @since 3.0.0
     */
    val location: Point?
)
