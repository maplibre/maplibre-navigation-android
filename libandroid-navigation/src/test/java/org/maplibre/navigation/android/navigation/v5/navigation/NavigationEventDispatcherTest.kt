package org.maplibre.navigation.android.navigation.v5.navigation

import android.content.Context
import android.location.Location
import com.google.gson.GsonBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.milestone.BannerInstructionMilestone
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone
import org.maplibre.navigation.android.navigation.v5.milestone.MilestoneEventListener
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.offroute.OffRouteListener
import org.maplibre.navigation.android.navigation.v5.route.FasterRouteListener
import org.maplibre.navigation.android.navigation.v5.routeprogress.ProgressChangeListener
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.utils.RouteUtils
import org.robolectric.RobolectricTestRunner

// TODO fabi755, fix tests, after updated JSON parsing
//@RunWith(RobolectricTestRunner::class)
//class NavigationEventDispatcherTest : BaseTest() {
//    @Mock
//    var milestoneEventListener: MilestoneEventListener? = null
//
//    @Mock
//    var progressChangeListener: ProgressChangeListener? = null
//
//    @Mock
//    var offRouteListener: OffRouteListener? = null
//
//    @Mock
//    var navigationEventListener: NavigationEventListener? = null
//
//    @Mock
//    var fasterRouteListener: FasterRouteListener? = null
//
//    @Mock
//    var location: Location? = null
//
//    @Mock
//    var milestone: Milestone? = null
//
//    private var navigationEventDispatcher: NavigationEventDispatcher? = null
//    private var navigation: MapLibreNavigation? = null
//    private var route: DirectionsRoute? = null
//    private var routeProgress: RouteProgress? = null
//
//    @Before
//    @Throws(Exception::class)
//    fun setup() {
//        MockitoAnnotations.initMocks(this)
//        val context = Mockito.mock(Context::class.java)
//        Mockito.`when`(context.applicationContext).thenReturn(
//            Mockito.mock(
//                Context::class.java
//            )
//        )
//        navigation = MapLibreNavigation(context, Mockito.mock(LocationEngine::class.java))
//        navigationEventDispatcher = navigation!!.eventDispatcher
//
//        val gson = GsonBuilder()
//            .registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create()
//        val body = loadJsonFixture(PRECISION_6)
//        val response = gson.fromJson(
//            body,
//            DirectionsResponse::class.java
//        )
//        route = response.routes()[0]
//
//        routeProgress = buildTestRouteProgress(route!!, 100.0, 100.0, 100.0, 0, 0)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun sanity() {
//        val navigationEventDispatcher = NavigationEventDispatcher()
//        Assert.assertNotNull(navigationEventDispatcher)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addMilestoneEventListener_didAddListener() {
//        navigationEventDispatcher!!.onMilestoneEvent(routeProgress, "", milestone)
//        Mockito.verify(milestoneEventListener, Mockito.times(0))!!
//            .onMilestoneEvent(routeProgress, "", milestone)
//
//        navigation!!.addMilestoneEventListener(milestoneEventListener!!)
//        navigationEventDispatcher!!.onMilestoneEvent(routeProgress, "", milestone)
//        Mockito.verify(milestoneEventListener, Mockito.times(1))!!
//            .onMilestoneEvent(routeProgress, "", milestone)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addMilestoneEventListener_onlyAddsListenerOnce() {
//        navigationEventDispatcher!!.onMilestoneEvent(routeProgress, "", milestone)
//        Mockito.verify(milestoneEventListener, Mockito.times(0))!!
//            .onMilestoneEvent(routeProgress, "", milestone)
//
//        navigation!!.addMilestoneEventListener(milestoneEventListener!!)
//        navigation!!.addMilestoneEventListener(milestoneEventListener!!)
//        navigation!!.addMilestoneEventListener(milestoneEventListener!!)
//        navigationEventDispatcher!!.onMilestoneEvent(routeProgress, "", milestone)
//        Mockito.verify(milestoneEventListener, Mockito.times(1))!!
//            .onMilestoneEvent(routeProgress, "", milestone)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeMilestoneEventListener_didRemoveListener() {
//        navigation!!.addMilestoneEventListener(milestoneEventListener!!)
//        navigation!!.removeMilestoneEventListener(milestoneEventListener)
//        navigationEventDispatcher!!.onMilestoneEvent(routeProgress, "", milestone)
//        Mockito.verify(milestoneEventListener, Mockito.times(0))!!
//            .onMilestoneEvent(routeProgress, "", milestone)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeMilestoneEventListener_nullRemovesAllListeners() {
//        navigation!!.addMilestoneEventListener(milestoneEventListener!!)
//        navigation!!.addMilestoneEventListener(
//            Mockito.mock(
//                MilestoneEventListener::class.java
//            )
//        )
//        navigation!!.addMilestoneEventListener(
//            Mockito.mock(
//                MilestoneEventListener::class.java
//            )
//        )
//        navigation!!.addMilestoneEventListener(
//            Mockito.mock(
//                MilestoneEventListener::class.java
//            )
//        )
//
//        navigation!!.removeMilestoneEventListener(null)
//        navigationEventDispatcher!!.onMilestoneEvent(routeProgress, "", milestone)
//        Mockito.verify(milestoneEventListener, Mockito.times(0))!!
//            .onMilestoneEvent(routeProgress, "", milestone)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addProgressChangeListener_didAddListener() {
//        navigationEventDispatcher!!.onProgressChange(location, routeProgress)
//        Mockito.verify(progressChangeListener, Mockito.times(0))!!.onProgressChange(
//            location!!, routeProgress
//        )
//
//        navigation!!.addProgressChangeListener(progressChangeListener!!)
//        navigationEventDispatcher!!.onProgressChange(location, routeProgress)
//        Mockito.verify(progressChangeListener, Mockito.times(1))!!.onProgressChange(
//            location!!, routeProgress
//        )
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addProgressChangeListener_onlyAddsListenerOnce() {
//        navigationEventDispatcher!!.onProgressChange(location, routeProgress)
//        Mockito.verify(progressChangeListener, Mockito.times(0))!!.onProgressChange(
//            location!!, routeProgress
//        )
//
//        navigation!!.addProgressChangeListener(progressChangeListener!!)
//        navigation!!.addProgressChangeListener(progressChangeListener!!)
//        navigation!!.addProgressChangeListener(progressChangeListener!!)
//        navigationEventDispatcher!!.onProgressChange(location, routeProgress)
//        Mockito.verify(progressChangeListener, Mockito.times(1))!!.onProgressChange(
//            location!!, routeProgress
//        )
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeProgressChangeListener_didRemoveListener() {
//        navigation!!.addProgressChangeListener(progressChangeListener!!)
//        navigation!!.removeProgressChangeListener(progressChangeListener)
//        navigationEventDispatcher!!.onProgressChange(location, routeProgress)
//        Mockito.verify(progressChangeListener, Mockito.times(0))!!.onProgressChange(
//            location!!, routeProgress
//        )
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeProgressChangeListener_nullRemovesAllListeners() {
//        navigation!!.addProgressChangeListener(progressChangeListener!!)
//        navigation!!.addProgressChangeListener(
//            Mockito.mock(
//                ProgressChangeListener::class.java
//            )
//        )
//        navigation!!.addProgressChangeListener(
//            Mockito.mock(
//                ProgressChangeListener::class.java
//            )
//        )
//        navigation!!.addProgressChangeListener(
//            Mockito.mock(
//                ProgressChangeListener::class.java
//            )
//        )
//
//        navigation!!.removeProgressChangeListener(null)
//        navigationEventDispatcher!!.onProgressChange(location, routeProgress)
//        Mockito.verify(progressChangeListener, Mockito.times(0))!!.onProgressChange(
//            location!!, routeProgress
//        )
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addOffRouteListener_didAddListener() {
//        navigationEventDispatcher!!.onUserOffRoute(location)
//        Mockito.verify(offRouteListener, Mockito.times(0))!!.userOffRoute(location)
//
//        navigation!!.addOffRouteListener(offRouteListener!!)
//        navigationEventDispatcher!!.onUserOffRoute(location)
//        Mockito.verify(offRouteListener, Mockito.times(1))!!.userOffRoute(location)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addOffRouteListener_onlyAddsListenerOnce() {
//        navigationEventDispatcher!!.onUserOffRoute(location)
//        Mockito.verify(offRouteListener, Mockito.times(0))!!.userOffRoute(location)
//
//        navigation!!.addOffRouteListener(offRouteListener!!)
//        navigation!!.addOffRouteListener(offRouteListener!!)
//        navigation!!.addOffRouteListener(offRouteListener!!)
//        navigationEventDispatcher!!.onUserOffRoute(location)
//        Mockito.verify(offRouteListener, Mockito.times(1))!!.userOffRoute(location)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeOffRouteListener_didRemoveListener() {
//        navigation!!.addOffRouteListener(offRouteListener!!)
//        navigation!!.removeOffRouteListener(offRouteListener)
//        navigationEventDispatcher!!.onUserOffRoute(location)
//        Mockito.verify(offRouteListener, Mockito.times(0))!!.userOffRoute(location)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeOffRouteListener_nullRemovesAllListeners() {
//        navigation!!.addOffRouteListener(offRouteListener!!)
//        navigation!!.addOffRouteListener(Mockito.mock(OffRouteListener::class.java))
//        navigation!!.addOffRouteListener(Mockito.mock(OffRouteListener::class.java))
//        navigation!!.addOffRouteListener(Mockito.mock(OffRouteListener::class.java))
//        navigation!!.addOffRouteListener(Mockito.mock(OffRouteListener::class.java))
//
//        navigation!!.removeOffRouteListener(null)
//        navigationEventDispatcher!!.onUserOffRoute(location)
//        Mockito.verify(offRouteListener, Mockito.times(0))!!.userOffRoute(location)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addNavigationEventListener_didAddListener() {
//        navigationEventDispatcher!!.onNavigationEvent(true)
//        Mockito.verify(navigationEventListener, Mockito.times(0))!!.onRunning(true)
//
//        navigation!!.addNavigationEventListener(navigationEventListener!!)
//        navigationEventDispatcher!!.onNavigationEvent(true)
//        Mockito.verify(navigationEventListener, Mockito.times(1))!!.onRunning(true)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addNavigationEventListener_onlyAddsListenerOnce() {
//        navigationEventDispatcher!!.onNavigationEvent(true)
//        Mockito.verify(navigationEventListener, Mockito.times(0))!!.onRunning(true)
//
//        navigation!!.addNavigationEventListener(navigationEventListener!!)
//        navigation!!.addNavigationEventListener(navigationEventListener!!)
//        navigation!!.addNavigationEventListener(navigationEventListener!!)
//        navigationEventDispatcher!!.onNavigationEvent(true)
//        Mockito.verify(navigationEventListener, Mockito.times(1))!!.onRunning(true)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeNavigationEventListener_didRemoveListener() {
//        navigation!!.addNavigationEventListener(navigationEventListener!!)
//        navigation!!.removeNavigationEventListener(navigationEventListener)
//        navigationEventDispatcher!!.onNavigationEvent(true)
//        Mockito.verify(navigationEventListener, Mockito.times(0))!!.onRunning(true)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeNavigationEventListener_nullRemovesAllListeners() {
//        navigation!!.addNavigationEventListener(navigationEventListener!!)
//        navigation!!.addNavigationEventListener(
//            Mockito.mock(
//                NavigationEventListener::class.java
//            )
//        )
//        navigation!!.addNavigationEventListener(
//            Mockito.mock(
//                NavigationEventListener::class.java
//            )
//        )
//        navigation!!.addNavigationEventListener(
//            Mockito.mock(
//                NavigationEventListener::class.java
//            )
//        )
//        navigation!!.addNavigationEventListener(
//            Mockito.mock(
//                NavigationEventListener::class.java
//            )
//        )
//
//        navigation!!.removeNavigationEventListener(null)
//        navigationEventDispatcher!!.onNavigationEvent(true)
//        Mockito.verify(navigationEventListener, Mockito.times(0))!!.onRunning(true)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addFasterRouteListener_didAddListener() {
//        navigationEventDispatcher!!.onFasterRouteEvent(route)
//        Mockito.verify(fasterRouteListener, Mockito.times(0))!!.fasterRouteFound(route)
//
//        navigation!!.addFasterRouteListener(fasterRouteListener!!)
//        navigationEventDispatcher!!.onFasterRouteEvent(route)
//        Mockito.verify(fasterRouteListener, Mockito.times(1))!!.fasterRouteFound(route)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addFasterRouteListener_onlyAddsListenerOnce() {
//        navigationEventDispatcher!!.onFasterRouteEvent(route)
//        Mockito.verify(fasterRouteListener, Mockito.times(0))!!.fasterRouteFound(route)
//
//        navigation!!.addFasterRouteListener(fasterRouteListener!!)
//        navigation!!.addFasterRouteListener(fasterRouteListener!!)
//        navigation!!.addFasterRouteListener(fasterRouteListener!!)
//        navigationEventDispatcher!!.onFasterRouteEvent(route)
//        Mockito.verify(fasterRouteListener, Mockito.times(1))!!.fasterRouteFound(route)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeFasterRouteListener_didRemoveListener() {
//        navigation!!.addFasterRouteListener(fasterRouteListener!!)
//        navigation!!.removeFasterRouteListener(fasterRouteListener)
//        navigationEventDispatcher!!.onFasterRouteEvent(route)
//        Mockito.verify(fasterRouteListener, Mockito.times(0))!!.fasterRouteFound(route)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeFasterRouteListener_nullRemovesAllListeners() {
//        navigation!!.addFasterRouteListener(fasterRouteListener!!)
//        navigation!!.addFasterRouteListener(Mockito.mock(FasterRouteListener::class.java))
//        navigation!!.addFasterRouteListener(Mockito.mock(FasterRouteListener::class.java))
//        navigation!!.addFasterRouteListener(Mockito.mock(FasterRouteListener::class.java))
//        navigation!!.addFasterRouteListener(Mockito.mock(FasterRouteListener::class.java))
//
//        navigation!!.removeFasterRouteListener(null)
//        navigationEventDispatcher!!.onFasterRouteEvent(route)
//        Mockito.verify(fasterRouteListener, Mockito.times(0))!!.fasterRouteFound(route)
//    }
//
//    // TODO this test fails, we need to investigate why it fails.
//    @Ignore
//    fun onArrivalDuringLastLeg_offRouteListenerIsRemoved() {
//        val instruction = ""
//        val location = Mockito.mock(Location::class.java)
//        val milestone = Mockito.mock(
//            BannerInstructionMilestone::class.java
//        )
//        val routeUtils = Mockito.mock(RouteUtils::class.java)
//        Mockito.`when`(routeUtils.isArrivalEvent(routeProgress!!, milestone)).thenReturn(true)
//        Mockito.`when`(routeUtils.isLastLeg(routeProgress)).thenReturn(true)
//        val navigationEventDispatcher =
//            buildEventDispatcherHasArrived(instruction, routeUtils, milestone)
//
//        navigationEventDispatcher.onUserOffRoute(location)
//
//        Mockito.verify(offRouteListener, Mockito.times(0))!!.userOffRoute(location)
//    }
//
//    private fun buildEventDispatcherHasArrived(
//        instruction: String, routeUtils: RouteUtils,
//        milestone: Milestone
//    ): NavigationEventDispatcher {
//        val navigationEventDispatcher = NavigationEventDispatcher(routeUtils)
//        navigationEventDispatcher.addOffRouteListener(offRouteListener!!)
//        navigationEventDispatcher.onMilestoneEvent(routeProgress, instruction, milestone)
//        return navigationEventDispatcher
//    }
//
//    companion object {
//        private const val PRECISION_6 = "directions_v5_precision_6.json"
//    }
//}
