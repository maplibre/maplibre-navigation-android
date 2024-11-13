package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The file exposes speed limit annotations.
 */
object SpeedLimit {

    @Serializable
    enum class Unit(text: String) {
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