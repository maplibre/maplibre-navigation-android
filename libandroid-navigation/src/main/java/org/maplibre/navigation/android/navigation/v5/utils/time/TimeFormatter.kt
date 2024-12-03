package org.maplibre.navigation.android.navigation.v5.utils.time

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import org.maplibre.navigation.android.navigation.R
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.android.navigation.v5.utils.span.SpanItem
import org.maplibre.navigation.android.navigation.v5.utils.span.SpanUtils
import org.maplibre.navigation.android.navigation.v5.utils.span.TextSpanItem
import java.util.Calendar
import java.util.concurrent.TimeUnit

object TimeFormatter {
    private const val TIME_STRING_FORMAT = " %s "

    @JvmStatic
    fun formatTime(
        time: Calendar, routeDuration: Double,
        type: MapLibreNavigationOptions.TimeFormat,
        isDeviceTwentyFourHourFormat: Boolean
    ): String? {
        time.add(Calendar.SECOND, routeDuration.toInt())
        val chain = TimeFormattingChain()
        return chain.setup(isDeviceTwentyFourHourFormat).obtainTimeFormatted(type, time)
    }

    @JvmStatic
    fun formatTimeRemaining(context: Context, routeDuration: Double): SpannableStringBuilder {
        var seconds = routeDuration.toLong()

        require(seconds >= 0) { "Duration must be greater than zero." }

        val days = TimeUnit.SECONDS.toDays(seconds)
        seconds -= TimeUnit.DAYS.toSeconds(days)
        val hours = TimeUnit.SECONDS.toHours(seconds)
        seconds -= TimeUnit.HOURS.toSeconds(hours)
        var minutes = TimeUnit.SECONDS.toMinutes(seconds)
        seconds -= TimeUnit.MINUTES.toSeconds(minutes)

        if (seconds >= 30) {
            minutes += 1
        }

        val textSpanItems: MutableList<SpanItem> = ArrayList()
        val resources = context.resources
        formatDays(resources, days, textSpanItems)
        formatHours(context, hours, textSpanItems)
        formatMinutes(context, minutes, textSpanItems)
        formatNoData(context, days, hours, minutes, textSpanItems)
        return SpanUtils.combineSpans(textSpanItems)
    }

    private fun formatDays(resources: Resources, days: Long, textSpanItems: MutableList<SpanItem>) {
        if (days != 0L) {
            val dayQuantityString =
                resources.getQuantityString(R.plurals.numberOfDays, days.toInt())
            val dayString = String.format(TIME_STRING_FORMAT, dayQuantityString)
            textSpanItems.add(TextSpanItem(StyleSpan(Typeface.BOLD), days.toString()))
            textSpanItems.add(TextSpanItem(RelativeSizeSpan(1f), dayString))
        }
    }

    private fun formatHours(context: Context, hours: Long, textSpanItems: MutableList<SpanItem>) {
        if (hours != 0L) {
            val hourString = String.format(TIME_STRING_FORMAT, context.getString(R.string.hr))
            textSpanItems.add(TextSpanItem(StyleSpan(Typeface.BOLD), hours.toString()))
            textSpanItems.add(TextSpanItem(RelativeSizeSpan(1f), hourString))
        }
    }

    private fun formatMinutes(
        context: Context,
        minutes: Long,
        textSpanItems: MutableList<SpanItem>
    ) {
        if (minutes != 0L) {
            val minuteString = String.format(TIME_STRING_FORMAT, context.getString(R.string.min))
            textSpanItems.add(TextSpanItem(StyleSpan(Typeface.BOLD), minutes.toString()))
            textSpanItems.add(TextSpanItem(RelativeSizeSpan(1f), minuteString))
        }
    }

    private fun formatNoData(
        context: Context, days: Long, hours: Long, minutes: Long,
        textSpanItems: MutableList<SpanItem>
    ) {
        if (days == 0L && hours == 0L && minutes == 0L) {
            val minuteString = String.format(TIME_STRING_FORMAT, context.getString(R.string.min))
            textSpanItems.add(TextSpanItem(StyleSpan(Typeface.BOLD), 1.toString()))
            textSpanItems.add(TextSpanItem(RelativeSizeSpan(1f), minuteString))
        }
    }
}
