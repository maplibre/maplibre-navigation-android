package com.mapbox.services.android.navigation.v5.snap;

import android.location.Location;

import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

/**
 * This class handles calculating snapped position along the route. Latitude, longitude and bearing
 * should be provided.
 * <p>
 * The {@link com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation} uses
 * a {@link com.mapbox.services.android.navigation.v5.snap.SnapToRoute} by default. If you would
 * like to customize the camera position, create a concrete implementation of this class
 * or subclass {@link com.mapbox.services.android.navigation.v5.snap.SnapToRoute} and update {@link com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation#setSnapEngine(Snap)}}.
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
