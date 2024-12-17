package org.maplibre.navigation.core.location.engine

import kotlinx.coroutines.flow.Flow
import org.maplibre.navigation.core.location.Location

interface LocationEngine {

    /**
     * Listen to location updates.
     *
     * @param request request to configure location updates parameters
     * @return flow of location updates
     */
    fun listenToLocation(request: Request): Flow<Location>

    /**
     * Get last known location. If last location is not available, this method will return null.
     *
     * @return last known location or null if not available
     */
    suspend fun getLastLocation(): Location?


    data class Request(
        val minIntervalMilliseconds: Long = 1000,
        val maxIntervalMilliseconds: Long = 2000,
        val minUpdateDistanceMeters: Float = 0f,
        val maxUpdateDelayMilliseconds: Long = 1000,
        val accuracy: Accuracy = Accuracy.HIGH,
    ) {

        enum class Accuracy {
            HIGH
        }
    }
}