package com.mapbox.services.android.navigation.ui.v5.route;

import android.location.Location;

import com.mapbox.services.android.navigation.ui.v5.route.impl.MapLibrePrimaryRouteDrawer;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

class MapRouteProgressChangeListener implements ProgressChangeListener {

  private final PrimaryRouteDrawer routeDrawer;
  private final MapRouteArrow routeArrow;

  MapRouteProgressChangeListener(PrimaryRouteDrawer routeDrawer, MapRouteArrow routeArrow) {
    this.routeDrawer = routeDrawer;
    this.routeArrow = routeArrow;
  }

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    routeArrow.addUpcomingManeuverArrow(routeProgress);
    routeDrawer.updateRouteProgress(location, routeProgress);
  }
}
