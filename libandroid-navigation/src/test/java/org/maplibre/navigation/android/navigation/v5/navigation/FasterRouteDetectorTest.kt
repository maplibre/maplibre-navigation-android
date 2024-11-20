package org.maplibre.navigation.android.navigation.v5.navigation

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.route.FasterRoute
import org.maplibre.navigation.android.navigation.v5.route.FasterRouteDetector
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import java.io.IOException

class FasterRouteDetectorTest : BaseTest() {
    @Test
    @Throws(Exception::class)
    fun sanity() {
        val fasterRouteDetector = FasterRouteDetector(MapLibreNavigationOptions())

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
        val fasterRouteEngine = mockk<FasterRoute>()

        navigation.fasterRouteEngine = fasterRouteEngine

        Assert.assertEquals(navigation.fasterRouteEngine, fasterRouteEngine)
    }

    @Test
    @Throws(Exception::class)
    fun onFasterRouteResponse_isFasterRouteIsTrue() {
        val navigation = buildNavigationWithFasterRouteEnabled()
        val fasterRouteEngine = navigation.fasterRouteEngine
        var currentProgress = obtainDefaultRouteProgress()
        val longerRoute: DirectionsRoute = currentProgress.directionsRoute.copy(
            duration = 10000000.0
        )
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
        val longerRoute: DirectionsRoute = currentProgress.directionsRoute.copy(
            duration = 1000.0
        )
        currentProgress = currentProgress.copy(
            directionsRoute = longerRoute
        )
        val response = obtainADirectionsResponse()

        val isFasterRoute = fasterRouteEngine.isFasterRoute(response, currentProgress)

        Assert.assertFalse(isFasterRoute)
    }

    private fun buildNavigationWithFasterRouteEnabled(): MapLibreNavigation {
        val options = MapLibreNavigationOptions(enableFasterRouteDetection = true)
        val context = mockk<Context> {
            every { applicationContext } returns this
        }
        return MapLibreNavigation(context, options, mockk())
    }

    @Throws(Exception::class)
    private fun obtainDefaultRouteProgress(): RouteProgress {
        val aRoute = obtainADirectionsRoute()
        return buildTestRouteProgress(aRoute, 100.0, 700.0, 1000.0, 0, 0)
    }

    @Throws(IOException::class)
    private fun obtainADirectionsRoute(): DirectionsRoute {
        val body = loadJsonFixture(PRECISION_6)
        val response = DirectionsResponse.fromJson(body)
        val aRoute = response.routes[0]

        return aRoute
    }

    @Throws(IOException::class)
    private fun obtainADirectionsResponse(): DirectionsResponse {
        val body = loadJsonFixture(PRECISION_6)
        val response = DirectionsResponse.fromJson(body)
        return response
    }

    companion object {
        private const val PRECISION_6 = "directions_v5_precision_6.json"
    }
}
