package org.maplibre.navigation.android.navigation.v5.navigation

import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.route.FasterRoute
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class NavigationFasterRouteListenerTest {
    @Test
    fun onResponseReceived_fasterRouteIsSentToDispatcher() {
        val eventDispatcher = Mockito.mock(
            NavigationEventDispatcher::class.java
        )
        val fasterRoute = buildFasterRouteThatReturns(true)
        val listener = NavigationFasterRouteListener(eventDispatcher, fasterRoute)
        val response = buildDirectionsResponse()
        val routeProgress = Mockito.mock(RouteProgress::class.java)

        listener.onResponseReceived(response, routeProgress)

        Mockito.verify(eventDispatcher).onFasterRouteEvent(
            ArgumentMatchers.any(
                DirectionsRoute::class.java
            )
        )
    }

    @Test
    fun onResponseReceived_slowerRouteIsNotSentToDispatcher() {
        val eventDispatcher = Mockito.mock(
            NavigationEventDispatcher::class.java
        )
        val fasterRoute = buildFasterRouteThatReturns(false)
        val listener = NavigationFasterRouteListener(eventDispatcher, fasterRoute)
        val response = buildDirectionsResponse()
        val routeProgress = Mockito.mock(RouteProgress::class.java)

        listener.onResponseReceived(response, routeProgress)

        Mockito.verifyNoInteractions(eventDispatcher)
    }

    private fun buildFasterRouteThatReturns(isFaster: Boolean): FasterRoute {
        val fasterRoute = Mockito.mock(FasterRoute::class.java)
        Mockito.`when`(
            fasterRoute.isFasterRoute(
                ArgumentMatchers.any(
                    DirectionsResponse::class.java
                ), ArgumentMatchers.any(
                    RouteProgress::class.java
                )
            )
        ).thenReturn(isFaster)
        return fasterRoute
    }

    private fun buildDirectionsResponse(): DirectionsResponse {
        val response = Mockito.mock(
            DirectionsResponse::class.java
        )
        val routes: MutableList<DirectionsRoute> = ArrayList()
        routes.add(
            Mockito.mock(
                DirectionsRoute::class.java
            )
        )
        Mockito.`when`(response.routes).thenReturn(routes)
        return response
    }
}