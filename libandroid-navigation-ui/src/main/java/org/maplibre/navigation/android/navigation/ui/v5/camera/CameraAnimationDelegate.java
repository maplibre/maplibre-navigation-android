package org.maplibre.navigation.android.navigation.ui.v5.camera;

import org.maplibre.android.camera.CameraUpdate;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.maps.MapLibreMap;

class CameraAnimationDelegate {

  private final MapLibreMap mapLibreMap;

  CameraAnimationDelegate(MapLibreMap mapLibreMap) {
    this.mapLibreMap = mapLibreMap;
  }

  void render(NavigationCameraUpdate update, int durationMs, MapLibreMap.CancelableCallback callback) {
    CameraUpdateMode mode = update.getMode();
    CameraUpdate cameraUpdate = update.getCameraUpdate();
    if (mode == CameraUpdateMode.OVERRIDE) {
      mapLibreMap.getLocationComponent().setCameraMode(CameraMode.NONE);
      mapLibreMap.animateCamera(cameraUpdate, durationMs, callback);
    } else if (!isTracking()) {
      mapLibreMap.animateCamera(cameraUpdate, durationMs, callback);
    }
  }

  private boolean isTracking() {
    int cameraMode = mapLibreMap.getLocationComponent().getCameraMode();
    return cameraMode != CameraMode.NONE;
  }
}