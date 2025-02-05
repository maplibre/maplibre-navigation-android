package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The file exposes speed limit annotations.
 */
@Suppress("unused")
object SpeedLimit {

    @Serializable
    enum class Unit(val text: String) {
        /**
         * Speed limit unit in km/h.
         */
        @SerialName("km/h")
        KMPH("km/h"),

        /**
         * Speed limit unit in mph.
         */
        @SerialName("mph")
        MPH("mph"),
    }
}