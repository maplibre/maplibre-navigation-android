package org.maplibre.navigation.core.models

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
    val announcement: String? = null,

    /**
     * Get the same instruction string you'd get from [.announcement] but this one includes
     * Speech Synthesis Markup Language which helps voice synthesiser read information more humanely.
     *
     * @since 3.0.0
     */
    val ssmlAnnouncement: String? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `VoiceInstructions` instance.
     */
    fun toBuilder(): Builder {
        return Builder(
            distanceAlongGeometry = distanceAlongGeometry
        ).apply {
            withAnnouncement(announcement)
            withSsmlAnnouncement(ssmlAnnouncement)
        }
    }

    /**
     * Builder class for creating `VoiceInstructions` instances.
     * @param distanceAlongGeometry The distance along the geometry.
     */
    class Builder(
        private var distanceAlongGeometry: Double
    ) {
        private var announcement: String? = null
        private var ssmlAnnouncement: String? = null

        /**
         * Sets the announcement.
         *
         * @param announcement The instruction string.
         * @return The builder instance.
         */
        fun withAnnouncement(announcement: String?) = apply { this.announcement = announcement }

        /**
         * Sets the SSML announcement.
         *
         * @param ssmlAnnouncement The SSML instruction string.
         * @return The builder instance.
         */
        fun withSsmlAnnouncement(ssmlAnnouncement: String?) =
            apply { this.ssmlAnnouncement = ssmlAnnouncement }

        /**
         * Builds a `VoiceInstructions` instance with the current builder values.
         *
         * @return A new `VoiceInstructions` instance.
         */
        fun build(): VoiceInstructions {
            return VoiceInstructions(
                distanceAlongGeometry = distanceAlongGeometry,
                announcement = announcement,
                ssmlAnnouncement = ssmlAnnouncement
            )
        }
    }
}
