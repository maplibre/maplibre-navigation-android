package com.mapbox.services.android.navigation.ui.v5.route.impl;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_DESTINATION_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_ORIGIN_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_PROPERTY_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_SOURCE_ID;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.models.LegStep;
import com.mapbox.services.android.navigation.v5.models.RouteLeg;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.R;
import com.mapbox.services.android.navigation.ui.v5.route.MapRouteLayerFactory;
import com.mapbox.services.android.navigation.ui.v5.route.WayPointDrawer;
import com.mapbox.services.android.navigation.ui.v5.utils.MapUtils;

import java.util.ArrayList;
import java.util.List;

//TODO: check if we also need the processing task
//TODO: clean up
//TODO: fix test
public class MapLibreWayPointDrawer implements WayPointDrawer {

    protected Style mapStyle;

    @StyleRes
    private final int styleResId;

    private MapRouteLayerFactory routeLayerFactory;

    @Nullable
    private DirectionsRoute route;


    public MapLibreWayPointDrawer(MapView mapView, @StyleRes int styleResId, MapRouteLayerFactory routeLayerFactory, @Nullable String belowLayerId) {
        this.styleResId = styleResId;
        this.routeLayerFactory = routeLayerFactory;
        mapView.getMapAsync(mapboxMap -> {
            mapStyle = mapboxMap.getStyle();
            //TODO: check for style availability and wait with callback for loading when not ready yet
            initStyle(mapView.getContext(), mapStyle, styleResId, belowLayerId);
        });
    }

    /**
     * @noinspection resource
     */
    protected void initStyle(Context context, Style mapStyle, @StyleRes int styleResId, @Nullable String belowLayerId) {
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(styleResId, R.styleable.NavigationMapRoute);

            int originWaypointIcon = typedArray.getResourceId(
                    R.styleable.NavigationMapRoute_originWaypointIcon, R.drawable.ic_route_origin);
            int destinationWaypointIcon = typedArray.getResourceId(
                    R.styleable.NavigationMapRoute_destinationWaypointIcon, R.drawable.ic_route_destination);

            createLayers(
                    mapStyle,
                    ContextCompat.getDrawable(context, originWaypointIcon),
                    ContextCompat.getDrawable(context, destinationWaypointIcon),
                    belowLayerId
            );
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    void createLayers(Style mapStyle, Drawable originIcon, Drawable destinationIcon, String belowLayerId) {
        SymbolLayer wayPointLayer = routeLayerFactory.createWayPointLayer(
                mapStyle, originIcon, destinationIcon
        );
        MapUtils.addLayerToMap(mapStyle, wayPointLayer, belowLayerId);
    }

    public void setStyle(Style style) {
        this.mapStyle = style;

        if (route != null) {
            drawWayPoints(route.legs());
        }
    }

    public void setRoute(DirectionsRoute route) {
        this.route = route;

        if (route != null) {
            drawWayPoints(route.legs());
        }
    }

    @Override
    public void setVisibility(boolean isVisible) {
        if (mapStyle == null || !mapStyle.isFullyLoaded()) {
            return;
        }

        Layer wayPointLayer = mapStyle.getLayer(WAYPOINT_LAYER_ID);
        if (wayPointLayer != null) {
            wayPointLayer.setProperties(visibility(isVisible ? VISIBLE : NONE));
        }
    }

    private void drawWayPoints(List<RouteLeg> legs) {
        if (!mapStyle.isFullyLoaded()) {
            // The style is not available anymore. Skip processing.
            return;
        }

        final List<Feature> wayPointFeatures = new ArrayList<>();
        for (RouteLeg leg : legs) {
            Feature originFeature = createWayPointFeature(leg, 0);
            if (originFeature != null) {
                wayPointFeatures.add(originFeature);
            }

            if (leg.steps() != null) {
                Feature destinationFeature = createWayPointFeature(leg, leg.steps().size() - 1);
                if (destinationFeature != null) {
                    wayPointFeatures.add(destinationFeature);
                }
            }
        }

        getSource(mapStyle).setGeoJson(FeatureCollection.fromFeatures(wayPointFeatures));
    }

    @Nullable
    private Feature createWayPointFeature(RouteLeg leg, int index) {
        if (leg.steps() == null) {
            return null;
        }

        LegStep step = leg.steps().get(index);

        if (step == null) {
            return null;
        }

        Feature feature = Feature.fromGeometry(
                Point.fromLngLat(
                        step.maneuver().location().longitude(),
                        step.maneuver().location().latitude()
                )
        );
        feature.addStringProperty(WAYPOINT_PROPERTY_KEY, index == 0 ? WAYPOINT_ORIGIN_VALUE : WAYPOINT_DESTINATION_VALUE);

        return feature;
    }

    private GeoJsonSource getSource(Style style) {
        GeoJsonSource primaryRouteSource = (GeoJsonSource) style.getSource(WAYPOINT_SOURCE_ID);
        if (primaryRouteSource == null) {
            primaryRouteSource = new GeoJsonSource(WAYPOINT_SOURCE_ID, new GeoJsonOptions().withMaxZoom(16));
            style.addSource(primaryRouteSource);
        }

        return primaryRouteSource;
    }
}
