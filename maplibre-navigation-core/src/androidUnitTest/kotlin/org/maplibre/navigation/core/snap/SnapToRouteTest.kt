package org.maplibre.navigation.core.snap

import org.maplibre.navigation.core.json
import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.models.DirectionsResponse
import org.maplibre.navigation.core.models.DirectionsRoute
import kotlin.test.Test
import kotlin.test.assertEquals

class SnapToRouteTest : BaseTest() {

    @Test
    fun snappedLocation_returnsProviderNameCorrectly() {
        val routeProgress = buildDefaultTestRouteProgress()
        val snap: Snap = SnapToRoute()
        val location = Location(
            provider = "test",
            latitude = 0.0,
            longitude = 0.0,
        )

        val snappedLocation =
            snap.getSnappedLocation(location, routeProgress)

        assertEquals("test", snappedLocation.provider)
    }

    @Test
    fun snappedLocation_locationOnStart() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location(
            provider = "test",
            latitude = 38.7989792,
            longitude = -77.0638882,
            bearing = 20f
        )

        val snappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                100.0,
                100.0,
                200.0,
                0,
                0
            )
        )

        assertEquals(38.798979, snappedLocation.latitude, DELTA)
        assertEquals(-77.063888, snappedLocation.longitude, DELTA)
    }

    @Test
    fun snappedLocation_locationOnStep() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location(
            provider = "test",
            latitude = 38.7984052,
            longitude = -77.0629411,
            bearing = 20f
        )

        val snappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                50.0,
                50.0,
                150.0,
                2,
                0
            )
        )

        assertEquals(38.79840909601134, snappedLocation.latitude, DELTA)
        assertEquals(-77.06299551713687, snappedLocation.longitude, DELTA)
    }

    @Test
    fun snappedLocation_locationOnEnd() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location(
            provider = "test",
            latitude = 38.9623092,
            longitude = -77.0282631,
            bearing = 20f
        )

        val snappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                0.8,
                0.8,
                0.8,
                15,
                1
            )
        )

        assertEquals(38.9623092, snappedLocation.latitude, DELTA)
        assertEquals(-77.0282631, snappedLocation.longitude, DELTA)
    }

    @Test
    fun snappedLocation_bearingStart() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location(
            provider = "test",
            latitude = 38.7989792,
            longitude = -77.0638882,
            bearing = 20f
        )

        val snappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                100.0,
                100.0,
                200.0,
                0,
                0
            )
        )

        assertEquals(136.2322f, snappedLocation.bearing)
    }

    @Test
    fun snappedLocation_bearingOnStep() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location(
            provider = "test",
            latitude = 38.79881,
            longitude = -77.0629411,
            bearing = 20f
        )

        val snappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                50.0,
                50.0,
                150.0,
                2,
                0
            )
        )

        assertEquals(5.0284705f, snappedLocation.bearing)
    }

    @Test
    fun snappedLocation_bearingBeforeNextLeg() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location(
            provider = "test",
            latitude = 38.8943771,
            longitude = -77.0782341,
            bearing = 20f
        )

        val snappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                0.8,
                0.8,
                200.0,
                21,
                0
            )
        )

        assertEquals(358.19876f, snappedLocation.bearing)
    }

    @Test
    fun snappedLocation_bearingWithSingleStepLegBeforeNextLeg() {
        val routeProgress =
            buildMultipleLegRoute(SINGLE_STEP_LEG)
        val snap: Snap = SnapToRoute()
        val location = Location(
            provider = "test",
            latitude = 38.8943771,
            longitude = -77.0782341,
            bearing = 20f
        )

        val previousSnappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                0.8,
                0.8,
                200.0,
                20,
                0
            )
        )

        val snappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                0.8,
                0.8,
                200.0,
                21,
                0
            )
        )

        // Latest snapped bearing should be used, because next lef is not containing enough steps
        assertEquals(
            previousSnappedLocation.bearing,
            snappedLocation.bearing
        )
    }

    @Test
    fun snappedLocation_bearingNoBearingBeforeWithSingleStepLegBeforeNextLeg() {
        val routeProgress =
            buildMultipleLegRoute(SINGLE_STEP_LEG)
        val snap: Snap = SnapToRoute()
        val location = Location(
            provider = "test",
            latitude = 38.8943771,
            longitude = -77.0782341,
            bearing = 20f
        )

        val snappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                0.8,
                0.8,
                200.0,
                21,
                0
            )
        )

        // Fallback to location bearing if no previous bearing was calculated.
        assertEquals(location.bearing, snappedLocation.bearing)
    }

    @Test
    fun snappedLocation_bearingEnd() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location(
            provider = "test",
            latitude = 38.9623092,
            longitude = -77.0282631,
            bearing = 20f
        )

        val lastSnappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                0.6,
                0.6,
                0.6,
                14,
                1
            )
        )

        val snappedLocation = snap.getSnappedLocation(
            location, buildTestRouteProgress(
                routeProgress,
                0.8,
                0.8,
                0.8,
                15,
                1
            )
        )

        // Latest snapped bearing should be used, because no future steps are available
        assertEquals(
            lastSnappedLocation.bearing,
            snappedLocation.bearing
        )
    }

    @Throws(Exception::class)
    private fun buildMultipleLegRoute(file: String = MULTI_LEG_ROUTE_FIXTURE): DirectionsRoute {
        val fixtureJsonString = loadJsonFixture(file)
        val response = json.decodeFromString<DirectionsResponse>(fixtureJsonString)
        return response.routes[0]
    }

    companion object {
        private const val MULTI_LEG_ROUTE_FIXTURE = "directions_two_leg_route.json"
        private const val SINGLE_STEP_LEG = "directions_three_leg_single_step_route.json"
    }
}
