package com.mapbox.services.android.navigation.ui.v5.route;

import android.location.Location;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import java.util.List;

class MapRouteProgressChangeListener implements ProgressChangeListener {

  private final MapPrimaryRouteDrawer routeDrawer;
  private final MapRouteArrow routeArrow;

  MapRouteProgressChangeListener(MapPrimaryRouteDrawer routeDrawer, MapRouteArrow routeArrow) {
    this.routeDrawer = routeDrawer;
    this.routeArrow = routeArrow;
  }

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    routeArrow.addUpcomingManeuverArrow(routeProgress);
    routeDrawer.updateRouteProgress(location, routeProgress);
  }
}
