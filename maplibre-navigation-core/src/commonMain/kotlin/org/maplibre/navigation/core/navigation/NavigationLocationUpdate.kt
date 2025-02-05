package org.maplibre.navigation.core.navigation

import org.maplibre.navigation.core.location.Location

data class NavigationLocationUpdate(
    val location: Location,
    val mapLibreNavigation: MapLibreNavigation
)
