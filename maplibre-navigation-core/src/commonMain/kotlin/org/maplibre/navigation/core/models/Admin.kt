package org.maplibre.navigation.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An objects describing the administrative boundaries the route leg travels through.
 */
@Serializable
data class Admin(
    /**
     * Contains the 2 character ISO 3166-1 alpha-2 code that applies to a country boundary.
     * Example: `"US"`.
     */
    @SerialName("iso_3166_1")
    val countryCode: String? = null,

    /**
     * Contains the 3 character ISO 3166-1 alpha-3 code that applies to a country boundary.
     * Example: `"USA"`.
     */
    @SerialName("iso_3166_1_alpha3")
    val countryCodeAlpha3: String? = null
) {
    /**
     * Creates a builder initialized with the current values of the `Admin` instance.
     */
    fun toBuilder(): Builder {
        return Builder()
            .withCountryCode(countryCode)
            .withCountryCodeAlpha3(countryCodeAlpha3)
    }

    /**
     * Builder class for creating `Admin` instances.
     */
    class Builder {
        private var countryCode: String? = null
        private var countryCodeAlpha3: String? = null

        /**
         * Sets the 2 character ISO 3166-1 alpha-2 code.
         *
         * @param countryCode The 2 character ISO 3166-1 alpha-2 code.
         * @return The builder instance.
         */
        fun withCountryCode(countryCode: String?) = apply { this.countryCode = countryCode }

        /**
         * Sets the 3 character ISO 3166-1 alpha-3 code.
         *
         * @param countryCodeAlpha3 The 3 character ISO 3166-1 alpha-3 code.
         * @return The builder instance.
         */
        fun withCountryCodeAlpha3(countryCodeAlpha3: String?) =
            apply { this.countryCodeAlpha3 = countryCodeAlpha3 }

        /**
         * Builds an `Admin` instance with the current builder values.
         *
         * @return A new `Admin` instance.
         */
        fun build(): Admin {
            return Admin(
                countryCode = countryCode,
                countryCodeAlpha3 = countryCodeAlpha3
            )
        }
    }
}