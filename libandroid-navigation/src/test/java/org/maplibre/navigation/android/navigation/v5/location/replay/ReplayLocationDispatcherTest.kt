package org.maplibre.navigation.android.navigation.v5.location.replay

import android.location.Location
import android.os.Handler
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test

class ReplayLocationDispatcherTest {

    @Test(expected = IllegalArgumentException::class)
    fun checksNonNullLocationListRequired() {
        ReplayLocationDispatcher(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun checksNonEmptyLocationListRequired() {
        val empty = emptyList<Location>()

        ReplayLocationDispatcher(empty)
    }

    @Test
    fun checksLocationDispatchedWhenIsNotLastLocation() {
        val aLocation = mockk<Location>(relaxed = true)
        val theReplayLocationDispatcher = ReplayLocationDispatcher(listOf(aLocation))
        val aReplayLocationListener = mockk<ReplayLocationListener>(relaxed = true)
        theReplayLocationDispatcher.addReplayLocationListener(aReplayLocationListener)

        theReplayLocationDispatcher.run()

        verify {
            aReplayLocationListener.onLocationReplay(aLocation)
        }
    }

    @Test
    fun checksNextDispatchScheduledWhenLocationsIsNotEmpty() {
        val anyLocations: MutableList<Location> = ArrayList(2)
        val firstLocation = mockk<Location>(relaxed = true) {
            every { time } returns 1000L
        }
        val secondLocation = mockk<Location>(relaxed = true) {
            every { time } returns 2000L
        }
        anyLocations.add(firstLocation)
        anyLocations.add(secondLocation)
        val aHandler = mockk<Handler>(relaxed = true)
        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)

        theReplayLocationDispatcher.run()

        verify {
            aHandler.postDelayed(
                theReplayLocationDispatcher,
                1000L
            )
        }
    }

    @Test
    fun checksNextDispatchNotScheduledWhenLocationsIsEmpty() {
        val anyLocations: MutableList<Location> = ArrayList(1)
        val firstLocation = mockk<Location>(relaxed = true)
        anyLocations.add(firstLocation)
        val aHandler = mockk<Handler>(relaxed = true)
        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)

        theReplayLocationDispatcher.run()

        verify(exactly = 0) {
            aHandler.postDelayed(any<Runnable>(), any())
        }
    }

    @Test
    fun checksStopDispatchingWhenLocationsIsEmpty() {
        val firstLocation = mockk<Location>()
        val aHandler = mockk<Handler>(relaxed = true)
        val theReplayLocationDispatcher =
            ReplayLocationDispatcher(mutableListOf(firstLocation), aHandler)

        theReplayLocationDispatcher.run()

        verify {
            aHandler.removeCallbacks(theReplayLocationDispatcher)
        }
    }

    @Test
    fun checksStopDispatchingWhenStop() {
        val aHandler = mockk<Handler>(relaxed = true)
        val theReplayLocationDispatcher = ReplayLocationDispatcher(mutableListOf(mockk()), aHandler)

        theReplayLocationDispatcher.stop()

        verify {
            aHandler.removeCallbacks(theReplayLocationDispatcher)
        }
    }

    @Test
    fun checksStopDispatchingWhenPause() {
        val anyLocations = mockk<MutableList<Location>>(relaxed = true) {
            every { removeAt(any()) } returns mockk()
        }
        val aHandler = mockk<Handler>(relaxed = true)
        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)

        theReplayLocationDispatcher.pause()

        verify {
            aHandler.removeCallbacks(theReplayLocationDispatcher)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun checksNonEmptyLocationListRequiredWhenUpdate() {
        val aHandler = mockk<Handler>(relaxed = true)
        val theReplayLocationDispatcher = ReplayLocationDispatcher(mutableListOf(), aHandler)
        val empty = emptyList<Location>()

        theReplayLocationDispatcher.update(empty)
    }
}