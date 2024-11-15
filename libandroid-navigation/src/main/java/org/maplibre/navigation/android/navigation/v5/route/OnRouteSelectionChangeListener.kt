package org.maplibre.navigation.android.navigation.v5.route

import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute

/**
 * Listener for determining which current route the user has selected as their primary route for
 * navigation.
 *
 * @since 0.8.0
 */
@Deprecated("Use in ui package instead")
interface OnRouteSelectionChangeListener {

    /**
     * Callback when the user selects a different route.
     *
     * @param directionsRoute the route which the user has currently selected
     * @since 0.8.0
     */
    fun onNewPrimaryRouteSelected(directionsRoute: DirectionsRoute?)
}
