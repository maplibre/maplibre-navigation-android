//package org.maplibre.navigation.core.navigation
//
//import android.content.Context
//import org.maplibre.navigation.core.navigation.notification.NavigationNotification
//import org.maplibre.navigation.core.routeprogress.RouteProgress
//
//open class NavigationNotificationProvider(
//    context: Context,
//    mapLibreNavigation: MapLibreNavigation
//) {
//    private val navigationNotification: NavigationNotification =
//        buildNotificationFrom(context, mapLibreNavigation)
//    private var shouldUpdate = true
//
//    fun retrieveNotification(): NavigationNotification {
//        return navigationNotification
//    }
//
//    fun updateNavigationNotification(routeProgress: RouteProgress) {
//        if (shouldUpdate) {
//            navigationNotification.updateNotification(routeProgress)
//        }
//    }
//
//    fun shutdown(context: Context) {
//        navigationNotification.onNavigationStopped(context)
//        shouldUpdate = false
//    }
//
//    private fun buildNotificationFrom(
//        context: Context,
//        mapLibreNavigation: MapLibreNavigation
//    ): NavigationNotification {
//        return mapLibreNavigation.options.navigationNotification
//            ?: MapLibreNavigationNotification(
//                context,
//                mapLibreNavigation
//            )
//    }
//}
