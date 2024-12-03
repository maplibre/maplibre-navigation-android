package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.Serializable

/**
 * This class provides information thats useful for properly making navigation announcements at the
 * correct time. Essentially, a distance and a string are given, using Turf Distance measurement
 * methods you can measure the users current location to the next steps maneuver point and if the
 * measured distance is less than the one the API provides, the announcement should be made.
 *
 * @since 3.0.0
 */
@Serializable
data class VoiceInstructions(

    /**
     * This provides the missing piece in which is needed to announce instructions at accurate
     * times. If the user is less distance away from the maneuver than what this
     * `distanceAlongGeometry()` than, the announcement should be called.
     *
     * @since 3.0.0
     */
    val distanceAlongGeometry: Double,

    /**
     * Provides the instruction string which was build on the server-side and can sometimes
     * concatenate instructions together if maneuver instructions are too close to each other.
     *
     * @since 3.0.0
     */
    val announcement: String?,

    /**
     * Get the same instruction string you'd get from [.announcement] but this one includes
     * Speech Synthesis Markup Language which helps voice synthesiser read information more humanely.
     *
     * @since 3.0.0
     */
    val ssmlAnnouncement: String?,
)
