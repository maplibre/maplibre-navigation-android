package org.maplibre.navigation.android.navigation.v5.offroute;

import android.location.Location;

import org.maplibre.navigation.android.navigation.v5.navigation.MapboxNavigationOptions;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;

public abstract class OffRoute {

  public abstract boolean isUserOffRoute(Location location, RouteProgress routeProgress,
                                         MapboxNavigationOptions options);
}
