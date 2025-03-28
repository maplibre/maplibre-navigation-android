package org.maplibre.navigation.android.navigation.ui.v5.route;

import android.os.Handler;

import org.maplibre.navigation.core.models.DirectionsRoute;
import org.maplibre.navigation.core.models.RouteLeg;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.navigation.core.utils.Constants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.maplibre.navigation.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY;

class FeatureProcessingTask extends Thread {

  private final List<DirectionsRoute> routes;
  private final List<FeatureCollection> routeFeatureCollections = new ArrayList<>();
  private final WeakReference<OnRouteFeaturesProcessedCallback> callbackWeakReference;
  private final HashMap<LineString, DirectionsRoute> routeLineStrings = new HashMap<>();
  private AtomicBoolean cancelThread = new AtomicBoolean(false);
  private Handler postHandler;

  FeatureProcessingTask(List<DirectionsRoute> routes, OnRouteFeaturesProcessedCallback callback, Handler handler) {
    this.routes = routes;
    this.callbackWeakReference = new WeakReference<>(callback);
    this.postHandler = handler;
  }

  @Override
  public void run() {
    for (int i = 0; i < routes.size(); i++) {
      if (cancelThread.get()) {
        return;
      }
      DirectionsRoute route = routes.get(i);
      boolean isPrimary = i == 0;
      FeatureCollection routeFeatureCollection = createRouteFeatureCollection(route, isPrimary);
      routeFeatureCollections.add(routeFeatureCollection);
    }
    if (!cancelThread.get()) {
      completion();
    }
  }

  void cancel() {
    cancelThread.set(true);
  }

  private void completion() {
    final OnRouteFeaturesProcessedCallback callback = callbackWeakReference.get();
    if (callback != null) {
      postHandler.post(new Runnable() {
        @Override
        public void run() {
          if (cancelThread.get()) {
            return;
          }
          callback.onRouteFeaturesProcessed(routeFeatureCollections, routeLineStrings);
        }
      });
    }
  }

  private FeatureCollection createRouteFeatureCollection(DirectionsRoute route, boolean isPrimary) {
    final List<Feature> features = new ArrayList<>();

    LineString routeGeometry = LineString.fromPolyline(route.getGeometry(), Constants.PRECISION_6);
    Feature routeFeature = Feature.fromGeometry(routeGeometry);
    routeFeature.addBooleanProperty(PRIMARY_ROUTE_PROPERTY_KEY, isPrimary);
    features.add(routeFeature);
    routeLineStrings.put(routeGeometry, route);

    List<Feature> congestionFeatures = buildCongestionFeaturesFromRoute(route, routeGeometry, isPrimary);
    features.addAll(congestionFeatures);
    return FeatureCollection.fromFeatures(features);
  }

  private List<Feature> buildCongestionFeaturesFromRoute(DirectionsRoute route, LineString lineString,
                                                         boolean isPrimary) {
    final List<Feature> features = new ArrayList<>();
    for (RouteLeg leg : route.getLegs()) {
      if (leg.getAnnotation() != null && leg.getAnnotation().getCongestion() != null) {
        for (int i = 0; i < leg.getAnnotation().getCongestion().size(); i++) {
          // See https://github.com/mapbox/mapbox-navigation-android/issues/353
          if (leg.getAnnotation().getCongestion().size() + 1 <= lineString.coordinates().size()) {

            List<Point> points = new ArrayList<>();
            points.add(lineString.coordinates().get(i));
            points.add(lineString.coordinates().get(i + 1));

            LineString congestionLineString = LineString.fromLngLats(points);
            Feature feature = Feature.fromGeometry(congestionLineString);
            String congestionValue = leg.getAnnotation().getCongestion().get(i);
            feature.addStringProperty(RouteConstants.CONGESTION_KEY, congestionValue);
            feature.addBooleanProperty(PRIMARY_ROUTE_PROPERTY_KEY, isPrimary);
            features.add(feature);
          }
        }
      } else {
        Feature feature = Feature.fromGeometry(lineString);
        features.add(feature);
      }
    }
    return features;
  }
}
