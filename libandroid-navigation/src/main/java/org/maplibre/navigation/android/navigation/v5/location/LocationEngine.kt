package org.maplibre.navigation.android.navigation.v5.location

import android.os.SystemClock
import kotlinx.coroutines.flow.Flow
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.geojson.Point

interface LocationEngine {

    fun listenToLocation(request: LocationEngineRequest): Flow<Location>

    suspend fun getLastLocation(): Location?
}