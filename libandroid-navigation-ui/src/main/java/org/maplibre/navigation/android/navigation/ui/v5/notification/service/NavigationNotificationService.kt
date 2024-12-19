package org.maplibre.navigation.android.navigation.ui.v5.notification.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import org.maplibre.navigation.android.navigation.ui.v5.notification.MapLibreNavigationNotification
import org.maplibre.navigation.android.navigation.ui.v5.notification.NavigationNotification
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.navigation.MapLibreNavigation
import org.maplibre.navigation.core.navigation.NavigationEventListener
import org.maplibre.navigation.core.routeprogress.ProgressChangeListener
import org.maplibre.navigation.core.routeprogress.RouteProgress
import java.lang.ref.WeakReference

/**
 * Foreground service that connected to [NavigationNotification]. This service is also
 * a [ProgressChangeListener] that will forward progress changes to the notification.
 */
open class NavigationNotificationService : Service(), ProgressChangeListener, NavigationEventListener {
    private val localBinder = LocalBinder()

    var navigationNotification: NavigationNotification? = null

    override fun onBind(intent: Intent): IBinder {
        return localBinder
    }

    override fun onRunning(running: Boolean) {
        navigationNotification?.let { navigationNotification ->
            if (running) {
                startForegroundNotification(navigationNotification)
            } else {
                stopForegroundNotification()
            }
        }
    }

    private fun startForegroundNotification(navigationNotification: NavigationNotification) {
        val notification = navigationNotification.getNotification()
        val notificationId = navigationNotification.getNotificationId()
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE
        val foregroundType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            else
                0
        ServiceCompat.startForeground(this, notificationId, notification, foregroundType)
    }

    private fun stopForegroundNotification() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    override fun onProgressChange(location: Location, routeProgress: RouteProgress) {
        navigationNotification?.updateNotification(routeProgress)
    }

    inner class LocalBinder : Binder() {
        val service: NavigationNotificationService
            get() = this@NavigationNotificationService
    }
}
