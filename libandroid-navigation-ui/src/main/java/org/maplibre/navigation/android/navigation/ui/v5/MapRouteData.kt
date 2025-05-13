package org.maplibre.navigation.android.navigation.ui.v5

data class MapRouteData(
    val accessToken: String,
    val mapStyle: String,
    val routeList: List<Pair<Double, Double>>,
    val userLocation: Pair<Double, Double>,
    val mapTheme: String
) {
    companion object {
        const val LIGHT_THEME = "light"
        const val DARK_THEME = "dark"
    }
}