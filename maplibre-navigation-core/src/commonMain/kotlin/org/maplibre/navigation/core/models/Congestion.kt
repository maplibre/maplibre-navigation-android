package org.maplibre.navigation.core.models

import kotlinx.serialization.Serializable

/**
 * Quantitative descriptor of congestion.
 */
@Serializable
data class Congestion(

    /**
     * Quantitative descriptor of congestion. 0 to 100.
     */
    val value: Int
)
