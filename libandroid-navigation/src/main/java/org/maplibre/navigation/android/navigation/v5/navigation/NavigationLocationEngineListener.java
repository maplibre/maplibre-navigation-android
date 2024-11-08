package org.maplibre.navigation.android.navigation.v5.navigation;

import android.location.Location;

import androidx.annotation.NonNull;

import org.maplibre.android.location.engine.LocationEngine;
import org.maplibre.android.location.engine.LocationEngineCallback;
import org.maplibre.android.location.engine.LocationEngineResult;
import org.maplibre.navigation.android.navigation.v5.location.LocationValidator;

class NavigationLocationEngineListener implements LocationEngineCallback<LocationEngineResult> {

    private final RouteProcessorBackgroundThread thread;
    private final LocationValidator validator;
    private final LocationEngine locationEngine;
    private MapLibreNavigation mapLibreNavigation;

    NavigationLocationEngineListener(RouteProcessorBackgroundThread thread, MapLibreNavigation mapLibreNavigation,
                                     LocationEngine locationEngine, LocationValidator validator) {
        this.thread = thread;
        this.mapLibreNavigation = mapLibreNavigation;
        this.locationEngine = locationEngine;
        this.validator = validator;
    }

    boolean isValidLocationUpdate(Location location) {
        return location != null && validator.isValidUpdate(location);
    }

    /**
     * Queues a new task created from a location update to be sent
     * to {@link RouteProcessorBackgroundThread} for processing.
     *
     * @param location to be processed
     */
    void queueLocationUpdate(Location location) {
        thread.queueUpdate(NavigationLocationUpdate.create(location, mapLibreNavigation));
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        if (isValidLocationUpdate(result.getLastLocation())) {
            queueLocationUpdate(result.getLastLocation());
        }
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
    }
}