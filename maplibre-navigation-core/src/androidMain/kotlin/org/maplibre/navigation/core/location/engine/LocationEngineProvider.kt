package org.maplibre.navigation.core.location.engine

import android.content.Context
import android.os.Looper

/**
 * The main entry point for location engine integration.
 */
object LocationEngineProvider {

    /**
     * Returns instance to the best location engine, given the included libraries.
     *
     * @param context [Context].
     * @return a unique instance of [LocationEngine] every time method is called.
     * @since 1.1.0
     */
    @JvmStatic
    fun getBestLocationEngine(context: Context): LocationEngine {
        return MapLibreLocationEngine(
            context = context.applicationContext,
            looper = Looper.getMainLooper()
        )
    }
}
