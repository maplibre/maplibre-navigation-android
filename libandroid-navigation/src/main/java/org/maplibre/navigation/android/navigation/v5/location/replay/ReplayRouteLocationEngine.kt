package org.maplibre.navigation.android.navigation.v5.location.replay

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.location.Location
import android.os.Handler
import android.os.Looper
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
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
    private val callbackList = mutableListOf<LocationEngineCallback<LocationEngineResult>>()
    private val replayLocationListener = ReplayLocationListener { location ->
            lastLocation = location
            val result = LocationEngineResult.create(location)
            for (callback in callbackList) {
                callback.onSuccess(result)
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
        initializeLastLocation()
        lastLocation?.longitude = currentPosition.longitude()
        lastLocation?.latitude = currentPosition.latitude()
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

    private fun initializeLastLocation() {
        if (lastLocation == null) {
            lastLocation = Location(REPLAY_ROUTE)
        }
    }

    fun onStop() {
        dispatcher?.stop()
        handler.removeCallbacks(this)
        callbackList.removeAll(callbackList)
        dispatcher?.removeReplayLocationListener(replayLocationListener)
    }

    @Throws(SecurityException::class)
    override fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult>) {
        lastLocation?.let { lastLocation ->
            callback.onSuccess(LocationEngineResult.create(lastLocation))
        } ?: callback.onFailure(Exception("No last location"))
    }

    @Throws(SecurityException::class)
    override fun requestLocationUpdates(
        request: LocationEngineRequest,
        callback: LocationEngineCallback<LocationEngineResult>,
        looper: Looper?
    ) {
        callbackList.add(callback)
    }

    @Throws(SecurityException::class)
    override fun requestLocationUpdates(
        request: LocationEngineRequest,
        pendingIntent: PendingIntent
    ) {
    }

    override fun removeLocationUpdates(callback: LocationEngineCallback<LocationEngineResult>) {
        callbackList.remove(callback)
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent) {
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
}
