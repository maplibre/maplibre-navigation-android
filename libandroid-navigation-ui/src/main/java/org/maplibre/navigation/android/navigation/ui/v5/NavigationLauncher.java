package org.maplibre.navigation.android.navigation.ui.v5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import org.maplibre.navigation.android.navigation.ui.v5.route.NavigationRoute;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationConstants;
import org.maplibre.navigation.android.navigation.v5.location.replay.ReplayRouteLocationEngine;

/**
 * Use this class to launch the navigation UI
 * <p>
 * You can launch the UI a route you have already retrieved from
 * {@link NavigationRoute}.
 * </p><p>
 * For testing, you can launch with simulation, in which our
 * {@link ReplayRouteLocationEngine} will begin
 * following the given {@link DirectionsRoute} once the UI is initialized.
 * </p>
 */
public class NavigationLauncher {

  /**
   * Starts the UI with a {@link DirectionsRoute} already retrieved from
   * {@link NavigationRoute}
   *
   * @param context to launch the navigation {@link Activity}
   * @param options  with fields to customize the navigation view
   */
  public static void startNavigation(Context context, NavigationLauncherOptions options) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();

    storeDirectionsRouteValue(options, editor);
    storeConfiguration(options, editor);

    storeThemePreferences(options, editor);

    editor.apply();
  }

  /**
   * Used to extract the route used to launch the drop-in UI.
   * <p>
   * Extracts the route {@link String} from {@link SharedPreferences} and converts
   * it back to a {@link DirectionsRoute} object with {@link Gson}.
   *
   * @param context to retrieve {@link SharedPreferences}
   * @return {@link DirectionsRoute} stored when launching
   */
  public static DirectionsRoute extractRoute(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    String directionsRouteJson = preferences.getString(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY, "");
    return DirectionsRoute.fromJson(directionsRouteJson);
  }

  public static void cleanUpPreferences(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor
      .remove(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY)
      .remove(NavigationConstants.NAVIGATION_VIEW_SIMULATE_ROUTE)
      .remove(NavigationConstants.NAVIGATION_VIEW_PREFERENCE_SET_THEME)
      .remove(NavigationConstants.NAVIGATION_VIEW_PREFERENCE_SET_THEME)
      .remove(NavigationConstants.NAVIGATION_VIEW_LIGHT_THEME)
      .remove(NavigationConstants.NAVIGATION_VIEW_DARK_THEME)
      .apply();
  }

  private static void storeDirectionsRouteValue(NavigationLauncherOptions options, SharedPreferences.Editor editor) {
    editor.putString(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY, options.directionsRoute().toJson());
  }

  private static void storeConfiguration(NavigationLauncherOptions options, SharedPreferences.Editor editor) {
    editor.putBoolean(NavigationConstants.NAVIGATION_VIEW_SIMULATE_ROUTE, options.shouldSimulateRoute());
  }

  private static void storeThemePreferences(NavigationLauncherOptions options, SharedPreferences.Editor editor) {
    boolean preferenceThemeSet = options.lightThemeResId() != null || options.darkThemeResId() != null;
    editor.putBoolean(NavigationConstants.NAVIGATION_VIEW_PREFERENCE_SET_THEME, preferenceThemeSet);

    if (preferenceThemeSet) {
      if (options.lightThemeResId() != null) {
        editor.putInt(NavigationConstants.NAVIGATION_VIEW_LIGHT_THEME, options.lightThemeResId());
      }
      if (options.darkThemeResId() != null) {
        editor.putInt(NavigationConstants.NAVIGATION_VIEW_DARK_THEME, options.darkThemeResId());
      }
    }
  }

  private static void storeInitialMapPosition(NavigationLauncherOptions options, Intent navigationActivity) {
    if (options.initialMapCameraPosition() != null) {
      navigationActivity.putExtra(
        NavigationConstants.NAVIGATION_VIEW_INITIAL_MAP_POSITION, options.initialMapCameraPosition()
      );
    }
  }
}
