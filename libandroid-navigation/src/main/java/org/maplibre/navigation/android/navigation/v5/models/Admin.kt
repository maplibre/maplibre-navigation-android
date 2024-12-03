package org.maplibre.navigation.android.navigation.v5.models

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
    val countryCode: String?,

    /**
     * Contains the 3 character ISO 3166-1 alpha-3 code that applies to a country boundary.
     * Example: `"USA"`.
     */
    @SerialName("iso_3166_1_alpha3")
    val countryCodeAlpha3: String?
)
