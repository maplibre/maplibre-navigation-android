package org.maplibre.navigation.android.navigation.ui.v5.route;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.content.res.AppCompatResources;

import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.Layer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonOptions;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.android.utils.MathUtils;
import org.maplibre.navigation.android.navigation.ui.v5.R;
import org.maplibre.navigation.android.navigation.ui.v5.utils.MapImageUtils;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;
import org.maplibre.turf.TurfConstants;
import org.maplibre.turf.TurfMeasurement;
import org.maplibre.turf.TurfMisc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.maplibre.android.style.expressions.Expression.color;
import static org.maplibre.android.style.expressions.Expression.get;
import static org.maplibre.android.style.expressions.Expression.interpolate;
import static org.maplibre.android.style.expressions.Expression.linear;
import static org.maplibre.android.style.expressions.Expression.step;
import static org.maplibre.android.style.expressions.Expression.stop;
import static org.maplibre.android.style.expressions.Expression.zoom;
import static org.maplibre.android.style.layers.Property.ICON_ROTATION_ALIGNMENT_MAP;
import static org.maplibre.android.style.layers.Property.NONE;
import static org.maplibre.android.style.layers.Property.VISIBLE;
import static org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap;
import static org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement;
import static org.maplibre.android.style.layers.PropertyFactory.visibility;

class MapRouteArrow {

  @ColorInt
  private final int arrowColor;
  @ColorInt
  private final int arrowBorderColor;

  private List<String> arrowLayerIds;
  private GeoJsonSource arrowShaftGeoJsonSource;
  private GeoJsonSource arrowHeadGeoJsonSource;

  private final MapView mapView;
  private final MapLibreMap mapLibreMap;

  MapRouteArrow(MapView mapView, MapLibreMap mapLibreMap, @StyleRes int styleRes) {
    this.mapView = mapView;
    this.mapLibreMap = mapLibreMap;

    Context context = mapView.getContext();
    TypedArray typedArray = context.obtainStyledAttributes(styleRes, R.styleable.NavigationMapRoute);
    arrowColor = typedArray.getColor(R.styleable.NavigationMapRoute_upcomingManeuverArrowColor,
      ContextCompat.getColor(context, R.color.maplibre_navigation_route_upcoming_maneuver_arrow_color));
    arrowBorderColor = typedArray.getColor(R.styleable.NavigationMapRoute_upcomingManeuverArrowBorderColor,
      ContextCompat.getColor(context, R.color.maplibre_navigation_route_upcoming_maneuver_arrow_border_color));
    typedArray.recycle();

    initialize();
  }

  void addUpcomingManeuverArrow(RouteProgress routeProgress) {
    boolean invalidUpcomingStepPoints = routeProgress.upcomingStepPoints() == null
      || routeProgress.upcomingStepPoints().size() < RouteConstants.TWO_POINTS;
    boolean invalidCurrentStepPoints = routeProgress.currentStepPoints().size() < RouteConstants.TWO_POINTS;
    if (invalidUpcomingStepPoints || invalidCurrentStepPoints) {
      updateVisibilityTo(false);
      return;
    }
    updateVisibilityTo(true);

    List<Point> maneuverPoints = obtainArrowPointsFrom(routeProgress);
    updateArrowShaftWith(maneuverPoints);
    updateArrowHeadWith(maneuverPoints);
  }

  void updateVisibilityTo(boolean visible) {
    Style style = mapLibreMap.getStyle();
    if (style != null) {
      for (String layerId : arrowLayerIds) {
        Layer layer = style.getLayer(layerId);
        if (layer != null) {
          String targetVisibility = visible ? VISIBLE : NONE;
          if (!targetVisibility.equals(layer.getVisibility().getValue())) {
            layer.setProperties(visibility(targetVisibility));
          }
        }
      }
    }
  }

  private List<Point> obtainArrowPointsFrom(RouteProgress routeProgress) {
    List<Point> reversedCurrent = new ArrayList<>(routeProgress.currentStepPoints());
    Collections.reverse(reversedCurrent);

    LineString arrowLineCurrent = LineString.fromLngLats(reversedCurrent);
    LineString arrowLineUpcoming = LineString.fromLngLats(routeProgress.upcomingStepPoints());

    LineString arrowCurrentSliced = TurfMisc.lineSliceAlong(arrowLineCurrent, 0, RouteConstants.THIRTY, TurfConstants.UNIT_METERS);
    LineString arrowUpcomingSliced = TurfMisc.lineSliceAlong(arrowLineUpcoming, 0, RouteConstants.THIRTY, TurfConstants.UNIT_METERS);

    Collections.reverse(arrowCurrentSliced.coordinates());

    List<Point> combined = new ArrayList<>();
    combined.addAll(arrowCurrentSliced.coordinates());
    combined.addAll(arrowUpcomingSliced.coordinates());
    return combined;
  }

  private void updateArrowShaftWith(List<Point> points) {
    LineString shaft = LineString.fromLngLats(points);
    Feature arrowShaftGeoJsonFeature = Feature.fromGeometry(shaft);
    arrowShaftGeoJsonSource.setGeoJson(arrowShaftGeoJsonFeature);
  }

