package org.maplibre.navigation.android.navigation.v5.location.replay

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.navigation.android.navigation.v5.location.Location
import java.util.concurrent.CopyOnWriteArrayList

open class ReplayLocationDispatcher(
    locationsToReplay: List<Location>,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var locationsToReplay = locationsToReplay.toMutableList()
    private var current: Location? = null
    private val replayLocationListeners = mutableListOf<ReplayLocationListener>()

    init {
        checkValidInput(locationsToReplay)
        initialize()
    }

    fun start() {
        scheduleNextDispatch()
    }

    fun stop() {
        clearLocations()
    }

    fun update(locationsToReplay: List<Location>) {
        checkValidInput(locationsToReplay)
        this.locationsToReplay = CopyOnWriteArrayList(locationsToReplay)
        initialize()
    }

    fun add(toReplay: List<Location>) {
        val shouldRedispatch = locationsToReplay.isEmpty()
        addLocations(toReplay)
        if (shouldRedispatch) {
            stopDispatching()
            scheduleNextDispatch()
        }
    }

    fun addReplayLocationListener(listener: ReplayLocationListener) {
        replayLocationListeners.add(listener)
    }

    fun removeReplayLocationListener(listener: ReplayLocationListener) {
        replayLocationListeners.remove(listener)
    }

    private fun checkValidInput(locations: List<Location>?) {
        val isValidInput = locations.isNullOrEmpty()
        require(!isValidInput) { NON_NULL_AND_NON_EMPTY_LOCATION_LIST_REQUIRED }
    }

    private fun initialize() {
        current = locationsToReplay.removeFirstOrNull()
    }

    private fun addLocations(toReplay: List<Location>) {
        locationsToReplay.addAll(toReplay)
    }

    private fun dispatchLocation(location: Location) {
        for (listener in replayLocationListeners) {
            listener.onLocationReplay(location)
        }
    }

    private fun scheduleNextDispatch() {
        if (locationsToReplay.isEmpty()) {
            stopDispatching()
            return
        }

        val currentTime = current!!.elapsedRealtimeMilliseconds
        current = locationsToReplay.removeFirstOrNull()
        val nextTime = current!!.elapsedRealtimeMilliseconds
        val diff = nextTime - currentTime

        coroutineScope.launch {
            delay(diff)

            current?.let { current ->
                dispatchLocation(current)
                scheduleNextDispatch()
            }
        }
    }

    private fun clearLocations() {
        locationsToReplay.clear()
    }

    private fun stopDispatching() {
        clearLocations()
    }

    companion object {
        private const val NON_NULL_AND_NON_EMPTY_LOCATION_LIST_REQUIRED =
            "Non-null and non-empty location list required."
    }
}