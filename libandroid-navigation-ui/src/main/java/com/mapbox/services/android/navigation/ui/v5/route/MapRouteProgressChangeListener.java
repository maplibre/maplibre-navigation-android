package com.mapbox.services.android.navigation.ui.v5.route;

import android.location.Location;

import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

class MapRouteProgressChangeListener implements ProgressChangeListener {

  private final PrimaryRouteDrawer routeDrawer;
  private final RouteArrowDrawer routeArrowDrawer;

  MapRouteProgressChangeListener(PrimaryRouteDrawer routeDrawer, RouteArrowDrawer routeArrowDrawer) {
    this.routeDrawer = routeDrawer;
    this.routeArrowDrawer = routeArrowDrawer;
  }

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    routeArrowDrawer.addUpcomingManeuverArrow(routeProgress);
    routeDrawer.updateRouteProgress(location, routeProgress);
  }
}
