package org.maplibre.navigation.android.navigation.ui.v5


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.maplibre.android.location.LocationComponent
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.LocationValidator
import org.maplibre.navigation.core.location.engine.LocationEngine
import org.maplibre.navigation.core.location.toAndroidLocation
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions.Defaults
import org.maplibre.navigation.core.navigation.engine.MapLibreNavigationEngine.Companion.LOCATION_ENGINE_INTERVAL

class PreNavigationLocationEngine(
    private val locationEngine: LocationEngine,
    private val locationComponent: LocationComponent,
    private val locationValidator: LocationValidator = LocationValidator(Defaults.LOCATION_ACCEPTABLE_ACCURACY_IN_METERS_THRESHOLD),//todo maybe provide accuracyThreshold through options
    private val backgroundScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private var collectLocationJob: Job? = null

    fun start() {
        collectLocationJob?.cancel() // Cancel previous started run

        collectLocationJob = backgroundScope.launch {
            locationEngine.getLastLocation()?.let { processLocationUpdate(it) }

            locationEngine.listenToLocation(
                LocationEngine.Request(
                    minIntervalMilliseconds = LOCATION_ENGINE_INTERVAL,
                    maxIntervalMilliseconds = LOCATION_ENGINE_INTERVAL,
                )
            ).collect(::processLocationUpdate)
        }
    }

    fun stop() {
        collectLocationJob?.cancel()
        collectLocationJob = null
    }

    private fun processLocationUpdate(rawLocation: Location) {
        if (!locationValidator.isValidUpdate(rawLocation)) {
            return
        }
        mainScope.launch {
            locationComponent.forceLocationUpdate(rawLocation.toAndroidLocation())
        }
    }
}
