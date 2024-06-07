package org.maplibre.navigation.android.navigation.v5.navigation;

import android.content.Context;
import androidx.annotation.NonNull;

import org.maplibre.navigation.android.navigation.v5.navigation.notification.NavigationNotification;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NavigationNotificationProviderTest {

  @Test
  public void updateNavigationNotification() {
    NavigationNotification notification = mock(NavigationNotification.class);
    MapLibreNavigation mapLibreNavigation = buildNavigationWithNotificationOptions(notification);
    Context context = mock(Context.class);
    NavigationNotificationProvider provider = new NavigationNotificationProvider(context, mapLibreNavigation);

    RouteProgress routeProgress = mock(RouteProgress.class);
    provider.updateNavigationNotification(routeProgress);

    verify(notification).updateNotification(eq(routeProgress));
  }

  @Test
  public void updateNavigationNotification_doesNotUpdateAfterShutdown() {
    NavigationNotification notification = mock(NavigationNotification.class);
    MapLibreNavigation mapLibreNavigation = buildNavigationWithNotificationOptions(notification);
    Context context = mock(Context.class);
    NavigationNotificationProvider provider = new NavigationNotificationProvider(context, mapLibreNavigation);
    RouteProgress routeProgress = mock(RouteProgress.class);

    provider.shutdown(context);
    provider.updateNavigationNotification(routeProgress);

    verify(notification, times(0)).updateNotification(routeProgress);
  }

  @Test
  public void onShutdown_onNavigationStoppedIsCalled() {
    NavigationNotification notification = mock(NavigationNotification.class);
    MapLibreNavigation mapLibreNavigation = buildNavigationWithNotificationOptions(notification);
    Context context = mock(Context.class);
    NavigationNotificationProvider provider = new NavigationNotificationProvider(context, mapLibreNavigation);

    provider.shutdown(context);

    verify(notification).onNavigationStopped(context);
  }

  @NonNull
  private MapLibreNavigation buildNavigationWithNotificationOptions(NavigationNotification notification) {
    MapLibreNavigation mapLibreNavigation = mock(MapLibreNavigation.class);
    MapLibreNavigationOptions options = mock(MapLibreNavigationOptions.class);
    when(options.navigationNotification()).thenReturn(notification);
    when(mapLibreNavigation.options()).thenReturn(options);
    return mapLibreNavigation;
  }
}