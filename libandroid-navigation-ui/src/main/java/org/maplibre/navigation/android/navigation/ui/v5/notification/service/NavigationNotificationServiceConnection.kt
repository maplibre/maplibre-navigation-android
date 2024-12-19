package org.maplibre.navigation.android.navigation.ui.v5.notification.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.maplibre.navigation.android.navigation.ui.v5.notification.MapLibreNavigationNotification
import org.maplibre.navigation.android.navigation.ui.v5.notification.NavigationNotification
import org.maplibre.navigation.core.navigation.MapLibreNavigation


class NavigationNotificationServiceConnection(
    private val mapLibreNavigation: MapLibreNavigation,
    private val navigationNotification: NavigationNotification,
) : ServiceConnection {

    constructor(
        context: Context,
        mapLibreNavigation: MapLibreNavigation,
    ) : this(mapLibreNavigation, MapLibreNavigationNotification(context, mapLibreNavigation))

    private var serviceBinder: NavigationNotificationService.LocalBinder? = null

    fun start(context: Context) {
        val intent = Intent(context, NavigationNotificationService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    fun stop(context: Context) {
        context.unbindService(this)
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        (service as NavigationNotificationService.LocalBinder).also { serviceBinder ->
            this.serviceBinder = serviceBinder

            serviceBinder.service.navigationNotification = navigationNotification
            mapLibreNavigation.addNavigationEventListener(serviceBinder.service)
            mapLibreNavigation.addProgressChangeListener(serviceBinder.service)
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        serviceBinder?.let { serviceBinder ->
            mapLibreNavigation.removeNavigationEventListener(serviceBinder.service)
            mapLibreNavigation.removeProgressChangeListener(serviceBinder.service)
        }

        serviceBinder = null
    }
}