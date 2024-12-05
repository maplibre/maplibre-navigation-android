package org.maplibre.navigation.android.navigation.v5.snap

import android.location.Location
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigation

/**
 * This class handles calculating snapped position along the route. Latitude, longitude and bearing
 * should be provided.
 *
 * The [MapLibreNavigation] uses
 * a [SnapToRoute] by default. If you would ike to customize the camera position, create a concrete implementation of this class
 * or subclass [SnapToRoute] and set it on [MapLibreNavigation] constructor}.
 */
abstract class Snap {

    /**
     * Calculate a snapped location along the route. Latitude, longitude and bearing should be
     * provided.
     *
     * @param location Current raw user location
     * @param routeProgress Current route progress
     * @return Snapped location along route
     */
    abstract fun getSnappedLocation(location: Location, routeProgress: RouteProgress): Location
}
