package org.maplibre.navigation.core.models.notification

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * The type of notifications. Notification types supported are `violation` and `alert`.
 * `violation` is more severe in nature and are delivered when
 * user-set parameters (ex: `exclude=unpaved`) are violated during route generation.
 *
 *
 * The severity of `alert` is less, and is intended to inform users when implicit preferences cannot
 * be satisfied (ex: `countryBorderCrossing`)
 * */
@Serializable
@JvmInline
value class NotificationType(val value: String) {
    companion object {
        /**
         * Alert that is relevant to a route leg.
         * */
        val Alert = NotificationType("alert")

        /**
         * RouteLeg violates a restriction.
         * */
        val Violation = NotificationType("violation")
    }
    /**
     * The optional subtype of notification
     * */
    @Serializable
    @JvmInline
    value class Subtype(val value: String) {
        companion object {
            /**
             * `type=violation`-> The height of the vehicle is greater than the allowed height of the road. It is provided when max_height is specified.
             * */
            val MaxHeight = Subtype("maxHeight")
            /**
             * `type=violation`-> The width of the vehicle is greater than the allowed width of the road. It is provided when max_width is specified.
             * */
            val MaxWidth = Subtype("maxWidth")
            /**
             * `type=violation`-> The weight of the vehicle is greater than the allowed weight of the road. It is provided when max_weight is specified.
             * */
            val MaxWeight = Subtype("maxWeight")
            /**
             * `type=violation`-> Unpaved road, while it was explicitly requested to exclude unpaved roads from the route. It is provided when exclude=unpaved
             * */
            val Unpaved = Subtype("unpaved")
            /**
             * `type=violation`-> Road with tunnel, while it was explicitly requested to exclude tunnel roads from the route. It is provided when exclude=tunnel
             * `type=alert`-> Indicates a tunnel is on route.
             * */
            val Tunnel = Subtype("tunnel")
            /**
             * `type=violation`-> Toll road, while it was explicitly requested to exclude toll roads from the route. It is provided when exclude=toll.
             * `type=alert`-> Indicates a toll road is on route.
             * */
            val Toll = Subtype("toll")
            /**
             * `type=violation`-> Undesirable road, while it was explicitly requested to exclude a road from the route by point. It is provided when exclude=point(longitude latitude).
             * */
            val PointExclusion = Subtype("pointExclusion")
            /**
             * `type=violation`-> Indicates a country border crossing. It is provided when exclude=country_border.
             * `type=alert`-> Indicates a country border crossing.
             * */
            val CountryBorderCrossing = Subtype("countryBorderCrossing")
            /**
             * `type=violation`-> Indicates a state border crossing. It is provided when exclude=state_border.
             * `type=alert`-> Indicates a state border crossing.
             * */
            val StateBorderCrossing = Subtype("stateBorderCrossing")
            /**
             * `type=violation`-> Indicates a ferry crossing. It is provided when exclude=ferry.
             * */
            val Ferry = Subtype("ferry")
        }
    }
}