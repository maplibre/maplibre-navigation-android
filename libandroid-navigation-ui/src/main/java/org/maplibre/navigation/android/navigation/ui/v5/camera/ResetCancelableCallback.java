package org.maplibre.navigation.android.navigation.ui.v5.camera;

import org.maplibre.android.maps.MapLibreMap;

class ResetCancelableCallback implements MapLibreMap.CancelableCallback {

  private final NavigationCamera camera;

  ResetCancelableCallback(NavigationCamera camera) {
    this.camera = camera;
  }

  @Override
  public void onCancel() {
    camera.updateIsResetting(false);
  }

  @Override
  public void onFinish() {
    camera.updateIsResetting(false);
  }
}