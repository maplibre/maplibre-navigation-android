package com.mapbox.services.android.navigation.ui.v5.route;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_SHIELD_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_DESTINATION_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_ORIGIN_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_PROPERTY_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_SOURCE_ID;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.utils.MapUtils;

import java.util.ArrayList;
import java.util.List;

//TODO: public to be able to change logic?!
//TODO: add as interface?!
//TODO: `drawer` feels wrong, while we update the geo json and don't draw anything
public class MapWayPointDrawer {

    private Style style;

    private MapRouteLayerFactory routeLayerFactory;

    @Nullable
    private DirectionsRoute route;

    MapWayPointDrawer(Style style, MapRouteLayerFactory routeLayerFactory) {
        this.style = style;
        this.routeLayerFactory = routeLayerFactory;
    }

    void createLayers(Drawable originIcon, Drawable destinationIcon, String belowLayerId) {
        SymbolLayer wayPointLayer = routeLayerFactory.createWayPointLayer(
                style, originIcon, destinationIcon
        );
        MapUtils.addLayerToMap(style, wayPointLayer, belowLayerId);
    }

    void setStyle(Style style) {
        this.style = style;

        if (route != null) {
            drawWayPoints(route.legs());
        }
    }

    void setRoute(DirectionsRoute route) {
        this.route = route;

        if (route != null) {
            drawWayPoints(route.legs());
        }
    }

    void setVisibility(boolean isVisible) {
        if (style == null || !style.isFullyLoaded()) {
            return;
        }

        Layer wayPointLayer = style.getLayer(WAYPOINT_LAYER_ID);
        if (wayPointLayer != null) {
            wayPointLayer.setProperties(visibility(isVisible ? VISIBLE : NONE));
        }
    }

    private void drawWayPoints(List<RouteLeg> legs) {
        if (!style.isFullyLoaded()) {
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

        getSource(style).setGeoJson(FeatureCollection.fromFeatures(wayPointFeatures));
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
