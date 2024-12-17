package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UnitType(val text: String) {

    @SerialName("imperial")
    IMPERIAL("imperial"),

    @SerialName("metric")
    METRIC("metric")
}