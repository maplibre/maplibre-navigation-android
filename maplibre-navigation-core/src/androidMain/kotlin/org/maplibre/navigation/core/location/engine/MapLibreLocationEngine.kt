package org.maplibre.navigation.core.location.engine

import android.content.Context
import android.location.LocationListener
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest as MapLibreLocationRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.location.engine.MapLibreFusedLocationEngineImpl
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.toLocation
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Location engine, that using the default MapLibreLocation engine.
 *
 * @param context used to initialize the underlying [MapLibreFusedLocationEngineImpl]
 * @param looper looper that is ued by the [MapLibreFusedLocationEngineImpl] to listen on for location updates
 */
open class MapLibreLocationEngine(
    context: Context,
    private val looper: Looper?
) : LocationEngine {

    /**
     * Underlying [MapLibreFusedLocationEngineImpl] that is used to fetch location and listen to location updates.
     */
    private val maplibreLocationEngine = MapLibreFusedLocationEngineImpl(context)

    override fun listenToLocation(request: LocationEngine.Request): Flow<Location> = callbackFlow {
        val listener = LocationListener { location -> trySend(location.toLocation()) }

        maplibreLocationEngine.requestLocationUpdates(
            toMapLibreLocationRequest(request),
            listener,
            looper,
        )

        awaitClose { maplibreLocationEngine.removeLocationUpdates(listener) }
    }

    override suspend fun getLastLocation(): Location? = suspendCoroutine { continuation ->
        maplibreLocationEngine.getLastLocation(object :
            LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(locationEngineResult: LocationEngineResult) {
                continuation.resume(locationEngineResult.lastLocation?.toLocation())
            }

            override fun onFailure(exception: Exception) {
                continuation.resumeWithException(exception)
            }
        })
    }

    private fun toMapLibreLocationRequest(request: LocationEngine.Request): MapLibreLocationRequest {
        return MapLibreLocationRequest.Builder(request.maxIntervalMilliseconds)
            .setFastestInterval(request.minIntervalMilliseconds)
            .setDisplacement(request.minUpdateDistanceMeters)
            .setMaxWaitTime(request.maxUpdateDelayMilliseconds)
            .setPriority(toMapLibrePriority(request.accuracy))
            .build()
    }

    private fun toMapLibrePriority(accuracy: LocationEngine.Request.Accuracy): Int {
        return when (accuracy) {
            LocationEngine.Request.Accuracy.PASSIVE -> MapLibreLocationRequest.PRIORITY_NO_POWER
            LocationEngine.Request.Accuracy.LOW -> MapLibreLocationRequest.PRIORITY_LOW_POWER
            LocationEngine.Request.Accuracy.BALANCED -> MapLibreLocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            LocationEngine.Request.Accuracy.HIGH -> MapLibreLocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}
