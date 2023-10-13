package com.mapbox.services.android.navigation.v5.route;

import android.location.Location;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.models.RouteOptions;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * You can extend this to fetch a route. When a route was successfully fetched, you should notify the routeListeners about the new route
 */
public abstract class RouteFetcher {
    protected final List<RouteListener> routeListeners = new CopyOnWriteArrayList<>();

    public void addRouteListener(RouteListener listener) {
        if (!routeListeners.contains(listener)) {
            routeListeners.add(listener);
        }
    }

    public void clearListeners() {
        routeListeners.clear();
    }

    /**
     * Calculates a new {@link DirectionsRoute} given
     * the current {@link Location} and {@link RouteProgress} along the route.
     * <p>
     * Uses {@link RouteOptions#coordinates()} and {@link RouteProgress#remainingWaypoints()}
     * to determine the amount of remaining waypoints there are along the given route.
     *
     * @param location      current location of the device
     * @param routeProgress for remaining waypoints along the route
     * @since 0.13.0
     */
    public abstract void findRouteFromRouteProgress(Location location, RouteProgress routeProgress);

    /**
     * Cancels the Directions API call if it has not been executed yet.
     */
    public abstract void cancelRouteCall();

}
