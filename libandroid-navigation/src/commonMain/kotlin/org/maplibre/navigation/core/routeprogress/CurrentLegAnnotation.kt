package org.maplibre.navigation.core.routeprogress

import org.maplibre.navigation.core.models.MaxSpeed

/**
 * This class represents the current annotation being traveled along at a given time
 * during navigation.
 *
 *
 * The Mapbox Directions API gives a list of annotations, each item in the list representing an
 * annotation between two points along the leg.
 *
 * @since 0.13.0
 */
data class CurrentLegAnnotation(

    /**
     * The index used to retrieve the annotation values from each array in
     * [org.maplibre.navigation.core.models].
     *
     * @since 0.13.0
     */
    val index: Int,

    /**
     * Distance along the [org.maplibre.navigation.core.models.RouteLeg] that adds
     * up to this set of annotation data.
     *
     * @since 0.13.0
     */
    val distanceToAnnotation: Double,

    /**
     * The distance, in meters, for the given annotation segment.
     *
     * @since 0.13.0
     */
    val distance: Double,

    /**
     * The speed, in meters per second, for the given annotation segment.
     *
     * @since 0.13.0
     */
    val duration: Double?,

    /**
     * The speed, in meters per second, for the given annotation segment.
     *
     * @since 0.13.0
     */
    val speed: Double?,

    /**
     * The posted speed limit, for the given annotation segment.
     * Maxspeed is only available for the `mapbox/driving` and `mapbox/driving-traffic`
     * profiles, other profiles will return `unknown`s only.
     *
     * @since 0.13.0
     */
    val maxSpeed: MaxSpeed?,

    /**
     * The congestion for the given annotation segment.
     *
     * @since 0.13.0
     */
    val congestion: String? = null
)
