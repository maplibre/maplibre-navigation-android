package org.maplibre.navigation.android.navigation.v5.models

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
    val geometryIndexStart: Int?,

    /**
     * Closure's geometry index end point.
     */
    @SerialName("geometry_index_end")
    val geometryIndexEnd: Int?
)
