package org.maplibre.navigation.android.navigation.ui.v5;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.maplibre.geojson.Point;
import org.maplibre.navigation.core.location.Location;
import org.maplibre.navigation.core.models.DirectionsRoute;

class NavigationPresenter {

    private NavigationContract.View view;
    private boolean resumeState;
    private boolean isTrackingCamera = false;

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
        isTrackingCamera = true;
    }

    void onCameraTrackingDismissed() {
        view.updateWayNameVisibility(false);
        isTrackingCamera = false;
    }


    void onRouteUpdate(DirectionsRoute directionsRoute) {
        view.drawRoute(directionsRoute);
        if (resumeState && isTrackingCamera) {
            view.updateCameraRouteOverview();
        } else {
            view.startCamera(directionsRoute);
        }
    }

    void onDestinationUpdate(Point point) {
        view.addMarker(point);
    }

    void onNavigationLocationUpdate(Location location) {
        if (resumeState && !isTrackingCamera) {
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