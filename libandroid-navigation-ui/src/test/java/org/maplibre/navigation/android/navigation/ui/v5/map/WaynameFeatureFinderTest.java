package org.maplibre.navigation.android.navigation.ui.v5.map;

import android.graphics.PointF;

import org.maplibre.android.maps.MapLibreMap;

import org.junit.Test;
import org.maplibre.navigation.android.navigation.ui.v5.map.WaynameFeatureFinder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WaynameFeatureFinderTest {

  @Test
  public void queryRenderedFeatures_mapLibreMapIsCalled() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    WaynameFeatureFinder featureFinder = new WaynameFeatureFinder(mapLibreMap);
    PointF point = mock(PointF.class);
    String[] layerIds = {"id", "id"};

    featureFinder.queryRenderedFeatures(point, layerIds);

    verify(mapLibreMap).queryRenderedFeatures(point, layerIds);
  }
}