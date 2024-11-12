package org.maplibre.navigation.android.navigation.v5.location.replay

import android.location.Location
import android.os.Handler
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class ReplayLocationDispatcherTest {
    @Test(expected = IllegalArgumentException::class)
    fun checksNonNullLocationListRequired() {
        ReplayLocationDispatcher(emptyList<Location>())
    }

    @Test(expected = IllegalArgumentException::class)
    fun checksNonEmptyLocationListRequired() {
        val empty = emptyList<Location>()

        ReplayLocationDispatcher(empty)
    }

    @Test
    fun checksLocationDispatchedWhenIsNotLastLocation() {
        val anyLocations: MutableList<Location> = ArrayList(1)
        val aLocation = createALocation()
        anyLocations.add(aLocation)
        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations)
        val aReplayLocationListener = Mockito.mock(
            ReplayLocationListener::class.java
        )
        theReplayLocationDispatcher.addReplayLocationListener(aReplayLocationListener)

        theReplayLocationDispatcher.run()

        Mockito.verify(aReplayLocationListener).onLocationReplay(ArgumentMatchers.eq(aLocation))
    }

    @Test
    fun checksNextDispatchScheduledWhenLocationsIsNotEmpty() {
        val anyLocations: MutableList<Location> = ArrayList(2)
        val firstLocation = createALocation()
        Mockito.`when`(firstLocation.time).thenReturn(1000L)
        val secondLocation = createALocation()
        Mockito.`when`(secondLocation.time).thenReturn(2000L)
        anyLocations.add(firstLocation)
        anyLocations.add(secondLocation)
        val aHandler = Mockito.mock(Handler::class.java)
        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)

        theReplayLocationDispatcher.run()

        Mockito.verify(aHandler, Mockito.times(1)).postDelayed(
            ArgumentMatchers.eq(theReplayLocationDispatcher),
            ArgumentMatchers.eq(1000L)
        )
    }

    @Test
    fun checksNextDispatchNotScheduledWhenLocationsIsEmpty() {
        val anyLocations: MutableList<Location> = ArrayList(1)
        val firstLocation = createALocation()
        anyLocations.add(firstLocation)
        val aHandler = Mockito.mock(Handler::class.java)
        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)

        theReplayLocationDispatcher.run()

        Mockito.verify(aHandler, Mockito.never()).postDelayed(
            ArgumentMatchers.any(
                Runnable::class.java
            ), ArgumentMatchers.anyLong()
        )
    }

    @Test
    fun checksStopDispatchingWhenLocationsIsEmpty() {
        val anyLocations: MutableList<Location> = ArrayList(1)
        val firstLocation = createALocation()
        anyLocations.add(firstLocation)
        val aHandler = Mockito.mock(Handler::class.java)
        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)

        theReplayLocationDispatcher.run()

        Mockito.verify(aHandler, Mockito.times(1))
            .removeCallbacks(ArgumentMatchers.eq(theReplayLocationDispatcher))
    }

    //TODO fabi755: finalize mockito conversation
//    @Test
//    fun checksClearLocationsWhenStop() {
//        val theLocations: MutableList<Location> = Mockito.mock(
//            MutableList::class.java
//        )
//        val aHandler = Mockito.mock(Handler::class.java)
//        val theReplayLocationDispatcher = ReplayLocationDispatcher(theLocations, aHandler)
//
//        theReplayLocationDispatcher.stop()
//
//        Mockito.verify(theLocations, Mockito.times(1)).clear()
//    }
//
//    @Test
//    fun checksStopDispatchingWhenStop() {
//        val anyLocations: List<Location> = Mockito.mock<List<*>>(
//            MutableList::class.java
//        )
//        val aHandler = Mockito.mock(Handler::class.java)
//        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)
//
//        theReplayLocationDispatcher.stop()
//
//        Mockito.verify(aHandler, Mockito.times(1))
//            .removeCallbacks(ArgumentMatchers.eq(theReplayLocationDispatcher))
//    }
//
//    @Test
//    fun checksStopDispatchingWhenPause() {
//        val anyLocations: List<Location> = Mockito.mock<List<*>>(
//            MutableList::class.java
//        )
//        val aHandler = Mockito.mock(Handler::class.java)
//        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)
//
//        theReplayLocationDispatcher.pause()
//
//        Mockito.verify(aHandler, Mockito.times(1))
//            .removeCallbacks(ArgumentMatchers.eq(theReplayLocationDispatcher))
//    }
//
//    @Test(expected = IllegalArgumentException::class)
//    fun checksNonNullLocationListRequiredWhenUpdate() {
//        val anyLocations: List<Location> = Mockito.mock<List<*>>(
//            MutableList::class.java
//        )
//        val aHandler = Mockito.mock(Handler::class.java)
//        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)
//        val nullLocations: List<Location>? = null
//
//        theReplayLocationDispatcher.update(nullLocations!!)
//    }
//
//    @Test(expected = IllegalArgumentException::class)
//    fun checksNonEmptyLocationListRequiredWhenUpdate() {
//        val anyLocations: List<Location> = Mockito.mock<List<*>>(
//            MutableList::class.java
//        )
//        val aHandler = Mockito.mock(Handler::class.java)
//        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)
//        val empty = emptyList<Location>()
//
//        theReplayLocationDispatcher.update(empty)
//    }
//
//    @Test
//    fun checksAddLocationsWhenAdd() {
//        val anyLocations: MutableList<*> = Mockito.mock(
//            MutableList::class.java
//        )
//        val aHandler = Mockito.mock(Handler::class.java)
//        val theReplayLocationDispatcher = ReplayLocationDispatcher(anyLocations, aHandler)
//        val locationsToReplay: List<Location> = Mockito.mock<List<*>>(
//            MutableList::class.java
//        )
//
//        theReplayLocationDispatcher.add(locationsToReplay)
//
//        Mockito.verify(anyLocations, Mockito.times(1))
//            .addAll(ArgumentMatchers.eq(locationsToReplay))
//    }

    private fun createALocation(): Location {
        val location = Mockito.mock(Location::class.java)
        return location
    }
}