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
) {

    /**
     * Creates a builder initialized with the current values of the `Closure` instance.
     */
    fun toBuilder(): Builder {
        return Builder()
            .withGeometryIndexStart(geometryIndexStart)
            .withGeometryIndexEnd(geometryIndexEnd)
    }

    /**
     * Builder class for creating `Closure` instances.
     * @param geometryIndexStart Closure's geometry index start point.
     * @param geometryIndexEnd Closure's geometry index end point.
     */
    class Builder {
        private var geometryIndexStart: Int? = null
        private var geometryIndexEnd: Int? = null

        /**
         * Sets the geometry index start point.
         *
         * @param geometryIndexStart The geometry index start point.
         * @return The builder instance.
         */
        fun withGeometryIndexStart(geometryIndexStart: Int?) = apply { this.geometryIndexStart = geometryIndexStart }

        /**
         * Sets the geometry index end point.
         *
         * @param geometryIndexEnd The geometry index end point.
         * @return The builder instance.
         */
        fun withGeometryIndexEnd(geometryIndexEnd: Int?) = apply { this.geometryIndexEnd = geometryIndexEnd }

        /**
         * Builds a `Closure` instance with the current builder values.
         *
         * @return A new `Closure` instance.
         */
        fun build(): Closure {
            return Closure(
                geometryIndexStart = geometryIndexStart,
                geometryIndexEnd = geometryIndexEnd
            )
        }
    }
}
