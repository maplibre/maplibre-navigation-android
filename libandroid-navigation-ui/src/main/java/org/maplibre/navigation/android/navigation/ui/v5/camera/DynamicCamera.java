package org.maplibre.navigation.android.navigation.ui.v5.camera;


import static org.maplibre.geojson.common.CommonExtKt.toJvm;

import org.maplibre.android.geometry.LatLng;
import org.maplibre.geojson.Point;
import org.maplibre.navigation.core.location.Location;
import androidx.annotation.NonNull;

import org.maplibre.navigation.core.models.LegStep;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.navigation.core.navigation.NavigationConstants;
import org.maplibre.navigation.core.navigation.camera.RouteInformation;
import org.maplibre.navigation.core.navigation.camera.SimpleCamera;
import org.maplibre.navigation.core.routeprogress.RouteProgress;

import java.util.ArrayList;
import java.util.List;

public class DynamicCamera extends SimpleCamera {

  private static final double MAX_CAMERA_TILT = 60d;
  private static final double MIN_CAMERA_TILT = 45d;
  private static final double MAX_CAMERA_ZOOM = 16d;
  private static final double MIN_CAMERA_ZOOM = 12d;

  private MapLibreMap mapLibreMap;
  private LegStep currentStep;
  private boolean hasPassedLowAlertLevel;
  private boolean hasPassedMediumAlertLevel;
  private boolean hasPassedHighAlertLevel;
  private boolean forceUpdateZoom;
  private boolean isShutdown = false;

  public DynamicCamera(@NonNull MapLibreMap mapLibreMap) {
    this.mapLibreMap = mapLibreMap;
  }

  @Override
  public double tilt(RouteInformation routeInformation) {
    if (isShutdown) {
      return DEFAULT_TILT;
    }

    RouteProgress progress = routeInformation.getRouteProgress();
    if (progress != null) {
      double distanceRemaining = progress.getCurrentLegProgress().getCurrentStepProgress().getDistanceRemaining();
      return createTilt(distanceRemaining);
    }
    return super.tilt(routeInformation);
  }

  @Override
  public double zoom(RouteInformation routeInformation) {
    if (isShutdown) {
      return DEFAULT_ZOOM;
    }

    if (validLocationAndProgress(routeInformation) && shouldUpdateZoom(routeInformation)) {
      return createZoom(routeInformation);
    } else if (routeInformation.getRoute() != null) {
      return super.zoom(routeInformation);
    }
    return mapLibreMap.getCameraPosition().zoom;
  }


  /**
   * Called when the zoom level should force update on the next usage
   * of {@link DynamicCamera#zoom(RouteInformation)}.
   */
  public void forceResetZoomLevel() {
    forceUpdateZoom = true;
  }

  public void clearMap() {
    isShutdown = true;
    mapLibreMap = null;
  }

  /**
   * Creates a tilt value based on the distance remaining for the current {@link LegStep}.
   * <p>
   * Checks if the calculated value is within the set min / max bounds.
   *
   * @param distanceRemaining from the current step
   * @return tilt within set min / max bounds
   */
  private double createTilt(double distanceRemaining) {
    double tilt = distanceRemaining / 5;
    if (tilt > MAX_CAMERA_TILT) {
      return MAX_CAMERA_TILT;
    } else if (tilt < MIN_CAMERA_TILT) {
      return MIN_CAMERA_TILT;
    }
    return Math.round(tilt);
  }

  /**
   * Creates a zoom value based on the result of {@link MapLibreMap#getCameraForLatLngBounds(LatLngBounds, int[])}.
   * <p>
   * 0 zoom is the world view, while 22 (default max threshold) is the closest you can position
   * the camera to the map.
   *
   * @param routeInformation for current location and progress
   * @return zoom within set min / max bounds
   */
  private double createZoom(RouteInformation routeInformation) {
    CameraPosition position = createCameraPosition(routeInformation.getLocation(), routeInformation.getRouteProgress());
    if (position == null) {
      return DEFAULT_ZOOM;
    }
    if (position.zoom > MAX_CAMERA_ZOOM) {
      return MAX_CAMERA_ZOOM;
    } else if (position.zoom < MIN_CAMERA_ZOOM) {
      return MIN_CAMERA_ZOOM;
    }
    return position.zoom;
  }

