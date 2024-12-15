package org.maplibre.navigation.core.location.engine

import android.content.Context
import android.location.LocationListener
import android.os.Looper
import android.location.Location as AndroidLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.location.engine.MapLibreFusedLocationEngineImpl
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.LocationEngine
import org.maplibre.navigation.core.location.toLocation
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Location engine, that using the default MapLibreLocation engine.
 *
 * @param context used to initialize the underlying [MapLibreFusedLocationEngineImpl]
 * @param looper looper that is ued by the [MapLibreFusedLocationEngineImpl] to listen on for location updates
 */
open class MapLibreLocationEngine(
    context: Context,
    private val looper: Looper
) : LocationEngine {

    /**
     * Underlying [MapLibreFusedLocationEngineImpl] that is used to fetch location and listen to location updates.
     */
    private val maplibreLocationEngine = MapLibreFusedLocationEngineImpl(context)

    override fun listenToLocation(request: LocationEngineRequest): Flow<Location> = callbackFlow {
        val listener = LocationListener { location -> trySend(location.toLocation()) }

        maplibreLocationEngine.requestLocationUpdates(
            request,
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

            override fun onFailure(p0: Exception) {
                //TODO fabi755
            }
        })
    }
}
