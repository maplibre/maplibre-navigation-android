package org.maplibre.navigation.android.navigation.ui.v5

sealed class RoutingService(open val baseUrl: String, open val accessToken: String) {
    class GraphHopper(override val baseUrl: String, override val accessToken: String) : RoutingService(baseUrl, accessToken)
    class Mapbox(override val baseUrl: String, override val accessToken: String) : RoutingService(baseUrl, accessToken)
    class Other(override val baseUrl: String, override val accessToken: String) : RoutingService(baseUrl, accessToken)
}