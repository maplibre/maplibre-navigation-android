package org.maplibre.navigation.android.navigation.ui.v5;

import androidx.annotation.Nullable;

import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;

public abstract class NavigationUiOptions {

  public abstract DirectionsRoute directionsRoute();

  @Nullable
  public abstract Integer lightThemeResId();

  @Nullable
  public abstract Integer darkThemeResId();

  public abstract boolean shouldSimulateRoute();

  public abstract boolean waynameChipEnabled();
}
