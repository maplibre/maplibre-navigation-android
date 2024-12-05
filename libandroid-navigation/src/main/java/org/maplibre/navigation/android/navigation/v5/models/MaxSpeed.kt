package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.Serializable

/**
 * Object representing max speeds along a route.
 *
 * @since 3.0.0
 */
@Serializable
data class MaxSpeed(
    /**
     * Number indicating the posted speed limit.
     *
     * @since 3.0.0
     */
    val speed: Int?,

    /**
     * String indicating the unit of speed, either as `km/h` or `mph`.
     *
     * @since 3.0.0
     */
    val unit: SpeedLimit.Unit?,

    /**
     * Boolean is true if the speed limit is not known, otherwise null.
     *
     * @since 3.0.0
     */
    val unknown: Boolean?,

    /**
     * Boolean is `true` if the speed limit is unlimited, otherwise null.
     *
     * @since 3.0.0
     */
    val none: Boolean?,
)