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
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.toLocation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * A [LocationEngine] that uses the Google Play Services Location API.
 *
 * @param context used to initialize the [FusedLocationProviderClient]
 * @param looper looper that is ued by the [FusedLocationProviderClient] to listen on for location updates
 */
open class GoogleLocationEngine(
    context: Context,
    private val looper: Looper?
) : LocationEngine {

    /**
     * Underlying [FusedLocationProviderClient] that is used to fetch location and listen to location updates.
     */
    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun listenToLocation(request: LocationEngine.Request): Flow<Location> = callbackFlow {
        val listener = LocationListener { location -> trySend(location.toLocation()) }

        fusedLocationProviderClient.requestLocationUpdates(
            toGMSLocationRequest(request),
            listener,
            looper
        )

        awaitClose { fusedLocationProviderClient.removeLocationUpdates(listener) }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? = suspendCoroutine { continuation ->
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { androidLocation: AndroidLocation? ->
                continuation.resume(androidLocation?.toLocation())
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }

    private fun toGMSLocationRequest(request: LocationEngine.Request): LocationRequest {
        return with(LocationRequest.Builder(request.maxIntervalMilliseconds)) {
            setMinUpdateIntervalMillis(request.minIntervalMilliseconds)
            setMinUpdateDistanceMeters(request.minUpdateDistanceMeters)
            setMaxUpdateDelayMillis(request.maxUpdateDelayMilliseconds)
            setPriority(toGMSLocationPriority(request.accuracy))
            build()
        }
    }

    private fun toGMSLocationPriority(accuracy: LocationEngine.Request.Accuracy): Int {
        return when (accuracy) {
            LocationEngine.Request.Accuracy.PASSIVE -> Priority.PRIORITY_PASSIVE
            LocationEngine.Request.Accuracy.LOW -> Priority.PRIORITY_LOW_POWER
            LocationEngine.Request.Accuracy.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            LocationEngine.Request.Accuracy.HIGH -> Priority.PRIORITY_HIGH_ACCURACY
        }
    }
}
