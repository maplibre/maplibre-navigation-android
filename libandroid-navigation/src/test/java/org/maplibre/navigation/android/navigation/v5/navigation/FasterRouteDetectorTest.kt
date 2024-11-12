package org.maplibre.navigation.android.navigation.v5.navigation

import android.content.Context
import android.location.Location
import com.google.gson.GsonBuilder
import junit.framework.Assert
import org.junit.Test
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.DirectionsAdapterFactory
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.route.FasterRoute
import org.maplibre.navigation.android.navigation.v5.route.FasterRouteDetector
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.mockito.Mockito
import java.io.IOException

class FasterRouteDetectorTest : BaseTest() {
    @Test
    @Throws(Exception::class)
    fun sanity() {
        val fasterRouteDetector = FasterRouteDetector()

        Assert.assertNotNull(fasterRouteDetector)
    }

    @Test
    @Throws(Exception::class)
    fun defaultFasterRouteEngine_didGetAddedOnInitialization() {
        val navigation = buildNavigationWithFasterRouteEnabled()

        Assert.assertNotNull(navigation.fasterRouteEngine)
    }

    @Test
    @Throws(Exception::class)
    fun addFasterRouteEngine_didGetAdded() {
        val navigation = buildNavigationWithFasterRouteEnabled()
        val fasterRouteEngine = Mockito.mock(FasterRoute::class.java)

        navigation.fasterRouteEngine = fasterRouteEngine

        Assert.assertEquals(navigation.fasterRouteEngine, fasterRouteEngine)
    }

    @Test
    @Throws(Exception::class)
    fun onFasterRouteResponse_isFasterRouteIsTrue() {
        val navigation = buildNavigationWithFasterRouteEnabled()
        val fasterRouteEngine = navigation.fasterRouteEngine
        var currentProgress = obtainDefaultRouteProgress()
        val longerRoute: DirectionsRoute = currentProgress!!.directionsRoute!!.toBuilder()
            .duration(10000000.0)
            .build()
        currentProgress = currentProgress.copy(
            directionsRoute = longerRoute
        )
        val response = obtainADirectionsResponse()

        val isFasterRoute = fasterRouteEngine.isFasterRoute(response, currentProgress)

        Assert.assertTrue(isFasterRoute)
    }

    @Test
    @Throws(Exception::class)
    fun onSlowerRouteResponse_isFasterRouteIsFalse() {
        val navigation = buildNavigationWithFasterRouteEnabled()
        val fasterRouteEngine = navigation.fasterRouteEngine
        var currentProgress = obtainDefaultRouteProgress()
        val longerRoute: DirectionsRoute = currentProgress!!.directionsRoute!!.toBuilder()
            .duration(1000.0)
            .build()
        currentProgress = currentProgress.copy(
            directionsRoute = longerRoute
        )
        val response = obtainADirectionsResponse()

        val isFasterRoute = fasterRouteEngine.isFasterRoute(response, currentProgress)

        Assert.assertFalse(isFasterRoute)
    }

    @Test
    @Throws(Exception::class)
    fun onNullLocationPassed_shouldCheckFasterRouteIsFalse() {
        val navigation = buildNavigationWithFasterRouteEnabled()
        val fasterRouteEngine = navigation.fasterRouteEngine

        val checkFasterRoute =
            fasterRouteEngine.shouldCheckFasterRoute(null, obtainDefaultRouteProgress())

        Assert.assertFalse(checkFasterRoute)
    }

    @Test
    @Throws(Exception::class)
    fun onNullRouteProgressPassed_shouldCheckFasterRouteIsFalse() {
        val navigation = buildNavigationWithFasterRouteEnabled()
        val fasterRouteEngine = navigation.fasterRouteEngine

        val checkFasterRoute = fasterRouteEngine.shouldCheckFasterRoute(
            Mockito.mock(
                Location::class.java
            ), null
        )

        Assert.assertFalse(checkFasterRoute)
    }

    private fun buildNavigationWithFasterRouteEnabled(): MapLibreNavigation {
        val options = MapLibreNavigationOptions.builder()
            .enableFasterRouteDetection(true)
            .build()
        val context = Mockito.mock(Context::class.java)
        Mockito.`when`(context.applicationContext).thenReturn(
            Mockito.mock(
                Context::class.java
            )
        )
        return MapLibreNavigation(context, options, Mockito.mock(LocationEngine::class.java))
    }

    @Throws(Exception::class)
    private fun obtainDefaultRouteProgress(): RouteProgress {
        val aRoute = obtainADirectionsRoute()
        return buildTestRouteProgress(aRoute, 100.0, 700.0, 1000.0, 0, 0)
    }

    @Throws(IOException::class)
    private fun obtainADirectionsRoute(): DirectionsRoute {
        val gson = GsonBuilder()
            .registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create()
        val body = loadJsonFixture(PRECISION_6)
        val response = gson.fromJson(
            body,
            DirectionsResponse::class.java
        )
        val aRoute = response.routes()[0]

        return aRoute
    }

    @Throws(IOException::class)
    private fun obtainADirectionsResponse(): DirectionsResponse {
        val gson = GsonBuilder()
            .registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create()
        val body = loadJsonFixture(PRECISION_6)
        val response = gson.fromJson(
            body,
            DirectionsResponse::class.java
        )
        return response
    }

    companion object {
        private const val PRECISION_6 = "directions_v5_precision_6.json"
    }
}
