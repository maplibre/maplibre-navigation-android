package com.mapbox.services.android.navigation.v5.snap;

import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.services.android.navigation.v5.models.DirectionsAdapterFactory;
import com.mapbox.services.android.navigation.v5.models.DirectionsResponse;
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.BaseTest;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class SnapToRouteTest extends BaseTest {

  private static final String MULTI_LEG_ROUTE_FIXTURE = "directions_two_leg_route.json";

    @Test
    public void getSnappedLocation_returnsProviderNameCorrectly() throws Exception {
        RouteProgress routeProgress = buildDefaultTestRouteProgress();
        Snap snap = new SnapToRoute();
        Location location = new Location("test");

        Location snappedLocation = snap.getSnappedLocation(location, routeProgress);

      assertEquals("test", snappedLocation.getProvider());
    }

  @Test
  public void getSnappedLocation_locationOnStart() throws Exception {
    DirectionsRoute routeProgress = buildMultipleLegRoute();
    Snap snap = new SnapToRoute();
    Location location = new Location("test");
    location.setLatitude(38.7989792);
    location.setLongitude(-77.0638882);
    location.setBearing(20);

    Location snappedLocation = snap.getSnappedLocation(location, buildTestRouteProgress(
        routeProgress,
        100,
        100,
        200,
        0,
        0
    ));

    assertEquals(38.798979, snappedLocation.getLatitude());
    assertEquals(-77.063888, snappedLocation.getLongitude());
  }

  @Test
  public void getSnappedLocation_locationOnStep() throws Exception {
    DirectionsRoute routeProgress = buildMultipleLegRoute();
    Snap snap = new SnapToRoute();
    Location location = new Location("test");
    location.setLatitude(38.7984052);
    location.setLongitude(-77.0629411);
    location.setBearing(20);

    Location snappedLocation = snap.getSnappedLocation(location, buildTestRouteProgress(
        routeProgress,
        50,
        50,
        150,
        2,
        0
    ));

    assertEquals(38.79840909601134, snappedLocation.getLatitude());
    assertEquals(-77.06299551713687, snappedLocation.getLongitude());
  }

  @Test
  public void getSnappedLocation_locationOnEnd() throws Exception {
    DirectionsRoute routeProgress = buildMultipleLegRoute();
    Snap snap = new SnapToRoute();
    Location location = new Location("test");
    location.setLatitude(38.9623092);
    location.setLongitude(-77.0282631);
    location.setBearing(20);

    Location snappedLocation = snap.getSnappedLocation(location, buildTestRouteProgress(
        routeProgress,
        0.8,
        0.8,
        0.8,
        15,
        1
    ));

    assertEquals(38.9623092, snappedLocation.getLatitude());
    assertEquals(-77.0282631, snappedLocation.getLongitude());
  }

  @Test
  public void getSnappedLocation_bearingStart() throws Exception {
    DirectionsRoute routeProgress = buildMultipleLegRoute();
    Snap snap = new SnapToRoute();
    Location location = new Location("test");
    location.setLatitude(38.7989792);
    location.setLongitude(-77.0638882);
    location.setBearing(20);

    Location snappedLocation = snap.getSnappedLocation(location, buildTestRouteProgress(
        routeProgress,
        100,
        100,
        200,
        0,
        0
    ));

    assertEquals(136.2322f, snappedLocation.getBearing());
  }

  @Test
  public void getSnappedLocation_bearingOnStep() throws Exception {
    DirectionsRoute routeProgress = buildMultipleLegRoute();
    Snap snap = new SnapToRoute();
    Location location = new Location("test");
    location.setLatitude(38.79881);
    location.setLongitude(-77.0629411);
    location.setBearing(20);

    Location snappedLocation = snap.getSnappedLocation(location, buildTestRouteProgress(
        routeProgress,
        50,
        50,
        150,
        2,
        0
    ));

    assertEquals(5.0284705f, snappedLocation.getBearing());
  }

  @Test
  public void getSnappedLocation_bearingBeforeNextLeg() throws Exception {
    DirectionsRoute routeProgress = buildMultipleLegRoute();
    Snap snap = new SnapToRoute();
    Location location = new Location("test");
    location.setLatitude(38.8943771);
    location.setLongitude(-77.0782341);
    location.setBearing(20);

    Location snappedLocation = snap.getSnappedLocation(location, buildTestRouteProgress(
        routeProgress,
        0.8,
        0.8,
        200,
        21,
        0
    ));

    assertEquals(358.19876f, snappedLocation.getBearing());
  }

  @Test
  public void getSnappedLocation_bearingEnd() throws Exception {
    DirectionsRoute routeProgress = buildMultipleLegRoute();
    Snap snap = new SnapToRoute();
    Location location = new Location("test");
    location.setLatitude(38.9623091);
    location.setLongitude(-77.0282631);
    location.setBearing(20);

    Location snappedLocation = snap.getSnappedLocation(location, buildTestRouteProgress(
        routeProgress,
        0.8,
        0.8,
        0.8,
        15,
        1
    ));

    assertEquals(0f, snappedLocation.getBearing());
  }

  private DirectionsRoute buildMultipleLegRoute() throws Exception {
    String body = loadJsonFixture(MULTI_LEG_ROUTE_FIXTURE);
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create();
    DirectionsResponse response = gson.fromJson(body, DirectionsResponse.class);
    return response.routes().get(0);
  }
}
