package org.maplibre.navigation.android.navigation.v5.navigation

import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineResult
import android.location.Location as AndroidLocation
import org.maplibre.navigation.android.navigation.v5.location.Location
import org.maplibre.navigation.android.navigation.v5.location.LocationValidator

open class NavigationLocationEngineListener(
    private val thread: RouteProcessorBackgroundThread,
    private val mapLibreNavigation: MapLibreNavigation,
    private val validator: LocationValidator
) : LocationEngineCallback<LocationEngineResult> {

    fun isValidLocationUpdate(location: Location): Boolean {
        return validator.isValidUpdate(location)
    }

    /**
     * Queues a new task created from a location update to be sent
     * to [RouteProcessorBackgroundThread] for processing.
     *
     * @param location to be processed
     */
    fun queueLocationUpdate(location: Location) {
        thread.queueUpdate(NavigationLocationUpdate(location, mapLibreNavigation))
    }

    override fun onSuccess(result: LocationEngineResult) {
        result.lastLocation?.let { lastLocation ->
val location = lastLocation.toLocation()
            if (isValidLocationUpdate(location)) {
                queueLocationUpdate(location)
            }
        }
    }

    override fun onFailure(exception: Exception) {
    }

    fun AndroidLocation.toLocation() = Location(
        latitude = latitude,
        longitude = longitude,
        bearing = bearing.takeIf { hasBearing() },
        speedMetersPerSeconds = speed.takeIf { hasSpeed() },
        accuracyMeters = accuracy.takeIf { hasAccuracy() }
    )
}