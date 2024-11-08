package org.maplibre.navigation.android.navigation.ui.v5.map;

import static org.maplibre.navigation.android.navigation.ui.v5.map.NavigationSymbolManager.MAPLIBRE_NAVIGATION_MARKER_NAME;

import android.graphics.Bitmap;

import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;

class SymbolOnStyleLoadedListener implements MapView.OnDidFinishLoadingStyleListener {

  private final MapLibreMap mapLibreMap;
  private final Bitmap markerBitmap;

  SymbolOnStyleLoadedListener(MapLibreMap mapLibreMap, Bitmap markerBitmap) {
    this.mapLibreMap = mapLibreMap;
    this.markerBitmap = markerBitmap;
  }

  @Override
  public void onDidFinishLoadingStyle() {
    mapLibreMap.getStyle().addImage(MAPLIBRE_NAVIGATION_MARKER_NAME, markerBitmap);
  }
}
