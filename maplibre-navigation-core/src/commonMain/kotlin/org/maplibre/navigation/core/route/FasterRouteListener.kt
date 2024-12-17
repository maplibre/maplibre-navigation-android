package org.maplibre.navigation.core.route

import org.maplibre.navigation.core.models.DirectionsRoute

/**
 * Listener that can be added to monitor faster routes retrieved
 * based on the logic set in [FasterRoute].
 */
fun interface FasterRouteListener {

    /**
     * Will be fired when a faster route has been found based on the logic
     * provided by [FasterRoute].
     *
     * @param directionsRoute faster route retrieved
     */
    fun fasterRouteFound(directionsRoute: DirectionsRoute?)
}
