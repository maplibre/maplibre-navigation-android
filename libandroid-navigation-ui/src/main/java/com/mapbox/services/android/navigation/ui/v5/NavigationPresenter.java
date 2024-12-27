package com.mapbox.services.android.navigation.ui.v5;

import android.location.Location;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.core.utils.TextUtils;
import com.mapbox.geojson.Point;

class NavigationPresenter {

  private NavigationContract.View view;
  private boolean resumeState;

  NavigationPresenter(NavigationContract.View view) {
    this.view = view;
  }

  void updateResumeState(boolean resumeState) {
    this.resumeState = resumeState;
  }

  void onRecenterClick() {
    view.updateWayNameVisibility(true);
    view.resetCameraPosition();
    view.hideRecenterBtn();
  }

  void onCameraTrackingDismissed() {
      view.updateWayNameVisibility(false);
  }

  void onSummaryBottomSheetHidden() {
      view.showRecenterBtn();
  }

  void onRouteUpdate(DirectionsRoute directionsRoute) {
    view.drawRoute(directionsRoute);
    if (resumeState && view.isRecenterButtonVisible()) {
      view.updateCameraRouteOverview();
    } else {
      view.startCamera(directionsRoute);
    }
  }

  void onDestinationUpdate(Point point) {
    view.addMarker(point);
  }

  void onNavigationLocationUpdate(Location location) {
    if (resumeState && !view.isRecenterButtonVisible()) {
      view.resumeCamera(location);
      resumeState = false;
    }
    view.updateNavigationMap(location);
  }

  void onWayNameChanged(@NonNull String wayName) {
    if (TextUtils.isEmpty(wayName)) {
      view.updateWayNameVisibility(false);
      return;
    }
    view.updateWayNameView(wayName);
    view.updateWayNameVisibility(true);
  }

  void onNavigationStopped() {
    view.updateWayNameVisibility(false);
  }

  void onRouteOverviewClick() {
    view.updateWayNameVisibility(false);
    view.updateCameraRouteOverview();
    view.showRecenterBtn();
  }
}