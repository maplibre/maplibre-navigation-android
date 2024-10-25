package com.mapbox.services.android.navigation.ui.v5.route;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.List;

public interface AlternativeRouteDrawer {

    void setRoutes(List<DirectionsRoute> routes);

    void setStyle(Style style);

    void setVisibility(boolean isVisible);
}
