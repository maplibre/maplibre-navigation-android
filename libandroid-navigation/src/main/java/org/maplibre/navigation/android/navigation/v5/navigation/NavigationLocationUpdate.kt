package org.maplibre.navigation.android.navigation.v5.navigation;

import android.location.Location;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class NavigationLocationUpdate {

  static NavigationLocationUpdate create(Location location, MapLibreNavigation mapLibreNavigation) {
    return new AutoValue_NavigationLocationUpdate(location, mapLibreNavigation);
  }

  abstract Location location();

  abstract MapLibreNavigation mapLibreNavigation();
}
