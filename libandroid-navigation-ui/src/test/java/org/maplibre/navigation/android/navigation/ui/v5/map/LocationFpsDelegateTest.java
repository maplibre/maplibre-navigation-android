package org.maplibre.navigation.android.navigation.ui.v5.map;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.maps.MapLibreMap;

import org.junit.Test;
import org.maplibre.navigation.android.navigation.ui.v5.map.LocationFpsDelegate;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocationFpsDelegateTest {

  @Test
  public void onCameraIdle_newFpsIsSetZoom16() {
    double zoom = 16d;
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    when(mapLibreMap.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(mapLibreMap, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(25));
  }

  @Test
  public void onCameraIdle_newFpsIsSetZoom14() {
    double zoom = 14d;
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    when(mapLibreMap.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(mapLibreMap, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(15));
  }

  @Test
  public void onCameraIdle_newFpsIsSetZoom10() {
    double zoom = 10d;
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    when(mapLibreMap.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(mapLibreMap, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(10));
  }

  @Test
  public void onCameraIdle_newFpsIsSet5() {
    double zoom = 5d;
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    when(mapLibreMap.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(mapLibreMap, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(5));
  }

  @Test
  public void onStart_idleListenerAdded() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(mapLibreMap, locationComponent);

    locationFpsDelegate.onStart();

    verify(mapLibreMap, times(2)).addOnCameraIdleListener(eq(locationFpsDelegate));
  }

  @Test
  public void onStop_idleListenerRemoved() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(mapLibreMap, locationComponent);

    locationFpsDelegate.onStop();

    verify(mapLibreMap).removeOnCameraIdleListener(eq(locationFpsDelegate));
  }

  @Test
  public void updateEnabled_falseResetsToMax() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(mapLibreMap, locationComponent);

    locationFpsDelegate.updateEnabled(false);

    verify(locationComponent).setMaxAnimationFps(eq(Integer.MAX_VALUE));
  }

  @Test
  public void isEnabled_returnsFalseWhenSet() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(mapLibreMap, locationComponent);

    locationFpsDelegate.updateEnabled(false);

    assertFalse(locationFpsDelegate.isEnabled());
  }

  private CameraPosition buildCameraPosition(double zoom) {
    return new CameraPosition.Builder().zoom(zoom).build();
  }
}