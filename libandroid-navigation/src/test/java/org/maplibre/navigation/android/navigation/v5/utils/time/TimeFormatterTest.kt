package org.maplibre.navigation.android.navigation.v5.utils.time

import org.junit.Assert
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions
import java.util.Calendar

class TimeFormatterTest {
    @Test
    @Throws(Exception::class)
    fun checksTwelveHoursTimeFormat() {
        val time = Calendar.getInstance()
        val anyYear = 2018
        val anyMonth = 3
        val anyDay = 26
        val sixPm = 18
        val eighteenMinutes = 18
        val zeroSeconds = 0
        time[anyYear, anyMonth, anyDay, sixPm, eighteenMinutes] = zeroSeconds
        val elevenMinutes = 663.7
        val twelveHoursTimeFormatType = MapLibreNavigationOptions.TimeFormat.TWELVE_HOURS
        val indifferentDeviceTwentyFourHourFormat = true

        val formattedTime = TimeFormatter.formatTime(
            time, elevenMinutes, twelveHoursTimeFormatType,
            indifferentDeviceTwentyFourHourFormat
        )

        Assert.assertEquals("6:29 pm", formattedTime)
    }

    @Test
    @Throws(Exception::class)
    fun checksTwentyFourHoursTimeFormat() {
        val time = Calendar.getInstance()
        val anyYear = 2018
        val anyMonth = 3
        val anyDay = 26
        val sixPm = 18
        val eighteenMinutes = 18
        val zeroSeconds = 0
        time[anyYear, anyMonth, anyDay, sixPm, eighteenMinutes] = zeroSeconds
        val elevenMinutes = 663.7
        val twentyFourHoursTimeFormatType = MapLibreNavigationOptions.TimeFormat.TWENTY_FOUR_HOURS
        val indifferentDeviceTwentyFourHourFormat = false

        val formattedTime = TimeFormatter.formatTime(
            time, elevenMinutes, twentyFourHoursTimeFormatType,
            indifferentDeviceTwentyFourHourFormat
        )

        Assert.assertEquals("18:29", formattedTime)
    }

    @Test
    @Throws(Exception::class)
    fun checksDefaultTwelveHoursTimeFormat() {
        val time = Calendar.getInstance()
        val anyYear = 2018
        val anyMonth = 3
        val anyDay = 26
        val sixPm = 18
        val eighteenMinutes = 18
        val zeroSeconds = 0
        time[anyYear, anyMonth, anyDay, sixPm, eighteenMinutes] = zeroSeconds
        val elevenMinutes = 663.7
        val noneSpecifiedTimeFormatType = MapLibreNavigationOptions.TimeFormat.NONE_SPECIFIED
        val deviceTwelveHourFormat = false

        val formattedTime = TimeFormatter.formatTime(
            time, elevenMinutes, noneSpecifiedTimeFormatType,
            deviceTwelveHourFormat
        )

        Assert.assertEquals("6:29 pm", formattedTime)
    }

    @Test
    @Throws(Exception::class)
    fun checksDefaultTwentyFourHoursTimeFormat() {
        val time = Calendar.getInstance()
        val anyYear = 2018
        val anyMonth = 3
        val anyDay = 26
        val sixPm = 18
        val eighteenMinutes = 18
        val zeroSeconds = 0
        time[anyYear, anyMonth, anyDay, sixPm, eighteenMinutes] = zeroSeconds
        val elevenMinutes = 663.7
        val noneSpecifiedTimeFormatType = MapLibreNavigationOptions.TimeFormat.NONE_SPECIFIED
        val deviceTwentyFourHourFormat = true

        val formattedTime = TimeFormatter.formatTime(
            time, elevenMinutes, noneSpecifiedTimeFormatType,
            deviceTwentyFourHourFormat
        )

        Assert.assertEquals("18:29", formattedTime)
    }
}