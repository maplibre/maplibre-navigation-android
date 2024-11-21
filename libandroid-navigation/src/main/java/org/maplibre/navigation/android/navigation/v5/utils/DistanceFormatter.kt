package org.maplibre.navigation.android.navigation.v5.utils

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import org.maplibre.navigation.android.navigation.R
import org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationConstants.RoundingIncrement
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfConversion
import java.text.NumberFormat
import java.util.Locale

/**
 * DistanceFormatter, which can format distances in meters based
 * on a language format and unit type.
 *
 * @param context  from which to get localized strings from
 * @param language for which language
 * @param unitType to use, or NONE_SPECIFIED to use default for locale country
 * @param roundingIncrement increment by which to round small distances
 */
class DistanceFormatter(
    context: Context,
    language: String?,
    @DirectionsCriteria.VoiceUnitCriteria unitType: String?,
    @property:RoundingIncrement @RoundingIncrement private val roundingIncrement: Int
) {
    private val unitStrings: Map<String, String> = mapOf(
        TurfConstants.UNIT_KILOMETERS to context.getString(R.string.kilometers),
        TurfConstants.UNIT_METERS to context.getString(R.string.meters),
        TurfConstants.UNIT_MILES to context.getString(R.string.miles),
        TurfConstants.UNIT_FEET to context.getString(R.string.feet),
    )
    private val numberFormat: NumberFormat
    private val localeUtils: LocaleUtils = LocaleUtils()
    private val language: String
    private val unitType: String

    private val largeUnit: String
        get() = if (DirectionsCriteria.IMPERIAL == unitType) TurfConstants.UNIT_MILES else TurfConstants.UNIT_KILOMETERS
    private val smallUnit: String
        get() = if (DirectionsCriteria.IMPERIAL == unitType) TurfConstants.UNIT_FEET else TurfConstants.UNIT_METERS

    init {
        val locale = language?.let { l -> Locale(l) } ?: localeUtils.inferDeviceLocale(context)
        this.language = locale.language
        numberFormat = NumberFormat.getNumberInstance(locale)

        this.unitType = unitType
            .takeIf { type ->
                type in listOf(
                    DirectionsCriteria.IMPERIAL,
                    DirectionsCriteria.METRIC
                )
            }
            ?: localeUtils.getUnitTypeForDeviceLocale(context)
    }

    /**
     * Returns a formatted SpannableString with bold and size formatting. I.e., "10 mi", "350 m"
     *
     * @param distance in meters
     * @return SpannableString representation which has a bolded number and units which have a
     * relative size of .65 times the size of the number
     */
    fun formatDistance(distance: Double): SpannableString {
        val distanceSmallUnit =
            TurfConversion.convertLength(distance, TurfConstants.UNIT_METERS, smallUnit)
        val distanceLargeUnit =
            TurfConversion.convertLength(distance, TurfConstants.UNIT_METERS, largeUnit)

        return when {
            distanceLargeUnit > LARGE_UNIT_THRESHOLD ->
                // If the distance is greater than 10 miles/kilometers, then round to nearest mile/kilometer
                getDistanceString(roundToDecimalPlace(distanceLargeUnit, 0), largeUnit)

            distanceSmallUnit < SMALL_UNIT_THRESHOLD ->
                // If the distance is less than 401 feet/meters, round by fifty feet/meters
                getDistanceString(roundToClosestIncrement(distanceSmallUnit), smallUnit)

            else ->
                // If the distance is between 401 feet/meters and 10 miles/kilometers, then round to one decimal place
                getDistanceString(roundToDecimalPlace(distanceLargeUnit, 1), largeUnit)
        }
    }

    /**
     * Method that can be used to check if an instance of [DistanceFormatter]
     * needs to be updated based on the passed language / unitType.
     *
     * @param language to check against the current formatter language
     * @param unitType to check against the current formatter unitType
     * @return true if new formatter is needed, false otherwise
     */
    @Suppress("unused")
    fun shouldUpdate(language: String, unitType: String, roundingIncrement: Int): Boolean {
        return this.language != language || this.unitType != unitType || this.roundingIncrement != roundingIncrement
    }

    /**
     * Returns number rounded to closest specified rounding increment, unless the number is less than
     * the rounding increment, then the rounding increment is returned
     *
     * @param distance to round to closest specified rounding increment
     * @return number rounded to closest rounding increment, or rounding increment if distance is less
     */
    private fun roundToClosestIncrement(distance: Double): String {
        val roundedNumber = (Math.round(distance).toInt()) / roundingIncrement * roundingIncrement

        return (if (roundedNumber < roundingIncrement) roundingIncrement else roundedNumber).toString()
    }

    /**
     * Rounds given number to the given decimal place
     *
     * @param distance     to round
     * @param decimalPlace number of decimal places to round
     * @return distance rounded to given decimal places
     */
    private fun roundToDecimalPlace(distance: Double, decimalPlace: Int): String {
        numberFormat.maximumFractionDigits = decimalPlace

        return numberFormat.format(distance)
    }

    /**
     * Takes in a distance and units and returns a formatted SpannableString where the number is bold
     * and the unit is shrunked to .65 times the size
     *
     * @param distance formatted with appropriate decimal places
     * @param unit     string from TurfConstants. This will be converted to the abbreviated form.
     * @return String with bolded distance and shrunken units
     */
    private fun getDistanceString(distance: String, unit: String): SpannableString {
        return SpannableString(
            String.format("%s %s", distance, unitStrings[unit])
        ).apply {
            setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                distance.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            setSpan(
                RelativeSizeSpan(0.65f),
                distance.length + 1,
                this.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    companion object {
        private const val LARGE_UNIT_THRESHOLD = 10
        private const val SMALL_UNIT_THRESHOLD = 401
    }
}
