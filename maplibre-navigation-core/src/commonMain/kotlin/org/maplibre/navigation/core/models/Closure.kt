package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * An object indicating the geometry indexes defining a road closure.
 */
@Serializable
data class Closure(
    /**
     * Closure's geometry index start point.
     */
    @SerialName("geometry_index_start")
    val geometryIndexStart: Int? = null,

    /**
     * Closure's geometry index end point.
     */
    @SerialName("geometry_index_end")
    val geometryIndexEnd: Int? = null
)
