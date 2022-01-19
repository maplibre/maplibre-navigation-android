package com.mapbox.services.android.navigation.v5.navigation;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.location.engine.LocationEngine;
import com.mapbox.mapboxsdk.location.engine.LocationEngineCallback;
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest;
import com.mapbox.mapboxsdk.location.engine.LocationEngineResult;
import com.mapbox.services.android.navigation.v5.utils.RouteUtils;

class NavigationLocationEngineUpdater {

    private final NavigationLocationEngineListener listener;
    private RouteUtils routeUtils;
    private LocationEngine locationEngine;

    @SuppressLint("MissingPermission")
    NavigationLocationEngineUpdater(LocationEngine locationEngine, NavigationLocationEngineListener listener) {
        this.locationEngine = locationEngine;
        this.listener = listener;
        locationEngine.requestLocationUpdates(new LocationEngineRequest.Builder(1000).build(), listener, Looper.getMainLooper());
    }

    @SuppressLint("MissingPermission")
    void updateLocationEngine(LocationEngine locationEngine) {
        removeLocationEngineListener();
        this.locationEngine = locationEngine;
        locationEngine.requestLocationUpdates(new LocationEngineRequest.Builder(1000).build(), listener, Looper.getMainLooper());
    }

    @SuppressWarnings("MissingPermission")
    void forceLocationUpdate(final DirectionsRoute route) {
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();
                if (!listener.isValidLocationUpdate(location)) {
                    routeUtils = obtainRouteUtils();
                    location = routeUtils.createFirstLocationFromRoute(route);
                }
                listener.queueLocationUpdate(location);
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

    }

    void removeLocationEngineListener() {
        locationEngine.removeLocationUpdates(listener);
    }

    private RouteUtils obtainRouteUtils() {
        if (routeUtils == null) {
            return new RouteUtils();
        }
        return routeUtils;
    }
}
