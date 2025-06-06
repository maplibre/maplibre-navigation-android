package org.maplibre.navigation.android.navigation.ui.v5;

import org.maplibre.navigation.core.location.Location;
import org.maplibre.navigation.core.routeprogress.ProgressChangeListener;
import org.maplibre.navigation.core.routeprogress.RouteProgress;

class NavigationViewModelProgressChangeListener implements ProgressChangeListener {

  private final NavigationViewModel viewModel;

  NavigationViewModelProgressChangeListener(NavigationViewModel viewModel) {
    this.viewModel = viewModel;
  }

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    viewModel.updateRouteProgress(routeProgress);
    viewModel.updateLocation(location);
  }
}