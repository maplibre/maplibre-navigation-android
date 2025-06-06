package org.maplibre.navigation.android.navigation.ui.v5.camera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.maplibre.navigation.core.location.Location;
import org.maplibre.navigation.core.models.DirectionsResponse;
import org.maplibre.navigation.core.models.DirectionsRoute;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.navigation.android.navigation.ui.v5.BaseTest;
import org.maplibre.navigation.core.navigation.camera.RouteInformation;
import org.maplibre.navigation.core.routeprogress.RouteProgress;

import org.junit.Test;
import org.maplibre.navigation.core.utils.Constants;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.maplibre.navigation.android.navigation.ui.v5.GeoJsonExtKt.toJvmPoints;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamicCameraTest extends BaseTest {

  private static final String DIRECTIONS_PRECISION_6 = "directions_v5_precision_6.json";

  @Test
  public void sanity() {
    DynamicCamera cameraEngine = buildDynamicCamera();

    assertNotNull(cameraEngine);
  }

  @Test
  public void onInformationFromRoute_engineCreatesCorrectZoom() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(buildDirectionsRoute(), null, null);

    double zoom = cameraEngine.zoom(routeInformation);

    assertEquals(15d, zoom);
  }

  @Test
  public void onCameraPositionNull_engineReturnsDefaultZoom() throws Exception {
    DynamicCamera theCameraEngine = buildDynamicCamera();
    RouteInformation anyRouteInformation = new RouteInformation(null, buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(1000d));

    double defaultZoom = theCameraEngine.zoom(anyRouteInformation);

    assertEquals(15d, defaultZoom);
  }

  @Test
  public void onCameraPositionZoomGreaterThanMax_engineReturnsMaxCameraZoom() throws Exception {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    CameraPosition cameraPositionWithZoomGreaterThanMax = new CameraPosition.Builder()
      .zoom(20d)
      .build();
    when(mapLibreMap.getCameraForLatLngBounds(any(LatLngBounds.class), any(int[].class))).thenReturn(cameraPositionWithZoomGreaterThanMax);
    DynamicCamera theCameraEngine = new DynamicCamera(mapLibreMap);
    RouteInformation anyRouteInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(1000d));

    double maxCameraZoom = theCameraEngine.zoom(anyRouteInformation);

    assertEquals(16d, maxCameraZoom);
  }

  @Test
  public void onCameraPositionZoomLessThanMin_engineReturnsMinCameraZoom() throws Exception {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    CameraPosition cameraPositionWithZoomLessThanMin = new CameraPosition.Builder()
      .zoom(10d)
      .build();
    when(mapLibreMap.getCameraForLatLngBounds(any(LatLngBounds.class), any(int[].class))).thenReturn(cameraPositionWithZoomLessThanMin);
    DynamicCamera theCameraEngine = new DynamicCamera(mapLibreMap);
    RouteInformation anyRouteInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(1000d));

    double maxCameraZoom = theCameraEngine.zoom(anyRouteInformation);

    assertEquals(12d, maxCameraZoom);
  }

  @Test
  public void onCameraPositionZoomGreaterThanMinAndLessThanMax_engineReturnsCameraPositionZoom() throws Exception {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    CameraPosition cameraPositionWithZoomGreaterThanMinAndLessThanMax = new CameraPosition.Builder()
      .zoom(14d)
      .build();
    when(mapLibreMap.getCameraForLatLngBounds(any(LatLngBounds.class), any(int[].class))).thenReturn(cameraPositionWithZoomGreaterThanMinAndLessThanMax);
    DynamicCamera theCameraEngine = new DynamicCamera(mapLibreMap);
    RouteInformation anyRouteInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(1000d));

    double maxCameraZoom = theCameraEngine.zoom(anyRouteInformation);

    assertEquals(14d, maxCameraZoom);
  }

  @Test
  public void onIsResetting_dynamicCameraReturnsDefault() throws Exception {
    RouteInformation routeInformation = new RouteInformation(buildDirectionsRoute(), null, null);
    DynamicCamera cameraEngine = buildDynamicCamera();
    cameraEngine.forceResetZoomLevel();

    double zoom = cameraEngine.zoom(routeInformation);

    assertEquals(15d, zoom);
  }

  @Test
  public void onInformationFromRoute_engineCreatesCorrectTilt() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(buildDirectionsRoute(), null, null);

    double tilt = cameraEngine.tilt(routeInformation);

    assertEquals(50d, tilt);
  }

  @Test
  public void onHighDistanceRemaining_engineCreatesCorrectTilt() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(1000d));

    double tilt = cameraEngine.tilt(routeInformation);

    assertEquals(60d, tilt);
  }

  @Test
  public void onMediumDistanceRemaining_engineCreatesCorrectTilt() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(200d));

    double tilt = cameraEngine.tilt(routeInformation);

    assertEquals(45d, tilt);
  }

  @Test
  public void onLowDistanceRemaining_engineCreatesCorrectTilt() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(null));

    double tilt = cameraEngine.tilt(routeInformation);

    assertEquals(45d, tilt);
  }

  @Test
  public void onInformationFromRoute_engineCreatesOverviewPointList() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    DirectionsRoute route = buildDirectionsRoute();
    List<Point> routePoints = generateRouteCoordinates(route);
    RouteInformation routeInformation = new RouteInformation(route, null, null);

    List<Point> overviewPoints = toJvmPoints(cameraEngine.overview(routeInformation));

    assertEquals(routePoints, overviewPoints);
  }

  @Test
  public void onInformationFromRouteProgress_engineCreatesOverviewPointList() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteProgress routeProgress = buildDefaultRouteProgress(null);
    List<Point> routePoints = buildRouteCoordinatesFrom(routeProgress);
    RouteInformation routeInformation = new RouteInformation(null, null, routeProgress);

    List<Point> overviewPoints = toJvmPoints(cameraEngine.overview(routeInformation));

    assertEquals(routePoints, overviewPoints);
  }

  @Test
  public void noRouteInformation_engineCreatesEmptyOverviewPointList() {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(null, null, null);

    List<Point> overviewPoints = toJvmPoints(cameraEngine.overview(routeInformation));

    assertTrue(overviewPoints.isEmpty());
  }

  @Nullable
  private List<Point> buildRouteCoordinatesFrom(RouteProgress routeProgress) {
    DirectionsRoute route = routeProgress.getDirectionsRoute();
    return generateRouteCoordinates(route);
  }

  @NonNull
  private DynamicCamera buildDynamicCamera() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    return new DynamicCamera(mapLibreMap);
  }

  private Location buildDefaultLocationUpdate(double lng, double lat) {
    return buildLocationUpdate(lng, lat, System.currentTimeMillis());
  }

  private Location buildLocationUpdate(double lng, double lat, long time) {
    Location location = mock(Location.class);
    when(location.getLongitude()).thenReturn(lng);
    when(location.getLatitude()).thenReturn(lat);
    when(location.getSpeedMetersPerSeconds()).thenReturn(30f);
    when(location.getBearing()).thenReturn(100f);
    when(location.getAccuracyMeters()).thenReturn(10f);
    when(location.getTimeMilliseconds()).thenReturn(time);
    return location;
  }

  private RouteProgress buildDefaultRouteProgress(@Nullable Double stepDistanceRemaining) throws Exception {
    DirectionsRoute aRoute = buildDirectionsRoute();
    double stepDistanceRemainingFinal = stepDistanceRemaining == null ? 100 : stepDistanceRemaining;
    return buildRouteProgress(aRoute, stepDistanceRemainingFinal, 0, 0, 0, 0);
  }

  private DirectionsRoute buildDirectionsRoute() throws IOException {
    String body = loadJsonFixture(DIRECTIONS_PRECISION_6);
    DirectionsResponse response = DirectionsResponse.fromJson(body);
    return response.getRoutes().get(0);
  }

  private List<Point> generateRouteCoordinates(DirectionsRoute route) {
    if (route == null) {
      return Collections.emptyList();
    }
    LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.PRECISION_6);
    return lineString.coordinates();
  }
}
