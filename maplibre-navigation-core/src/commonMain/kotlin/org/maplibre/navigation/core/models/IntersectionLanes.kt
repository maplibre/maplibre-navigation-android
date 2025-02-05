package org.maplibre.navigation.core.models

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
    val valid: Boolean? = null,

    /**
     * Indicates whether this lane is a preferred lane (true) or not (false).
     * A preferred lane is a lane that is recommended if there are multiple lanes available.
     * For example, if guidance indicates that the driver must turn left at an intersection
     * and there are multiple left turn lanes, the left turn lane that will better prepare
     * the driver for the next maneuver will be marked as active.
     * Only available on the mapbox/driving profile.
     *
     */
    val active: Boolean? = null,

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
    val validIndication: String? = null,

    /**
     * Array that can be made up of multiple signs such as `left`, `right`, etc.
     * There can be multiple signs. For example, a turning
     * lane can have a sign with an arrow pointing left and another sign with an arrow pointing
     * straight.
     *
     * @since 2.0.0
     */
    val indications: List<String>? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `IntersectionLanes` instance.
     */
    fun toBuilder(): Builder {
        return Builder()
            .withValid(valid)
            .withActive(active)
            .withValidIndication(validIndication)
            .withIndications(indications)
    }

    /**
     * Builder class for creating `IntersectionLanes` instances.
     * @param valid Provides a boolean value you can use to determine if the given lane is valid for the user to complete the maneuver.
     * @param active Indicates whether this lane is a preferred lane (true) or not (false).
     * @param validIndication When either valid or active is set to true, this property shows which of the lane indications is applicable to the current route.
     * @param indications Array that can be made up of multiple signs such as `left`, `right`, etc.
     */
    class Builder {
        private var valid: Boolean? = null
        private var active: Boolean? = null
        private var validIndication: String? = null
        private var indications: List<String>? = null

        /**
         * Sets the valid status.
         *
         * @param valid The valid status.
         * @return The builder instance.
         */
        fun withValid(valid: Boolean?) = apply { this.valid = valid }

        /**
         * Sets the active status.
         *
         * @param active The active status.
         * @return The builder instance.
         */
        fun withActive(active: Boolean?) = apply { this.active = active }

        /**
         * Sets the valid indication.
         *
         * @param validIndication The valid indication.
         * @return The builder instance.
         */
        fun withValidIndication(validIndication: String?) =
            apply { this.validIndication = validIndication }

        /**
         * Sets the indications.
         *
         * @param indications The indications.
         * @return The builder instance.
         */
        fun withIndications(indications: List<String>?) = apply { this.indications = indications }

        /**
         * Builds an `IntersectionLanes` instance with the current builder values.
         *
         * @return A new `IntersectionLanes` instance.
         */
        fun build(): IntersectionLanes {
            return IntersectionLanes(
                valid = valid,
                active = active,
                validIndication = validIndication,
                indications = indications
            )
        }
    }
}
