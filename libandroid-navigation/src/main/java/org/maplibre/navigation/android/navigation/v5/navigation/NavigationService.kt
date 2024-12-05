package org.maplibre.navigation.android.navigation.v5.navigation

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import androidx.core.app.ServiceCompat
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.navigation.android.navigation.v5.location.LocationValidator
import org.maplibre.navigation.android.navigation.v5.navigation.notification.NavigationNotification
import org.maplibre.navigation.android.navigation.v5.utils.RouteUtils
import timber.log.Timber
import java.lang.ref.WeakReference


/**
 * Internal usage only, use navigation by initializing a new instance of [MapLibreNavigation]
 * and customizing the navigation experience through that class.
 *
 *
 * This class is first created and started when [MapLibreNavigation.startNavigation]
 * get's called and runs in the background until either the navigation sessions ends implicitly or
 * the hosting activity gets destroyed. Location updates are also tracked and handled inside this
 * service. Thread creation gets created in this service and maintains the thread until the service
 * gets destroyed.
 *
 */
open class NavigationService : Service() {
    private val localBinder: IBinder = LocalBinder(this)
    private var thread: RouteProcessorBackgroundThread? = null
    private var locationEngineUpdater: NavigationLocationEngineUpdater? = null
    private var notificationProvider: NavigationNotificationProvider? = null

    override fun onBind(intent: Intent): IBinder {
        return localBinder
    }

    /**
     * Only should be called once since we want the service to continue running until the navigation
     * session ends.
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        locationEngineUpdater?.removeLocationEngineListener()
        locationEngineUpdater = null
        super.onDestroy()
    }

    /**
     * This gets called when [MapLibreNavigation.startNavigation] is called and
     * setups variables among other things on the Navigation Service side.
     */
    fun startNavigation(mapLibreNavigation: MapLibreNavigation, routeUtils: RouteUtils) {
        initialize(mapLibreNavigation, routeUtils)

        mapLibreNavigation.route
            ?.let { route ->
                notificationProvider?.retrieveNotification()?.let { navigationNotification ->
                    startForegroundNotification(navigationNotification)
                }

                locationEngineUpdater?.forceLocationUpdate(route)
            }
            ?: throw IllegalStateException("Route not found. Service can only start with a valid navigation route.")
    }

    /**
     * Removes the location / route listeners and  quits the thread.
     */
    fun endNavigation() {
        locationEngineUpdater?.removeLocationEngineListener()
        locationEngineUpdater = null

        notificationProvider?.shutdown(application)
        thread?.quit()
    }

    /**
     * Called with [MapLibreNavigation.locationEngine].
     * Updates this service with the new [LocationEngine].
     *
     * @param locationEngine to update the provider
     */
    fun updateLocationEngine(locationEngine: LocationEngine) {
        locationEngineUpdater?.updateLocationEngine(locationEngine)
    }

    private fun initialize(mapLibreNavigation: MapLibreNavigation, routeUtils: RouteUtils) {
        val notificationProvider = NavigationNotificationProvider(application, mapLibreNavigation)
        this.notificationProvider = notificationProvider

        val thread =
            initializeRouteProcessorThread(mapLibreNavigation.eventDispatcher, notificationProvider, routeUtils)
        initializeLocationProvider(mapLibreNavigation, thread, routeUtils)
    }

    private fun initializeRouteProcessorThread(
        dispatcher: NavigationEventDispatcher,
        notificationProvider: NavigationNotificationProvider,
        routeUtils: RouteUtils
    ): RouteProcessorBackgroundThread {
        val listener = RouteProcessorThreadListener(dispatcher, notificationProvider)
        return RouteProcessorBackgroundThread(Handler(), listener, routeUtils)
            .also { t -> this.thread = t }
    }

    private fun initializeLocationProvider(
        mapLibreNavigation: MapLibreNavigation,
        thread: RouteProcessorBackgroundThread,
        routeUtils: RouteUtils
    ) {
        val locationEngine = mapLibreNavigation.locationEngine
        val listener = NavigationLocationEngineListener(
            mapLibreNavigation = mapLibreNavigation,
            validator = LocationValidator(mapLibreNavigation.options.locationAcceptableAccuracyInMetersThreshold),
            thread = thread,
        )
        locationEngineUpdater = NavigationLocationEngineUpdater(locationEngine, listener, routeUtils)
    }

    private fun startForegroundNotification(navigationNotification: NavigationNotification) {
        val notification = navigationNotification.getNotification()
        val notificationId = navigationNotification.getNotificationId()
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE
        startForeground(notificationId, notification)
    }

    internal class LocalBinder internal constructor(service: NavigationService) : Binder() {
        private val serviceRef = WeakReference(service)

        val service: NavigationService?
            get() {
                Timber.d("Local binder called.")
                return serviceRef.get()
            }
    }
}
