package org.maplibre.navigation.android.navigation.ui.v5.map;

import android.graphics.Bitmap;

import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;

import org.junit.Test;
import org.maplibre.navigation.android.navigation.ui.v5.map.SymbolOnStyleLoadedListener;

import static org.maplibre.navigation.android.navigation.ui.v5.map.NavigationSymbolManager.MAPLIBRE_NAVIGATION_MARKER_NAME;
import static org.maplibre.navigation.android.navigation.ui.v5.map.NavigationSymbolManager.MAPLIBRE_NAVIGATION_MARKER_NAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SymbolOnStyleLoadedListenerTest {

  @Test
  public void onDidFinishLoadingStyle_markerIsAdded() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    Style style = mock(Style.class);
    when(mapLibreMap.getStyle()).thenReturn(style);
    Bitmap markerBitmap = mock(Bitmap.class);
    SymbolOnStyleLoadedListener listener = new SymbolOnStyleLoadedListener(mapLibreMap, markerBitmap);

    listener.onDidFinishLoadingStyle();

    verify(style).addImage(eq(MAPLIBRE_NAVIGATION_MARKER_NAME), eq(markerBitmap));
  }
}