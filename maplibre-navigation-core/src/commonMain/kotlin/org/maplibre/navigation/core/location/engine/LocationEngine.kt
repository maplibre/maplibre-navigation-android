package org.maplibre.navigation.core.location.engine

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.maplibre.navigation.core.location.Location

/**
 * Location engine that is used to fetch current location and listen to location updates.
 */
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

    /**
     * Request to configure location updates parameters
     */
    data class Request(
        /**
         * Minimum interval between location updates. This is the fastest interval that will
         * be used to get location updates.
         */
        val minIntervalMilliseconds: Long = 1000,

        /**
         * Maximum interval between location updates. This is the slowest interval that will
         * be used to get location updates.
         */
        val maxIntervalMilliseconds: Long = 2000,

        /**
         * Minimum distance between location updates. All updates that are closer than
         * this distance will be ignored.
         */
        val minUpdateDistanceMeters: Float = 0f,

        /**
         * Maximum delay between location updates. If the location updates occur at shorter intervals,
         * they may be sent as a batch
         */
        val maxUpdateDelayMilliseconds: Long = 1000,

        /**
         * Accuracy type for location fetching
         */
        val accuracy: Accuracy = Accuracy.HIGH,
    ) {

        /**
         * Accuracy type of location updates.
         */
        enum class Accuracy {
            /**
             * Passive accuracy will don't enable any sensores or location fetching, it will only
             * receive location updates that are fetched by other apps.
             */
            PASSIVE,

            /**
             * Low accuracy for save battery power.
             */
            LOW,

            /**
             * A balanced mid accuracy, that is saving battery power and give good location results
             */
            BALANCED,

            /**
             * Highest possible accuracy. This all possible sensors and calculate the most recend location.
             * Because of intensive use of sensors and GPS, it will costs more battery power.
             */
            HIGH
        }
    }
}