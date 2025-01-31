package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Reproduces one of road incidents type ([Type]) that might be on the way.
 */
@Suppress("unused")
@Serializable
data class Incident(

    /**
     * Unique identifier for incident. It might be the only one **non-null** filed which meant
     * that incident started on previous leg and one has an incident with the same **id**.
     */
    val id: String,

    /**
     * One of incident types.
     *
     * @see Type
     */
    val type: Type? = null,

    /**
     * **True** if road is closed and no possibility to pass through there. **False**
     * otherwise.
     */
    val closed: Boolean? = null,

    /**
     * Quantitative descriptor of congestion.
     */
    val congestion: Congestion? = null,

    /**
     * Human-readable description of the incident suitable for displaying to the users.
     */
    val description: String? = null,

    /**
     * Human-readable long description of the incident suitable for displaying to the users.
     */
    @SerialName("long_description")
    val longDescription: String? = null,

    /**
     * Severity level of incident.
     *
     * @see Impact
     */
    val impact: Impact? = null,

    /**
     * Sub-type of the incident.
     */
    @SerialName("sub_type")
    val subType: String? = null,

    /**
     * Sub-type-specific description.
     */
    @SerialName("sub_type_description")
    val subTypeDescription: String? = null,

    /**
     * AlertC codes.
     *
     * @see [AlertC](https://www.iso.org/standard/59231.html)
     */
    @SerialName("alertc_codes")
    val alertcCodes: List<Int>? = null,

    /**
     * Incident's geometry index start point.
     */
    @SerialName("geometry_index_start")
    val geometryIndexStart: Int? = null,

    /**
     * Incident's geometry index end point.
     */
    @SerialName("geometry_index_end")
    val geometryIndexEnd: Int? = null,

    /**
     * Time the incident was created/updated in ISO8601 format. Not the same
     * [.startTime]/[.endTime], incident can be created/updated before the incident.
     */
    @SerialName("creation_time")
    val creationTime: String? = null,

    /**
     * Start time of the incident in ISO8601 format.
     */
    @SerialName("start_time")
    val startTime: String? = null,

    /**
     * End time of the incident in ISO8601 format.
     */
    @SerialName("end_time")
    val endTime: String? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `Incident` instance.
     */
    fun toBuilder(): Builder {
        return Builder(id).apply {
            withType(type)
            withClosed(closed)
            withCongestion(congestion)
            withDescription(description)
            withLongDescription(longDescription)
            withImpact(impact)
            withSubType(subType)
            withSubTypeDescription(subTypeDescription)
            withAlertcCodes(alertcCodes)
            withGeometryIndexStart(geometryIndexStart)
            withGeometryIndexEnd(geometryIndexEnd)
            withCreationTime(creationTime)
            withStartTime(startTime)
            withEndTime(endTime)
        }
    }

    /**
     * Builder class for creating `Incident` instances.
     * @param id Unique identifier for incident.
     */
    class Builder(
        private var id: String
    ) {
        private var type: Type? = null
        private var closed: Boolean? = null
        private var congestion: Congestion? = null
        private var description: String? = null
        private var longDescription: String? = null
        private var impact: Impact? = null
        private var subType: String? = null
        private var subTypeDescription: String? = null
        private var alertcCodes: List<Int>? = null
        private var geometryIndexStart: Int? = null
        private var geometryIndexEnd: Int? = null
        private var creationTime: String? = null
        private var startTime: String? = null
        private var endTime: String? = null

        /**
         * Sets the type.
         *
         * @param type The type.
         * @return The builder instance.
         */
        fun withType(type: Type?) = apply { this.type = type }

        /**
         * Sets the closed status.
         *
         * @param closed The closed status.
         * @return The builder instance.
         */
        fun withClosed(closed: Boolean?) = apply { this.closed = closed }

        /**
         * Sets the congestion.
         *
         * @param congestion The congestion.
         * @return The builder instance.
         */
        fun withCongestion(congestion: Congestion?) = apply { this.congestion = congestion }

        /**
         * Sets the description.
         *
         * @param description The description.
         * @return The builder instance.
         */
        fun withDescription(description: String?) = apply { this.description = description }

        /**
         * Sets the long description.
         *
         * @param longDescription The long description.
         * @return The builder instance.
         */
        fun withLongDescription(longDescription: String?) =
            apply { this.longDescription = longDescription }

        /**
         * Sets the impact.
         *
         * @param impact The impact.
         * @return The builder instance.
         */
        fun withImpact(impact: Impact?) = apply { this.impact = impact }

        /**
         * Sets the sub-type.
         *
         * @param subType The sub-type.
         * @return The builder instance.
         */
        fun withSubType(subType: String?) = apply { this.subType = subType }

        /**
         * Sets the sub-type description.
         *
         * @param subTypeDescription The sub-type description.
         * @return The builder instance.
         */
        fun withSubTypeDescription(subTypeDescription: String?) =
            apply { this.subTypeDescription = subTypeDescription }

        /**
         * Sets the AlertC codes.
         *
         * @param alertcCodes The AlertC codes.
         * @return The builder instance.
         */
        fun withAlertcCodes(alertcCodes: List<Int>?) = apply { this.alertcCodes = alertcCodes }

        /**
         * Sets the geometry index start point.
         *
         * @param geometryIndexStart The geometry index start point.
         * @return The builder instance.
         */
        fun withGeometryIndexStart(geometryIndexStart: Int?) =
            apply { this.geometryIndexStart = geometryIndexStart }

        /**
         * Sets the geometry index end point.
         *
         * @param geometryIndexEnd The geometry index end point.
         * @return The builder instance.
         */
        fun withGeometryIndexEnd(geometryIndexEnd: Int?) =
            apply { this.geometryIndexEnd = geometryIndexEnd }

        /**
         * Sets the creation time.
         *
         * @param creationTime The creation time.
         * @return The builder instance.
         */
        fun withCreationTime(creationTime: String?) = apply { this.creationTime = creationTime }

        /**
         * Sets the start time.
         *
         * @param startTime The start time.
         * @return The builder instance.
         */
        fun withStartTime(startTime: String?) = apply { this.startTime = startTime }

        /**
         * Sets the end time.
         *
         * @param endTime The end time.
         * @return The builder instance.
         */
        fun withEndTime(endTime: String?) = apply { this.endTime = endTime }

        /**
         * Builds an `Incident` instance with the current builder values.
         *
         * @return A new `Incident` instance.
         */
        fun build(): Incident {
            return Incident(
                id = id,
                type = type,
                closed = closed,
                congestion = congestion,
                description = description,
                longDescription = longDescription,
                impact = impact,
                subType = subType,
                subTypeDescription = subTypeDescription,
                alertcCodes = alertcCodes,
                geometryIndexStart = geometryIndexStart,
                geometryIndexEnd = geometryIndexEnd,
                creationTime = creationTime,
                startTime = startTime,
                endTime = endTime
            )
        }
    }

    @Serializable
    enum class Type(val text: String) {

        /**
         * [Type] accident.
         */
        @SerialName("accident")
        ACCIDENT("accident"),

        /**
         * [Type] congestion.
         */
        @SerialName("congestion")
        CONGESTION("congestion"),

        /**
         * [Type] construction.
         */
        @SerialName("construction")
        CONSTRUCTION("construction"),

        /**
         * [Type] disabled vehicle.
         */
        @SerialName("disabled_vehicle")
        DISABLED_VEHICLE("disabled_vehicle"),

        /**
         * [Type] lane restriction.
         */
        @SerialName("lane_restriction")
        LANE_RESTRICTION("lane_restriction"),

        /**
         * [Type] mass transit.
         */
        @SerialName("mass_transit")
        INCIDENT_MASS_TRANSIT("mass_transit"),

        /**
         * [Type] miscellaneous.
         */
        @SerialName("miscellaneous")
        MISCELLANEOUS("miscellaneous"),

        /**
         * [Type] other news.
         */
        @SerialName("other_news")
        OTHER_NEWS("other_news"),

        /**
         * [Type] planned event.
         */
        @SerialName("planned_event")
        PLANNED_EVENT("planned_event"),

        /**
         * [Type] road closure.
         */
        @SerialName("road_closure")
        ROAD_CLOSURE("road_closure"),

        /**
         * [Type] road hazard.
         */
        @SerialName("road_hazard")
        ROAD_HAZARD("road_hazard"),

        /**
         * [Type] weather.
         */
        @SerialName("weather")
        WEATHER("weather"),
    }

    @Serializable
    enum class Impact(val text: String) {

        /**
         * [Impact] unknown.
         */
        @SerialName("unknown")
        UNKNOWN("unknown"),

        /**
         * [Impact] critical.
         */
        @SerialName("critical")
        CRITICAL("critical"),

        /**
         * [Impact] major.
         */
        @SerialName("major")
        MAJOR("major"),

        /**
         * [Impact] minor.
         */
        @SerialName("minor")
        MINOR("minor"),

        /**
         * [Impact] low.
         */
        @SerialName("low")
        LOW("low"),
    }
}
