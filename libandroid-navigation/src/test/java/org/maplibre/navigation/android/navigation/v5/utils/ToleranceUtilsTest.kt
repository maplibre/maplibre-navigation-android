package org.maplibre.navigation.android.navigation.v5.utils;

import static org.maplibre.navigation.android.navigation.v5.utils.Constants.PRECISION_6;

import org.maplibre.navigation.android.navigation.v5.BaseTest;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.geojson.utils.PolylineUtils;
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;
import org.maplibre.turf.TurfConstants;
import org.maplibre.turf.TurfMeasurement;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

public class ToleranceUtilsTest extends BaseTest {

  @Test
  public void dynamicRerouteDistanceTolerance_userFarAwayFromIntersection() throws Exception {
    DirectionsRoute route = buildTestDirectionsRoute();
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    List<Point> stepPoints = PolylineUtils.decode(route.geometry(), PRECISION_6);
    Point midPoint = TurfMeasurement.midpoint(stepPoints.get(0), stepPoints.get(1));

    double tolerance = ToleranceUtils.dynamicRerouteDistanceTolerance(midPoint, routeProgress, MapLibreNavigationOptions.builder().build());

    assertEquals(25.0, tolerance, DELTA);
  }


  @Test
  public void dynamicRerouteDistanceTolerance_userCloseToIntersection() throws Exception {
    DirectionsRoute route = buildTestDirectionsRoute();
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    double distanceToIntersection = route.distance() - 39;
    LineString lineString = LineString.fromPolyline(route.geometry(), PRECISION_6);
    Point closePoint = TurfMeasurement.along(lineString, distanceToIntersection, TurfConstants.UNIT_METERS);

    double tolerance = ToleranceUtils.dynamicRerouteDistanceTolerance(closePoint, routeProgress, MapLibreNavigationOptions.builder().build());

    assertEquals(50.0, tolerance, DELTA);
  }

  @Test
  public void dynamicRerouteDistanceTolerance_userJustPastTheIntersection() throws Exception {
    DirectionsRoute route = buildTestDirectionsRoute();
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    double distanceToIntersection = route.distance();
    LineString lineString = LineString.fromPolyline(route.geometry(), PRECISION_6);
    Point closePoint = TurfMeasurement.along(lineString, distanceToIntersection, TurfConstants.UNIT_METERS);

    double tolerance = ToleranceUtils.dynamicRerouteDistanceTolerance(closePoint, routeProgress, MapLibreNavigationOptions.builder().build());

    assertEquals(50.0, tolerance, DELTA);
  }
}