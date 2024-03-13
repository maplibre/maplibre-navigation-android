package com.mapbox.services.android.navigation.ui.v5.route.impl;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_SHIELD_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_SOURCE_ID;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.SystemClock;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
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
import com.mapbox.services.android.navigation.ui.v5.route.PrimaryRouteDrawer;
import com.mapbox.services.android.navigation.ui.v5.utils.MapUtils;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfMisc;

import java.util.ArrayList;
import java.util.List;

//TODO: is route-eating the correct term?
//TODO: check if we also need the processing task
//TODO: clean up
//TODO: fix test
public class MapLibrePrimaryRouteDrawer implements PrimaryRouteDrawer {

    protected Style mapStyle;

    @StyleRes
    private final int styleResId;

    protected boolean isRouteEatingEnabled;

    protected final MapRouteLayerFactory routeLayerFactory;

    @Nullable
    protected DirectionsRoute route;

    protected Point lastLocationPoint;

    protected ValueAnimator valueAnimator;

    protected double minUpdateIntervalNanoSeconds = 1E9 / Integer.MAX_VALUE; // Seconds to nano seconds

    protected long lastUpdateTime = System.nanoTime();

    protected long locationUpdateTimestamp;

    protected boolean isRouteVisible = true;

    //    MapPrimaryRouteDrawer(Style mapStyle, boolean isRouteEatingEnabled, MapRouteLayerFactory routeLayerFactory) {
    public MapLibrePrimaryRouteDrawer(MapView mapView, @StyleRes int styleResId, boolean isRouteEatingEnabled, MapRouteLayerFactory routeLayerFactory, @Nullable String belowLayerId) {
        this.styleResId = styleResId;
        this.isRouteEatingEnabled = isRouteEatingEnabled;
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

            // Primary route
            float routeScale = typedArray.getFloat(R.styleable.NavigationMapRoute_routeScale, 1.0f);
            int routeColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_blue));
            int routeShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeShieldColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_shield_layer_color));
            int drivenRouteColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_color));
            int drivenRouteShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteShieldColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_shield_color));

            createLayers(mapStyle, routeScale, routeColor, routeShieldColor, drivenRouteColor, drivenRouteShieldColor, belowLayerId);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    private void createLayers(
            Style mapStyle,
            float routeScale,
            @ColorInt int routeColor,
            @ColorInt int routeShieldColor,
            @ColorInt int drivenRouteColor,
            @ColorInt int drivenRouteShieldColor,
            String belowLayerId) {
        LineLayer shieldLineLayer = routeLayerFactory.createPrimaryRouteShieldLayer(routeScale, routeShieldColor, drivenRouteShieldColor);
        MapUtils.addLayerToMap(mapStyle, shieldLineLayer, belowLayerId);

        LineLayer routeLineLayer = routeLayerFactory.createPrimaryRouteLayer(routeScale, routeColor, drivenRouteColor);
        MapUtils.addLayerToMap(mapStyle, routeLineLayer, belowLayerId);
    }

    @Override
    public void setStyle(Style style) {
        this.mapStyle = style;

        if (route != null) {
            drawRoute(route);
        }
    }

    @Override
    public void setRouteEatingEnabled(boolean isRouteEatingEnabled) {
        this.isRouteEatingEnabled = isRouteEatingEnabled;
    }

    @Override
    public void setRoute(DirectionsRoute route) {
        this.route = route;

        drawRoute(route);
    }

    @Override
    public void setVisibility(boolean isVisible) {
        isRouteVisible = isVisible;

        if (mapStyle == null || !mapStyle.isFullyLoaded()) {
            return;
        }

        Layer shieldLayer = mapStyle.getLayer(PRIMARY_ROUTE_SHIELD_LAYER_ID);
        if (shieldLayer != null) {
            shieldLayer.setProperties(visibility(isVisible ? VISIBLE : NONE));
        }

        Layer routeLayer = mapStyle.getLayer(PRIMARY_ROUTE_LAYER_ID);
        if (routeLayer != null) {
            routeLayer.setProperties(visibility(isVisible ? VISIBLE : NONE));
        }
    }

    public void setMaxAnimationFps(int maxAnimationFps) {
        this.minUpdateIntervalNanoSeconds = 1E9 / maxAnimationFps;
    }

    @Override
    public void updateRouteProgress(Location location, RouteProgress routeProgress) {
        if (!isRouteEatingEnabled) {
            // Route eating disabled, route will only drawn once. Skip progress updating.
            return;
        }

        if (!isRouteVisible) {
            return;
        }

        if (lastLocationPoint == null) {
            lastLocationPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            return;
        }

        if (route == null) {
            return;
        }

        String routeGeometry = route.geometry();
        if (routeGeometry == null) {
            //TODO: when this can happen?!
            return;
        }

        if (valueAnimator != null) {
            valueAnimator.cancel();
        }

        /* make animation slightly longer (with 1.1f) */
        valueAnimator = ValueAnimator.ofFloat(0f, 1.1f);

        Point startPoint = lastLocationPoint;
        Location target = location;
        valueAnimator.addUpdateListener(animation -> {
            if (System.nanoTime() - lastUpdateTime < minUpdateIntervalNanoSeconds) {
                return;
            }

            if (route == null) {
                return;
            }

            if (routeGeometry == null) {
                return;
            }

            float fraction = animation.getAnimatedFraction();
            LineString routeLine = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6);

            Point animatedPoint = Point.fromLngLat(
                    startPoint.longitude() + fraction * (target.getLongitude() - startPoint.longitude()),
                    startPoint.latitude() + fraction * (target.getLatitude() - startPoint.latitude())
            );

            lastLocationPoint = animatedPoint;

            drawRoute(route, animatedPoint, routeProgress);
            lastUpdateTime = System.nanoTime();
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (route == null) {
                    return;
                }

                lastLocationPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                drawRoute(route, lastLocationPoint, routeProgress);
            }
        });

        long previousUpdateTimeStamp = locationUpdateTimestamp;
        locationUpdateTimestamp = SystemClock.elapsedRealtime();

        valueAnimator.setDuration((long) ((locationUpdateTimestamp - previousUpdateTimeStamp) * 1.1)); // 1.1 second
        valueAnimator.start();
    }

    protected void drawRoute(DirectionsRoute route) {
        drawRoute(route, null, null);
    }

    protected void drawRoute(DirectionsRoute route, @Nullable Point location, @Nullable RouteProgress routeProgress) {
        String routeGeometry = route.geometry();
        if (routeGeometry == null) {
            return;
        }

        LineString routeLine = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6);
        Point routeLineStartPoint = TurfMeasurement.along(routeLine, 0, TurfConstants.UNIT_METERS);
        if (location != null && !routeLineStartPoint.equals(location)) {
            // Route eating enabled and position data is available
            Point routeLineEndPoint = TurfMeasurement.along(routeLine, route.distance(), TurfConstants.UNIT_METERS);

            LineString drivenLine = TurfMisc.lineSlice(routeLineStartPoint, location, routeLine);
            LineString upcomingLine = TurfMisc.lineSlice(location, routeLineEndPoint, routeLine);
            drawLineStrings(drivenLine, upcomingLine);
        } else {
            // Route eating disabled or position data is missing
            drawLineStrings(null, routeLine);
        }
    }

    private void drawLineStrings(LineString drivenLine, LineString upcomingLine) {
        ArrayList<Feature> routeLineFeatures = new ArrayList<>();

        if (drivenLine != null) {
            Feature drivenLineFeature = Feature.fromGeometry(drivenLine);
            drivenLineFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, true);
            routeLineFeatures.add(drivenLineFeature);
        }

        Feature upcomingLineFeature = Feature.fromGeometry(upcomingLine);
        upcomingLineFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, false);
        routeLineFeatures.add(upcomingLineFeature);

        drawFeatures(routeLineFeatures);
    }

    private void drawFeatures(List<Feature> features) {
        if (!mapStyle.isFullyLoaded()) {
            // The style is not available anymore. Skip processing.
            return;
        }

        getSource(mapStyle).setGeoJson(FeatureCollection.fromFeatures(features));
    }

    private GeoJsonSource getSource(Style style) {
        GeoJsonSource primaryRouteSource = (GeoJsonSource) style.getSource(PRIMARY_ROUTE_SOURCE_ID);
        if (primaryRouteSource == null) {
            primaryRouteSource = new GeoJsonSource(PRIMARY_ROUTE_SOURCE_ID, new GeoJsonOptions().withMaxZoom(16));
            style.addSource(primaryRouteSource);
        }

        return primaryRouteSource;
    }
}
