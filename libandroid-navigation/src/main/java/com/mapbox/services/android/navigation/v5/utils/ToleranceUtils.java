package com.mapbox.services.android.navigation.v5.utils;

import com.mapbox.services.android.navigation.v5.models.StepIntersection;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.turf.TurfClassification;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;

public final class ToleranceUtils {

  private ToleranceUtils() {
    // Utils class therefore, shouldn't be initialized.
  }

  /**
   * Reduce the minimumDistanceBeforeRerouting if we are close to an intersection.
   * You can define these values in the navigationOptions
   */
  public static double dynamicRerouteDistanceTolerance(Point snappedPoint,
                                                       RouteProgress routeProgress,
                                                       MapboxNavigationOptions navigationOptions) {
    List<StepIntersection> intersections
      = routeProgress.currentLegProgress().currentStepProgress().intersections();

    if(!intersections.isEmpty()){
      List<Point> intersectionsPoints = new ArrayList<>();
      for (StepIntersection intersection : intersections) {
        intersectionsPoints.add(intersection.location());
      }

      Point closestIntersection = TurfClassification.nearestPoint(snappedPoint, intersectionsPoints);

      if (closestIntersection.equals(snappedPoint)) {
        return navigationOptions.minimumDistanceBeforeRerouting();
      }

      double distanceToNextIntersection = TurfMeasurement.distance(snappedPoint, closestIntersection,
              TurfConstants.UNIT_METERS);

      if (distanceToNextIntersection <= navigationOptions.maneuverZoneRadius()) {
        return navigationOptions.minimumDistanceBeforeRerouting() / 2;
      }
    }

    return navigationOptions.minimumDistanceBeforeRerouting();
  }
}