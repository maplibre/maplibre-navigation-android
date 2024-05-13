package com.mapbox.services.android.navigation.ui.v5.route;

import android.location.Location;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

public interface PrimaryRouteDrawer {

    void updateRouteProgress(Location location, RouteProgress routeProgress);

    public void setMaxAnimationFps(int maxAnimationFps);

    void setVisibility(boolean isVisible);

    void setRoute(DirectionsRoute route);

    void setRouteEatingEnabled(boolean isRouteEatingEnabled);

    void setStyle(Style style);
}
