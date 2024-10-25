package org.maplibre.navigation.android.navigation.ui.v5.route;

import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;

import java.util.HashMap;
import java.util.List;

interface OnRouteFeaturesProcessedCallback {
  void onRouteFeaturesProcessed(List<FeatureCollection> routeFeatureCollections,
                                HashMap<LineString, DirectionsRoute> routeLineStrings);
}
