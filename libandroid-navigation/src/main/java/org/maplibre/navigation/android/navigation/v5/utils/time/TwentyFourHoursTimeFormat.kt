package org.maplibre.navigation.android.navigation.v5.utils.time

import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions
import java.util.Calendar
import java.util.Locale


open class TwentyFourHoursTimeFormat : TimeFormatResolver {
    private var chain: TimeFormatResolver? = null

    override fun nextChain(chain: TimeFormatResolver?) {
        this.chain = chain
    }

    override fun obtainTimeFormatted(
        type: MapLibreNavigationOptions.TimeFormat,
        time: Calendar
    ): String? {
        return if (type == MapLibreNavigationOptions.TimeFormat.TWENTY_FOUR_HOURS) {
            String.format(
                Locale.getDefault(),
                TWENTY_FOUR_HOURS_FORMAT,
                time,
                time
            )
        } else {
            chain?.obtainTimeFormatted(type, time)
        }
    }

    companion object {
        const val TWENTY_FOUR_HOURS_FORMAT: String = "%tk:%tM"
    }
}
