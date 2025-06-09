package org.maplibre.navigation.core.navigation.engine

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.LocationValidator
import org.maplibre.navigation.core.location.engine.LocationEngine
import org.maplibre.navigation.core.navigation.MapLibreNavigation
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.core.routeprogress.ProgressChangeListener
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.utils.RouteUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class MapLibreNavigationEngineTest : BaseTest() {
    private val dispatcher = StandardTestDispatcher()
    private val testScope  = TestScope(dispatcher)
    private lateinit var mockLocationEngine: LocationEngine
    private lateinit var mockRouteUtils: RouteUtils
    private lateinit var mockLocationValidator: LocationValidator
    private lateinit var mapLibreNavigation: MapLibreNavigation
    private lateinit var navigationEngine: MapLibreNavigationEngine

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        
        mockLocationEngine = mockk(relaxed = true)
        mockRouteUtils = mockk(relaxed = true)
        mockLocationValidator = mockk(relaxed = true)
        
        val options = MapLibreNavigationOptions()
        mapLibreNavigation = MapLibreNavigation(options = options, locationEngine = mockLocationEngine)
        navigationEngine = MapLibreNavigationEngine(
            mapLibreNavigation = mapLibreNavigation,
            routeUtils = mockRouteUtils,
            locationValidator = mockLocationValidator,
            backgroundScope = testScope
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        testScope.cancel()
    }

    @Test
    fun sanity() {
        assertNotNull(navigationEngine)
    }

    @Test
    @Throws(Exception::class)
    fun triggerManualRouteUpdate_withValidLocation_callsSetIndexDirectly() = testScope.runTest {
        // Setup
        val mockLocation = buildDefaultLocationUpdate(-77.034043, 38.900205)
        
        coEvery { mockLocationEngine.getLastLocation() } returns mockLocation
        every { mockLocationValidator.isValidUpdate(any()) } returns true

        var resultProgress: RouteProgress? = null
        mapLibreNavigation.addProgressChangeListener(object : ProgressChangeListener {
            override fun onProgressChange(location: Location, routeProgress: RouteProgress) {
                print("Test: onProgressChange: ${routeProgress.legIndex}, ${routeProgress.stepIndex}")
                resultProgress = routeProgress
            }
        })

        mapLibreNavigation.startNavigation(buildTestDirectionsRoute())
        testScheduler.advanceUntilIdle()
        
        // Execute - trigger manual route update to leg 0, step 5
        navigationEngine.triggerManualRouteUpdate(0, 5)
        testScheduler.advanceUntilIdle()
        
        // Assert
        assertEquals(5,resultProgress?.stepIndex)
        assertEquals(0,resultProgress?.legIndex)
    }
}