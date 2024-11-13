package org.maplibre.navigation.android.navigation.v5.navigation

import android.app.NotificationManager
import android.content.Context
import com.google.gson.GsonBuilder
import junit.framework.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.mockito.Mock
import org.mockito.Mockito

// TODO fabi755, fix tests, after updated JSON parsing
//class MapLibreNavigationNotificationTest : BaseTest() {
//    @Mock
//    var notificationManager: NotificationManager? = null
//
//    private var route: DirectionsRoute? = null
//
//    @Before
//    @Throws(Exception::class)
//    fun setUp() {
//        val json = loadJsonFixture(DIRECTIONS_ROUTE_FIXTURE)
//        val gson = GsonBuilder()
//            .registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create()
//        val response = gson.fromJson(
//            json,
//            DirectionsResponse::class.java
//        )
//        route = response.routes()[0]
//    }
//
//    @Ignore
//    @Test
//    @Throws(Exception::class)
//    fun sanity() {
//        val mapLibreNavigationNotification = MapLibreNavigationNotification(
//            Mockito.mock(Context::class.java), Mockito.mock(
//                MapLibreNavigation::class.java
//            )
//        )
//        Assert.assertNotNull(mapLibreNavigationNotification)
//    }
//
//    @Ignore
//    @Test
//    @Throws(Exception::class)
//    fun updateDefaultNotification_onlyUpdatesNameWhenNew() {
//        val routeProgress = RouteProgress(
//            directionsRoute = route!!,
//            legIndex = 0,
//            distanceRemaining = route!!.distance,
//            currentStepPoints = null,
//            upcomingStepPoints = null,
//            stepIndex = 0,
//            legDistanceRemaining = route!!.distance,
//            stepDistanceRemaining = route!!.distance,
//            intersections = null,
//            currentIntersection = null,
//            upcomingIntersection = null,
//            currentLegAnnotation = null,
//            intersectionDistancesAlongStep = null
//        )
//
//        val mapLibreNavigationNotification = MapLibreNavigationNotification(
//            Mockito.mock(Context::class.java), Mockito.mock(
//                MapLibreNavigation::class.java
//            )
//        )
//
//        mapLibreNavigationNotification.updateNotification(routeProgress)
//        //    notificationManager.getActiveNotifications()[0].getNotification().contentView;
//        //    verify(notificationManager, times(1)).getActiveNotifications()[0];
//    }
//
//    companion object {
//        private const val DIRECTIONS_ROUTE_FIXTURE = "directions_v5_precision_6.json"
//    }
//}
