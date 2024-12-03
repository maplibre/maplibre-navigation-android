package org.maplibre.navigation.android.navigation.v5.navigation

import android.app.NotificationManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.R

class MapLibreNavigationNotificationTest : BaseTest() {
    private var notificationManager: NotificationManager? = mockk()

    private var route: DirectionsRoute? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        val json = loadJsonFixture(DIRECTIONS_ROUTE_FIXTURE)
        val response = DirectionsResponse.fromJson(json)
        route = response.routes[0]
    }

    @Ignore
    @Test
    @Throws(Exception::class)
    fun sanity() {
        val mapLibreNavigationNotification = MapLibreNavigationNotification(mockk(), mockk())
        Assert.assertNotNull(mapLibreNavigationNotification)
    }

    @Ignore
    @Test
    @Throws(Exception::class)
    fun updateDefaultNotification_onlyUpdatesNameWhenNew() {
        val routeProgress = RouteProgress(
            directionsRoute = route!!,
            legIndex = 0,
            distanceRemaining = route!!.distance,
            currentStepPoints = listOf(),
            upcomingStepPoints = null,
            stepIndex = 0,
            legDistanceRemaining = route!!.distance,
            stepDistanceRemaining = route!!.distance,
            intersections = null,
            currentIntersection = null,
            upcomingIntersection = null,
            currentLegAnnotation = null,
            intersectionDistancesAlongStep = null
        )

        val mapLibreNavigationNotification = MapLibreNavigationNotification(mockk(), mockk())

        mapLibreNavigationNotification.updateNotification(routeProgress)

        verify {
            notificationManager!!.getActiveNotifications()[0].notification.contentView.setTextViewText(
                R.id.notificationDistanceText, "0.0 mi"
            )
        }
    }

    companion object {
        private const val DIRECTIONS_ROUTE_FIXTURE = "directions_v5_precision_6.json"
    }
}
