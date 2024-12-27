package com.mapbox.services.android.navigation.ui.v5;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;

/**
 * Listener used to detect user interaction with the map while driving.
 * <p>
 * If the camera tracking is dismissed, we notify the presenter to adjust UI accordingly.
 */
class NavigationOnCameraTrackingChangedListener implements OnCameraTrackingChangedListener {

  private final NavigationPresenter navigationPresenter;

  NavigationOnCameraTrackingChangedListener(NavigationPresenter navigationPresenter) {
    this.navigationPresenter = navigationPresenter;
  }

  @Override
  public void onCameraTrackingDismissed() {
      navigationPresenter.onCameraTrackingDismissed();
  }

  @Override
  public void onCameraTrackingChanged(int currentMode) {
    // Intentionally empty
  }
}
