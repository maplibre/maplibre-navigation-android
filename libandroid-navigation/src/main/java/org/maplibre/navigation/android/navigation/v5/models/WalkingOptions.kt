package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class for specifying options for use with the walking profile.
 * @since 4.8.0
 */
@Serializable
data class WalkingOptions(

    /**
     * Walking speed in meters per second. Must be between 0.14 and 6.94 meters per second.
     * Defaults to 1.42 meters per second
     *
     * @since 4.8.0
     */
    @SerialName("walking_speed")
    val walkingSpeed: Double?,

    /**
     * A bias which determines whether the route should prefer or avoid the use of roads or paths
     * that are set aside for pedestrian-only use (walkways). The allowed range of values is from
     * -1 to 1, where -1 indicates indicates preference to avoid walkways, 1 indicates preference
     * to favor walkways, and 0 indicates no preference (the default).
     *
     * @since 4.8.0
     */
    @SerialName("walkway_bias")
    val walkwayBias: Double?,

    /**
     * A bias which determines whether the route should prefer or avoid the use of alleys. The
     * allowed range of values is from -1 to 1, where -1 indicates indicates preference to avoid
     * alleys, 1 indicates preference to favor alleys, and 0 indicates no preference (the default).
     *
     * @since 4.8.0
     */
    @SerialName("alley_bias")
    val alleyBias: Double?,
)
