package org.maplibre.navigation.android.navigation.v5.snap;

import android.location.Location;

import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;
import org.maplibre.navigation.android.navigation.v5.navigation.MapboxNavigation;

/**
 * This class handles calculating snapped position along the route. Latitude, longitude and bearing
 * should be provided.
 * <p>
 * The {@link MapboxNavigation} uses
 * a {@link SnapToRoute} by default. If you would
 * like to customize the camera position, create a concrete implementation of this class
 * or subclass {@link SnapToRoute} and update {@link MapboxNavigation#setSnapEngine(Snap)}}.
 */
public abstract class Snap {

  /**
   * Calculate a snapped location along the route. Latitude, longitude and bearing should be
   * provided.
   *
   * @param location Current raw user location
   * @param routeProgress Current route progress
   * @return Snapped location along route
   */
  public abstract Location getSnappedLocation(Location location, RouteProgress routeProgress);
}
