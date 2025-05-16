package org.maplibre.navigation.core.location.replay

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.navigation.core.location.Location

open class ReplayLocationDispatcher(
    locationsToReplay: List<Location>,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var locationsToReplay = locationsToReplay.toMutableList()
    private var current: Location? = null
    private val replayLocationListeners = mutableListOf<ReplayLocationListener>()
    private var currentDispatcherJob: Job? = null

    init {
        checkValidInput(locationsToReplay)
        initialize()
    }

    fun start() {
        scheduleNextDispatch()
    }

    fun stop() {
        clearLocations()
        stopDispatching()
    }

    fun update(locationsToReplay: List<Location>) {
        checkValidInput(locationsToReplay)
        this.locationsToReplay = locationsToReplay.toMutableList()
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

        val currentTime = current?.timeMilliseconds ?: 0
        current = locationsToReplay.removeFirstOrNull()
        val nextTime = current?.timeMilliseconds ?: 0
        val diff = nextTime - currentTime

        currentDispatcherJob?.cancel()
        currentDispatcherJob = coroutineScope.launch {
            current?.let { current ->
                dispatchLocation(current)
            }

            delay(diff)
            scheduleNextDispatch()
        }
    }

    private fun clearLocations() {
        locationsToReplay.clear()
    }

    private fun stopDispatching() {
        currentDispatcherJob?.cancel()
    }

    companion object {
        private const val NON_NULL_AND_NON_EMPTY_LOCATION_LIST_REQUIRED =
            "Non-null and non-empty location list required."
    }
}