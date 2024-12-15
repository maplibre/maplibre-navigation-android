package org.maplibre.navigation.core.location.engine

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
import kotlinx.coroutines.flow.callbackFlow
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.LocationEngine
import org.maplibre.navigation.core.location.toLocation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A [LocationEngine] that uses the Google Play Services Location API.
 *
 * @param context used to initialize the [FusedLocationProviderClient]
 * @param looper looper that is ued by the [FusedLocationProviderClient] to listen on for location updates
 */
open class GoogleLocationEngine(
    context: Context,
    private val looper: Looper
) : LocationEngine {

    /**
     * Underlying [FusedLocationProviderClient] that is used to fetch location and listen to location updates.
     */
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

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
}
