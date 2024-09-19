package org.maplibre.navigation.android.navigation.ui.v5;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.maplibre.navigation.android.navigation.ui.v5.route.MapLibreRouteFetcher;
import org.maplibre.navigation.android.navigation.ui.v5.route.NavigationRoute;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions;
import org.maplibre.geojson.Point;

import org.maplibre.navigation.android.navigation.v5.route.RouteListener;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;

import java.util.Date;
import java.util.List;

public class NavigationViewRouter implements RouteListener {

  private final MapLibreRouteFetcher onlineRouter;
  private final ConnectivityStatusProvider connectivityStatus;
  private final RouteComparator routeComparator;
  private final ViewRouteListener listener;

  private RouteOptions routeOptions;
  private DirectionsRoute currentRoute;
  private Location location;
  private RouteCallStatus callStatus;

  public NavigationViewRouter(MapLibreRouteFetcher onlineRouter, ConnectivityStatusProvider connectivityStatus,
                              ViewRouteListener listener) {
    this.onlineRouter = onlineRouter;
    this.connectivityStatus = connectivityStatus;
    this.listener = listener;
    this.routeComparator = new RouteComparator(this);
    onlineRouter.addRouteListener(this);
  }

  // Extra fields for testing purposes
  NavigationViewRouter(MapLibreRouteFetcher onlineRouter,
                       ConnectivityStatusProvider connectivityStatus, RouteComparator routeComparator,
                       ViewRouteListener listener, RouteCallStatus callStatus) {
    this.onlineRouter = onlineRouter;
    this.connectivityStatus = connectivityStatus;
    this.routeComparator = routeComparator;
    this.listener = listener;
    this.callStatus = callStatus;
    onlineRouter.addRouteListener(this);
  }

  @Override
  public void onResponseReceived(DirectionsResponse response, @Nullable RouteProgress routeProgress) {
    if (validRouteResponse(response)) {
      routeComparator.compare(response, currentRoute);
    }
    updateCallStatusReceived();
  }

  @Override
  public void onErrorReceived(Throwable throwable) {
    onRequestError(throwable.getMessage());
    updateCallStatusReceived();
  }

  void extractRouteOptions(NavigationViewOptions options) {
    extractRouteFrom(options);
  }

  public void findRouteFrom(@Nullable RouteProgress routeProgress) {
    if (isRouting()) {
      return;
    }
    NavigationRoute.Builder builder = onlineRouter.buildRequest(location, routeProgress);
    findOnlineRouteWith(builder);
  }

  public void updateLocation(@NonNull Location location) {
    this.location = location;
  }

  void updateCurrentRoute(DirectionsRoute currentRoute) {
    this.currentRoute = currentRoute;
    listener.onRouteUpdate(currentRoute);
  }

  void updateCallStatusReceived() {
    if (callStatus != null) {
      callStatus.setResponseReceived();
    }
  }

  void onRequestError(String errorMessage) {
    listener.onRouteRequestError(errorMessage);
  }

  void onDestroy() {
    onlineRouter.cancelRouteCall();
    onlineRouter.clearListeners();
  }

  private boolean validRouteResponse(DirectionsResponse response) {
    return response != null && !response.routes().isEmpty();
  }

  private void extractRouteFrom(NavigationViewOptions options) {
    DirectionsRoute route = options.directionsRoute();
    cacheRouteOptions(route.routeOptions());
    updateCurrentRoute(route);
  }

  private void cacheRouteOptions(RouteOptions routeOptions) {
    this.routeOptions = routeOptions;
    cacheRouteDestination();
  }

  private void cacheRouteDestination() {
    boolean hasValidCoordinates = routeOptions != null && !routeOptions.coordinates().isEmpty();
    if (hasValidCoordinates) {
      List<Point> coordinates = routeOptions.coordinates();
      int destinationCoordinate = coordinates.size() - 1;
      Point destinationPoint = coordinates.get(destinationCoordinate);
      listener.onDestinationSet(destinationPoint);
    }
  }

  private void findOnlineRouteWith(NavigationRoute.Builder builder) {
    onlineRouter.cancelRouteCall();
    onlineRouter.findRouteWith(builder);
    callStatus = new RouteCallStatus(new Date());
  }

  private boolean isRouting() {
    if (callStatus == null) {
      return false;
    }
    return callStatus.isRouting(new Date());
  }
}
