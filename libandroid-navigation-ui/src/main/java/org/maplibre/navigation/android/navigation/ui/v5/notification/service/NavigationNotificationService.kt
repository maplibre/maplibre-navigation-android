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


///**
// * Internal usage only, use navigation by initializing a new instance of [MapLibreNavigation]
// * and customizing the navigation experience through that class.
// *
// *
// * This class is first created and started when [MapLibreNavigation.startNavigation]
// * get's called and runs in the background until either the navigation sessions ends implicitly or
// * the hosting activity gets destroyed. Location updates are also tracked and handled inside this
// * service. Thread creation gets created in this service and maintains the thread until the service
// * gets destroyed.
// *
// */
open class NavigationNotificationService : Service(), ProgressChangeListener, NavigationEventListener {
    private val localBinder = LocalBinder()

    //    private var thread: RouteProcessorBackgroundThread? = null
//    private var locationEngineUpdater: NavigationLocationEngineUpdater? = null
//    private var notificationProvider: NavigationNotificationProvider? = null
//

//    var navigation: MapLibreNavigation? = null
    var navigationNotification: NavigationNotification? = null

    override fun onBind(intent: Intent): IBinder {
        return localBinder
    }

    override fun onRunning(running: Boolean) {
        navigationNotification?.let {navigationNotification ->
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
