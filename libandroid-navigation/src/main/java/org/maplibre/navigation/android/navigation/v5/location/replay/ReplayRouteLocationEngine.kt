package org.maplibre.navigation.android.navigation.v5.location.replay

import android.annotation.SuppressLint
import android.os.Handler
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.location.Location
import org.maplibre.navigation.android.navigation.v5.location.LocationEngine
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute

open class ReplayRouteLocationEngine : LocationEngine, Runnable {
    private var converter: ReplayRouteLocationConverter? = null
    private var speed = DEFAULT_SPEED
    private var delay = DEFAULT_DELAY
    private val handler = Handler()
    private var mockedLocations: MutableList<Location> = mutableListOf()
    private var dispatcher: ReplayLocationDispatcher? = null

    @get:SuppressLint("MissingPermission")
    var lastLocation: Location? = null
        private set

    private val callbackList = mutableListOf<Callback>()
    private val replayLocationListener = ReplayLocationListener { location ->
        lastLocation = location
        for (callback in callbackList) {
            callback.onLocationUpdate(location)
        }
        mockedLocations.removeFirstOrNull()
    }

    @SuppressLint("MissingPermission")
    fun assign(route: DirectionsRoute) {
        start(route)
    }

    @SuppressLint("MissingPermission")
    fun moveTo(point: Point) {
        val lastLocation = lastLocation ?: return
        startRoute(point, lastLocation)
    }

    fun assignLastLocation(currentPosition: Point) {
        lastLocation = Location(
            latitude = currentPosition.latitude(),
            longitude = currentPosition.longitude()
        )
    }

    fun updateSpeed(customSpeedInKmPerHour: Int) {
        require(customSpeedInKmPerHour > 0) { SPEED_MUST_BE_GREATER_THAN_ZERO_KM_H }
        this.speed = customSpeedInKmPerHour
    }

    fun updateDelay(customDelayInSeconds: Int) {
        require(customDelayInSeconds > 0) { DELAY_MUST_BE_GREATER_THAN_ZERO_SECONDS }
        this.delay = customDelayInSeconds
    }

    override fun run() {
        converter?.let { converter ->
            var nextMockedLocations = converter.toLocations()
            if (nextMockedLocations.isEmpty()) {
                if (converter.isMultiLegRoute) {
                    nextMockedLocations = converter.toLocations()
                } else {
                    handler.removeCallbacks(this)
                    return
                }
            }
            dispatcher?.add(nextMockedLocations)
            mockedLocations.addAll(nextMockedLocations)
            scheduleNextDispatch()
        }
    }

    private fun start(route: DirectionsRoute) {
        handler.removeCallbacks(this)
        converter = ReplayRouteLocationConverter(route, speed, delay).apply {
            initializeTime()
            mockedLocations = toLocations()
        }

        dispatcher = obtainDispatcher().apply {
            run()
        }

        scheduleNextDispatch()
    }

    private fun obtainDispatcher(): ReplayLocationDispatcher {
        dispatcher?.stop()
        dispatcher?.removeReplayLocationListener(replayLocationListener)

        return ReplayLocationDispatcher(mockedLocations).also { dispatch ->
            dispatch.addReplayLocationListener(replayLocationListener)
            dispatcher = dispatch
        }
    }

    private fun startRoute(point: Point, lastLocation: Location) {
        converter?.let { converter ->
            handler.removeCallbacks(this)
            converter.updateSpeed(speed)
            converter.updateDelay(delay)
            converter.initializeTime()

            val route = obtainRoute(point, lastLocation)
            mockedLocations = converter.calculateMockLocations(converter.sliceRoute(route))
            dispatcher = obtainDispatcher()
            dispatcher?.run()
        }
    }

    private fun obtainRoute(point: Point, lastLocation: Location): LineString {
        val pointList: MutableList<Point> = ArrayList()
        pointList.add(Point.fromLngLat(lastLocation.longitude, lastLocation.latitude))
        pointList.add(point)
        return LineString.fromLngLats(pointList)
    }

    private fun scheduleNextDispatch() {
        val currentMockedPoints = mockedLocations.size
        if (currentMockedPoints == ZERO) {
            handler.postDelayed(this, DO_NOT_DELAY.toLong())
        } else if (currentMockedPoints <= MOCKED_POINTS_LEFT_THRESHOLD) {
            handler.postDelayed(this, ONE_SECOND_IN_MILLISECONDS.toLong())
        } else {
            handler.postDelayed(
                this,
                ((currentMockedPoints - MOCKED_POINTS_LEFT_THRESHOLD) * ONE_SECOND_IN_MILLISECONDS).toLong()
            )
        }
    }

    fun onStop() {
        dispatcher?.stop()
        handler.removeCallbacks(this)
        callbackList.removeAll(callbackList)
        dispatcher?.removeReplayLocationListener(replayLocationListener)
    }

    override fun listenToLocation(request: LocationEngineRequest): Flow<Location> = callbackFlow {
        val callback = Callback { location ->
            trySend(location)
        }

        callbackList.add(callback)
        awaitClose { callbackList.remove(callback) }
    }

    override suspend fun getLastLocation(): Location? {
        return lastLocation
    }

    companion object {
        private const val MOCKED_POINTS_LEFT_THRESHOLD = 5
        private const val ONE_SECOND_IN_MILLISECONDS = 1000
        private const val FORTY_FIVE_KM_PER_HOUR = 45
        private const val DEFAULT_SPEED = FORTY_FIVE_KM_PER_HOUR
        private const val ONE_SECOND = 1
        private const val DEFAULT_DELAY = ONE_SECOND
        private const val DO_NOT_DELAY = 0
        private const val ZERO = 0
        private const val SPEED_MUST_BE_GREATER_THAN_ZERO_KM_H =
            "Speed must be greater than 0 km/h."
        private const val DELAY_MUST_BE_GREATER_THAN_ZERO_SECONDS =
            "Delay must be greater than 0 seconds."
        private const val REPLAY_ROUTE = "ReplayRouteLocation"
    }

    fun interface Callback {
        fun onLocationUpdate(location: Location)
    }
}
