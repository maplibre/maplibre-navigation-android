package org.maplibre.navigation.android.navigation.v5.snap

import android.location.Location
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.navigation.android.json
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SnapToRouteTest : BaseTest() {

    @Test
    fun snappedLocation_returnsProviderNameCorrectly() {
        val routeProgress = buildDefaultTestRouteProgress()
        val snap: Snap = SnapToRoute()
        val location = Location("test")

        val snappedLocation =
            snap.getSnappedLocation(location, routeProgress)

        Assert.assertEquals("test", snappedLocation.provider)
    }

    @Test
    fun snappedLocation_locationOnStart() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location("test")
        location.latitude = 38.7989792
        location.longitude = -77.0638882
        location.bearing = 20f

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

        Assert.assertEquals(38.798979, snappedLocation.latitude, DELTA)
        Assert.assertEquals(-77.063888, snappedLocation.longitude, DELTA)
    }

    @Test
    fun snappedLocation_locationOnStep() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location("test")
        location.latitude = 38.7984052
        location.longitude = -77.0629411
        location.bearing = 20f

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

        Assert.assertEquals(38.79840909601134, snappedLocation.latitude, DELTA)
        Assert.assertEquals(-77.06299551713687, snappedLocation.longitude, DELTA)
    }

    @Test
    fun snappedLocation_locationOnEnd() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location("test")
        location.latitude = 38.9623092
        location.longitude = -77.0282631
        location.bearing = 20f

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

        Assert.assertEquals(38.9623092, snappedLocation.latitude, DELTA)
        Assert.assertEquals(-77.0282631, snappedLocation.longitude, DELTA)
    }

    @Test
    fun snappedLocation_bearingStart() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location("test")
        location.latitude = 38.7989792
        location.longitude = -77.0638882
        location.bearing = 20f

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

        Assert.assertEquals(136.2322f, snappedLocation.bearing)
    }

    @Test
    fun snappedLocation_bearingOnStep() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location("test")
        location.latitude = 38.79881
        location.longitude = -77.0629411
        location.bearing = 20f

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

        Assert.assertEquals(5.0284705f, snappedLocation.bearing)
    }

    @Test
    fun snappedLocation_bearingBeforeNextLeg() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location("test")
        location.latitude = 38.8943771
        location.longitude = -77.0782341
        location.bearing = 20f

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

        Assert.assertEquals(358.19876f, snappedLocation.bearing)
    }

    @Test
    fun snappedLocation_bearingWithSingleStepLegBeforeNextLeg() {
        val routeProgress =
            buildMultipleLegRoute(SINGLE_STEP_LEG)
        val snap: Snap = SnapToRoute()
        val location = Location("test")
        location.latitude = 38.8943771
        location.longitude = -77.0782341
        location.bearing = 20f

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
        Assert.assertEquals(
            previousSnappedLocation.bearing,
            snappedLocation.bearing
        )
    }

    @Test
    fun snappedLocation_bearingNoBearingBeforeWithSingleStepLegBeforeNextLeg() {
        val routeProgress =
            buildMultipleLegRoute(SINGLE_STEP_LEG)
        val snap: Snap = SnapToRoute()
        val location = Location("test")
        location.latitude = 38.8943771
        location.longitude = -77.0782341
        location.bearing = 20f

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
        Assert.assertEquals(location.bearing, snappedLocation.bearing)
    }

    @Test
    fun snappedLocation_bearingEnd() {
        val routeProgress =
            buildMultipleLegRoute()
        val snap: Snap = SnapToRoute()
        val location = Location("test")
        location.latitude = 38.9623091
        location.longitude = -77.0282631
        location.bearing = 20f

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
        Assert.assertEquals(
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
