package com.mapbox.services.android.navigation.ui.v5.route.impl;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_CONGESTION_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_SHIELD_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.CONGESTION_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.HEAVY_CONGESTION_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.MODERATE_CONGESTION_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_CONGESTION_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.SEVERE_CONGESTION_VALUE;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.models.RouteLeg;
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
import com.mapbox.services.android.navigation.ui.v5.route.AlternativeRouteDrawer;
import com.mapbox.services.android.navigation.ui.v5.route.MapRouteLayerFactory;
import com.mapbox.services.android.navigation.ui.v5.utils.MapUtils;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfMisc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//TODO: check if we also need the processing task
//TODO: clean up
//TODO: fix test
public class MapLibreCongestionAlternativeRouteDrawer extends MapLibreAlternativeRouteDrawer {

    public MapLibreCongestionAlternativeRouteDrawer(MapView mapView, int styleResId, MapRouteLayerFactory routeLayerFactory, @Nullable String belowLayerId) {
        super(mapView, styleResId, routeLayerFactory, belowLayerId);
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

            float alternativeRouteScale = typedArray.getFloat(R.styleable.NavigationMapRoute_alternativeRouteScale, 1.0f);
            int alternativeRouteColor = typedArray.getColor(R.styleable.NavigationMapRoute_alternativeRouteColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_alternative_color));
            int alternativeRouteShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_alternativeRouteShieldColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_alternative_shield_color));
            int alternativeRouteModerateColor = typedArray.getColor(R.styleable.NavigationMapRoute_alternativeRouteModerateCongestionColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_alternative_route_driven_congestion_moderate));
            int alternativeRouteHeavyColor = typedArray.getColor(R.styleable.NavigationMapRoute_alternativeRouteModerateCongestionColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_alternative_route_driven_congestion_heavy));
            int alternativeRouteSevereColor = typedArray.getColor(R.styleable.NavigationMapRoute_alternativeRouteSevereCongestionColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_alternative_route_driven_congestion_severe));

            createLayers(
                    mapStyle,
                    alternativeRouteScale,
                    alternativeRouteColor,
                    alternativeRouteModerateColor,
                    alternativeRouteHeavyColor,
                    alternativeRouteSevereColor,
                    belowLayerId);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    private void createLayers(Style mapStyle,
                              float routeScale,
                              @ColorInt int routeColor,
                              @ColorInt int routeModerateColor,
                              @ColorInt int routeHeavyColor,
                              @ColorInt int routeSevereColor,
                              String belowLayerId) {
        LineLayer routeLineLayer = routeLayerFactory.createAlternativeRouteCongestion(
                routeScale,
                routeColor,
                routeModerateColor,
                routeHeavyColor,
                routeSevereColor
        );
        MapUtils.addLayerToMap(mapStyle, routeLineLayer, belowLayerId);
    }

    @Override
    protected void drawRoutes(List<DirectionsRoute> routes) {
        super.drawRoutes(routes);

        if (routes == null || routes.isEmpty()) {
            return;
        }

        ArrayList<Feature> congestionFeatures = new ArrayList<>();
        for (DirectionsRoute route : routes) {
            congestionFeatures.addAll(createRouteFeatures(route));
        }

        drawFeatures(congestionFeatures);
    }

    private List<Feature> createRouteFeatures(DirectionsRoute route) {
        if (route.legs() == null || route.geometry() == null) {
            return Collections.emptyList();
        }

        LineString routeLineString = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6);
        List<Point> routeCoordinates = routeLineString.coordinates();

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

                Point segmentStartPoint = routeCoordinates.get(routeLineCoordinatesIndex);
                Point segmentEndPoint = routeCoordinates.get(routeLineCoordinatesIndex + 1);

                LineString congestionLineString = LineString.fromLngLats(Arrays.asList(segmentStartPoint, segmentEndPoint));
                Feature segmentFeature = Feature.fromGeometry(congestionLineString);
                String congestionValue = leg.annotation().congestion().get(i);
                switch (congestionValue) {
                    default:
                    case MODERATE_CONGESTION_VALUE:
                    case HEAVY_CONGESTION_VALUE:
                    case SEVERE_CONGESTION_VALUE:
                    case "low":
                        segmentFeature.addStringProperty(CONGESTION_KEY, congestionValue);
                }

                congestionFeatures.add(segmentFeature);
                routeLineCoordinatesIndex++;
            }
        }

        return congestionFeatures;
    }

    private void drawFeatures(List<Feature> features) {
        if (!mapStyle.isFullyLoaded()) {
            // The style is not available anymore. Skip processing.
            return;
        }

        getSource(mapStyle).setGeoJson(FeatureCollection.fromFeatures(features));
    }

    private GeoJsonSource getSource(Style style) {
        GeoJsonSource source = (GeoJsonSource) style.getSource(ALTERNATIVE_ROUTE_CONGESTION_SOURCE_ID);
        if (source == null) {
            source = new GeoJsonSource(ALTERNATIVE_ROUTE_CONGESTION_SOURCE_ID, new GeoJsonOptions().withMaxZoom(16));
            style.addSource(source);
        }

        return source;
    }
}
