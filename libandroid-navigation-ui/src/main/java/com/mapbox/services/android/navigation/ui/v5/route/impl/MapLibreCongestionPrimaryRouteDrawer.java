package com.mapbox.services.android.navigation.ui.v5.route.impl;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.CONGESTION_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.HEAVY_CONGESTION_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.MODERATE_CONGESTION_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_CONGESTION_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_CONGESTION_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.SEVERE_CONGESTION_VALUE;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.R;
import com.mapbox.services.android.navigation.ui.v5.route.MapRouteLayerFactory;
import com.mapbox.services.android.navigation.ui.v5.utils.MapUtils;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfMisc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO: check if we also need the processing task
//TODO: clean up
//TODO: fix test

/**
 *
 */
public class MapLibreCongestionPrimaryRouteDrawer extends MapLibrePrimaryRouteDrawer {

    public MapLibreCongestionPrimaryRouteDrawer(MapView mapView, int styleResId, boolean isRouteEatingEnabled, MapRouteLayerFactory routeLayerFactory, @Nullable String belowLayerId) {
        super(mapView, styleResId, isRouteEatingEnabled, routeLayerFactory, belowLayerId);
    }

    /**
     * @noinspection resource
     */
    @Override
    protected void initStyle(Context context, Style mapStyle, @StyleRes int styleResId, @Nullable String belowLayerId) {
        super.initStyle(context, mapStyle, styleResId, belowLayerId);

        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(styleResId, R.styleable.NavigationMapRoute);

            // Primary route
            float routeScale = typedArray.getFloat(R.styleable.NavigationMapRoute_routeScale, 1.0f);
            int routeColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_blue));
            int routeModerateColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeModerateCongestionColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_congestion_moderate));
            int routeHeavyColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeHeavyCongestionColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_congestion_heavy));
            int routeSevereColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeSevereCongestionColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_congestion_severe));
            int drivenRouteColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_color));
            int drivenRouteModerateColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteModerateCongestionColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_congestion_moderate));
            int drivenRouteHeavyColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteHeavyCongestionColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_congestion_heavy));
            int drivenRouteSevereColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteSevereCongestionColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_congestion_severe));

            createLayers(
                    mapStyle,
                    routeScale,
                    routeColor,
                    routeModerateColor,
                    routeHeavyColor,
                    routeSevereColor,
                    drivenRouteColor,
                    drivenRouteModerateColor,
                    drivenRouteHeavyColor,
                    drivenRouteSevereColor,
                    belowLayerId);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    /**
     * Create all needed layers for the primary route.
     * @param mapStyle
     * @param routeScale
     * @param routeColor
     * @param routeModerateColor
     * @param routeHeavyColor
     * @param routeSevereColor
     * @param drivenRouteColor
     * @param drivenRouteModerateColor
     * @param drivenRouteHeavyColor
     * @param drivenRouteSevereColor
     * @param belowLayerId
     */
    private void createLayers(Style mapStyle,
                              float routeScale,
                              @ColorInt int routeColor,
                              @ColorInt int routeModerateColor,
                              @ColorInt int routeHeavyColor,
                              @ColorInt int routeSevereColor,
                              @ColorInt int drivenRouteColor,
                              @ColorInt int drivenRouteModerateColor,
                              @ColorInt int drivenRouteHeavyColor,
                              @ColorInt int drivenRouteSevereColor,
                              String belowLayerId) {
        LineLayer routeLineLayer = routeLayerFactory.createPrimaryRouteCongestion(
                routeScale,
                routeColor,
                routeModerateColor,
                routeHeavyColor,
                routeSevereColor,
                drivenRouteColor,
                drivenRouteModerateColor,
                drivenRouteHeavyColor,
                drivenRouteSevereColor
        );
        MapUtils.addLayerToMap(mapStyle, routeLineLayer, belowLayerId);
    }

    @Override
    public void setVisibility(boolean isVisible) {
        super.setVisibility(isVisible);

        if (mapStyle == null || !mapStyle.isFullyLoaded()) {
            return;
        }

        Layer congestionRouteLayer = mapStyle.getLayer(PRIMARY_ROUTE_CONGESTION_LAYER_ID);
        if (congestionRouteLayer != null) {
            congestionRouteLayer.setProperties(visibility(isVisible ? VISIBLE : NONE));
        }
    }

    @Override
    protected void drawRoute(DirectionsRoute route, @Nullable Point location, @Nullable RouteProgress routeProgress) {
        super.drawRoute(route, location, routeProgress);

        if (route.legs() == null || route.geometry() == null) {
            return;
        }

        LineString routeLineString = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6);
        List<Point> routeCoordinates = routeLineString.coordinates();

        Double drivenRouteMeters = null;
        if (location != null && !location.equals(routeCoordinates.get(0))) {
            LineString drivenRouteLine = TurfMisc.lineSlice(routeCoordinates.get(0), location, routeLineString);
            drivenRouteMeters = TurfMeasurement.length(drivenRouteLine.coordinates(), TurfConstants.UNIT_METERS);
        }

        List<Feature> congestionFeatures = new ArrayList<>();

        int routeLineCoordinatesIndex = 0;
        for (RouteLeg leg : route.legs()) {
            if (leg.annotation() == null || leg.annotation().congestion() == null) {
                continue;
            }

            for (int i = 0; i < leg.annotation().congestion().size(); i++) {
                if (routeLineCoordinatesIndex >= routeCoordinates.size() - 1) {
                    continue;
                }

                List<Feature> segmentFeatures = new ArrayList<>();

                Point segmentStartPoint = routeCoordinates.get(routeLineCoordinatesIndex);
                Point segmentEndPoint = routeCoordinates.get(routeLineCoordinatesIndex + 1);
                double segmentStartDistance = TurfMeasurement.length(routeCoordinates.subList(0, routeLineCoordinatesIndex + 1), TurfConstants.UNIT_METERS);
                double segmentEndDistance = TurfMeasurement.length(routeCoordinates.subList(0, routeLineCoordinatesIndex + 2), TurfConstants.UNIT_METERS);

                if (drivenRouteMeters != null && segmentEndDistance > drivenRouteMeters && segmentStartDistance < drivenRouteMeters) {
                    // Current passing segment
                    LineString drivenCongestionLineString = LineString.fromLngLats(Arrays.asList(segmentStartPoint, location));
                    Feature drivenFeature = Feature.fromGeometry(drivenCongestionLineString);

                    LineString upcomingCongestionLineString = LineString.fromLngLats(Arrays.asList(location, segmentEndPoint));
                    Feature upcomingFeature = Feature.fromGeometry(upcomingCongestionLineString);

                    String congestionValue = leg.annotation().congestion().get(i);
                    switch (congestionValue) {
                        case MODERATE_CONGESTION_VALUE:
                        case HEAVY_CONGESTION_VALUE:
                        case SEVERE_CONGESTION_VALUE:
                        case "low":
                            drivenFeature.addStringProperty(CONGESTION_KEY, congestionValue);
                            drivenFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, true);
                            segmentFeatures.add(drivenFeature);

                            upcomingFeature.addStringProperty(CONGESTION_KEY, congestionValue);
                            upcomingFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, false);
                            segmentFeatures.add(upcomingFeature);
                    }
                } else {
                    // Driven or upcoming route
                    LineString congestionLineString = LineString.fromLngLats(Arrays.asList(segmentStartPoint, segmentEndPoint));
                    Feature feature = Feature.fromGeometry(congestionLineString);
                    String congestionValue = leg.annotation().congestion().get(i);
                    switch (congestionValue) {
                        case MODERATE_CONGESTION_VALUE:
                        case HEAVY_CONGESTION_VALUE:
                        case SEVERE_CONGESTION_VALUE:
                        case "low":
                            feature.addStringProperty(CONGESTION_KEY, congestionValue);
                            feature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, drivenRouteMeters != null && segmentEndDistance < drivenRouteMeters);
                            segmentFeatures.add(feature);
                    }
                }

                congestionFeatures.addAll(segmentFeatures);
                routeLineCoordinatesIndex++;
            }
        }

        drawFeatures(congestionFeatures);
    }

    private void drawFeatures(List<Feature> features) {
        if (!mapStyle.isFullyLoaded()) {
            // The style is not available anymore. Skip processing.
            return;
        }

        getSource(mapStyle).setGeoJson(FeatureCollection.fromFeatures(features));
    }

    private GeoJsonSource getSource(Style style) {
        GeoJsonSource primaryRouteSource = (GeoJsonSource) style.getSource(PRIMARY_ROUTE_CONGESTION_SOURCE_ID);
        if (primaryRouteSource == null) {
            primaryRouteSource = new GeoJsonSource(PRIMARY_ROUTE_CONGESTION_SOURCE_ID, new GeoJsonOptions().withMaxZoom(16));
            style.addSource(primaryRouteSource);
        }

        return primaryRouteSource;
    }
}
