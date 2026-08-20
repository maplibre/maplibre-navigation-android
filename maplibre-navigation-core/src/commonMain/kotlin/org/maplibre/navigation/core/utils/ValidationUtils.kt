package org.maplibre.navigation.core.utils

import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.models.RouteOptions
import kotlin.jvm.JvmStatic

object ValidationUtils {

    @JvmStatic
    fun validDirectionsRoute(
        directionsRoute: DirectionsRoute,
        defaultMilestonesEnabled: Boolean
    ) {
        if (defaultMilestonesEnabled) {
            val routeOptions = directionsRoute.routeOptions ?: throw IllegalStateException(
                "Using the default milestones requires the "
                        + "directions route to include the route options object."
            )
            checkInvalidVoiceInstructions(routeOptions)
            checkInvalidBannerInstructions(routeOptions)
        }

        for ((index, leg) in directionsRoute.legs.withIndex()) {
            val annotation = leg.annotation
            if (annotation?.maxSpeed != null && annotation.distance == null) {
                throw IllegalArgumentException("Leg $index has maxspeed annotation but missing distance annotation")
            }
        }
    }

    private fun checkInvalidVoiceInstructions(routeOptions: RouteOptions) {
        if (routeOptions.voiceInstructions != true) {
            throw IllegalStateException(
                "Using the default milestones requires the "
                        + "directions route to be requested with voice instructions enabled."
            )
        }
    }

    private fun checkInvalidBannerInstructions(routeOptions: RouteOptions) {
        if (routeOptions.bannerInstructions != true) {
            throw IllegalStateException(
                "Using the default milestones requires the "
                        + "directions route to be requested with banner instructions enabled."
            )
        }
    }
}
