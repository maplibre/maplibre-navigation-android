package org.maplibre.navigation.android.navigation.ui.v5.map;

import android.content.Context;

import org.maplibre.navigation.core.models.LegStep;
import org.maplibre.navigation.core.models.ManeuverModifier;
import org.maplibre.navigation.core.models.StepManeuver;
import org.maplibre.android.maps.MapView;
import org.maplibre.navigation.android.navigation.ui.v5.camera.NavigationCamera;
import org.maplibre.navigation.core.navigation.MapLibreNavigation;
import org.maplibre.navigation.core.routeprogress.RouteLegProgress;
import org.maplibre.navigation.core.routeprogress.RouteProgress;
import org.maplibre.navigation.core.routeprogress.RouteStepProgress;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapFpsDelegateTest {

  @Test
  public void addProgressChangeListener_navigationReceivesListener() {
    MapLibreNavigation navigation = mock(MapLibreNavigation.class);
    MapFpsDelegate delegate = new MapFpsDelegate(mock(MapView.class), mock(MapBatteryMonitor.class));

    delegate.addProgressChangeListener(navigation);

    verify(navigation).addProgressChangeListener(any(FpsDelegateProgressChangeListener.class));
  }

  @Test
  public void onTransitionFinished_resetFpsWhenNotTracking() {
    MapView mapView = mock(MapView.class);
    MapFpsDelegate delegate = new MapFpsDelegate(mapView, mock(MapBatteryMonitor.class));

    delegate.onTransitionFinished(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE);

    verify(mapView).setMaximumFps(eq(Integer.MAX_VALUE));
  }

  @Test
  public void onTransitionCancelled_resetFpsWhenNotTracking() {
    MapView mapView = mock(MapView.class);
    MapFpsDelegate delegate = new MapFpsDelegate(mapView, mock(MapBatteryMonitor.class));

    delegate.onTransitionCancelled(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE);

    verify(mapView).setMaximumFps(eq(Integer.MAX_VALUE));
  }

  @Test
  public void onStop_navigationListenerRemoved() {
    MapLibreNavigation navigation = mock(MapLibreNavigation.class);
    MapFpsDelegate delegate = new MapFpsDelegate(mock(MapView.class), mock(MapBatteryMonitor.class));
    delegate.addProgressChangeListener(navigation);

    delegate.onStop();

    verify(navigation).removeProgressChangeListener(any(FpsDelegateProgressChangeListener.class));
  }

  @Test
  public void updateEnabledFalse_maxFpsReset() {
    MapView mapView = mock(MapView.class);
    MapFpsDelegate delegate = new MapFpsDelegate(mapView, mock(MapBatteryMonitor.class));

    delegate.updateEnabled(false);

    mapView.setMaximumFps(eq(Integer.MAX_VALUE));
  }

  @Test
  public void adjustFpsFor_thresholdSetWithCorrectManeuver() {
    MapView mapView = mock(MapView.class);
    MapBatteryMonitor batteryMonitor = mock(MapBatteryMonitor.class);
    when(batteryMonitor.isPluggedIn(any(Context.class))).thenReturn(false);
    MapFpsDelegate delegate = new MapFpsDelegate(mapView, batteryMonitor);
    RouteProgress routeProgress = buildRouteProgressWith(ManeuverModifier.Type.STRAIGHT);
    int maxFps = 5;
    delegate.updateMaxFpsThreshold(maxFps);

    delegate.adjustFpsFor(routeProgress);

    verify(mapView).setMaximumFps(eq(maxFps));
  }

  @Test
  public void adjustFpsFor_thresholdSetWithCorrectDuration() {
    MapView mapView = mock(MapView.class);
    MapBatteryMonitor batteryMonitor = mock(MapBatteryMonitor.class);
    when(batteryMonitor.isPluggedIn(any(Context.class))).thenReturn(false);
    MapFpsDelegate delegate = new MapFpsDelegate(mapView, batteryMonitor);
    RouteProgress routeProgress = buildRouteProgressWith(100d, 20d);
    int maxFps = 5;
    delegate.updateMaxFpsThreshold(maxFps);

    delegate.adjustFpsFor(routeProgress);

    verify(mapView).setMaximumFps(eq(maxFps));
  }

  @Test
  public void adjustFpsFor_nullModifier_thresholdSetWithNullModifier() {
    MapView mapView = mock(MapView.class);
    MapBatteryMonitor batteryMonitor = mock(MapBatteryMonitor.class);
    when(batteryMonitor.isPluggedIn(any(Context.class))).thenReturn(false);
    MapFpsDelegate delegate = new MapFpsDelegate(mapView, batteryMonitor);

    // Build a RouteProgress whose current step's maneuver has a null modifier
    RouteProgress routeProgress = mock(RouteProgress.class);
    RouteLegProgress routeLegProgress = mock(RouteLegProgress.class);
    RouteStepProgress routeStepProgress = mock(RouteStepProgress.class);
    StepManeuver currentManeuver = mock(StepManeuver.class);
    when(currentManeuver.getModifier()).thenReturn(null);
    LegStep currentStep = mock(LegStep.class);
    when(currentStep.getDuration()).thenReturn(100d);
    when(routeStepProgress.getDurationRemaining()).thenReturn(20d);
    when(routeLegProgress.getCurrentStepProgress()).thenReturn(routeStepProgress);
    when(routeProgress.getCurrentLegProgress()).thenReturn(routeLegProgress);
    when(currentStep.getManeuver()).thenReturn(currentManeuver);
    when(routeLegProgress.getCurrentStep()).thenReturn(currentStep);

    int maxFps = 5;
    delegate.updateMaxFpsThreshold(maxFps);
    delegate.adjustFpsFor(routeProgress);

    verify(mapView).setMaximumFps(eq(maxFps)) ;
  }

  private RouteProgress buildRouteProgressWith(ManeuverModifier.Type maneuverModifier) {
    RouteProgress routeProgress = mock(RouteProgress.class);
    RouteLegProgress routeLegProgress = mock(RouteLegProgress.class);
    LegStep currentStep = mock(LegStep.class);
    StepManeuver currentManeuver = mock(StepManeuver.class);
    when(currentManeuver.getModifier()).thenReturn(maneuverModifier);
    when(currentStep.getManeuver()).thenReturn(currentManeuver);
    when(routeLegProgress.getCurrentStep()).thenReturn(currentStep);
    when(routeProgress.getCurrentLegProgress()).thenReturn(routeLegProgress);
    return routeProgress;
  }

  private RouteProgress buildRouteProgressWith(double totalDuration, double durationRemaining) {
    RouteProgress routeProgress = mock(RouteProgress.class);
    RouteLegProgress routeLegProgress = mock(RouteLegProgress.class);
    RouteStepProgress routeStepProgress = mock(RouteStepProgress.class);
    StepManeuver currentManeuver = mock(StepManeuver.class);
    when(currentManeuver.getModifier()).thenReturn(ManeuverModifier.Type.LEFT);
    LegStep currentStep = mock(LegStep.class);
    when(currentStep.getDuration()).thenReturn(totalDuration);
    when(routeStepProgress.getDurationRemaining()).thenReturn(durationRemaining);
    when(routeLegProgress.getCurrentStepProgress()).thenReturn(routeStepProgress);
    when(routeProgress.getCurrentLegProgress()).thenReturn(routeLegProgress);
    when(currentStep.getManeuver()).thenReturn(currentManeuver);
    when(routeLegProgress.getCurrentStep()).thenReturn(currentStep);
    return routeProgress;
  }
}