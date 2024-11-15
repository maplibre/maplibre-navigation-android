package org.maplibre.navigation.android.navigation.v5.route

import android.location.Location
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationMapRoute
import org.maplibre.navigation.android.navigation.v5.routeprogress.ProgressChangeListener
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

@Deprecated("Use in ui package instead")
class MapRouteProgressChangeListener(private val mapRoute: NavigationMapRoute) : ProgressChangeListener {
    
    override fun onProgressChange(location: Location, routeProgress: RouteProgress) {
        val currentRoute = routeProgress.directionsRoute
        val directionsRoutes = mapRoute.retrieveDirectionsRoutes()
        val primaryRouteIndex = mapRoute.retrievePrimaryRouteIndex()
        addNewRoute(currentRoute, directionsRoutes, primaryRouteIndex)
        mapRoute.addUpcomingManeuverArrow(routeProgress)
    }

    private fun addNewRoute(
        currentRoute: DirectionsRoute,
        directionsRoutes: List<DirectionsRoute>,
        primaryRouteIndex: Int
    ) {
        if (isANewRoute(currentRoute, directionsRoutes, primaryRouteIndex)) {
            mapRoute.addRoute(currentRoute)
        }
    }

    private fun isANewRoute(
        currentRoute: DirectionsRoute,
        directionsRoutes: List<DirectionsRoute>,
        primaryRouteIndex: Int
    ): Boolean {
        val noRoutes = directionsRoutes.isEmpty()
        return noRoutes || currentRoute != directionsRoutes[primaryRouteIndex]
    }
}
