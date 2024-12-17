package org.maplibre.navigation.core.navigation

import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.maplibre.navigation.core.models.DirectionsResponse
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.route.FasterRoute
import org.maplibre.navigation.core.routeprogress.RouteProgress
import kotlin.test.Test

class NavigationFasterRouteListenerTest {

    @Test
    fun onResponseReceived_fasterRouteIsSentToDispatcher() {
        val eventDispatcher = mockk<NavigationEventDispatcher>(relaxed = true)
        val fasterRoute = buildFasterRouteThatReturns(true)
        val listener = NavigationFasterRouteListener(eventDispatcher, fasterRoute)
        val response = buildDirectionsResponse()
        val routeProgress = mockk<RouteProgress>(relaxed = true)

        listener.onResponseReceived(response, routeProgress)

        verify { eventDispatcher.onFasterRouteEvent(any()) }
    }

    @Test
    fun onResponseReceived_slowerRouteIsNotSentToDispatcher() {
        val eventDispatcher = mockk<NavigationEventDispatcher>(relaxed = true)
        val fasterRoute = buildFasterRouteThatReturns(false)
        val listener = NavigationFasterRouteListener(eventDispatcher, fasterRoute)
        val response = buildDirectionsResponse()
        val routeProgress = mockk<RouteProgress>(relaxed = true)

        listener.onResponseReceived(response, routeProgress)

        verify {
            eventDispatcher.wasNot(Called)
        }
    }

    private fun buildFasterRouteThatReturns(isFaster: Boolean): FasterRoute {
        val fasterRoute = mockk<FasterRoute> {
            every { isFasterRoute(any(), any()) } returns isFaster
        }

        return fasterRoute
    }

    private fun buildDirectionsResponse(): DirectionsResponse {
        val response = mockk<DirectionsResponse> {
            every { routes } returns listOf(mockk<DirectionsRoute>())
        }
        return response
    }
}