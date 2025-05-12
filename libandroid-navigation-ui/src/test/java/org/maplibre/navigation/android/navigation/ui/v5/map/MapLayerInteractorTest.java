package org.maplibre.navigation.android.navigation.ui.v5.map;

import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.layers.HeatmapLayer;
import org.maplibre.android.style.layers.Layer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyValue;
import org.maplibre.android.style.layers.SymbolLayer;

import org.junit.Test;
import org.maplibre.navigation.android.navigation.ui.v5.map.MapLayerInteractor;

import java.util.ArrayList;
import java.util.List;

import static org.maplibre.android.style.layers.PropertyFactory.visibility;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapLayerInteractorTest {

  @Test
  public void updateLayerVisibility_visibilityIsSet() {
    LineLayer anySymbolOrLineLayer = mock(LineLayer.class);
    when(anySymbolOrLineLayer.getSourceLayer()).thenReturn("any");
    List<Layer> layers = buildLayerListWith(anySymbolOrLineLayer);
    MapLibreMap map = mock(MapLibreMap.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    layerInteractor.updateLayerVisibility(true, "any");

    verify(anySymbolOrLineLayer).setProperties(any(PropertyValue.class));
  }

  @Test
  public void updateLayerVisibility_visibilityIsNotSet() {
    SymbolLayer anySymbolOrLineLayer = mock(SymbolLayer.class);
    when(anySymbolOrLineLayer.getSourceLayer()).thenReturn("any");
    List<Layer> layers = buildLayerListWith(anySymbolOrLineLayer);
    MapLibreMap map = mock(MapLibreMap.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    layerInteractor.updateLayerVisibility(true, "random");

    verify(anySymbolOrLineLayer, times(0)).setProperties(any(PropertyValue.class));
  }

  @Test
  public void updateLayerVisibility_visibilityIsNotSetIfInvalidLayer() {
    CircleLayer invalidLayer = mock(CircleLayer.class);
    List<Layer> layers = buildLayerListWith(invalidLayer);
    MapLibreMap map = mock(MapLibreMap.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    layerInteractor.updateLayerVisibility(true, "circle");

    verify(invalidLayer, times(0)).setProperties(any(PropertyValue.class));
  }

  @Test
  public void isLayerVisible_visibleReturnsTrue() {
    SymbolLayer anySymbolOrLineLayer = mock(SymbolLayer.class);
    when(anySymbolOrLineLayer.getSourceLayer()).thenReturn("any");
    when(anySymbolOrLineLayer.getVisibility()).thenReturn(visibility(Property.VISIBLE));
    List<Layer> layers = buildLayerListWith(anySymbolOrLineLayer);
    MapLibreMap map = mock(MapLibreMap.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    boolean isVisible = layerInteractor.isLayerVisible("any");

    assertTrue(isVisible);
  }

  @Test
  public void isLayerVisible_visibleReturnsFalse() {
    LineLayer anySymbolOrLineLayer = mock(LineLayer.class);
    when(anySymbolOrLineLayer.getSourceLayer()).thenReturn("any");
    when(anySymbolOrLineLayer.getVisibility()).thenReturn(visibility(Property.NONE));
    List<Layer> layers = buildLayerListWith(anySymbolOrLineLayer);
    MapLibreMap map = mock(MapLibreMap.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    boolean isVisible = layerInteractor.isLayerVisible("any");

    assertFalse(isVisible);
  }

  @Test
  public void isLayerVisible_visibleReturnsFalseIfInvalidLayer() {
    HeatmapLayer invalidLayer = mock(HeatmapLayer.class);
    List<Layer> layers = buildLayerListWith(invalidLayer);
    MapLibreMap map = mock(MapLibreMap.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    boolean isVisible = layerInteractor.isLayerVisible("heatmap");

    assertFalse(isVisible);
  }

  private List<Layer> buildLayerListWith(Layer layerToAdd) {
    List<Layer> layers = new ArrayList<>();
    layers.add(layerToAdd);
    return layers;
  }
}