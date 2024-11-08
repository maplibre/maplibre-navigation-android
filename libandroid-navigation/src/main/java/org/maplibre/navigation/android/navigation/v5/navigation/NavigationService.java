package org.maplibre.navigation.android.navigation.v5.navigation;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.maplibre.android.location.engine.LocationEngine;
import org.maplibre.navigation.android.navigation.v5.navigation.notification.NavigationNotification;
import org.maplibre.navigation.android.navigation.v5.location.LocationValidator;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Internal usage only, use navigation by initializing a new instance of {@link MapLibreNavigation}
 * and customizing the navigation experience through that class.
 * <p>
 * This class is first created and started when {@link MapLibreNavigation#startNavigation(DirectionsRoute)}
 * get's called and runs in the background until either the navigation sessions ends implicitly or
 * the hosting activity gets destroyed. Location updates are also tracked and handled inside this
 * service. Thread creation gets created in this service and maintains the thread until the service
 * gets destroyed.
 * </p>
 */
public class NavigationService extends Service {

    private final IBinder localBinder = new LocalBinder(this);
    private RouteProcessorBackgroundThread thread;
    private NavigationLocationEngineUpdater locationEngineUpdater;
    private NavigationNotificationProvider notificationProvider;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    /**
     * Only should be called once since we want the service to continue running until the navigation
     * session ends.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (locationEngineUpdater != null)
            locationEngineUpdater.removeLocationEngineListener();
        super.onDestroy();
    }

    /**
     * This gets called when {@link MapLibreNavigation#startNavigation(DirectionsRoute)} is called and
     * setups variables among other things on the Navigation Service side.
     */
    void startNavigation(MapLibreNavigation mapLibreNavigation) {
        initialize(mapLibreNavigation);
        startForegroundNotification(notificationProvider.retrieveNotification());
        locationEngineUpdater.forceLocationUpdate(mapLibreNavigation.getRoute());
    }

    /**
     * Removes the location / route listeners and  quits the thread.
     */
    void endNavigation() {
        locationEngineUpdater.removeLocationEngineListener();
        notificationProvider.shutdown(getApplication());
        thread.quit();
    }

    /**
     * Called with {@link MapLibreNavigation#setLocationEngine(LocationEngine)}.
     * Updates this service with the new {@link LocationEngine}.
     *
     * @param locationEngine to update the provider
     */
    void updateLocationEngine(LocationEngine locationEngine) {
        locationEngineUpdater.updateLocationEngine(locationEngine);
    }

    private void initialize(MapLibreNavigation mapLibreNavigation) {
        NavigationEventDispatcher dispatcher = mapLibreNavigation.getEventDispatcher();
        initializeNotificationProvider(mapLibreNavigation);
        initializeRouteProcessorThread(dispatcher, notificationProvider);
        initializeLocationProvider(mapLibreNavigation);
    }

    private void initializeNotificationProvider(MapLibreNavigation mapLibreNavigation) {
        notificationProvider = new NavigationNotificationProvider(getApplication(), mapLibreNavigation);
    }

    private void initializeRouteProcessorThread(NavigationEventDispatcher dispatcher, NavigationNotificationProvider notificationProvider) {
        RouteProcessorThreadListener listener = new RouteProcessorThreadListener(dispatcher, notificationProvider);
        thread = new RouteProcessorBackgroundThread(new Handler(), listener);
    }

    private void initializeLocationProvider(MapLibreNavigation mapLibreNavigation) {
        LocationEngine locationEngine = mapLibreNavigation.getLocationEngine();
        int accuracyThreshold = mapLibreNavigation.options().locationAcceptableAccuracyInMetersThreshold();
        LocationValidator validator = new LocationValidator(accuracyThreshold);
        NavigationLocationEngineListener listener = new NavigationLocationEngineListener(
                thread, mapLibreNavigation, locationEngine, validator
        );
        locationEngineUpdater = new NavigationLocationEngineUpdater(locationEngine, listener);
    }

    private void startForegroundNotification(NavigationNotification navigationNotification) {
        Notification notification = navigationNotification.getNotification();
        int notificationId = navigationNotification.getNotificationId();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(notificationId, notification);
    }

    final static class LocalBinder extends Binder {

        private final WeakReference<NavigationService> serviceRef;

        private LocalBinder(final NavigationService service) {
            this.serviceRef = new WeakReference<>(service);
        }

        @Nullable
        NavigationService getService() {
            Timber.d("Local binder called.");
            return serviceRef.get();
        }
    }
}