  /**
   * Creates a camera position with the current location and upcoming maneuver location.
   * <p>
   * Using {@link MapLibreMap#getCameraForLatLngBounds(LatLngBounds, int[])} with a {@link LatLngBounds}
   * that includes the current location and upcoming maneuver location.
   *
   * @param location      for current location
   * @param routeProgress for upcoming maneuver location
   * @return camera position that encompasses both locations
   */
  private CameraPosition createCameraPosition(Location location, RouteProgress routeProgress) {
    LegStep upComingStep = routeProgress.getCurrentLegProgress().getUpComingStep();
    if (upComingStep != null) {
      Point stepManeuverPoint = toJvm(upComingStep.getManeuver().getLocation());

      List<LatLng> latLngs = new ArrayList<>();
      LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
      LatLng maneuverLatLng = new LatLng(stepManeuverPoint.latitude(), stepManeuverPoint.longitude());
      latLngs.add(currentLatLng);
      latLngs.add(maneuverLatLng);

      if (latLngs.size() < 1 || currentLatLng.equals(maneuverLatLng)) {
        return mapLibreMap.getCameraPosition();
      }

      LatLngBounds cameraBounds = new LatLngBounds.Builder()
          .includes(latLngs)
          .build();

      int[] padding = {0, 0, 0, 0};
      return mapLibreMap.getCameraForLatLngBounds(cameraBounds, padding);
    }
    return mapLibreMap.getCameraPosition();
  }

  private boolean isForceUpdate() {
    if (forceUpdateZoom) {
      forceUpdateZoom = false;
      return true;
    }
    return false;
  }

  /**
   * Looks to see if we have a new step.
   *
   * @param routeProgress provides updated step information
   * @return true if new step, false if not
   */
  private boolean isNewStep(RouteProgress routeProgress) {
    boolean isNewStep = currentStep == null || !currentStep.equals(routeProgress.getCurrentLegProgress().getCurrentStep());
    currentStep = routeProgress.getCurrentLegProgress().getCurrentStep();
    resetAlertLevels(isNewStep);
    return isNewStep;
  }

  private void resetAlertLevels(boolean isNewStep) {
    if (isNewStep) {
      hasPassedLowAlertLevel = false;
      hasPassedMediumAlertLevel = false;
      hasPassedHighAlertLevel = false;
    }
  }

  private boolean validLocationAndProgress(RouteInformation routeInformation) {
    return routeInformation.getLocation() != null && routeInformation.getRouteProgress() != null;
  }

  private boolean shouldUpdateZoom(RouteInformation routeInformation) {
    RouteProgress progress = routeInformation.getRouteProgress();
    return isForceUpdate()
      || isNewStep(progress)
      || isLowAlert(progress)
      || isMediumAlert(progress)
      || isHighAlert(progress);
  }

  private boolean isLowAlert(RouteProgress progress) {
    if (!hasPassedLowAlertLevel) {
      double durationRemaining = progress.getCurrentLegProgress().getCurrentStepProgress().getDurationRemaining();
      double stepDuration = progress.getCurrentLegProgress().getCurrentStep().getDuration();
      boolean isLowAlert = durationRemaining < NavigationConstants.NAVIGATION_LOW_ALERT_DURATION;
      boolean hasValidStepDuration = stepDuration > NavigationConstants.NAVIGATION_LOW_ALERT_DURATION;
      if (hasValidStepDuration && isLowAlert) {
        hasPassedLowAlertLevel = true;
        return true;
      }
    }
    return false;
  }

  private boolean isMediumAlert(RouteProgress progress) {
    if (!hasPassedMediumAlertLevel) {
      double durationRemaining = progress.getCurrentLegProgress().getCurrentStepProgress().getDurationRemaining();
      double stepDuration = progress.getCurrentLegProgress().getCurrentStep().getDuration();
      boolean isMediumAlert = durationRemaining < NavigationConstants.NAVIGATION_MEDIUM_ALERT_DURATION;
      boolean hasValidStepDuration = stepDuration > NavigationConstants.NAVIGATION_MEDIUM_ALERT_DURATION;
      if (hasValidStepDuration && isMediumAlert) {
        hasPassedMediumAlertLevel = true;
        return true;
      }
    }
    return false;
  }

  private boolean isHighAlert(RouteProgress progress) {
    if (!hasPassedHighAlertLevel) {
      double durationRemaining = progress.getCurrentLegProgress().getCurrentStepProgress().getDurationRemaining();
      double stepDuration = progress.getCurrentLegProgress().getCurrentStep().getDuration();
      boolean isHighAlert = durationRemaining < NavigationConstants.NAVIGATION_HIGH_ALERT_DURATION;
      boolean hasValidStepDuration = stepDuration > NavigationConstants.NAVIGATION_HIGH_ALERT_DURATION;
      if (hasValidStepDuration && isHighAlert) {
        hasPassedHighAlertLevel = true;
        return true;
      }
    }
    return false;
  }
}
