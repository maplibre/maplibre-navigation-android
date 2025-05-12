package org.maplibre.navigation.core.utils

/**
 * Includes common variables used throughout the MapLibre Service modules.
 *
 * @since 3.0.0
 */
object Constants {

    /**
     * Base URL for all API calls, not hardcoded to enable testing.
     *
     * @since 1.0.0
     */
    @Deprecated("Mapbox specific API URL. Will be removed in future.")
    const val BASE_API_URL: String = "https://api.mapbox.com"

    /**
     * The default user variable used for all the Mapbox user names.
     *
     * @since 1.0.0
     */
    @Suppress("unused")
    @Deprecated("Mapbox specific parameter. Will be removed in future.")
    const val MAPBOX_USER: String = "mapbox"

    /**
     * Use a precision of 6 decimal places when encoding or decoding a polyline.
     *
     * @since 2.1.0
     */
    const val PRECISION_6: Int = 6

    /**
     * Use a precision of 5 decimal places when encoding or decoding a polyline.
     *
     * @since 1.0.0
     */
    @Suppress("unused")
    const val PRECISION_5: Int = 5
}
