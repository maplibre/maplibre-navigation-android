package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.Serializable

/**
 * An object containing information about passing rest stops along the route.
 * Only available on the [DirectionsCriteria.PROFILE_DRIVING] profile.
 */
@Serializable
data class RestStop(
    /**
     * The type of rest stop, either `rest_area` (includes parking only) or `service_area`
     * (includes amenities such as gas or restaurants).
     * Note that adding new possible types is not considered a breaking change.
     */
    val type: String?
)
