package org.maplibre.navigation.android.navigation.ui.v5.camera;

import org.maplibre.android.location.modes.CameraMode;

import org.junit.Test;
import org.maplibre.navigation.android.navigation.ui.v5.camera.NavigationCamera;
import org.maplibre.navigation.android.navigation.ui.v5.camera.NavigationCameraTransitionListener;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NavigationCameraTransitionListenerTest {

  @Test
  public void onLocationCameraTransitionFinished() {
    NavigationCamera camera = mock(NavigationCamera.class);
    NavigationCameraTransitionListener listener = new NavigationCameraTransitionListener(camera);
    int trackingGps = CameraMode.TRACKING_GPS;

    listener.onLocationCameraTransitionFinished(trackingGps);

    verify(camera).updateTransitionListenersFinished(trackingGps);
  }

  @Test
  public void onLocationCameraTransitionCanceled() {
    NavigationCamera camera = mock(NavigationCamera.class);
    NavigationCameraTransitionListener listener = new NavigationCameraTransitionListener(camera);
    int trackingGpsNorth = CameraMode.TRACKING_GPS_NORTH;

    listener.onLocationCameraTransitionCanceled(trackingGpsNorth);

    verify(camera).updateTransitionListenersCancelled(trackingGpsNorth);
  }

  @Test
  public void onLocationCameraTransitionCanceled_cameraStopsResetting() {
    NavigationCamera camera = mock(NavigationCamera.class);
    NavigationCameraTransitionListener listener = new NavigationCameraTransitionListener(camera);
    int trackingGpsNorth = CameraMode.TRACKING_GPS_NORTH;

    listener.onLocationCameraTransitionCanceled(trackingGpsNorth);

    verify(camera).updateIsResetting(eq(false));
  }
}