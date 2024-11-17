package org.maplibre.navigation.android.navigation.v5.utils.time


internal class TimeFormattingChain {

    fun setup(isDeviceTwentyFourHourFormat: Boolean): TimeFormatResolver {
        val noneSpecified = NoneSpecifiedTimeFormat(isDeviceTwentyFourHourFormat)
        val twentyFourHours = TwentyFourHoursTimeFormat()
        twentyFourHours.nextChain(noneSpecified)
        val rootOfTheChain = TwelveHoursTimeFormat()
        rootOfTheChain.nextChain(twentyFourHours)
        return rootOfTheChain
    }
}
