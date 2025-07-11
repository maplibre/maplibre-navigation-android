package org.maplibre.navigation.android.navigation.ui.v5

import org.maplibre.geojson.Point
import java.util.Locale

data class NavigationRequest(
    val origin: Point,
    val stops: List<Point>? = null,
    val destination: Point,
    val routingService: RoutingService,
    val language: Locale
)