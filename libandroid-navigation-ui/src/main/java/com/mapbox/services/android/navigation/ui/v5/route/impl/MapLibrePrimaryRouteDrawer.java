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
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
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
import com.mapbox.services.android.navigation.v5.models.LegStep;
import com.mapbox.services.android.navigation.v5.models.RouteLeg;
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

    protected MapView mapView;

    protected Style mapStyle;

    @StyleRes
    private final int styleResId;

    protected boolean isRouteEatingEnabled;

    protected String belowLayerId;

    protected final MapRouteLayerFactory routeLayerFactory;

    @Nullable
    protected DirectionsRoute route;

    /**
     * Route separated into leg LineStrings
     */
    protected List<LineString> legLines;

    /**
     * Route separated into steps, grouped by legs
     */
    protected List<List<LineString>> stepLines;

    protected Point lastLocationPoint;

    protected ValueAnimator valueAnimator;

    protected double minUpdateIntervalNanoSeconds = 1E9 / Integer.MAX_VALUE; // Seconds to nano seconds

    protected long lastUpdateTime = System.nanoTime();

    protected long locationUpdateTimestamp;

    protected boolean isRouteVisible = true;

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

        initStyle(mapView.getContext(), style, styleResId, belowLayerId);
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
        // Clear route, and set new route, only if all other data are updated
        // This prevents the background running `updateRouteProgress` using invalid data
        this.route = null;

        if (route != null) {
            // Draw full route
            drawRoute(route, null, null);

            ArrayList<LineString> legLines = new ArrayList<>();
            ArrayList<List<LineString>> stepLines = new ArrayList<>();
            if (route.legs() != null) {
                for (RouteLeg leg : route.legs()) {
                    ArrayList<Point> legCoordinates = new ArrayList<>();
                    ArrayList<LineString> legSteps = new ArrayList<>();

                    if (leg.steps() == null) {
                        continue;
                    }

                    for (LegStep step : leg.steps()) {
                        String geometry = step.geometry();
                        if (geometry != null) {
                            LineString lineString = LineString.fromPolyline(geometry, Constants.PRECISION_6);
                            legCoordinates.addAll(lineString.coordinates());
                            legSteps.add(lineString);
                        }
                    }

                    stepLines.add(legSteps);
                    legLines.add(LineString.fromLngLats(legCoordinates));
                }
            }

            this.legLines = legLines;
            this.stepLines = stepLines;

            this.route = route;
        }
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

        valueAnimator = ValueAnimator.ofFloat(0f, 1.0f);
        valueAnimator.setInterpolator(new LinearInterpolator());

        Point startPoint = lastLocationPoint;
        Point targetPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());

        // Location not changed, nothing to process
        if (startPoint.equals(targetPoint)) {
            return;
        }

        List<Point> prevStepCoordinates = getPreviousStepCoordinates(routeProgress);
        List<Point> currentStepCoordinates = stepLines.get(routeProgress.legIndex()).get(routeProgress.currentLegProgress().stepIndex()).coordinates();

        ArrayList<Point> currentSegmentCoordinates = new ArrayList<>();
        currentSegmentCoordinates.addAll(prevStepCoordinates);
        currentSegmentCoordinates.addAll(currentStepCoordinates);

        LineString currentSegmentLine = LineString.fromLngLats(currentSegmentCoordinates);
        LineString animatedSegmentLine = TurfMisc.lineSlice(startPoint, targetPoint, currentSegmentLine);
        double segmentLength = TurfMeasurement.length(animatedSegmentLine, TurfConstants.UNIT_METERS);

        valueAnimator.addUpdateListener(animation -> {
            if (System.nanoTime() - lastUpdateTime < minUpdateIntervalNanoSeconds) {
                return;
            }

            if (route == null) {
                return;
            }

            float fraction = animation.getAnimatedFraction();
            Point animatedPoint = TurfMeasurement.along(animatedSegmentLine, segmentLength * fraction, TurfConstants.UNIT_METERS);
            lastLocationPoint = animatedPoint;

            drawRoute(route, animatedPoint, routeProgress);
            lastUpdateTime = System.nanoTime();
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            boolean isCanceled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                isCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isCanceled) {
                    return;
                }

                lastLocationPoint = targetPoint;

                drawRoute(route, lastLocationPoint, routeProgress);
            }
        });

        long previousUpdateTimeStamp = locationUpdateTimestamp;
        locationUpdateTimestamp = SystemClock.elapsedRealtime();

        /* make animation slightly longer (with 1.1f) */
        long duration = (long) ((locationUpdateTimestamp - previousUpdateTimeStamp) * 1.1);
        duration = Math.min(duration, 2000L); //TODO: outsource max duration as field
        valueAnimator.setDuration(duration);
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

        if (location == null || routeProgress == null) {
            // Draw full route
            drawLineStrings(null, LineString.fromPolyline(routeGeometry, Constants.PRECISION_6));
            return;
        }

        List<Point> currentStepCoordinates = stepLines.get(routeProgress.legIndex())
            .get(routeProgress.currentLegProgress().stepIndex())
            .coordinates();

        // Driven path
        ArrayList<Point> drivenLineCoordinates = new ArrayList<>();
        // Draw previous legs
        if (routeProgress.legIndex() > 0) {
            for (int i = 0; i < routeProgress.legIndex(); i++) {
                drivenLineCoordinates.addAll(legLines.get(i).coordinates());
            }
        }

        // Draw previous steps
        if (routeProgress.currentLegProgress().stepIndex() > 0) {
            for (int i = 0; i < routeProgress.currentLegProgress().stepIndex(); i++) {
                drivenLineCoordinates.addAll(stepLines.get(routeProgress.legIndex()).get(i).coordinates());
            }
        }

        // Draw current step
        if (!currentStepCoordinates.get(0).equals(location)) {
            LineString drivenCurrentStepLine = TurfMisc.lineSlice(
                currentStepCoordinates.get(0),
                location,
                LineString.fromLngLats(currentStepCoordinates)
            );

            drivenLineCoordinates.addAll(drivenCurrentStepLine.coordinates());
        }

        // Upcoming path
        ArrayList<Point> firstSegmentCoordinates = new ArrayList<>();
        // Include previous step to have smooth transitions
        firstSegmentCoordinates.addAll(getPreviousStepCoordinates(routeProgress));
        firstSegmentCoordinates.addAll(currentStepCoordinates);

        // Draw current step
        LineString drivenCurrentStepLine;
        Point firstSegmentEndPoint = firstSegmentCoordinates.get(firstSegmentCoordinates.size() - 1);
        if (!location.equals(firstSegmentCoordinates.get(0)) && !location.equals(firstSegmentEndPoint)) {
            drivenCurrentStepLine = TurfMisc.lineSlice(
                location,
                firstSegmentEndPoint,
                LineString.fromLngLats(firstSegmentCoordinates)
            );
        } else {
            drivenCurrentStepLine = LineString.fromLngLats(firstSegmentCoordinates);
        }

        ArrayList<Point> upcomingLineCoordinates = new ArrayList<>(drivenCurrentStepLine.coordinates());

        // Draw upcoming steps
        List<LineString> currentLegSteps = stepLines.get(routeProgress.legIndex());
        for (int i = routeProgress.currentLegProgress().stepIndex() + 1; i < currentLegSteps.size(); i++) {
            upcomingLineCoordinates.addAll(currentLegSteps.get(i).coordinates());
        }

        // Draw upcoming legs
        if (routeProgress.legIndex() < legLines.size()) {
            for (int i = routeProgress.legIndex() + 1; i < legLines.size(); i++) {
                upcomingLineCoordinates.addAll(legLines.get(i).coordinates());
            }
        }

        drawLineStrings(
            LineString.fromLngLats(drivenLineCoordinates),
            LineString.fromLngLats(upcomingLineCoordinates)
        );
    }

    @NonNull
    private List<Point> getPreviousStepCoordinates(@NonNull RouteProgress routeProgress) {
        List<Point> prevStepCoordinates = new ArrayList<>();
        if (routeProgress.currentLegProgress().stepIndex() > 0) {
            List<LineString> steps = stepLines.get(routeProgress.legIndex());
            prevStepCoordinates = steps.get(routeProgress.currentLegProgress().stepIndex() - 1).coordinates();
        } else if (routeProgress.legIndex() > 0) {
            List<LineString> steps = stepLines.get(routeProgress.legIndex() - 1);
            prevStepCoordinates = steps.get(steps.size() - 1).coordinates();
        }

        return prevStepCoordinates;
    }

    private void drawLineStrings(@Nullable LineString drivenLine, LineString upcomingLine) {
        ArrayList<Feature> routeLineFeatures = new ArrayList<>();

        if (drivenLine != null) {
            Feature drivenLineFeature = Feature.fromGeometry(drivenLine, new JsonObject(), "driven-line");
            drivenLineFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, true);
            routeLineFeatures.add(drivenLineFeature);
        }

        Feature upcomingLineFeature = Feature.fromGeometry(upcomingLine, new JsonObject(), "upcoming-line");
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