  private void updateArrowHeadWith(List<Point> points) {
    double azimuth = TurfMeasurement.bearing(points.get(points.size() - 2), points.get(points.size() - 1));
    Feature arrowHeadGeoJsonFeature = Feature.fromGeometry(points.get(points.size() - 1));
    arrowHeadGeoJsonFeature.addNumberProperty(RouteConstants.ARROW_BEARING, (float) MathUtils.wrap(azimuth, 0, RouteConstants.MAX_DEGREES));
    arrowHeadGeoJsonSource.setGeoJson(arrowHeadGeoJsonFeature);
  }

  private void initialize() {
    initializeArrowShaft();
    initializeArrowHead();

    addArrowHeadIcon();
    addArrowHeadIconCasing();

    LineLayer shaftLayer = createArrowShaftLayer();
    LineLayer shaftCasingLayer = createArrowShaftCasingLayer();
    SymbolLayer headLayer = createArrowHeadLayer();
    SymbolLayer headCasingLayer = createArrowHeadCasingLayer();

    mapLibreMap.getStyle().addLayerBelow(shaftCasingLayer, RouteConstants.LAYER_ABOVE_UPCOMING_MANEUVER_ARROW);
    mapLibreMap.getStyle().addLayerAbove(headCasingLayer, shaftCasingLayer.getId());

    mapLibreMap.getStyle().addLayerAbove(shaftLayer, headCasingLayer.getId());
    mapLibreMap.getStyle().addLayerAbove(headLayer, shaftLayer.getId());

    createArrowLayerList(shaftLayer, shaftCasingLayer, headLayer, headCasingLayer);
  }

  private void initializeArrowShaft() {
    arrowShaftGeoJsonSource = new GeoJsonSource(
      RouteConstants.ARROW_SHAFT_SOURCE_ID,
      FeatureCollection.fromFeatures(new Feature[]{}),
      new GeoJsonOptions().withMaxZoom(16)
    );
    mapLibreMap.getStyle().addSource(arrowShaftGeoJsonSource);
  }

  private void initializeArrowHead() {
    arrowHeadGeoJsonSource = new GeoJsonSource(
      RouteConstants.ARROW_HEAD_SOURCE_ID,
      FeatureCollection.fromFeatures(new Feature[]{}),
      new GeoJsonOptions().withMaxZoom(16)
    );
    mapLibreMap.getStyle().addSource(arrowHeadGeoJsonSource);
  }

  private void addArrowHeadIcon() {
    int headResId = R.drawable.ic_arrow_head;
    Drawable arrowHead = AppCompatResources.getDrawable(mapView.getContext(), headResId);
    if (arrowHead == null) {
      return;
    }
    Drawable head = DrawableCompat.wrap(arrowHead);
    DrawableCompat.setTint(head.mutate(), arrowColor);
    Bitmap icon = MapImageUtils.getBitmapFromDrawable(head);
    mapLibreMap.getStyle().addImage(RouteConstants.ARROW_HEAD_ICON, icon);
  }

  private void addArrowHeadIconCasing() {
    int casingResId = R.drawable.ic_arrow_head_casing;
    Drawable arrowHeadCasing = AppCompatResources.getDrawable(mapView.getContext(), casingResId);
    if (arrowHeadCasing == null) {
      return;
    }
    Drawable headCasing = DrawableCompat.wrap(arrowHeadCasing);
    DrawableCompat.setTint(headCasing.mutate(), arrowBorderColor);
    Bitmap icon = MapImageUtils.getBitmapFromDrawable(headCasing);
    mapLibreMap.getStyle().addImage(RouteConstants.ARROW_HEAD_ICON_CASING, icon);
  }

  private LineLayer createArrowShaftLayer() {
    LineLayer shaftLayer = (LineLayer) mapLibreMap.getStyle().getLayer(RouteConstants.ARROW_SHAFT_LINE_LAYER_ID);
    if (shaftLayer != null) {
      mapLibreMap.getStyle().removeLayer(shaftLayer);
    }
    return new LineLayer(RouteConstants.ARROW_SHAFT_LINE_LAYER_ID, RouteConstants.ARROW_SHAFT_SOURCE_ID).withProperties(
      PropertyFactory.lineColor(color(arrowColor)),
      PropertyFactory.lineWidth(
        interpolate(linear(), zoom(),
          stop(RouteConstants.MIN_ARROW_ZOOM, RouteConstants.MIN_ZOOM_ARROW_SHAFT_SCALE),
          stop(RouteConstants.MAX_ARROW_ZOOM, RouteConstants.MAX_ZOOM_ARROW_SHAFT_SCALE)
        )
      ),
      PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
      PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
      PropertyFactory.visibility(NONE),
      PropertyFactory.lineOpacity(
        step(zoom(), RouteConstants.OPAQUE,
          stop(
            RouteConstants.ARROW_HIDDEN_ZOOM_LEVEL, RouteConstants.TRANSPARENT
          )
        )
      )
    );
  }

