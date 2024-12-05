package org.maplibre.navigation.android.navigation.v5.navigation

import android.location.Location
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineResult
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
            if (isValidLocationUpdate(lastLocation)) {
                queueLocationUpdate(lastLocation)
            }
        }
    }

    override fun onFailure(exception: Exception) {
    }
}