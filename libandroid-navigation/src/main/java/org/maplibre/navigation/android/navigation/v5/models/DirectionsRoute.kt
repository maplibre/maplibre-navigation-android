package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.maplibre.navigation.android.json

/**
 * Detailed information about an individual route such as the duration, distance and geometry.
 *
 * @since 1.0.0
 */
@Serializable
data class DirectionsRoute(

    /**
     * Gives the geometry of the route. Commonly used to draw the route on the map view.
     *
     * @since 1.0.0
     */
    val geometry: String,

    /**
     * A Leg is a route between only two waypoints.
     *
     * @since 1.0.0
     */
    val legs: List<RouteLeg>,


    /**
     * The distance traveled from origin to destination.
     *
     * @return a double number with unit meters
     * @since 1.0.0
     */
    val distance: Double,

    /**
     * The estimated travel time from origin to destination.
     *
     * @since 1.0.0
     */
    val duration: Double,

    /**
     * The typical travel time from this route's origin to destination. There's a delay along
     * this route if you subtract this durationTypical() value from the route's duration()
     * value and the resulting difference is greater than 0. The delay is because of any
     * number of real-world situations (road repair, traffic jam, etc).
     *
     * @since 5.5.0
     */
    @SerialName("duration_typical")
    val durationTypical: Double?,

    /**
     * The calculated weight of the route.
     *
     * @since 2.1.0
     */
    val weight: Double?,

    /**
     * The name of the weight profile used while calculating during extraction phase. The default is
     * `routability` which is duration based, with additional penalties for less desirable
     * maneuvers.
     *
     * @since 2.1.0
     */
    @SerialName("weight_name")
    val weightName: String?,

    /**
     * Holds onto the parameter information used when making the directions request. Useful for
     * re-requesting a directions route using the same information previously used.
     *
     * @since 3.0.0
     */
    val routeOptions: RouteOptions?,

    /**
     * String of the language to be used for voice instructions.  Defaults to en, and
     * can be any accepted instruction language.  Will be <tt>null</tt> when the language provided
     * <tt>MapboxDirections.Builder#language()</tt> via is not compatible with API Voice.
     *
     * @since 3.1.0
     */
    @SerialName("voiceLocale")
    val voiceLanguage: String?,
) {

    fun toJson(): String = json.encodeToString(this)

    fun toBuilder(): Builder {
        return Builder(
            geometry = geometry,
            legs = legs,
            distance = distance,
            duration = duration
        ).apply {
            durationTypical(durationTypical)
            withWeight(weight)
            withWeightName(weightName)
            withRouteOptions(routeOptions)
            withVoiceLanguage(voiceLanguage)
        }
    }

    class Builder(
        private var geometry: String,
        private var legs: List<RouteLeg>,
        private var distance: Double,
        private var duration: Double
    ) {
        private var durationTypical: Double? = null
        private var weight: Double? = null
        private var weightName: String? = null
        private var routeOptions: RouteOptions? = null
        private var voiceLanguage: String? = null

        fun durationTypical(durationTypical: Double?) =
            apply { this.durationTypical = durationTypical }

        fun withWeight(weight: Double?) = apply { this.weight = weight }

        fun withWeightName(weightName: String?) = apply { this.weightName = weightName }

        fun withRouteOptions(routeOptions: RouteOptions?) =
            apply { this.routeOptions = routeOptions }

        fun withVoiceLanguage(voiceLanguage: String?) = apply { this.voiceLanguage = voiceLanguage }

        fun build(): DirectionsRoute {
            return DirectionsRoute(
                geometry = geometry,
                legs = legs,
                distance = distance,
                duration = duration,
                durationTypical = durationTypical,
                weight = weight,
                weightName = weightName,
                routeOptions = routeOptions,
                voiceLanguage = voiceLanguage
            )
        }
    }

    companion object {

        @JvmStatic
        fun fromJson(jsonString: String): DirectionsRoute = json.decodeFromString(jsonString)
    }
}
