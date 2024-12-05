package org.maplibre.navigation.android.navigation.v5.navigation

import android.annotation.SuppressLint
import android.location.Location as AndroidLocation
import android.os.Looper
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.navigation.android.navigation.v5.location.Location
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.utils.RouteUtils
import timber.log.Timber

open class NavigationLocationEngineUpdater(
    private var locationEngine: LocationEngine,
    private val listener: NavigationLocationEngineListener,
    private val routeUtils: RouteUtils
) {

    init {
        requestLocationUpdates()
    }

    fun updateLocationEngine(locationEngine: LocationEngine) {
        removeLocationEngineListener()
        this.locationEngine = locationEngine
        requestLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        locationEngine.requestLocationUpdates(
            LocationEngineRequest.Builder(LOCATION_ENGINE_INTERVAL)
                .setFastestInterval(LOCATION_ENGINE_INTERVAL)
                .build(),
            listener,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    fun forceLocationUpdate(route: DirectionsRoute) {
        locationEngine.getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult) {
                listener.queueLocationUpdate(
                    result.lastLocation
                        ?.toLocation()
                        ?.takeIf { loc -> listener.isValidLocationUpdate(loc) }
                        ?: routeUtils.createFirstLocationFromRoute(route)
                )
            }

            override fun onFailure(exception: Exception) {
                Timber.w(exception, "Cannot get a forced location update")
            }
        })
    }

    fun removeLocationEngineListener() {
        locationEngine.removeLocationUpdates(listener)
    }

    fun AndroidLocation.toLocation() = Location(
        latitude = latitude,
        longitude = longitude,
        bearing = bearing.takeIf { hasBearing() },
        speedMetersPerSeconds = speed.takeIf { hasSpeed() },
        accuracyMeters = accuracy.takeIf { hasAccuracy() }
    )

    companion object {
        private const val LOCATION_ENGINE_INTERVAL = 1000L
    }
}
