package org.maplibre.navigation.android.navigation.ui.v5.map;

import static org.maplibre.navigation.android.navigation.ui.v5.GeoJsonExtKt.toJvmPoints;

import org.maplibre.navigation.core.location.Location;

import org.maplibre.navigation.core.routeprogress.ProgressChangeListener;
import org.maplibre.navigation.core.routeprogress.RouteProgress;

class MapWaynameProgressChangeListener implements ProgressChangeListener {

  private final MapWayName mapWayName;

  MapWaynameProgressChangeListener(MapWayName mapWayName) {
    this.mapWayName = mapWayName;
  }

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    mapWayName.updateProgress(location, toJvmPoints(routeProgress.getCurrentStepPoints()));
  }
}
