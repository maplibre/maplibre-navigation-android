package org.maplibre.navigation.android.navigation.v5.navigation;

import android.content.Context;

import org.maplibre.navigation.android.navigation.v5.navigation.notification.NavigationNotification;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;

class NavigationNotificationProvider {

  private NavigationNotification navigationNotification;
  private boolean shouldUpdate = true;

  NavigationNotificationProvider(Context context, MapLibreNavigation mapLibreNavigation) {
    navigationNotification = buildNotificationFrom(context, mapLibreNavigation);
  }

  NavigationNotification retrieveNotification() {
    return navigationNotification;
  }

  void updateNavigationNotification(RouteProgress routeProgress) {
    if (shouldUpdate) {
      navigationNotification.updateNotification(routeProgress);
    }
  }

  void shutdown(Context context) {
    navigationNotification.onNavigationStopped(context);
    navigationNotification = null;
    shouldUpdate = false;
  }

  private NavigationNotification buildNotificationFrom(Context context, MapLibreNavigation mapLibreNavigation) {
    MapLibreNavigationOptions options = mapLibreNavigation.options();
    if (options.navigationNotification() != null) {
      return options.navigationNotification();
    } else {
      return new MapLibreNavigationNotification(context, mapLibreNavigation);
    }
  }
}
