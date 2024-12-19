package org.maplibre.navigation.core

import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
}