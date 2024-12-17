package org.maplibre.navigation.core.location.engine

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocationManager
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.toLocation
import platform.CoreLocation.CLLocation as AppleLocation
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * A [LocationEngine] that uses the Apple CLLocationManager API.
 */
open class AppleLocationEngine : LocationEngine {

    /**
     * Underlying CLLocationManager that is used to fetch location and listen to location updates.
     */
    private val locationManager: CLLocationManager = CLLocationManager()

    override fun listenToLocation(request: LocationEngine.Request): Flow<Location> = callbackFlow {
        locationManager.delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                (didUpdateLocations.lastOrNull() as? AppleLocation?)
                    ?.toLocation()
                    ?.let { location ->
                        trySend(location)
                    }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                close(Exception(didFailWithError.localizedDescription))
            }
        }

        locationManager.startUpdatingLocation()
        awaitClose { locationManager.stopUpdatingLocation() }
    }

    override suspend fun getLastLocation(): Location? = suspendCoroutine { continuation ->
        locationManager.delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(
                manager: CLLocationManager,
                didUpdateLocations: List<*>
            ) {
                continuation.resume((didUpdateLocations.lastOrNull() as? AppleLocation?)?.toLocation())
            }

            override fun locationManager(
                manager: CLLocationManager,
                didFailWithError: NSError
            ) {
                continuation.resumeWithException(Exception(didFailWithError.localizedDescription))
            }
        }

        locationManager.requestLocation()
    }
}
