package org.maplibre.navigation.android.navigation.v5.models

import com.google.auto.value.AutoValue
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName

/**
 * An object indicating the geometry indexes defining a road closure.
 */
data class Closure(
    /**
     * Closure's geometry index start point.
     */
    @SerializedName("geometry_index_start")
    val geometryIndexStart: Int?,

    /**
     * Closure's geometry index end point.
     */
    @SerializedName("geometry_index_end")
    val geometryIndexEnd: Int?
)

//TODO: json parsing