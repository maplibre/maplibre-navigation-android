package com.mapbox.services.android.navigation.ui.v5.route;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

public interface WayPointDrawer {

    void setRoute(DirectionsRoute route);

    void setStyle(Style style);

    void setVisibility(boolean isVisible);
}
