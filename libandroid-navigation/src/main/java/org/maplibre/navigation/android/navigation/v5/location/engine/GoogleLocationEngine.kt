package org.maplibre.navigation.android.navigation.v5.location.engine

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.location.Location as AndroidLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.navigation.android.navigation.v5.location.Location
import org.maplibre.navigation.android.navigation.v5.location.LocationEngine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Wraps implementation of Fused Location Provider
 */
open class GoogleLocationEngine(
    context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context),
    private val looper: Looper
) : LocationEngine {

    //    override fun createListener(callback: LocationEngineCallback<LocationEngineResult>): LocationCallback {
//        return GoogleLocationEngineCallbackTransport(callback)
//    }
//
//    @SuppressLint("MissingPermission")
//    @Throws(SecurityException::class)
//    override fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult>) {
//        val transport = GoogleLastLocationEngineCallbackTransport(callback)
//        fusedLocationProviderClient.lastLocation
//            .addOnSuccessListener(transport)
//            .addOnFailureListener(transport)
//    }
//
//    @SuppressLint("MissingPermission")
//    @Throws(SecurityException::class)
//    override fun requestLocationUpdates(
//        request: LocationEngineRequest,
//        listener: LocationCallback,
//        looper: Looper?
//    ) {
//        fusedLocationProviderClient.requestLocationUpdates(
//            toGMSLocationRequest(request),
//            listener,
//            looper
//        )
//    }
//
//    @SuppressLint("MissingPermission")
//    @Throws(SecurityException::class)
//    override fun requestLocationUpdates(
//        request: LocationEngineRequest,
//        pendingIntent: PendingIntent
//    ) {
//        fusedLocationProviderClient.requestLocationUpdates(
//            toGMSLocationRequest(request),
//            pendingIntent
//        )
//    }
//
//    override fun removeLocationUpdates(listener: LocationCallback) {
//        // The listener is annotated with @NonNull, but there seems to be cases where a null
//        // listener is somehow passed into this method.
//        @Suppress("SENSELESS_COMPARISON")
//        if (listener != null) {
//            fusedLocationProviderClient.removeLocationUpdates(listener)
//        }
//    }
//
//    override fun removeLocationUpdates(pendingIntent: PendingIntent) {
//        fusedLocationProviderClient.removeLocationUpdates(pendingIntent)
//    }
//
//    private class GoogleLocationEngineCallbackTransport(
//        private val callback: LocationEngineCallback<LocationEngineResult>
//    ) : LocationCallback() {
//
//        override fun onLocationResult(locationResult: LocationResult) {
//            super.onLocationResult(locationResult)
//            val locations = locationResult.locations
//            if (locations.isNotEmpty()) {
//                callback.onSuccess(LocationEngineResult.create(locations))
//            } else {
//                callback.onFailure(Exception("Unavailable location"))
//            }
//        }
//    }
//
//    @VisibleForTesting
//    internal class GoogleLastLocationEngineCallbackTransport(
//        private val callback: LocationEngineCallback<LocationEngineResult>
//    ) : OnSuccessListener<AndroidLocation>, OnFailureListener {
//
//        override fun onSuccess(location: AndroidLocation?) {
//            callback.onSuccess(
//                if (location != null)
//                    LocationEngineResult.create(location)
//                else
//                    LocationEngineResult.create(emptyList())
//            )
//        }
//
//        override fun onFailure(e: Exception) {
//            callback.onFailure(e)
//        }
//    }


    @SuppressLint("MissingPermission")
    override fun listenToLocation(request: LocationEngineRequest): Flow<Location> = callbackFlow {
        val listener = LocationListener { location -> trySend(location.toLocation()) }

        fusedLocationProviderClient.requestLocationUpdates(
            toGMSLocationRequest(request),
            listener,
            looper
        )

        awaitClose { fusedLocationProviderClient.removeLocationUpdates(listener) }
    }
//
//
//
//    //TODO fabi755: implement stop location requests
//    @SuppressLint("MissingPermission")
//    override fun listenToLocation(request: LocationEngineRequest): Flow<Location> {
//        val resultFlow = MutableStateFlow<Location?>(null)
//
//        fusedLocationProviderClient.requestLocationUpdates(
//            toGMSLocationRequest(request),
//            { location -> resultFlow.tryEmit(location.toLocation()) },
//            looper
//        )
//
//        return resultFlow.filterNotNull()
//    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location = suspendCoroutine { continuation ->
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { androidLocation: AndroidLocation ->
                continuation.resume(androidLocation.toLocation())
            }
            .addOnFailureListener {
                //TODO fabi755
            }
    }

    private fun toGMSLocationRequest(request: LocationEngineRequest): LocationRequest {
        return with(LocationRequest.Builder(request.interval)) {
            setMinUpdateIntervalMillis(request.fastestInterval)
            setMinUpdateDistanceMeters(request.displacement)
            setMaxUpdateDelayMillis(request.maxWaitTime)
            setPriority(toGMSLocationPriority(request.priority))
            build()
        }
    }

    private fun toGMSLocationPriority(enginePriority: Int): Int {
        return when (enginePriority) {
            LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            LocationEngineRequest.PRIORITY_LOW_POWER -> Priority.PRIORITY_LOW_POWER
            LocationEngineRequest.PRIORITY_NO_POWER -> Priority.PRIORITY_PASSIVE
            LocationEngineRequest.PRIORITY_HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
            else -> Priority.PRIORITY_HIGH_ACCURACY
        }
    }

    fun AndroidLocation.toLocation() = Location(
        latitude = latitude,
        longitude = longitude,
        bearing = bearing.takeIf { hasBearing() },
        speedMetersPerSeconds = speed.takeIf { hasSpeed() },
        accuracyMeters = accuracy.takeIf { hasAccuracy() }
    )
}
