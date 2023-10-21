package com.mapbox.services.android.navigation.v5.navigation;

import android.content.Context;
import android.location.Location;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.location.engine.LocationEngine;
import com.mapbox.services.android.navigation.v5.BaseTest;
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.services.android.navigation.v5.utils.Constants;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.mapbox.services.android.navigation.v5.navigation.NavigationHelper.buildSnappedLocation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NavigationRouteProcessorTest extends BaseTest {

  private NavigationRouteProcessor routeProcessor;
  private MapboxNavigation navigation;

  @Before
  public void before() throws Exception {
    routeProcessor = new NavigationRouteProcessor();
    MapboxNavigationOptions options = MapboxNavigationOptions.builder().build();
    Context context = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(context);
    navigation = new MapboxNavigation(context, options, mock(LocationEngine.class));
    navigation.startNavigation(buildTestDirectionsRoute());
  }

  @Test
  public void sanity() throws Exception {
    assertNotNull(routeProcessor);
  }

  @Test
  public void onFirstRouteProgressBuilt_newRouteIsDecoded() throws Exception {
    RouteProgress progress = routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));
    assertEquals(0, progress.legIndex());
    assertEquals(0, progress.currentLegProgress().stepIndex());
  }

  @Test
  public void onShouldIncreaseStepIndex_indexIsIncreased() throws Exception {
    RouteProgress progress = routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));
    int currentStepIndex = progress.currentLegProgress().stepIndex();
    routeProcessor.onShouldIncreaseIndex();
    routeProcessor.checkIncreaseIndex(navigation);

    RouteProgress secondProgress = routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));
    int secondStepIndex = secondProgress.currentLegProgress().stepIndex();

    assertTrue(currentStepIndex != secondStepIndex);
  }

  @Test
  public void onSnapToRouteEnabledAndUserOnRoute_snappedLocationReturns() throws Exception {
    RouteProgress progress = routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));
    boolean snapEnabled = true;
    boolean userOffRoute = false;
    List<Point> coordinates = createCoordinatesFromCurrentStep(progress);
    Point lastPointInCurrentStep = coordinates.remove(coordinates.size() - 1);
    Location rawLocation = buildDefaultLocationUpdate(
      lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
    );

    Location snappedLocation = buildSnappedLocation(
      navigation, snapEnabled, rawLocation, progress, userOffRoute
    );

    assertTrue(!rawLocation.equals(snappedLocation));
  }

  @Test
  public void onSnapToRouteDisabledAndUserOnRoute_rawLocationReturns() throws Exception {
    RouteProgress progress = routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));
    boolean snapEnabled = false;
    boolean userOffRoute = false;
    List<Point> coordinates = createCoordinatesFromCurrentStep(progress);
    Point lastPointInCurrentStep = coordinates.remove(coordinates.size() - 1);
    Location rawLocation = buildDefaultLocationUpdate(
      lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
    );

    Location snappedLocation = buildSnappedLocation(
      navigation, snapEnabled, rawLocation, progress, userOffRoute
    );

    assertTrue(rawLocation.equals(snappedLocation));
  }

  @Test
  public void onSnapToRouteEnabledAndUserOffRoute_rawLocationReturns() throws Exception {
    RouteProgress progress = routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));
    boolean snapEnabled = false;
    boolean userOffRoute = false;
    List<Point> coordinates = createCoordinatesFromCurrentStep(progress);
    Point lastPointInCurrentStep = coordinates.remove(coordinates.size() - 1);
    Location rawLocation = buildDefaultLocationUpdate(
      lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
    );

    Location snappedLocation = buildSnappedLocation(
      navigation, snapEnabled, rawLocation, progress, userOffRoute
    );

    assertTrue(rawLocation.equals(snappedLocation));
  }

  @Test
  public void onStepDistanceRemainingZeroAndNoBearingMatch_stepIndexForceIncreased() throws Exception {
    RouteProgress firstProgress = routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));
    int firstProgressIndex = firstProgress.currentLegProgress().stepIndex();
    List<Point> coordinates = createCoordinatesFromCurrentStep(firstProgress);
    Point lastPointInCurrentStep = coordinates.remove(coordinates.size() - 1);
    Location rawLocation = buildDefaultLocationUpdate(
      lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
    );

    RouteProgress secondProgress = routeProcessor.buildNewRouteProgress(navigation, rawLocation);
    int secondProgressIndex = secondProgress.currentLegProgress().stepIndex();

    assertTrue(firstProgressIndex != secondProgressIndex);
  }

  @Test
  public void onInvalidNextLeg_indexIsNotIncreased() throws Exception {
    routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));
    int legSize = navigation.getRoute().legs().size();

    for (int i = 0; i < legSize; i++) {
      routeProcessor.onShouldIncreaseIndex();
      routeProcessor.checkIncreaseIndex(navigation);
    }
    RouteProgress progress = routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));

    assertTrue(progress.legIndex() == legSize - 1);
  }

  @Test
  public void onInvalidNextStep_indexIsNotIncreased() throws Exception {
    routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));
    int stepSize = navigation.getRoute().legs().get(0).steps().size();

    for (int i = 0; i < stepSize; i++) {
      routeProcessor.onShouldIncreaseIndex();
      routeProcessor.checkIncreaseIndex(navigation);
    }
    RouteProgress progress = routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));

    assertTrue(progress.currentLegProgress().stepIndex() == stepSize - 1);
  }

  @Test
  public void onNewRoute_testStepProgressSetCorrectly() throws IOException {
    navigation.startNavigation(buildTestDirectionsRoute("directions_distance_congestion_annotation.json"));
    Location firstOnRoute = buildDefaultLocationUpdate(-77.034043, 38.900205);
    RouteProgress progress = routeProcessor.buildNewRouteProgress(navigation, firstOnRoute);
    assertEquals(35.1, progress.currentLegProgress().currentStepProgress().distanceRemaining(), 1);
    // If the step progress is calculated correctly, we must be on the first step of the route (index = 0)
    assertEquals(0, progress.currentLegProgress().currentLegAnnotation().index());

    Location otherOnRoute = buildDefaultLocationUpdate(-77.033638, 38.900207);
    RouteProgress progress2 = routeProcessor.buildNewRouteProgress(navigation, otherOnRoute);
    assertEquals(2, progress2.currentLegProgress().currentLegAnnotation().index());

    // Creating a new route should trigger a new routeProgress to be built. Annotation must be reset to 0 for same location
    DirectionsRoute testRoute2 = buildTestDirectionsRoute("directions_distance_congestion_annotation.json");
    List<Point> decoded = PolylineUtils.decode(testRoute2.geometry(), Constants.PRECISION_6);
    decoded.remove(0);
    String alteredGeometry = PolylineUtils.encode(decoded, Constants.PRECISION_6);
    navigation.startNavigation(testRoute2.toBuilder().geometry(alteredGeometry).build());

    RouteProgress progress3 = routeProcessor.buildNewRouteProgress(navigation, firstOnRoute);
    assertEquals(0, progress3.currentLegProgress().currentLegAnnotation().index());
  }

  @Test
  public void onAdvanceIndices_testAnnotationsSetCorrectly() throws IOException {
    navigation.startNavigation(buildTestDirectionsRoute("directions_two_leg_route_with_distances.json"));

    RouteProgress progress0 = routeProcessor.buildNewRouteProgress(navigation,
            buildDefaultLocationUpdate(-74.220588, 40.745062));
    assertEquals(0, progress0.currentLegProgress().stepIndex());
    // Location right before the via point, third annotation
    Location location1 = buildDefaultLocationUpdate(-74.219569,40.745062);
    RouteProgress progress = routeProcessor.buildNewRouteProgress(navigation, location1);
    assertEquals(1, progress.currentLegProgress().stepIndex());
    assertEquals(0, progress.legIndex());
    assertEquals(2, progress.currentLegProgress().currentLegAnnotation().index());

    // Location shortly after the via point, must have a lower annotation index!
    Location location2 = buildDefaultLocationUpdate(-74.219444,40.745065);
    RouteProgress progress2 = routeProcessor.buildNewRouteProgress(navigation, location2);
    assertEquals(1, progress2.legIndex());
    assertEquals(0, progress2.currentLegProgress().currentLegAnnotation().index());
    // This must mean that the annotation was reset correctly in the meantime
  }

  @Test
  public void withinManeuverRadiusAndBearingMatches_stepIndexIsIncreased() throws Exception {
    RouteProgress firstProgress = routeProcessor.buildNewRouteProgress(navigation, mock(Location.class));
    int firstProgressIndex = firstProgress.currentLegProgress().stepIndex();
    List<Point> coordinates = createCoordinatesFromCurrentStep(firstProgress);
    Point lastPointInCurrentStep = coordinates.remove(coordinates.size() - 1);
    Location rawLocation = buildDefaultLocationUpdate(
      lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
    );
    when(rawLocation.getBearing()).thenReturn(145f);

    RouteProgress secondProgress = routeProcessor.buildNewRouteProgress(navigation, rawLocation);
    int secondProgressIndex = secondProgress.currentLegProgress().stepIndex();

    assertTrue(firstProgressIndex != secondProgressIndex);
  }
}
