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

/**
 * A [LocationEngine] that uses the Apple CLLocationManager API.
 */
open class AppleLocationEngine : NSObject(), LocationEngine, CLLocationManagerDelegateProtocol {

    /**
     * Underlying CLLocationManager that is used to fetch location and listen to location updates.
     */
    private val locationManager = CLLocationManager().also { locationManager ->
        locationManager.delegate = this
    }

    private val locationFlow = MutableStateFlow<Location?>(null)

    override fun listenToLocation(request: LocationEngine.Request): Flow<Location> {
        locationManager.startUpdatingLocation()

        return locationFlow.filterNotNull()
    }

    override suspend fun getLastLocation(): Location? {
        return locationFlow.firstOrNull()
    }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        (didUpdateLocations.lastOrNull() as? AppleLocation?)
            ?.toLocation()
            ?.let { location ->
                locationFlow.tryEmit(location)
            }

        if (locationFlow.subscriptionCount.value == 0) {
            locationManager.stopUpdatingLocation()
        }
    }
}
