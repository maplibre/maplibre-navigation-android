package org.maplibre.navigation.android.navigation.ui.v5.map;

import org.maplibre.navigation.core.location.Location;

import org.maplibre.navigation.core.routeprogress.ProgressChangeListener;
import org.maplibre.navigation.core.routeprogress.RouteProgress;

class FpsDelegateProgressChangeListener implements ProgressChangeListener {

  private final MapFpsDelegate fpsDelegate;

  FpsDelegateProgressChangeListener(MapFpsDelegate fpsDelegate) {
    this.fpsDelegate = fpsDelegate;
  }

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    fpsDelegate.adjustFpsFor(routeProgress);
  }
}
