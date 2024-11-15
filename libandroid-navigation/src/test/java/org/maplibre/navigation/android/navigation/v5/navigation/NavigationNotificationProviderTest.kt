package org.maplibre.navigation.android.navigation.v5.navigation

import android.content.Context
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigation
import org.maplibre.navigation.android.navigation.v5.navigation.notification.NavigationNotification
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class NavigationNotificationProviderTest {
    @Test
    fun updateNavigationNotification() {
        val notification = Mockito.mock(
            NavigationNotification::class.java
        )
        val mapLibreNavigation = buildNavigationWithNotificationOptions(notification)
        val context = Mockito.mock(Context::class.java)
        val provider = NavigationNotificationProvider(context, mapLibreNavigation)

        val routeProgress = Mockito.mock(RouteProgress::class.java)
        provider.updateNavigationNotification(routeProgress)

        Mockito.verify(notification).updateNotification(ArgumentMatchers.eq(routeProgress))
    }

    @Test
    fun updateNavigationNotification_doesNotUpdateAfterShutdown() {
        val notification = Mockito.mock(
            NavigationNotification::class.java
        )
        val mapLibreNavigation = buildNavigationWithNotificationOptions(notification)
        val context = Mockito.mock(Context::class.java)
        val provider = NavigationNotificationProvider(context, mapLibreNavigation)
        val routeProgress = Mockito.mock(RouteProgress::class.java)

        provider.shutdown(context)
        provider.updateNavigationNotification(routeProgress)

        Mockito.verify(notification, Mockito.times(0)).updateNotification(routeProgress)
    }

    @Test
    fun onShutdown_onNavigationStoppedIsCalled() {
        val notification = Mockito.mock(
            NavigationNotification::class.java
        )
        val mapLibreNavigation = buildNavigationWithNotificationOptions(notification)
        val context = Mockito.mock(Context::class.java)
        val provider = NavigationNotificationProvider(context, mapLibreNavigation)

        provider.shutdown(context)

        Mockito.verify(notification).onNavigationStopped(context)
    }

    private fun buildNavigationWithNotificationOptions(notification: NavigationNotification): MapLibreNavigation {
        val mapLibreNavigation = Mockito.mock(
            MapLibreNavigation::class.java
        )
        val options = Mockito.mock(
            MapLibreNavigationOptions::class.java
        )
        Mockito.`when`(options.navigationNotification).thenReturn(notification)
        Mockito.`when`(mapLibreNavigation.options).thenReturn(options)
        return mapLibreNavigation
    }
}