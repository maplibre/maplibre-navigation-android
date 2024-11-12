package org.maplibre.navigation.android.navigation.v5.location.replay

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class ReplayJsonRouteLocationMapperTest {
    @Test(expected = IllegalArgumentException::class)
    fun checksNonNullLocationListRequired() {
        val nullLocations: List<ReplayLocationDto>? = null

        ReplayJsonRouteLocationMapper(nullLocations)
    }

    @Test(expected = IllegalArgumentException::class)
    fun checksNonEmptyLocationListRequired() {
        val empty = emptyList<ReplayLocationDto>()

        ReplayJsonRouteLocationMapper(empty)
    }

    @Test
    fun checksProviderMapping() {
        val anyReplayLocations: MutableList<ReplayLocationDto> = ArrayList(1)
        val aReplayLocation = ReplayLocationDto()
        anyReplayLocations.add(aReplayLocation)
        val theReplayJsonRouteLocationMapper = ReplayJsonRouteLocationMapper(anyReplayLocations)

        val locations = theReplayJsonRouteLocationMapper.toLocations()

        val theLocation = locations[0]
        Assert.assertEquals("ReplayLocation", theLocation.provider)
    }

    @Test
    fun checksLongitudeMapping() {
        val anyReplayLocations: MutableList<ReplayLocationDto> = ArrayList(1)
        val aReplayLocation = ReplayLocationDto()
        aReplayLocation.longitude = 2.0
        anyReplayLocations.add(aReplayLocation)
        val theReplayJsonRouteLocationMapper = ReplayJsonRouteLocationMapper(anyReplayLocations)

        val locations = theReplayJsonRouteLocationMapper.toLocations()

        val theLocation = locations[0]
        Assert.assertEquals(2.0, theLocation.longitude, DELTA)
    }

    @Test
    fun checksAccuracyMapping() {
        val anyReplayLocations: MutableList<ReplayLocationDto> = ArrayList(1)
        val aReplayLocation = ReplayLocationDto()
        aReplayLocation.horizontalAccuracyMeters = 3.0f
        anyReplayLocations.add(aReplayLocation)
        val theReplayJsonRouteLocationMapper = ReplayJsonRouteLocationMapper(anyReplayLocations)

        val locations = theReplayJsonRouteLocationMapper.toLocations()

        val theLocation = locations[0]
        Assert.assertEquals(3.0, theLocation.accuracy.toDouble(), DELTA)
    }

    @Test
    fun checksBearingMapping() {
        val anyReplayLocations: MutableList<ReplayLocationDto> = ArrayList(1)
        val aReplayLocation = ReplayLocationDto()
        aReplayLocation.bearing = 180.0
        anyReplayLocations.add(aReplayLocation)
        val theReplayJsonRouteLocationMapper = ReplayJsonRouteLocationMapper(anyReplayLocations)

        val locations = theReplayJsonRouteLocationMapper.toLocations()

        val theLocation = locations[0]
        Assert.assertEquals(180.0, theLocation.bearing.toDouble(), DELTA)
    }

    @Test
    @Config(sdk = [26])
    fun checksVerticalAccuracyMapping() {
        val anyReplayLocations: MutableList<ReplayLocationDto> = ArrayList(1)
        val aReplayLocation = ReplayLocationDto()
        aReplayLocation.verticalAccuracyMeters = 8.0f
        anyReplayLocations.add(aReplayLocation)
        val theReplayJsonRouteLocationMapper = ReplayJsonRouteLocationMapper(anyReplayLocations)

        val locations = theReplayJsonRouteLocationMapper.toLocations()

        val theLocation = locations[0]
        Assert.assertEquals(8.0, theLocation.verticalAccuracyMeters.toDouble(), DELTA)
    }

    @Test(expected = NoSuchMethodError::class)
    @Config(sdk = [25])
    fun checksVerticalAccuracyNotMappedForBelowOreo() {
        val anyReplayLocations: MutableList<ReplayLocationDto> = ArrayList(1)
        val aReplayLocation = ReplayLocationDto()
        aReplayLocation.verticalAccuracyMeters = 8.0f
        anyReplayLocations.add(aReplayLocation)
        val theReplayJsonRouteLocationMapper = ReplayJsonRouteLocationMapper(anyReplayLocations)

        val locations = theReplayJsonRouteLocationMapper.toLocations()

        val theLocation = locations[0]
        theLocation.verticalAccuracyMeters
    }

    @Test
    fun checksSpeedMapping() {
        val anyReplayLocations: MutableList<ReplayLocationDto> = ArrayList(1)
        val aReplayLocation = ReplayLocationDto()
        aReplayLocation.speed = 65.0
        anyReplayLocations.add(aReplayLocation)
        val theReplayJsonRouteLocationMapper = ReplayJsonRouteLocationMapper(anyReplayLocations)

        val locations = theReplayJsonRouteLocationMapper.toLocations()

        val theLocation = locations[0]
        Assert.assertEquals(65.0, theLocation.speed.toDouble(), DELTA)
    }

    @Test
    fun checksLatitudeMapping() {
        val anyReplayLocations: MutableList<ReplayLocationDto> = ArrayList(1)
        val aReplayLocation = ReplayLocationDto()
        aReplayLocation.latitude = 7.0
        anyReplayLocations.add(aReplayLocation)
        val theReplayJsonRouteLocationMapper = ReplayJsonRouteLocationMapper(anyReplayLocations)

        val locations = theReplayJsonRouteLocationMapper.toLocations()

        val theLocation = locations[0]
        Assert.assertEquals(7.0, theLocation.latitude, DELTA)
    }

    @Test
    fun checksAltitudeMapping() {
        val anyReplayLocations: MutableList<ReplayLocationDto> = ArrayList(1)
        val aReplayLocation = ReplayLocationDto()
        aReplayLocation.altitude = 9.0
        anyReplayLocations.add(aReplayLocation)
        val theReplayJsonRouteLocationMapper = ReplayJsonRouteLocationMapper(anyReplayLocations)

        val locations = theReplayJsonRouteLocationMapper.toLocations()

        val theLocation = locations[0]
        Assert.assertEquals(9.0, theLocation.altitude, DELTA)
    }

    @Test
    fun checksTimeMapping() {
        val anyReplayLocations: MutableList<ReplayLocationDto> = ArrayList(1)
        val aReplayLocation = ReplayLocationDto()
        val aDate = Date()
        aReplayLocation.date = aDate
        anyReplayLocations.add(aReplayLocation)
        val theReplayJsonRouteLocationMapper = ReplayJsonRouteLocationMapper(anyReplayLocations)

        val locations = theReplayJsonRouteLocationMapper.toLocations()

        val theLocation = locations[0]
        val timeFromDate = aDate.time
        Assert.assertEquals(timeFromDate.toDouble(), theLocation.time.toDouble(), DELTA)
    }

    companion object {
        private const val DELTA = 1e-15
    }
}