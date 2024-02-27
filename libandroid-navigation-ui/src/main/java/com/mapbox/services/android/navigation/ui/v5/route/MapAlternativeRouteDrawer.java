package com.mapbox.services.android.navigation.ui.v5.route;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_SHIELD_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_SHIELD_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_SOURCE_ID;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.location.Location;
import android.os.SystemClock;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.utils.MapUtils;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfMisc;

import java.util.ArrayList;
import java.util.List;

//TODO: public to be able to change logic?!
//TODO: add as interface?!
//TODO: `drawer` feels wrong, while we update the geo json and don't draw anything
public class MapAlternativeRouteDrawer {

    private Style style;

    private  MapRouteLayerFactory routeLayerFactory;

    @Nullable
    private List<DirectionsRoute> routes;

    MapAlternativeRouteDrawer(Style style, MapRouteLayerFactory routeLayerFactory) {
        this.style = style;
        this.routeLayerFactory = routeLayerFactory;
    }

    void createLayers(float routeScale,
                      @ColorInt int routeColor,
                      @ColorInt int routeShieldColor,
                      String belowLayerId) {
        LineLayer shieldLineLayer = routeLayerFactory.createAlternativeRouteLayer(routeScale, routeShieldColor);
        MapUtils.addLayerToMap(style, shieldLineLayer, belowLayerId);

        LineLayer routeLineLayer = routeLayerFactory.createAlternativeRoutesShieldLayer(routeScale, routeColor);
        MapUtils.addLayerToMap(style, routeLineLayer, belowLayerId);
    }

    void setStyle(Style style) {
        this.style = style;

        if (routes != null) {
            ArrayList<LineString> routeLines = new ArrayList<>();
            for (DirectionsRoute route : routes) {
                String routeGeometry = route.geometry();
                if (routeGeometry != null) {
                    LineString routeLineString = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6);
                    routeLines.add(routeLineString);
                }
            }

            drawRoutes(routeLines);
        }
    }

    void setRoutes(List<DirectionsRoute> routes) {
        this.routes = routes;

        ArrayList<LineString> routeLines = new ArrayList<>();
        for (DirectionsRoute route : routes) {
            String routeGeometry = route.geometry();
            if (routeGeometry != null) {
                LineString routeLineString = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6);
                routeLines.add(routeLineString);
            }
        }

        drawRoutes(routeLines);
    }

    void setVisibility(boolean isVisible) {
        if (style == null || !style.isFullyLoaded()) {
            return;
        }

        Layer shieldLayer = style.getLayer(ALTERNATIVE_ROUTE_SHIELD_LAYER_ID);
        if (shieldLayer != null) {
            shieldLayer.setProperties(visibility(isVisible ? VISIBLE : NONE));
        }

        Layer routeLayer = style.getLayer(ALTERNATIVE_ROUTE_LAYER_ID);
        if (routeLayer != null) {
            routeLayer.setProperties(visibility(isVisible ? VISIBLE : NONE));
        }
    }

    private void drawRoutes(List<LineString> routeLines) {
        if (!style.isFullyLoaded()) {
            // The style is not available anymore. Skip processing.
            return;
        }

        ArrayList<Feature> routeLineFeatures = new ArrayList<>();

        for (LineString routeLine : routeLines) {
            Feature routeLineFeature = Feature.fromGeometry(routeLine);
            routeLineFeatures.add(routeLineFeature);
        }

        getSource(style).setGeoJson(FeatureCollection.fromFeatures(routeLineFeatures));
    }

    private GeoJsonSource getSource(Style style) {
        GeoJsonSource primaryRouteSource = (GeoJsonSource) style.getSource(ALTERNATIVE_ROUTE_SOURCE_ID);
        if (primaryRouteSource == null) {
            primaryRouteSource = new GeoJsonSource(ALTERNATIVE_ROUTE_SOURCE_ID, new GeoJsonOptions().withMaxZoom(16));
            style.addSource(primaryRouteSource);
        }

        return primaryRouteSource;
    }
}
