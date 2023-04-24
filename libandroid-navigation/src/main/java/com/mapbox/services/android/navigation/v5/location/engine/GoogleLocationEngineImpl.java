package com.mapbox.services.android.navigation.v5.location.engine;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.mapboxsdk.location.engine.LocationEngineCallback;
import com.mapbox.mapboxsdk.location.engine.LocationEngineImpl;
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest;
import com.mapbox.mapboxsdk.location.engine.LocationEngineResult;

import java.util.Collections;
import java.util.List;

/**
 * Wraps implementation of Fused Location Provider
 */
public class GoogleLocationEngineImpl implements LocationEngineImpl<LocationCallback> {
    private final FusedLocationProviderClient fusedLocationProviderClient;

    @VisibleForTesting
    GoogleLocationEngineImpl(FusedLocationProviderClient fusedLocationProviderClient) {
        this.fusedLocationProviderClient = fusedLocationProviderClient;
    }

    public GoogleLocationEngineImpl(@NonNull Context context) {
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @NonNull
    @Override
    public LocationCallback createListener(LocationEngineCallback<LocationEngineResult> callback) {
        return new GoogleLocationEngineCallbackTransport(callback);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void getLastLocation(@NonNull LocationEngineCallback<LocationEngineResult> callback)
            throws SecurityException {
        GoogleLastLocationEngineCallbackTransport transport =
                new GoogleLastLocationEngineCallbackTransport(callback);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(transport).addOnFailureListener(transport);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                       @NonNull LocationCallback listener,
                                       @Nullable Looper looper) throws SecurityException {
        fusedLocationProviderClient.requestLocationUpdates(toGMSLocationRequest(request), listener, looper);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void requestLocationUpdates(@NonNull LocationEngineRequest request,
                                       @NonNull PendingIntent pendingIntent) throws SecurityException {
        fusedLocationProviderClient.requestLocationUpdates(toGMSLocationRequest(request), pendingIntent);
    }

    @Override
    public void removeLocationUpdates(@NonNull LocationCallback listener) {
        // The listener is annotated with @NonNull, but there seems to be cases where a null
        // listener is somehow passed into this method.
        if (listener != null) {
            fusedLocationProviderClient.removeLocationUpdates(listener);
        }
    }

    @Override
    public void removeLocationUpdates(PendingIntent pendingIntent) {
        if (pendingIntent != null) {
            fusedLocationProviderClient.removeLocationUpdates(pendingIntent);
        }
    }

    private static LocationRequest toGMSLocationRequest(LocationEngineRequest request) {
        LocationRequest.Builder builder = new LocationRequest.Builder(request.getInterval());
        builder.setMinUpdateIntervalMillis(request.getFastestInterval());
        builder.setMinUpdateDistanceMeters(request.getDisplacement());
        builder.setMaxUpdateDelayMillis(request.getMaxWaitTime());
        builder.setPriority(toGMSLocationPriority(request.getPriority()));
        return builder.build();
    }

    private static int toGMSLocationPriority(int enginePriority) {
        switch (enginePriority) {
            case LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                return Priority.PRIORITY_BALANCED_POWER_ACCURACY;
            case LocationEngineRequest.PRIORITY_LOW_POWER:
                return Priority.PRIORITY_LOW_POWER;
            case LocationEngineRequest.PRIORITY_NO_POWER:
                return Priority.PRIORITY_PASSIVE;
            case LocationEngineRequest.PRIORITY_HIGH_ACCURACY:
            default:
                return Priority.PRIORITY_HIGH_ACCURACY;
        }
    }

    private static final class GoogleLocationEngineCallbackTransport extends LocationCallback {
        private final LocationEngineCallback<LocationEngineResult> callback;

        GoogleLocationEngineCallbackTransport(LocationEngineCallback<LocationEngineResult> callback) {
            this.callback = callback;
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locations = locationResult.getLocations();
            if (!locations.isEmpty()) {
                callback.onSuccess(LocationEngineResult.create(locations));
            } else {
                callback.onFailure(new Exception("Unavailable location"));
            }
        }
    }

    @VisibleForTesting
    static final class GoogleLastLocationEngineCallbackTransport
            implements OnSuccessListener<Location>, OnFailureListener {
        private final LocationEngineCallback<LocationEngineResult> callback;

        GoogleLastLocationEngineCallbackTransport(LocationEngineCallback<LocationEngineResult> callback) {
            this.callback = callback;
        }

        @Override
        public void onSuccess(Location location) {
            callback.onSuccess(location != null ? LocationEngineResult.create(location) :
                    LocationEngineResult.create(Collections.<Location>emptyList()));
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            callback.onFailure(e);
        }
    }
}
