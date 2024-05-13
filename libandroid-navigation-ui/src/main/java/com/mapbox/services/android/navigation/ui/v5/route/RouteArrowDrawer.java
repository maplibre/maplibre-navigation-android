package com.mapbox.services.android.navigation.ui.v5.route;

import android.location.Location;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

public interface RouteArrowDrawer {

    void addUpcomingManeuverArrow(RouteProgress routeProgress);

    void updateVisibilityTo(boolean isVisible);
}
