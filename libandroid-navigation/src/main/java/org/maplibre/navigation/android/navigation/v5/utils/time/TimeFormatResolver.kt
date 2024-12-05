package org.maplibre.navigation.android.navigation.v5.utils.time

import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions
import java.util.Calendar


interface TimeFormatResolver {
    fun nextChain(chain: TimeFormatResolver?)

    fun obtainTimeFormatted(type: MapLibreNavigationOptions.TimeFormat, time: Calendar): String?
}