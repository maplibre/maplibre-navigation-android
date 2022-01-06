package com.mapbox.services.android.navigation.v5.navigation;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegAnnotation;
import com.mapbox.api.directionsrefresh.v1.models.DirectionsRefreshResponse;
import com.mapbox.api.directionsrefresh.v1.models.RouteLegRefresh;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class RouteRefreshCallback implements Callback<DirectionsRefreshResponse> {
  private final RouteAnnotationUpdater routeAnnotationUpdater;
  private final DirectionsRoute directionsRoute;
  private final int legIndex;
  private final RefreshCallback refreshCallback;

  RouteRefreshCallback(DirectionsRoute directionsRoute, int legIndex, RefreshCallback refreshCallback) {
    this(new RouteAnnotationUpdater(), directionsRoute, legIndex, refreshCallback);
  }

  RouteRefreshCallback(RouteAnnotationUpdater routeAnnotationUpdater, DirectionsRoute directionsRoute,
                       int legIndex, RefreshCallback refreshCallback) {
    this.routeAnnotationUpdater = routeAnnotationUpdater;
    this.directionsRoute = directionsRoute;
    this.legIndex = legIndex;
    this.refreshCallback = refreshCallback;
  }

  @Override
  public void onResponse(Call<DirectionsRefreshResponse> call, Response<DirectionsRefreshResponse> response) {
    if (response.body() == null || response.body().route() == null || response.body().route().legs() == null) {
      refreshCallback.onError(new RefreshError(response.message()));
    } else {
      List<LegAnnotation> annotations = new ArrayList<>();
      for (RouteLegRefresh  leg : response.body().route().legs()) {
        annotations.add(leg.annotation());
      }

      refreshCallback.onRefresh(routeAnnotationUpdater.update(directionsRoute, annotations, legIndex));
    }
  }

  @Override
  public void onFailure(Call<DirectionsRefreshResponse> call, Throwable throwable) {
    refreshCallback.onError(new RefreshError(throwable.getMessage()));
  }
}
