package org.maplibre.navigation.android.navigation.ui.v5.map;

import androidx.annotation.NonNull;

import org.maplibre.navigation.android.navigation.ui.v5.NavigationView;

/**
 * A listener that can be added to the {@link NavigationMapLibreMap} with
 * {@link NavigationMapLibreMap#addOnWayNameChangedListener(OnWayNameChangedListener)}.
 * <p>
 * This listener is triggered when a new way name is found along the route.  It will be triggered
 * regardless of the map way name visibility
 * ({@link NavigationView#updateWayNameVisibility(boolean)}).
 * This is so you can hide our implementation of the way name UI and update your own if you'd like.
 */
public interface OnWayNameChangedListener {

  /**
   * Triggered every time a new way name is found along the route.
   * This will mainly be when transitioning steps, onto new roads.
   *
   * @param wayName found along the route
   */
  void onWayNameChanged(@NonNull String wayName);
}
