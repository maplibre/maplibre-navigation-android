package com.mapbox.services.android.navigation.ui.v5.map;

import android.graphics.Bitmap;

import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;

import org.junit.Test;

import static com.mapbox.services.android.navigation.ui.v5.map.NavigationSymbolManager.MAPBOX_NAVIGATION_MARKER_NAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SymbolOnStyleLoadedListenerTest {

  @Test
  public void onDidFinishLoadingStyle_markerIsAdded() {
    MapLibreMap mapboxMap = mock(MapLibreMap.class);
    Style style = mock(Style.class);
    when(mapboxMap.getStyle()).thenReturn(style);
    Bitmap markerBitmap = mock(Bitmap.class);
    SymbolOnStyleLoadedListener listener = new SymbolOnStyleLoadedListener(mapboxMap, markerBitmap);

    listener.onDidFinishLoadingStyle();

    verify(style).addImage(eq(MAPBOX_NAVIGATION_MARKER_NAME), eq(markerBitmap));
  }
}