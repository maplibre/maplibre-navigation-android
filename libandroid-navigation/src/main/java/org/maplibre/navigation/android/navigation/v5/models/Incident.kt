package org.maplibre.navigation.android.navigation.v5.models

import com.google.gson.annotations.SerializedName

/**
 * Reproduces one of road incidents type ([IncidentType]) that might be on the way.
 */
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
    val type: Type?,

    /**
     * **True** if road is closed and no possibility to pass through there. **False**
     * otherwise.
     */
    val closed: Boolean?,

    /**
     * Quantitative descriptor of congestion.
     */
    val congestion: Congestion?,

    /**
     * Human-readable description of the incident suitable for displaying to the users.
     */
    val description: String?,

    /**
     * Human-readable long description of the incident suitable for displaying to the users.
     */
    @SerializedName("long_description")
    val longDescription: String?,

    /**
     * Severity level of incident.
     *
     * @see Impact
     */
    val impact: Impact?,

    /**
     * Sub-type of the incident.
     */
    @SerializedName("sub_type")
    val subType: String?,

    /**
     * Sub-type-specific description.
     */
    @SerializedName("sub_type_description")
    val subTypeDescription: String?,

    /**
     * AlertC codes.
     *
     * @see [AlertC](https://www.iso.org/standard/59231.html)
     */
    @SerializedName("alertc_codes")
    val alertcCodes: List<Int?>?,

    /**
     * Incident's geometry index start point.
     */
    @SerializedName("geometry_index_start")
    val geometryIndexStart: Int?,

    /**
     * Incident's geometry index end point.
     */
    @SerializedName("geometry_index_end")
    val geometryIndexEnd: Int?,

    /**
     * Time the incident was created/updated in ISO8601 format. Not the same
     * [.startTime]/[.endTime], incident can be created/updated before the incident.
     */
    @SerializedName("creation_time")
    val creationTime: String?,

    /**
     * Start time of the incident in ISO8601 format.
     */
    @SerializedName("start_time")
    val startTime: String?,

    /**
     * End time of the incident in ISO8601 format.
     */
    @SerializedName("end_time")
    val endTime: String?,
) {

    enum class Type(val text: String) {

        /**
         * [Type] accident.
         */
        ACCIDENT("accident"),

        /**
         * [Type] congestion.
         */
        CONGESTION("congestion"),

        /**
         * [Type] construction.
         */
        CONSTRUCTION("construction"),

        /**
         * [Type] disabled vehicle.
         */
        DISABLED_VEHICLE("disabled_vehicle"),

        /**
         * [Type] lane restriction.
         */
        LANE_RESTRICTION("lane_restriction"),

        /**
         * [Type] mass transit.
         */
        INCIDENT_MASS_TRANSIT("mass_transit"),

        /**
         * [Type] miscellaneous.
         */
        MISCELLANEOUS("miscellaneous"),

        /**
         * [Type] other news.
         */
        OTHER_NEWS("other_news"),

        /**
         * [Type] planned event.
         */
        PLANNED_EVENT("planned_event"),

        /**
         * [Type] road closure.
         */
        ROAD_CLOSURE("road_closure"),

        /**
         * [Type] road hazard.
         */
        ROAD_HAZARD("road_hazard"),

        /**
         * [Type] weather.
         */
        WEATHER("weather"),
    }

    enum class Impact(val text: String) {

        /**
         * [Impact] unknown.
         */
        UNKNOWN("unknown"),

        /**
         * [Impact] critical.
         */
        CRITICAL("critical"),

        /**
         * [Impact] major.
         */
        MAJOR("major"),

        /**
         * [Impact] minor.
         */
        MINOR("minor"),

        /**
         * [Impact] low.
         */
        LOW("low"),
    }
}

//TODO fabi755 json