package org.maplibre.navigation.core.location

import kotlinx.coroutines.flow.Flow
import org.maplibre.android.location.engine.LocationEngineRequest

interface LocationEngine {

    /**
     * Listen to location updates.
     *
     * @param request request to configure location updates parameters
     * @return flow of location updates
     */
    fun listenToLocation(request: LocationEngineRequest): Flow<Location>

    /**
     * Get last known location. If last location is not available, this method will return null.
     *
     * @return last known location or null if not available
     */
    suspend fun getLastLocation(): Location?
}