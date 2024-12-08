package org.maplibre.navigation.core.location.engine

import kotlinx.coroutines.flow.Flow
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.LocationEngine

actual class PlatformLocationEngine: LocationEngine {

    override fun listenToLocation(request: LocationEngineRequest): Flow<Location> {
        TODO("Not yet implemented")
    }

    override suspend fun getLastLocation(): Location? {
        TODO("Not yet implemented")
    }
}

