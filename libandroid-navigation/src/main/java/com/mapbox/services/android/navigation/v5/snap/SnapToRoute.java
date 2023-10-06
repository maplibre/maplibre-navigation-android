package com.mapbox.services.android.navigation.v5.snap;

import static com.mapbox.services.android.navigation.v5.utils.Constants.PRECISION_6;

import android.location.Location;

import androidx.annotation.Nullable;

import com.mapbox.services.android.navigation.v5.models.LegStep;
import com.mapbox.services.android.navigation.v5.models.RouteLeg;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteLegProgress;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.services.android.navigation.v5.utils.MathUtils;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfMisc;

import java.util.List;

/**
 * This attempts to snap the user to the closest position along the route. Prior to snapping the
 * user, their location's checked to ensure that the user didn't veer off-route. If your application
 * uses the Mapbox Map SDK, querying the map and snapping the user to the road grid might be a
 * better solution.
 *
 * @since 0.4.0
 */
public class SnapToRoute extends Snap {

  /**
   * Calculate a snapped location along the route. Latitude, longitude and bearing are provided.
   *
   * @param location Current raw user location
   * @param routeProgress Current route progress
   * @return Snapped location along route
   */
  @Override
  public Location getSnappedLocation(Location location, RouteProgress routeProgress) {
    Location snappedLocation = snapLocationLatLng(location, routeProgress.currentStepPoints());
    snappedLocation.setBearing(snapLocationBearing(routeProgress));
    return snappedLocation;
  }

  /**
   * Snap coordinates of user's location to the closest position along the current step.
   *
   * @param location        the raw location
   * @param stepCoordinates the list of step geometry coordinates
   * @return the altered user location
   * @since 0.4.0
   */
  private static Location snapLocationLatLng(Location location, List<Point> stepCoordinates) {
    Location snappedLocation = new Location(location);
    Point locationToPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());

    // Uses Turf's pointOnLine, which takes a Point and a LineString to calculate the closest
    // Point on the LineString.
    if (stepCoordinates.size() > 1) {
      Feature feature = TurfMisc.nearestPointOnLine(locationToPoint, stepCoordinates);
      if (feature.geometry() != null) {
        Point point = ((Point) feature.geometry());
        snappedLocation.setLongitude(point.longitude());
        snappedLocation.setLatitude(point.latitude());
      }
    }
    return snappedLocation;
  }

  /**
   * Creates a snapped bearing for the snapped {@link Location}.
   * <p>
   * This is done by measuring 1 meter ahead of the current step distance traveled and
   * creating a {@link Point} with this distance using {@link TurfMeasurement#along(LineString, double, String)}.
   * <p>
   * If the step distance remaining is zero, the distance ahead is the first point of upcoming leg.
   * This way, an accurate bearing is upheld transitioning between legs.
   *
   * @param routeProgress Current route progress
   * @return Float bearing snapped to route
   */
  private static float snapLocationBearing(RouteProgress routeProgress) {
    Point currentPoint = getCurrentPoint(routeProgress);
    if (currentPoint == null) {
      return 0f;
    }

    Point futurePoint = getFuturePoint(routeProgress);
    if (futurePoint == null) {
      return 0f;
    }

    // Get bearing and convert azimuth to degrees
    double azimuth = TurfMeasurement.bearing(currentPoint, futurePoint);
    return (float) MathUtils.wrap(azimuth, 0, 360);
  }

  /**
   * Current step point. If no current leg process is available, null is returned.
   *
   * @param routeProgress Current route progress
   * @return Current step point or null if no current leg process is available
   */
  @Nullable
  private static Point getCurrentPoint(RouteProgress routeProgress) {
    return getCurrentStepPoint(routeProgress, 0);
  }

  /**
   * Get future point. This might be the upcoming step or the following leg. If none of them are
   * available, null is returned.
   *
   * @param routeProgress Current route progress
   * @return Future point or null if no following point is available
   */
  @Nullable
  private static Point getFuturePoint(RouteProgress routeProgress) {
    if (routeProgress.currentLegProgress().distanceRemaining() > 1) {
      // User has not reaching the end of current leg. Use traveled distance + 1 meter for future point
      return getCurrentStepPoint(routeProgress, 1);
    } else {
      // User has reached the end of steps. Use upcoming leg for future point if available.
      return getUpcomingLegPoint(routeProgress);
    }
  }

  /**
   * Current step point plus additional distance value. If no current leg process is available,
   * null is returned.
   *
   * @param routeProgress Current route progress
   * @param additionalDistance Additional distance to add to current step point
   * @return Current step point + additional distance or null if no current leg process is available
   */
  @Nullable
  private static Point getCurrentStepPoint(RouteProgress routeProgress, double additionalDistance) {
    RouteLegProgress legProgress = routeProgress.currentLegProgress();
    if (legProgress == null || legProgress.currentStep().geometry() == null) {
      return null;
    }

    LineString currentStepLineString = LineString.fromPolyline(legProgress.currentStep().geometry(), PRECISION_6);
    if (currentStepLineString.coordinates().isEmpty()) {
      return null;
    }

    return TurfMeasurement.along(currentStepLineString, legProgress.currentStepProgress().distanceTraveled() + additionalDistance, TurfConstants.UNIT_METERS);
  }

  /**
   * Get next leg's start point. The second step of next leg is used as start point to avoid
   * returning the same coordinates as the end point of the leg before. If no next leg is available,
   * null is returned.
   *
   * @param routeProgress Current route progress
   * @return Next leg's start point or null if no next leg is available
   */
  @Nullable
  private static Point getUpcomingLegPoint(RouteProgress routeProgress) {
    if (routeProgress.directionsRoute().legs() != null && routeProgress.directionsRoute().legs().size() - 1 <= routeProgress.legIndex()) {
      return null;
    }

    RouteLeg upcomingLeg = routeProgress.directionsRoute().legs().get(routeProgress.legIndex() + 1);
    if (upcomingLeg.steps() == null || upcomingLeg.steps().size() < 1) {
      return null;
    }

    // While first step is the same point as the last point of the current step, use the second one.
    LegStep firstStep = upcomingLeg.steps().get(1);
    if (firstStep.geometry() == null) {
      return null;
    }

    LineString currentStepLineString = LineString.fromPolyline(firstStep.geometry(), PRECISION_6);
    if (currentStepLineString.coordinates().isEmpty()) {
      return null;
    }

    return TurfMeasurement.along(currentStepLineString, 1, TurfConstants.UNIT_METERS);
  }
}