package org.maplibre.navigation.core.models

import kotlinx.serialization.Serializable

/**
 * An object containing information about a toll collection point along the route.
 * This is a payment booth or overhead electronic gantry
 * [payment booth or overhead electronic gantry](https://wiki.openstreetmap.org/wiki/Tag:barrier%3Dtoll_booth)
 * where toll charge is collected.
 * Only available on the [DirectionsCriteria.PROFILE_DRIVING] profile.
 */
@Serializable
data class TollCollection(

    /**
     * The type of toll collection point, either `toll_booth` or `toll_gantry`.
     * Note that adding new possible types is not considered a breaking change.
     */
    val type: String? = null
)
