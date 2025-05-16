package org.maplibre.navigation.android.navigation.ui.v5

import org.maplibre.geojson.Point

data class MapRouteData(
    val accessToken: String,
    val userLocation: Point,
    val stops: List<Point>?,
    val destination: Point
) {
    companion object {
        const val LIGHT_THEME = "light"
        const val DARK_THEME = "dark"
    }
}