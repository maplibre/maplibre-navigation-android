package org.maplibre.navigation.core.location.engine

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.toLocation

/**
 * Location engine backed directly by the platform [LocationManager].
 *
 * Exactly one provider is subscribed per request, and its fixes are passed through unfiltered:
 *
 * - On Android 12+ (API 31) the system `fused` provider is used. It fuses GPS, Wi-Fi and cell
 *   in the platform itself and does not require Google Play Services. Updates are requested
 *   with an explicit [LocationRequest] quality, because the legacy
 *   `requestLocationUpdates(provider, minTime, minDistance, ...)` overload implies a low-power
 *   mode in which the fused provider never engages GPS and only delivers coarse,
 *   network-quality fixes (roughly one every 20 seconds).
 * - Below API 31 there is no GMS-free fused provider, so the raw GPS provider is used,
 *   with the network provider as fallback on devices without GPS hardware.
 *
 * Subscribing a single provider makes client-side arbitration between conflicting GPS and
 * network fixes unnecessary, so no filtering heuristic is applied.
 *
 * @param context used to obtain the [LocationManager]
 * @param looper looper that location updates are delivered on; the main looper is used when null
 */
open class MapLibreLocationEngine(
    context: Context,
    private val looper: Looper?,
) : LocationEngine {

    private val locationManager =
        context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    override fun listenToLocation(request: LocationEngine.Request): Flow<Location> = callbackFlow {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: android.location.Location) {
                trySend(location.toLocation())
            }

            // Explicit no-op overrides: the default implementations require newer API levels than minSdk
            @Deprecated("Deprecated in LocationListener")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val provider = selectProvider(request.accuracy)
        val callbackLooper = looper ?: Looper.getMainLooper()
        Logger.d { "Requesting location updates from provider '$provider'" }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val platformRequest = LocationRequest.Builder(request.intervalMilliseconds)
                .setQuality(
                    when (request.accuracy) {
                        LocationEngine.Request.Accuracy.HIGH -> LocationRequest.QUALITY_HIGH_ACCURACY
                        LocationEngine.Request.Accuracy.MEDIUM -> LocationRequest.QUALITY_BALANCED_POWER_ACCURACY
                        else -> LocationRequest.QUALITY_LOW_POWER
                    }
                )
                .setMinUpdateDistanceMeters(request.minUpdateDistanceMeters)
                .build()
            val handler = Handler(callbackLooper)
            locationManager.requestLocationUpdates(provider, platformRequest, { handler.post(it) }, listener)
        } else {
            // Pre-31 the provider is GPS or network, where the implied request quality is irrelevant
            locationManager.requestLocationUpdates(
                provider,
                request.intervalMilliseconds,
                request.minUpdateDistanceMeters,
                listener,
                callbackLooper,
            )
        }

        awaitClose { locationManager.removeUpdates(listener) }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? =
        locationManager.allProviders
            .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
            .maxByOrNull { location -> location.time }
            ?.toLocation()

    private fun selectProvider(accuracy: LocationEngine.Request.Accuracy): String {
        val providers = locationManager.allProviders
        return when {
            // A LOWEST request must not engage any sensor, like PRIORITY_NO_POWER did before
            accuracy == LocationEngine.Request.Accuracy.LOWEST -> LocationManager.PASSIVE_PROVIDER
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                providers.contains(LocationManager.FUSED_PROVIDER) -> LocationManager.FUSED_PROVIDER
            providers.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            providers.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> LocationManager.PASSIVE_PROVIDER
        }
    }
}
