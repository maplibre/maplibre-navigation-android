package org.maplibre.navigation.android.navigation.v5.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.navigation.android.navigation.R
import org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationConstants
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class DistanceFormatterTest {
    @Mock
    private val context: Context? = null

    @Mock
    private val resources: Resources? = null

    @Mock
    private val configuration: Configuration? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(context!!.resources).thenReturn(resources)
        Mockito.`when`(resources!!.configuration).thenReturn(configuration)
        Mockito.`when`(configuration!!.locales).thenReturn(LocaleList.getDefault())
        Mockito.`when`(context.getString(R.string.kilometers)).thenReturn("km")
        Mockito.`when`(context.getString(R.string.meters)).thenReturn("m")
        Mockito.`when`(context.getString(R.string.miles)).thenReturn("mi")
        Mockito.`when`(context.getString(R.string.feet)).thenReturn("ft")
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
