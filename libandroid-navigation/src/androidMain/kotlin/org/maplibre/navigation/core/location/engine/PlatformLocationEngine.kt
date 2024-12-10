package org.maplibre.navigation.core.location.engine

import kotlinx.coroutines.flow.Flow
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.LocationEngine

actual class PlatformLocationEngine(
    internalLocationEngine: LocationEngine
) : LocationEngine by internalLocationEngine


