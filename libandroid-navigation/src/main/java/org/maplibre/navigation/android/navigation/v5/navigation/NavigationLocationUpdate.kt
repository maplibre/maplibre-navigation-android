package org.maplibre.navigation.android.navigation.v5.navigation

import android.location.Location

data class NavigationLocationUpdate(
    val location: Location,
    val mapLibreNavigation: MapLibreNavigation
)
