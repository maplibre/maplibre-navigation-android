package org.maplibre.navigation.android.navigation.ui.v5.camera;

import org.maplibre.android.camera.CameraUpdate;
import org.maplibre.android.maps.MapLibreMap;

class CameraOverviewCancelableCallback implements MapLibreMap.CancelableCallback {

  private static final int OVERVIEW_UPDATE_DURATION_IN_MILLIS = 750;

  private CameraUpdate overviewUpdate;
  private MapLibreMap mapLibreMap;

  CameraOverviewCancelableCallback(CameraUpdate overviewUpdate, MapLibreMap mapLibreMap) {
    this.overviewUpdate = overviewUpdate;
    this.mapLibreMap = mapLibreMap;
  }

  @Override
  public void onCancel() {
    // No-op
  }

  @Override
  public void onFinish() {
    mapLibreMap.animateCamera(overviewUpdate, OVERVIEW_UPDATE_DURATION_IN_MILLIS);
  }
}
