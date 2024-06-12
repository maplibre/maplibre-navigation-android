package org.maplibre.navigation.android.navigation.ui.v5.map;

import android.graphics.PointF;

import org.maplibre.geojson.Feature;
import org.maplibre.android.maps.MapLibreMap;

import java.util.List;

class WaynameFeatureFinder {

  private MapLibreMap mapLibreMap;

  WaynameFeatureFinder(MapLibreMap mapLibreMap) {
    this.mapLibreMap = mapLibreMap;
  }

  List<Feature> queryRenderedFeatures(PointF point, String[] layerIds) {
    return mapLibreMap.queryRenderedFeatures(point, layerIds);
  }
}
