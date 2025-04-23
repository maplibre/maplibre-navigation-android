package org.maplibre.navigation.core.location.engine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import platform.CoreLocation.CLLocationManager
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.toLocation
import platform.CoreLocation.CLLocation as AppleLocation
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.suspendCoroutine
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A [LocationEngine] implementation that uses the Apple CLLocationManager API to provide
 * location updates and fetch the last known location.
 */
open class AppleLocationEngine : LocationEngine {

    /**
     * A [MutableStateFlow] that holds the current location or null if no location was emitted yet.
     */
    private val locationFlow = MutableStateFlow<Location?>(null)

    /**
     * The underlying CLLocationManager instance used to fetch location updates.
     */
    private val locationManager = CLLocationManager().also { locationManager ->
        locationManager.delegate = LocationDelegate(locationFlow)
    }

    /**
     * Starts listening to location updates based on the provided [request].
     *
     * @param request The location request parameters.
     * @return A [Flow] emitting location updates.
     */
    override fun listenToLocation(request: LocationEngine.Request): Flow<Location> {
        locationManager.startUpdatingLocation()
        return locationFlow.filterNotNull()
    }

    /**
     * Retrieves the last known location. If no location is available, it fetches the current location.
     *
     * @return The last known [Location] or null if unavailable.
     */
    override suspend fun getLastLocation(): Location? {
        return locationFlow.firstOrNull()
            ?: getLocation()
                .also { currentLocation ->
                    locationFlow.emit(currentLocation)
                }
    }

    /**
     * Suspends the coroutine and fetches the current location using CLLocationManager.
     *
     * @return The current [Location] or null if unavailable.
     */
    private suspend fun getLocation(): Location? = suspendCoroutine { continuation ->
        val locationManager = CLLocationManager()
        locationManager.delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            /**
             * Called when the location manager updates the location.
             *
             * @param manager The CLLocationManager instance.
             * @param didUpdateLocations The list of updated locations.
             */
            override fun locationManager(
                manager: CLLocationManager,
                didUpdateLocations: List<*>
            ) {
                continuation.resume((didUpdateLocations.lastOrNull() as? AppleLocation?)?.toLocation())
            }

            /**
             * Called when the location manager fails to fetch the location.
             *
             * @param manager The CLLocationManager instance.
             * @param didFailWithError The error that occurred.
             */
            override fun locationManager(
                manager: CLLocationManager,
                didFailWithError: NSError
            ) {
                continuation.resumeWithException(Exception(didFailWithError.localizedDescription))
            }
        }

        locationManager.requestLocation()
    }

    /**
     * A delegate class for handling location updates and emitting them to the [locationFlow].
     *
     * @param locationFlow The [MutableStateFlow] to emit location updates to.
     */
    class LocationDelegate(private val locationFlow: MutableStateFlow<Location?>): NSObject(), CLLocationManagerDelegateProtocol {

        /**
         * Called when the location manager updates the location.
         *
         * @param manager The CLLocationManager instance.
         * @param didUpdateLocations The list of updated locations.
         */
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            (didUpdateLocations.lastOrNull() as? AppleLocation?)
                ?.toLocation()
                ?.let { location ->
                    locationFlow.tryEmit(location)
                }

            // Stop updating location if there are no active subscribers.
            if (locationFlow.subscriptionCount.value == 0) {
                manager.stopUpdatingLocation()
            }
        }
    }
}