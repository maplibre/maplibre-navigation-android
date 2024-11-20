package org.maplibre.navigation.android.navigation.ui.v5.map;

import org.maplibre.android.maps.MapLibreMap;

import org.junit.Test;
import org.maplibre.navigation.android.navigation.ui.v5.map.MapPaddingAdjustor;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MapPaddingAdjustorTest {

  @Test
  public void adjustLocationIconWith_customPaddingIsSet() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 0, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(mapLibreMap, defaultPadding);

    paddingAdjustor.adjustLocationIconWith(customPadding);

    verify(mapLibreMap).setPadding(0, 0, 0, 0);
  }

  @Test
  public void isUsingDefault_falseAfterCustomPaddingIsSet() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 0, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(mapLibreMap, defaultPadding);

    paddingAdjustor.adjustLocationIconWith(customPadding);

    assertFalse(paddingAdjustor.isUsingDefault());
  }

  @Test
  public void isUsingDefault_trueWithoutCustomPadding() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    int[] defaultPadding = {0, 250, 0, 0};

    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(mapLibreMap, defaultPadding);

    assertTrue(paddingAdjustor.isUsingDefault());
  }

  @Test
  public void updatePaddingWithZero_updatesMapToZeroPadding() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(mapLibreMap, defaultPadding);

    paddingAdjustor.updatePaddingWith(new int[]{0, 0, 0, 0});

    verify(mapLibreMap).setPadding(0, 0, 0, 0);
  }

  @Test
  public void updatePaddingWithZero_retainsCustomPadding() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 350, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(mapLibreMap, defaultPadding);
    paddingAdjustor.adjustLocationIconWith(customPadding);
    paddingAdjustor.updatePaddingWith(new int[]{0, 0, 0, 0});

    paddingAdjustor.resetPadding();

    verify(mapLibreMap, times(2)).setPadding(0, 350, 0, 0);
  }

  @Test
  public void updatePaddingWithDefault_defaultIsRestoredAfterCustom() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 0, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(mapLibreMap, defaultPadding);
    paddingAdjustor.adjustLocationIconWith(customPadding);

    paddingAdjustor.updatePaddingWithDefault();

    verify(mapLibreMap).setPadding(0, 250, 0, 0);
  }

  @Test
  public void retrieveCurrentPadding_returnsCurrentMapPadding() {
    MapLibreMap mapLibreMap = mock(MapLibreMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(mapLibreMap, defaultPadding);

    paddingAdjustor.retrieveCurrentPadding();

    verify(mapLibreMap).getPadding();
  }
}