  private LineLayer createArrowShaftCasingLayer() {
    LineLayer shaftCasingLayer = (LineLayer) mapLibreMap.getStyle().getLayer(RouteConstants.ARROW_SHAFT_CASING_LINE_LAYER_ID);
    if (shaftCasingLayer != null) {
      mapLibreMap.getStyle().removeLayer(shaftCasingLayer);
    }
    return new LineLayer(RouteConstants.ARROW_SHAFT_CASING_LINE_LAYER_ID, RouteConstants.ARROW_SHAFT_SOURCE_ID).withProperties(
      PropertyFactory.lineColor(color(arrowBorderColor)),
      PropertyFactory.lineWidth(
        interpolate(linear(), zoom(),
          stop(RouteConstants.MIN_ARROW_ZOOM, RouteConstants.MIN_ZOOM_ARROW_SHAFT_CASING_SCALE),
          stop(RouteConstants.MAX_ARROW_ZOOM, RouteConstants.MAX_ZOOM_ARROW_SHAFT_CASING_SCALE)
        )
      ),
      PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
      PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
      PropertyFactory.visibility(NONE),
      PropertyFactory.lineOpacity(
        step(zoom(), RouteConstants.OPAQUE,
          stop(
            RouteConstants.ARROW_HIDDEN_ZOOM_LEVEL, RouteConstants.TRANSPARENT
          )
        )
      )
    );
  }

  private SymbolLayer createArrowHeadLayer() {
    SymbolLayer headLayer = (SymbolLayer) mapLibreMap.getStyle().getLayer(RouteConstants.ARROW_HEAD_LAYER_ID);
    if (headLayer != null) {
      mapLibreMap.getStyle().removeLayer(headLayer);
    }
    return new SymbolLayer(RouteConstants.ARROW_HEAD_LAYER_ID, RouteConstants.ARROW_HEAD_SOURCE_ID)
      .withProperties(
        PropertyFactory.iconImage(RouteConstants.ARROW_HEAD_ICON),
        iconAllowOverlap(true),
        iconIgnorePlacement(true),
        PropertyFactory.iconSize(interpolate(linear(), zoom(),
          stop(RouteConstants.MIN_ARROW_ZOOM, RouteConstants.MIN_ZOOM_ARROW_HEAD_SCALE),
          stop(RouteConstants.MAX_ARROW_ZOOM, RouteConstants.MAX_ZOOM_ARROW_HEAD_SCALE)
          )
        ),
        PropertyFactory.iconOffset(RouteConstants.ARROW_HEAD_OFFSET),
        PropertyFactory.iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP),
        PropertyFactory.iconRotate(get(RouteConstants.ARROW_BEARING)),
        PropertyFactory.visibility(NONE),
        PropertyFactory.iconOpacity(
          step(zoom(), RouteConstants.OPAQUE,
            stop(
              RouteConstants.ARROW_HIDDEN_ZOOM_LEVEL, RouteConstants.TRANSPARENT
            )
          )
        )
      );
  }

  private SymbolLayer createArrowHeadCasingLayer() {
    SymbolLayer headCasingLayer = (SymbolLayer) mapLibreMap.getStyle().getLayer(RouteConstants.ARROW_HEAD_CASING_LAYER_ID);
    if (headCasingLayer != null) {
      mapLibreMap.getStyle().removeLayer(headCasingLayer);
    }
    return new SymbolLayer(RouteConstants.ARROW_HEAD_CASING_LAYER_ID, RouteConstants.ARROW_HEAD_SOURCE_ID).withProperties(
      PropertyFactory.iconImage(RouteConstants.ARROW_HEAD_ICON_CASING),
      iconAllowOverlap(true),
      iconIgnorePlacement(true),
      PropertyFactory.iconSize(interpolate(
        linear(), zoom(),
        stop(RouteConstants.MIN_ARROW_ZOOM, RouteConstants.MIN_ZOOM_ARROW_HEAD_CASING_SCALE),
        stop(RouteConstants.MAX_ARROW_ZOOM, RouteConstants.MAX_ZOOM_ARROW_HEAD_CASING_SCALE)
      )),
      PropertyFactory.iconOffset(RouteConstants.ARROW_HEAD_CASING_OFFSET),
      PropertyFactory.iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP),
      PropertyFactory.iconRotate(get(RouteConstants.ARROW_BEARING)),
      PropertyFactory.visibility(NONE),
      PropertyFactory.iconOpacity(
        step(zoom(), RouteConstants.OPAQUE,
          stop(
            RouteConstants.ARROW_HIDDEN_ZOOM_LEVEL, RouteConstants.TRANSPARENT
          )
        )
      )
    );
  }

  private void createArrowLayerList(LineLayer shaftLayer, LineLayer shaftCasingLayer, SymbolLayer headLayer,
                                    SymbolLayer headCasingLayer) {
    arrowLayerIds = new ArrayList<>();
    arrowLayerIds.add(shaftCasingLayer.getId());
    arrowLayerIds.add(shaftLayer.getId());
    arrowLayerIds.add(headCasingLayer.getId());
    arrowLayerIds.add(headLayer.getId());
  }
}
