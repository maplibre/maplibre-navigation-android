package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Object representing lanes in an intersection.
 *
 * @since 2.0.0
 */
@Serializable
data class IntersectionLanes(
    /**
     * Provides a boolean value you can use to determine if the given lane is valid for the user to
     * complete the maneuver. For instance, if the lane array has four objects and the first two are marked as valid, then the
     * driver can take either of the left lanes and stay on the route.
     *
     * @since 2.0.0
     */
    val valid: Boolean?,

    /**
     * Indicates whether this lane is a preferred lane (true) or not (false).
     * A preferred lane is a lane that is recommended if there are multiple lanes available.
     * For example, if guidance indicates that the driver must turn left at an intersection
     * and there are multiple left turn lanes, the left turn lane that will better prepare
     * the driver for the next maneuver will be marked as active.
     * Only available on the mapbox/driving profile.
     *
     */
    val active: Boolean?,

    /**
     * When either valid or active is set to true, this property shows which of the lane indications
     * is applicable to the current route, when there is more than one. For example, if a lane allows
     * you to go left or straight but your current route is guiding you to the left,
     * then this value will be set to left.
     * See indications for possible values.
     * When both active and valid are false, this property will not be included in the response.
     * Only available on the mapbox/driving profile.
     */
    @SerialName("valid_indication")
    val validIndication: String?,

    /**
     * Array that can be made up of multiple signs such as `left`, `right`, etc.
     * There can be multiple signs. For example, a turning
     * lane can have a sign with an arrow pointing left and another sign with an arrow pointing
     * straight.
     *
     * @since 2.0.0
     */
    val indications: List<String>?,
)
