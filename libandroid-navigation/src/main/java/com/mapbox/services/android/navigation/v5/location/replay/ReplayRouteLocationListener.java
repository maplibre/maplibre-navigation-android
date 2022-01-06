package com.mapbox.services.android.navigation.v5.location.replay;

import android.location.Location;

import com.mapbox.mapboxsdk.location.engine.LocationEngineCallback;
import com.mapbox.mapboxsdk.location.engine.LocationEngineResult;


class ReplayRouteLocationListener implements ReplayLocationListener {

    private final ReplayRouteLocationEngine engine;
    private final LocationEngineCallback<LocationEngineResult> callback;

    ReplayRouteLocationListener(ReplayRouteLocationEngine engine,
                                LocationEngineCallback<LocationEngineResult> callback) {
        this.engine = engine;
        this.callback = callback;
    }

    @Override
    public void onLocationReplay(Location location) {
        engine.updateLastLocation(location);
        engine.removeLastMockedLocation();
        LocationEngineResult result = LocationEngineResult.create(location);
        callback.onSuccess(result);
    }
}