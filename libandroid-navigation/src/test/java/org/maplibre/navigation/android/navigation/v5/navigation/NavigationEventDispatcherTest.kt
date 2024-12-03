package org.maplibre.navigation.android.navigation.v5.navigation

import android.location.Location
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone
import org.maplibre.navigation.android.navigation.v5.milestone.MilestoneEventListener
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.offroute.OffRouteListener
import org.maplibre.navigation.android.navigation.v5.route.FasterRouteListener
import org.maplibre.navigation.android.navigation.v5.routeprogress.ProgressChangeListener
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NavigationEventDispatcherTest : BaseTest() {
    private var milestoneEventListener: MilestoneEventListener = mockk(relaxed = true)
    private var progressChangeListener: ProgressChangeListener = mockk(relaxed = true)
    private var offRouteListener: OffRouteListener = mockk(relaxed = true)
    private var navigationEventListener: NavigationEventListener = mockk(relaxed = true)
    private var fasterRouteListener: FasterRouteListener = mockk(relaxed = true)
    private var location: Location? = mockk(relaxed = true)
    private var milestone: Milestone? = mockk(relaxed = true)
    private var navigationEventDispatcher: NavigationEventDispatcher? = null
    private var navigation: MapLibreNavigation? = null
    private var route: DirectionsRoute? = null
    private var routeProgress: RouteProgress? = null

    @Before
    @Throws(Exception::class)
    fun setup() {
//        val context = mockk<Context>(relaxed = true) {
//            every { applicationContext } returns this
//        }
        navigation = MapLibreNavigation(ApplicationProvider.getApplicationContext(), mockk(relaxed = true))
        navigationEventDispatcher = navigation!!.eventDispatcher

        val body = loadJsonFixture(PRECISION_6)
        val response = DirectionsResponse.fromJson(body)
        route = response.routes[0]

        routeProgress = buildTestRouteProgress(route!!, 100.0, 100.0, 100.0, 0, 0)
    }

    @Test
    @Throws(Exception::class)
    fun sanity() {
        val navigationEventDispatcher = NavigationEventDispatcher()
        Assert.assertNotNull(navigationEventDispatcher)
    }

    @Test
    @Throws(Exception::class)
    fun addMilestoneEventListener_didAddListener() {
        navigationEventDispatcher!!.onMilestoneEvent(routeProgress!!, "", milestone!!)

        verify(exactly = 0) {
            milestoneEventListener.onMilestoneEvent(routeProgress!!, "", milestone!!)
        }

        navigation!!.addMilestoneEventListener(milestoneEventListener)
        navigationEventDispatcher!!.onMilestoneEvent(routeProgress!!, "", milestone!!)

        verify {
            milestoneEventListener.onMilestoneEvent(routeProgress!!, "", milestone!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun addMilestoneEventListener_onlyAddsListenerOnce() {
        navigationEventDispatcher!!.onMilestoneEvent(routeProgress!!, "", milestone!!)

        verify(exactly = 0) {
            milestoneEventListener.onMilestoneEvent(routeProgress!!, "", milestone!!)
        }

        navigation!!.addMilestoneEventListener(milestoneEventListener)
        navigation!!.addMilestoneEventListener(milestoneEventListener)
        navigation!!.addMilestoneEventListener(milestoneEventListener)
        navigationEventDispatcher!!.onMilestoneEvent(routeProgress!!, "", milestone!!)

        verify {
            milestoneEventListener.onMilestoneEvent(routeProgress!!, "", milestone!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestoneEventListener_didRemoveListener() {
        navigation!!.addMilestoneEventListener(milestoneEventListener)
        navigation!!.removeMilestoneEventListener(milestoneEventListener)
        navigationEventDispatcher!!.onMilestoneEvent(routeProgress!!, "", milestone!!)

        verify(exactly = 0) {
            milestoneEventListener.onMilestoneEvent(routeProgress!!, "", milestone!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestoneEventListener_nullRemovesAllListeners() {
        navigation!!.addMilestoneEventListener(milestoneEventListener)
        navigation!!.addMilestoneEventListener(mockk(relaxed = true))
        navigation!!.addMilestoneEventListener(mockk(relaxed = true))
        navigation!!.addMilestoneEventListener(mockk(relaxed = true))

        navigation!!.removeMilestoneEventListener(null)
        navigationEventDispatcher!!.onMilestoneEvent(routeProgress!!, "", milestone!!)

        verify(exactly = 0) {
            milestoneEventListener.onMilestoneEvent(routeProgress!!, "", milestone!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun addProgressChangeListener_didAddListener() {
        navigationEventDispatcher!!.onProgressChange(location!!, routeProgress!!)

        verify(exactly = 0) {
            progressChangeListener.onProgressChange(location!!, routeProgress!!)
        }

        navigation!!.addProgressChangeListener(progressChangeListener)
        navigationEventDispatcher!!.onProgressChange(location!!, routeProgress!!)

        verify {
            progressChangeListener.onProgressChange(location!!, routeProgress!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun addProgressChangeListener_onlyAddsListenerOnce() {
        navigationEventDispatcher!!.onProgressChange(location!!, routeProgress!!)

        verify(exactly = 0) {
            progressChangeListener.onProgressChange(location!!, routeProgress!!)
        }


        navigation!!.addProgressChangeListener(progressChangeListener)
        navigation!!.addProgressChangeListener(progressChangeListener)
        navigation!!.addProgressChangeListener(progressChangeListener)
        navigationEventDispatcher!!.onProgressChange(location!!, routeProgress!!)

        verify {
            progressChangeListener.onProgressChange(location!!, routeProgress!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun removeProgressChangeListener_didRemoveListener() {
        navigation!!.addProgressChangeListener(progressChangeListener)
        navigation!!.removeProgressChangeListener(progressChangeListener)
        navigationEventDispatcher!!.onProgressChange(location!!, routeProgress!!)

        verify(exactly = 0) {
            progressChangeListener.onProgressChange(location!!, routeProgress!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun removeProgressChangeListener_nullRemovesAllListeners() {
        navigation!!.addProgressChangeListener(progressChangeListener)
        navigation!!.addProgressChangeListener(mockk(relaxed = true))
        navigation!!.addProgressChangeListener(mockk(relaxed = true))
        navigation!!.addProgressChangeListener(mockk(relaxed = true))


        navigation!!.removeProgressChangeListener(null)
        navigationEventDispatcher!!.onProgressChange(location!!, routeProgress!!)

        verify(exactly = 0) {
            progressChangeListener.onProgressChange(location!!, routeProgress!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun addOffRouteListener_didAddListener() {
        navigationEventDispatcher!!.onUserOffRoute(location!!)

        verify(exactly = 0) {
            offRouteListener.userOffRoute(location!!)
        }

        navigation!!.addOffRouteListener(offRouteListener)
        navigationEventDispatcher!!.onUserOffRoute(location!!)

        verify {
            offRouteListener.userOffRoute(location!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun addOffRouteListener_onlyAddsListenerOnce() {
        navigationEventDispatcher!!.onUserOffRoute(location!!)

        verify(exactly = 0) {
            offRouteListener.userOffRoute(location!!)
        }

        navigation!!.addOffRouteListener(offRouteListener)
        navigation!!.addOffRouteListener(offRouteListener)
        navigation!!.addOffRouteListener(offRouteListener)
        navigationEventDispatcher!!.onUserOffRoute(location!!)

        verify {
            offRouteListener.userOffRoute(location!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun removeOffRouteListener_didRemoveListener() {
        navigation!!.addOffRouteListener(offRouteListener)
        navigation!!.removeOffRouteListener(offRouteListener)
        navigationEventDispatcher!!.onUserOffRoute(location!!)

        verify(exactly = 0) {
            offRouteListener.userOffRoute(location!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun removeOffRouteListener_nullRemovesAllListeners() {
        navigation!!.addOffRouteListener(offRouteListener)
        navigation!!.addOffRouteListener(mockk(relaxed = true))
        navigation!!.addOffRouteListener(mockk(relaxed = true))
        navigation!!.addOffRouteListener(mockk(relaxed = true))
        navigation!!.addOffRouteListener(mockk(relaxed = true))

        navigation!!.removeOffRouteListener(null)
        navigationEventDispatcher!!.onUserOffRoute(location!!)

        verify(exactly = 0) {
            offRouteListener.userOffRoute(location!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun addNavigationEventListener_didAddListener() {
        navigationEventDispatcher!!.onNavigationEvent(true)

        verify(exactly = 0) {
            navigationEventListener.onRunning(true)
        }

        navigation!!.addNavigationEventListener(navigationEventListener)
        navigationEventDispatcher!!.onNavigationEvent(true)

        verify {
            navigationEventListener.onRunning(true)
        }
    }

    @Test
    @Throws(Exception::class)
    fun addNavigationEventListener_onlyAddsListenerOnce() {
        navigationEventDispatcher!!.onNavigationEvent(true)

        verify(exactly = 0) {
            navigationEventListener.onRunning(true)
        }

        navigation!!.addNavigationEventListener(navigationEventListener)
        navigation!!.addNavigationEventListener(navigationEventListener)
        navigation!!.addNavigationEventListener(navigationEventListener)
        navigationEventDispatcher!!.onNavigationEvent(true)

        verify {
            navigationEventListener.onRunning(true)
        }
    }

    @Test
    @Throws(Exception::class)
    fun removeNavigationEventListener_didRemoveListener() {
        navigation!!.addNavigationEventListener(navigationEventListener)
        navigation!!.removeNavigationEventListener(navigationEventListener)
        navigationEventDispatcher!!.onNavigationEvent(true)

        verify(exactly = 0) {
            navigationEventListener.onRunning(true)
            }
    }

    @Test
    @Throws(Exception::class)
    fun removeNavigationEventListener_nullRemovesAllListeners() {
        navigation!!.addNavigationEventListener(navigationEventListener)
        navigation!!.addNavigationEventListener(mockk(relaxed = true))
        navigation!!.addNavigationEventListener(mockk(relaxed = true))
        navigation!!.addNavigationEventListener(mockk(relaxed = true))
        navigation!!.addNavigationEventListener(mockk(relaxed = true))

        navigation!!.removeNavigationEventListener(null)
        navigationEventDispatcher!!.onNavigationEvent(true)

        verify(exactly = 0) {
            navigationEventListener.onRunning(true)
        }
    }

    @Test
    @Throws(Exception::class)
    fun addFasterRouteListener_didAddListener() {
        navigationEventDispatcher!!.onFasterRouteEvent(route)

        verify(exactly = 0) {
            fasterRouteListener.fasterRouteFound(route)
        }

        navigation!!.addFasterRouteListener(fasterRouteListener)
        navigationEventDispatcher!!.onFasterRouteEvent(route)

        verify {
            fasterRouteListener.fasterRouteFound(route)
        }
    }

    @Test
    @Throws(Exception::class)
    fun addFasterRouteListener_onlyAddsListenerOnce() {
        navigationEventDispatcher!!.onFasterRouteEvent(route)

        verify(exactly = 0) {
            fasterRouteListener.fasterRouteFound(route)
        }

        navigation!!.addFasterRouteListener(fasterRouteListener)
        navigation!!.addFasterRouteListener(fasterRouteListener)
        navigation!!.addFasterRouteListener(fasterRouteListener)
        navigationEventDispatcher!!.onFasterRouteEvent(route)

        verify {
            fasterRouteListener.fasterRouteFound(route)
        }
    }

    @Test
    @Throws(Exception::class)
    fun removeFasterRouteListener_didRemoveListener() {
        navigation!!.addFasterRouteListener(fasterRouteListener)
        navigation!!.removeFasterRouteListener(fasterRouteListener)
        navigationEventDispatcher!!.onFasterRouteEvent(route)

        verify(exactly = 0) {
            fasterRouteListener.fasterRouteFound(route)
        }
    }

    @Test
    @Throws(Exception::class)
    fun removeFasterRouteListener_nullRemovesAllListeners() {
        navigation!!.addFasterRouteListener(fasterRouteListener)
        navigation!!.addFasterRouteListener(mockk(relaxed = true))
        navigation!!.addFasterRouteListener(mockk(relaxed = true))
        navigation!!.addFasterRouteListener(mockk(relaxed = true))
        navigation!!.addFasterRouteListener(mockk(relaxed = true))

        navigation!!.removeFasterRouteListener(null)
        navigationEventDispatcher!!.onFasterRouteEvent(route)

        verify(exactly = 0) {
            fasterRouteListener.fasterRouteFound(route)
        }
    }

//    // TODO this test fails, we need to investigate why it fails.
//    @Ignore
//    fun onArrivalDuringLastLeg_offRouteListenerIsRemoved() {
//        val instruction = ""
//        val location = mockk<Location>()
//        val milestone = mockk<BannerInstructionMilestone>()
////        Mockito.`when`(routeUtils.isArrivalEvent(routeProgress!!, milestone)).thenReturn(true)
////        Mockito.`when`(routeUtils.isLastLeg(routeProgress)).thenReturn(true)
////        val navigationEventDispatcher =
////            buildEventDispatcherHasArrived(instruction, routeUtils, milestone)
//
//        navigationEventDispatcher!!.onUserOffRoute(location)
//
////        Mockito.verify(offRouteListener, Mockito.times(0))!!.userOffRoute(location)
//    }

    private fun buildEventDispatcherHasArrived(
        instruction: String,
        milestone: Milestone
    ): NavigationEventDispatcher {
        val navigationEventDispatcher = NavigationEventDispatcher()
        navigationEventDispatcher.addOffRouteListener(offRouteListener)
        navigationEventDispatcher.onMilestoneEvent(routeProgress!!, instruction, milestone)
        return navigationEventDispatcher
    }

    companion object {
        private const val PRECISION_6 = "directions_v5_precision_6.json"
    }
}
