package org.maplibre.navigation.android.navigation.v5.location.replay

import android.location.Location
import android.os.Handler
import java.util.concurrent.CopyOnWriteArrayList

open class ReplayLocationDispatcher(
    locationsToReplay: List<Location>,
    private var handler: Handler = Handler()
) : Runnable {
    private var locationsToReplay = locationsToReplay.toMutableList()
    private var current: Location? = null
    private val replayLocationListeners = mutableListOf<ReplayLocationListener>()

    init {
        checkValidInput(locationsToReplay)
        initialize()
    }

    override fun run() {
        current?.let { current ->
            dispatchLocation(current)
            scheduleNextDispatch()
        }
    }

    fun stop() {
        clearLocations()
        stopDispatching()
    }

    fun pause() {
        stopDispatching()
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

        val currentTime = current!!.time
        current = locationsToReplay.removeFirstOrNull()
        val nextTime = current!!.time
        val diff = nextTime - currentTime
        handler.postDelayed(this, diff)
    }

    private fun clearLocations() {
        locationsToReplay.clear()
    }

    private fun stopDispatching() {
        handler.removeCallbacks(this)
    }

    companion object {
        private const val NON_NULL_AND_NON_EMPTY_LOCATION_LIST_REQUIRED =
            "Non-null and non-empty location list required."
    }
}