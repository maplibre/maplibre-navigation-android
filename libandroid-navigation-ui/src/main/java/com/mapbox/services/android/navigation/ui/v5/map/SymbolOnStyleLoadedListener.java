package com.mapbox.services.android.navigation.ui.v5.map;

import android.graphics.Bitmap;

import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;

import static com.mapbox.services.android.navigation.ui.v5.map.NavigationSymbolManager.MAPBOX_NAVIGATION_MARKER_NAME;

class SymbolOnStyleLoadedListener implements MapView.OnDidFinishLoadingStyleListener {

  private final MapLibreMap mapboxMap;
  private final Bitmap markerBitmap;

  SymbolOnStyleLoadedListener(MapLibreMap mapboxMap, Bitmap markerBitmap) {
    this.mapboxMap = mapboxMap;
    this.markerBitmap = markerBitmap;
  }

  @Override
  public void onDidFinishLoadingStyle() {
    mapboxMap.getStyle().addImage(MAPBOX_NAVIGATION_MARKER_NAME, markerBitmap);
  }
}
