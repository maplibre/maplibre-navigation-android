package org.maplibre.navigation.core.location.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocationManager
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.toLocation
import platform.CoreLocation.CLLocation as AppleLocation
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.suspendCoroutine
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration

/**
 * A [LocationEngine] implementation that uses the Apple CLLocationManager API to provide
 * location updates and fetch the last known location.
 *
 * Because the Apple side doesn't provide a last known location from system, we need to store
 * them in memory by ourself. This means also, that the first request needs to fetch the current
 * location. You can define a timeout for this request in the constructor.
 *
 * @param getLocationTimeout The maximum duration to wait for the one-time location request.
 */
open class AppleLocationEngine(private val getLocationTimeout: Duration = 3.seconds) :
    LocationEngine {

    /**
     * A [MutableStateFlow] that holds the current location or null if no location was emitted yet.
     */
    private val locationFlow = MutableStateFlow<Location?>(null)

    /**
     * A delegate instance responsible for handling location updates and emitting them to `locationFlow`.
     */
    private val locationDelegate = LocationDelegate(locationFlow)

    /**
     * The underlying CLLocationManager instance used to fetch location updates.
     */
    private val locationManager = CLLocationManager().also { locationManager ->
        locationManager.delegate = locationDelegate
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
     * Retrieves the last known location. If no location is stored in memory cached, we try fetching
     * the current position. The configured timeout in constructor is used to determine how long we
     * wait for maximum.
     *
     * @return The last known [Location] or null if unavailable or the timeout exceeded.
     */
    override suspend fun getLastLocation(): Location? {
        return locationFlow.firstOrNull()
            ?: getLocation(getLocationTimeout)
                .also { currentLocation ->
                    locationFlow.emit(currentLocation)
                }
    }

    /**
     * Fetches the current location using CLLocationManager.
     * *Note: The LocationManager needs to run on the main thread. [Dispatchers.Main] is used for this*
     *
     * @param timeout The maximum duration to wait for the location update.
     * @return The current [Location] or null if unavailable or the timeout is exceeded.
     */
    private suspend fun getLocation(timeout: Duration): Location? = withContext(Dispatchers.Main) {
        withTimeoutOrNull(timeout) {
            suspendCoroutine { continuation ->
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
                        val location =
                            (didUpdateLocations.lastOrNull() as? AppleLocation?)?.toLocation()
                        continuation.resume(location)
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
        }
    }

    /**
     * A delegate class for handling location updates and emitting them to the [locationFlow].
     *
     * @param locationFlow The [MutableStateFlow] to emit location updates to.
     */
    private class LocationDelegate(private val locationFlow: MutableStateFlow<Location?>) :
        NSObject(),
        CLLocationManagerDelegateProtocol {

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
                    locationFlow.update { location }
                }

            // Stop updating location if there are no active subscribers.
            if (locationFlow.subscriptionCount.value == 0) {
                manager.stopUpdatingLocation()
            }
        }
    }
}