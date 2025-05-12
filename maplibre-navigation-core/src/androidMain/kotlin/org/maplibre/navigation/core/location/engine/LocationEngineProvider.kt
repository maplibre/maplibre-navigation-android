package org.maplibre.navigation.core.location.engine

import android.content.Context
import android.os.Looper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * The main entry point for location engine integration.
 */
object LocationEngineProvider {
    private const val GOOGLE_LOCATION_SERVICES = "com.google.android.gms.location.LocationServices"
    private const val GOOGLE_API_AVAILABILITY =
        "com.google.android.gms.common.GoogleApiAvailability"

    /**
     * Returns instance to the best location engine, given the included libraries.
     *
     * @param context [Context].
     * @return a unique instance of [LocationEngine] every time method is called.
     * @since 1.1.0
     */
    @JvmStatic
    fun getBestLocationEngine(context: Context): LocationEngine {
        return getLocationEngine(
            context,
            isOnClasspath(GOOGLE_LOCATION_SERVICES)
                    && isOnClasspath(GOOGLE_API_AVAILABILITY)
                    && GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        )
    }

    private fun getLocationEngine(context: Context, isGoogle: Boolean): LocationEngine {
        return when (isGoogle) {
            true -> GoogleLocationEngine(
                context = context.applicationContext,
                looper = Looper.getMainLooper()
            )

            false ->
                MapLibreLocationEngine(
                    context = context.applicationContext,
                    looper = Looper.getMainLooper()
                )
        }
    }

    /**
     * Checks if class is on class path
     *
     * @param className of the class to check.
     * @return true if class in on class path, false otherwise.
     */
    private fun isOnClasspath(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (exception: ClassNotFoundException) {
            false
        }
    }
}
