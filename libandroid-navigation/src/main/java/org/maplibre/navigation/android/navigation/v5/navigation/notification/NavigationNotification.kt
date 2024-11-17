package org.maplibre.navigation.android.navigation.v5.navigation.notification

import android.app.Notification
import android.content.Context
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigation
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationService

/**
 * Defines a contract in which a custom notification must adhere to when
 * given to [MapLibreNavigationOptions].
 */
interface NavigationNotification {

    /**
     * Provides a custom [Notification] to launch
     * with the [NavigationService], specifically
     * [android.app.Service.startForeground].
     *
     * @return a custom notification
     */
    fun getNotification(): Notification

    /**
     * An integer id that will be used to start this notification
     * from [NavigationService] with
     * [android.app.Service.startForeground].
     *
     * @return an int id specific to the notification
     */
    fun getNotificationId(): Int

    /**
     * If enabled, this method will be called every time a new [RouteProgress] is generated.
     *
     *
     * This method can serve as a cue to update a [Notification] with a specific notification id.
     *
     * @param routeProgress with the latest progress data
     */
    fun updateNotification(routeProgress: RouteProgress)

    /**
     * Callback for when navigation is stopped via [MapLibreNavigation.stopNavigation].
     *
     *
     * This callback may be used to clean up any listeners or receivers, preventing leaks.
     *
     * @param context to be used if needed for Android-related work
     */
    fun onNavigationStopped(context: Context)

    companion object {
        const val END_NAVIGATION_ACTION: String =
            "org.maplibre.navigation.android.intent.action.END_NAVIGATION"
        const val OPEN_NAVIGATION_ACTION: String =
            "org.maplibre.navigation.android.intent.action.OPEN_NAVIGATION"
    }
}
