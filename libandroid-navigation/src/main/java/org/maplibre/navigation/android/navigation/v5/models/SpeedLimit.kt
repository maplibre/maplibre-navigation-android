package org.maplibre.navigation.android.navigation.v5.models

/**
 * The file exposes speed limit annotations.
 */
object SpeedLimit {

    enum class Unit(text: String) {
        /**
         * Speed limit unit in km/h.
         */
        KMPH("km/h"),

        /**
         * Speed limit unit in mph.
         */
        MPH("mph"),
    }
}