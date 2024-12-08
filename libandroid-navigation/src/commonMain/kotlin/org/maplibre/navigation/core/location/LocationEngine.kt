package org.maplibre.navigation.core.location

import kotlinx.coroutines.flow.Flow
import org.maplibre.android.location.engine.LocationEngineRequest

interface LocationEngine {

    fun listenToLocation(request: LocationEngineRequest): Flow<Location>

    suspend fun getLastLocation(): Location?
}