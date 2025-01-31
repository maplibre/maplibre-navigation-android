@file:Suppress("unused")

package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * A part of the [BannerText] which includes a snippet of the full banner text instruction. In
 * cases where data is available, an image url will be provided to visually include a road shield.
 * To receive this information, your request must have
 * <tt>MapboxDirections.Builder#bannerInstructions()</tt> set to true.
 *
 * @since 3.0.0
 */
@Serializable
data class BannerComponents(

    /**
     * A snippet of the full [BannerText.text] which can be used for visually altering parts
     * of the full string.
     *
     * @since 3.0.0
     */
    val text: String,

    /**
     * String giving you more context about the component which may help in visual markup/display
     * choices. If the type of the components is unknown it should be treated as text.
     *
     *
     * Possible values:
     *
     *  * **text (default)**: indicates the text is part of
     * the instructions and no other type
     *  * **icon**: this is text that can be replaced by an icon, see imageBaseURL
     *  * **delimiter**: this is text that can be dropped and
     * should be dropped if you are rendering icons
     *  * **exit-number**: the exit number for the maneuver
     *  * **exit**: the word for exit in the local language
     *
     *
     * @since 3.0.0
     */
    val type: Type,

    /**
     * String giving you more context about [Type] which
     * may help in visual markup/display choices.
     *
     * Possible values:
     *
     *  * **jct**: indicates a junction guidance view.
     *  * **signboard**: indicates a signboard guidance view.
     */
    val subType: Type? = null,

    /**
     * The abbreviated form of text.
     *
     * If this is present, there will also be an abbr_priority value.
     *
     *  @since 3.0.0
     */
    @SerialName("abbr")
    val abbreviation: String? = null,

    /**
     * An integer indicating the order in which the abbreviation abbr should be used in
     * place of text. The highest priority is 0 and a higher integer value indicates a lower
     * priority. There are no gaps in integer values.
     *
     *
     * Multiple components can have the same abbreviationPriority and when this happens all
     * components with the same abbr_priority should be abbreviated at the same time.
     * Finding no larger values of abbreviationPriority indicates that the string is
     * fully abbreviated.
     *
     * @since 3.0.0
     */
    @SerialName("abbr_priority")
    val abbreviationPriority: Int? = null,

    /**
     * In some cases when the [LegStep] is a highway or major roadway, there might be a shield
     * icon that's included to better identify to your user to roadway. Note that this doesn't
     * return the image itself but rather the url which can be used to download the file.
     *
     * @since 3.0.0
     */
    @SerialName("imageBaseURL")
    val imageBaseUrl: String? = null,

    /**
     * In some cases when the [StepManeuver] will be difficult to navigate, an image
     * can describe how to proceed. The domain name for this image is a Junction View.
     * Unlike the imageBaseUrl, this image url does not include image density encodings.
     *
     * @since 5.0.0
     */
    @SerialName("imageURL")
    val imageUrl: String? = null,

    /**
     * A List of directions indicating which way you can go from a lane
     * (left, right, or straight). If the value is ['left', 'straight'],
     * the driver can go straight or left from that lane.
     * Present if this is a lane component.
     *
     * @since 3.2.0
     */
    val directions: List<String>? = null,

    /**
     * A boolean telling you if that lane can be used to complete the upcoming maneuver.
     * If multiple lanes are active, then they can all be used to complete the upcoming maneuver.
     * Present if this is a lane component.
     *
     * @since 3.2.0
     */
    val active: Boolean? = null,
) {

    /**
     * Creates a builder initialized with the current values of the `BannerComponents` instance.
     */
    fun toBuilder(): Builder {
        return Builder(
            text = text,
            type = type
        ).apply {
            withSubType(subType)
            withAbbreviation(abbreviation)
            withAbbreviationPriority(abbreviationPriority)
            withImageBaseUrl(imageBaseUrl)
            withImageUrl(imageUrl)
            withDirections(directions)
            withActive(active)
        }
    }

    @Serializable
    enum class Type(val text: String) {
        /**
         * Default. Indicates the text is part of the instructions and no other type.
         *
         * @since 3.0.0
         */
        @SerialName("text")
        TEXT("text"),

        /**
         * This is text that can be replaced by an imageBaseURL icon.
         *
         * @since 3.0.0
         */
        @SerialName("icon")
        ICON("icon"),

        /**
         * This is text that can be dropped, and should be dropped if you are rendering icons.
         *
         * @since 3.0.0
         */
        @SerialName("delimiter")
        DELIMITER("delimiter"),

        /**
         * Indicates the exit number for the maneuver.
         *
         * @since 3.0.0
         */
        @SerialName("exit-number")
        EXIT_NUMBER("exit-number"),

        /**
         * Provides the the word for exit in the local language.
         *
         * @since 3.0.0
         */
        @SerialName("exit")
        EXIT("exit"),

        /**
         * Indicates which lanes can be used to complete the maneuver.
         *
         * @since 3.0.0
         */
        @SerialName("lane")
        LANE("lane"),

        /**
         * This view gives guidance through junctions and is used to complete maneuvers.
         */
        @SerialName("guidance-view")
        GUIDANCE_VIEW("guidance-view"),

        /**
         * This view gives guidance through signboards and is used to complete maneuvers.
         */
        @SerialName("signboard")
        SIGNBOARD("signboard"),

        /**
         * This view gives guidance through junctions and is used to complete maneuvers.
         */
        @SerialName("jct")
        JCT("jct")
    }

    /**
     * Builder class for creating `BannerComponents` instances.
     * @param text  A snippet of the full [BannerText.text] which can be used for visually altering parts of the full string.
     * @param type  String giving you more context about the component which may help in visual markup/display choices. If the type of the components is unknown it should be treated as text.
     */
    class Builder(
        private var text: String,
        private var type: Type
    ) {
        private var subType: Type? = null
        private var abbreviation: String? = null
        private var abbreviationPriority: Int? = null
        private var imageBaseUrl: String? = null
        private var imageUrl: String? = null
        private var directions: List<String>? = null
        private var active: Boolean? = null

        /**
         * Sets the subType.
         *
         * @param subType The subType.
         * @return The builder instance.
         */
        fun withSubType(subType: Type?) = apply { this.subType = subType }

        /**
         * Sets the abbreviation.
         *
         * @param abbreviation The abbreviation.
         * @return The builder instance.
         */
        fun withAbbreviation(abbreviation: String?) = apply { this.abbreviation = abbreviation }

        /**
         * Sets the abbreviation priority.
         *
         * @param abbreviationPriority The abbreviation priority.
         * @return The builder instance.
         */
        fun withAbbreviationPriority(abbreviationPriority: Int?) =
            apply { this.abbreviationPriority = abbreviationPriority }

        /**
         * Sets the image base URL.
         *
         * @param imageBaseUrl The image base URL.
         * @return The builder instance.
         */
        fun withImageBaseUrl(imageBaseUrl: String?) = apply { this.imageBaseUrl = imageBaseUrl }

        /**
         * Sets the image URL.
         *
         * @param imageUrl The image URL.
         * @return The builder instance.
         */
        fun withImageUrl(imageUrl: String?) = apply { this.imageUrl = imageUrl }

        /**
         * Sets the directions.
         *
         * @param directions The directions.
         * @return The builder instance.
         */
        fun withDirections(directions: List<String>?) = apply { this.directions = directions }

        /**
         * Sets the active status.
         *
         * @param active The active status.
         * @return The builder instance.
         */
        fun withActive(active: Boolean?) = apply { this.active = active }

        /**
         * Builds a `BannerComponents` instance with the current builder values.
         *
         * @return A new `BannerComponents` instance.
         */
        fun build(): BannerComponents {
            return BannerComponents(
                text = text,
                type = type,
                subType = subType,
                abbreviation = abbreviation,
                abbreviationPriority = abbreviationPriority,
                imageBaseUrl = imageBaseUrl,
                imageUrl = imageUrl,
                directions = directions,
                active = active
            )
        }
    }
}
