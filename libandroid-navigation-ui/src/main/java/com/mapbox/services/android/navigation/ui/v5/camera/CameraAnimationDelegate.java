package com.mapbox.services.android.navigation.ui.v5.camera;

import org.maplibre.android.camera.CameraUpdate;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.maps.MapLibreMap;

class CameraAnimationDelegate {

  private final MapLibreMap mapboxMap;

  CameraAnimationDelegate(MapLibreMap mapboxMap) {
    this.mapboxMap = mapboxMap;
  }

  void render(NavigationCameraUpdate update, int durationMs, MapLibreMap.CancelableCallback callback) {
    CameraUpdateMode mode = update.getMode();
    CameraUpdate cameraUpdate = update.getCameraUpdate();
    if (mode == CameraUpdateMode.OVERRIDE) {
      mapboxMap.getLocationComponent().setCameraMode(CameraMode.NONE);
      mapboxMap.animateCamera(cameraUpdate, durationMs, callback);
    } else if (!isTracking()) {
      mapboxMap.animateCamera(cameraUpdate, durationMs, callback);
    }
  }

  private boolean isTracking() {
    int cameraMode = mapboxMap.getLocationComponent().getCameraMode();
    return cameraMode != CameraMode.NONE;
  }
}