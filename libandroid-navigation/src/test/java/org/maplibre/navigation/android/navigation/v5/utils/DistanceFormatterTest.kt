package org.maplibre.navigation.android.navigation.v5.utils

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.navigation.android.navigation.R
import org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationConstants
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class DistanceFormatterTest {
     private lateinit var context: Context

    @Before
    fun setup() {
        context = mockk<Context> {
            every { resources } returns mockk {
                every { configuration } returns mockk()
            }
            every { getString(R.string.kilometers) } returns "km"
            every { getString(R.string.meters) } returns "m"
            every { getString(R.string.miles) } returns "mi"
            every { getString(R.string.feet) } returns "ft"
        }
    }

    @Test
    fun formatDistance_noLocaleCountry() {
        assertOutput(
            LARGE_LARGE_UNIT,
            Locale(Locale.ENGLISH.language),
            DirectionsCriteria.IMPERIAL,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "11 mi"
        )
    }

    @Test
    fun formatDistance_noLocale() {
        assertOutput(
            LARGE_LARGE_UNIT,
            Locale("", ""),
            DirectionsCriteria.IMPERIAL,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "11 mi"
        )
    }

    @Test
    fun formatDistance_unitTypeDifferentFromLocale() {
        assertOutput(
            LARGE_LARGE_UNIT,
            Locale.US,
            DirectionsCriteria.METRIC,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "18 km"
        )
    }

    @Test
    fun formatDistance_largeMiles() {
        assertOutput(
            LARGE_LARGE_UNIT,
            Locale.US,
            DirectionsCriteria.IMPERIAL,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "11 mi"
        )
    }

    @Test
    fun formatDistance_largeKilometers() {
        assertOutput(
            LARGE_LARGE_UNIT,
            Locale.FRANCE,
            DirectionsCriteria.METRIC,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "18 km"
        )
    }

    @Test
    fun formatDistance_largeKilometerNoUnitTypeButMetricLocale() {
        assertOutput(
            LARGE_LARGE_UNIT,
            Locale.FRANCE,
            DirectionsCriteria.METRIC,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "18 km"
        )
    }

    @Test
    fun formatDistance_mediumMiles() {
        assertOutput(
            MEDIUM_LARGE_UNIT,
            Locale.US,
            DirectionsCriteria.IMPERIAL,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "6.1 mi"
        )
    }

    @Test
    fun formatDistance_mediumKilometers() {
        assertOutput(
            MEDIUM_LARGE_UNIT,
            Locale.FRANCE,
            DirectionsCriteria.METRIC,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "9,8 km"
        )
    }

    @Test
    fun formatDistance_mediumKilometersUnitTypeDifferentFromLocale() {
        assertOutput(
            MEDIUM_LARGE_UNIT,
            Locale.FRANCE,
            DirectionsCriteria.IMPERIAL,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "6,1 mi"
        )
    }

    @Test
    fun formatDistance_smallFeet() {
        assertOutput(
            SMALL_SMALL_UNIT,
            Locale.US,
            DirectionsCriteria.IMPERIAL,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "50 ft"
        )
    }

    @Test
    fun formatDistance_smallFeet_roundToTen() {
        assertOutput(
            SMALL_SMALL_UNIT,
            Locale.US,
            DirectionsCriteria.IMPERIAL,
            NavigationConstants.ROUNDING_INCREMENT_TEN,
            "40 ft"
        )
    }

    @Test
    fun formatDistance_smallMeters() {
        assertOutput(
            SMALL_SMALL_UNIT,
            Locale.FRANCE,
            DirectionsCriteria.METRIC,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "50 m"
        )
    }

    @Test
    fun formatDistance_smallMeters_roundToTen() {
        assertOutput(
            SMALL_SMALL_UNIT,
            Locale.FRANCE,
            DirectionsCriteria.METRIC,
            NavigationConstants.ROUNDING_INCREMENT_TEN,
            "10 m"
        )
    }

    @Test
    fun formatDistance_largeFeet() {
        assertOutput(
            LARGE_SMALL_UNIT,
            Locale.US,
            DirectionsCriteria.IMPERIAL,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "350 ft"
        )
    }

    @Test
    fun formatDistance_largeMeters() {
        assertOutput(
            LARGE_SMALL_UNIT,
            Locale.FRANCE,
            DirectionsCriteria.METRIC,
            NavigationConstants.ROUNDING_INCREMENT_FIFTY,
            "100 m"
        )
    }

    private fun assertOutput(
        distance: Double,
        locale: Locale,
        unitType: String,
        roundIncrement: Int,
        output: String
    ) {
        Assert.assertEquals(
            output,
            DistanceFormatter(context, locale.language, unitType, roundIncrement).formatDistance(
                distance
            ).toString()
        )
    }

    companion object {
        private const val LARGE_LARGE_UNIT = 18124.65
        private const val MEDIUM_LARGE_UNIT = 9812.33
        private const val SMALL_SMALL_UNIT = 13.71
        private const val LARGE_SMALL_UNIT = 109.73
    }
}
