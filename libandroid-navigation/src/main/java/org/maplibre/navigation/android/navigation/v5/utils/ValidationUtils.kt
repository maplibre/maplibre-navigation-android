package org.maplibre.navigation.android.navigation.v5.utils

import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions
import java.util.MissingFormatArgumentException

object ValidationUtils {

    @JvmStatic
    fun validDirectionsRoute(
        directionsRoute: DirectionsRoute,
        defaultMilestonesEnabled: Boolean
    ) {
        if (defaultMilestonesEnabled) {
            val routeOptions = directionsRoute.routeOptions ?: throw MissingFormatArgumentException(
                "Using the default milestones requires the "
                        + "directions route to include the route options object."
            )
            checkInvalidVoiceInstructions(routeOptions)
            checkInvalidBannerInstructions(routeOptions)
        }
    }

    private fun checkInvalidVoiceInstructions(routeOptions: RouteOptions) {
        if (routeOptions.voiceInstructions != true) {
            throw MissingFormatArgumentException(
                "Using the default milestones requires the "
                        + "directions route to be requested with voice instructions enabled."
            )
        }
    }

    private fun checkInvalidBannerInstructions(routeOptions: RouteOptions) {
        if (routeOptions.bannerInstructions != true) {
            throw MissingFormatArgumentException(
                "Using the default milestones requires the "
                        + "directions route to be requested with banner instructions enabled."
            )
        }
    }
}
