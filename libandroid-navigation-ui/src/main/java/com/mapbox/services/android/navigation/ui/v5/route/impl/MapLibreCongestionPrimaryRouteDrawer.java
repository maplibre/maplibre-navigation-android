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
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.SEVERE_CONGESTION_VALUE;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.mapbox.geojson.MultiLineString;
import com.mapbox.services.android.navigation.v5.models.Congestion;
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.models.LegAnnotation;
import com.mapbox.services.android.navigation.v5.models.LegStep;
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
import com.mapbox.services.android.navigation.ui.v5.route.MapRouteLayerFactory;
import com.mapbox.services.android.navigation.ui.v5.utils.MapUtils;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfMeta;
import com.mapbox.turf.TurfMisc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//TODO: clean up
//   - reduce duplicated code
//   - reduce & improve loop performance
//   - ...
//TODO: fix test
//TODO: documentation

/**
 *
 */
public class MapLibreCongestionPrimaryRouteDrawer extends MapLibrePrimaryRouteDrawer {
    /**
     * Route separated into steps, grouped by legs
     */
    private List<List<CongestionStep>> congestionSteps;

    public MapLibreCongestionPrimaryRouteDrawer(MapView mapView, int styleResId, boolean isRouteEatingEnabled, MapRouteLayerFactory routeLayerFactory, @Nullable String belowLayerId) {
        super(mapView, styleResId, isRouteEatingEnabled, routeLayerFactory, belowLayerId);
    }

    /**
     * @noinspection resource
     */
    @Override
    protected void initStyle(Context context, Style mapStyle, @StyleRes int styleResId, @Nullable String belowLayerId) {
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
            int routeShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeShieldColor,
                ContextCompat.getColor(context, R.color.mapbox_navigation_route_shield_layer_color));
            int drivenRouteShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteShieldColor,
                ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_shield_color));

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
                routeShieldColor,
                drivenRouteShieldColor,
                belowLayerId);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    /**
     * Create all needed layers for the primary route.
     *
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
                              @ColorInt int routeShieldColor,
                              @ColorInt int drivenRouteShieldColor,
                              String belowLayerId) {
        LineLayer shieldLineLayer = routeLayerFactory.createPrimaryRouteShieldLayer(routeScale, routeShieldColor, drivenRouteShieldColor);
        MapUtils.addLayerToMap(mapStyle, shieldLineLayer, belowLayerId);

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
    public void setRoute(DirectionsRoute route) {
        // Clear route, and set new route, only if all other data are updated
        // This prevents the background running `updateRouteProgress` using invalid data
        this.route = null;

        if (route != null) {
            // Draw full route
            drawRoute(route, null, null);

            if (route.legs() != null) {
                ArrayList<List<CongestionStep>> legCongestionSteps = new ArrayList<>();

                for (RouteLeg leg : route.legs()) {

                    if (leg.steps() == null) {
                        continue;
                    }

                    LegAnnotation annotation = leg.annotation();
                    if (annotation != null && annotation.distance() != null) {
                        int stepIndex = 0;
                        double processedStepDistanceMeters = 0;

                        ArrayList<CongestionStep> congestionSteps = new ArrayList<>();
                        ArrayList<CongestionStep.CongestionSegment> congestionSegments = new ArrayList<>();
                        for (int i = 0; i < annotation.distance().size(); i++) {
                            double annotationDistance = annotation.distance().get(i);
                            double remainingAnnotationDistance = annotationDistance;

                            while (remainingAnnotationDistance > 0) {
                                if (stepIndex >= leg.steps().size()) {
                                    remainingAnnotationDistance = 0;
                                    continue;
                                }

                                LegStep step = leg.steps().get(stepIndex);
                                LineString stepLine = LineString.fromPolyline(step.geometry(), Constants.PRECISION_6);
                                double stepLengthMeters = TurfMeasurement.length(stepLine, TurfConstants.UNIT_METERS);

                                if (stepLengthMeters == 0) {
                                    congestionSteps.add(new CongestionStep(congestionSegments));
                                    congestionSegments = new ArrayList<>();

                                    processedStepDistanceMeters = 0;

                                    stepIndex++;
                                    continue;
                                }

                                double startDistance = processedStepDistanceMeters;

                                double distance = remainingAnnotationDistance;
                                if (startDistance + distance > stepLengthMeters) {
                                    distance = stepLengthMeters - startDistance;
                                }

                                remainingAnnotationDistance = remainingAnnotationDistance - distance;

                                LineString segmentLineString = TurfMisc.lineSliceAlong(stepLine, startDistance, startDistance + distance, TurfConstants.UNIT_METERS);
                                congestionSegments.add(new CongestionStep.CongestionSegment(segmentLineString, distance, annotation.congestion().get(i)));

                                if (remainingAnnotationDistance > 0) {
                                    congestionSteps.add(new CongestionStep(congestionSegments));
                                    congestionSegments = new ArrayList<>();

                                    processedStepDistanceMeters = 0;

                                    stepIndex++;
                                } else {
                                    processedStepDistanceMeters += distance;
                                }
                            }
                        }

                        legCongestionSteps.add(congestionSteps);
                    } else {
                        //TODO: no congestion available
                    }
                }

                this.congestionSteps = legCongestionSteps;
            }
        }

        super.setRoute(route);
    }

    @Override
    protected void drawRoute(DirectionsRoute route, @Nullable Point location, @Nullable RouteProgress routeProgress) {
        if (congestionSteps == null) {
            return;
        }

        if (location == null || routeProgress == null) {
            // Draw full route
            ArrayList<CongestionStep.CongestionSegment> upcomingLine = new ArrayList<>();

            List<List<CongestionStep>> congestionSteps = this.congestionSteps;
            for (int i = 0; i < congestionSteps.size(); i++) {
                List<CongestionStep> legSteps = congestionSteps.get(i);
                for (CongestionStep congestionStep : legSteps) {
                    upcomingLine.addAll(congestionStep.congestionSegments);
                }
            }

            drawLineStrings(null, upcomingLine);
            return;
        }

        // Driven path
        ArrayList<CongestionStep.CongestionSegment> drivenSegments = new ArrayList<>();

        // Draw previous legs
        if (routeProgress.legIndex() > 0) {
            for (int i = 0; i < routeProgress.legIndex(); i++) {
                for (CongestionStep previousCongestionStep : congestionSteps.get(i)) {
                    drivenSegments.addAll(previousCongestionStep.congestionSegments);
                }
            }
        }

        // Draw previous steps
        if (routeProgress.currentLegProgress().stepIndex() > 0) {
            for (int i = 0; i < routeProgress.currentLegProgress().stepIndex(); i++) {
                CongestionStep previousCongestionStep = congestionSteps.get(routeProgress.legIndex()).get(i);
                drivenSegments.addAll(previousCongestionStep.congestionSegments);
            }
        }

        // Draw current driven step
        LineString currentStepLine = stepLines.get(routeProgress.legIndex()).get(routeProgress.currentLegProgress().stepIndex());
        double drivenStepLength = 0;
        if (!location.equals(currentStepLine.coordinates().get(0))) {
            LineString drivenCurrentStepLine = TurfMisc.lineSlice(
                currentStepLine.coordinates().get(0),
                location,
                currentStepLine
            );
            drivenStepLength = TurfMeasurement.length(drivenCurrentStepLine, "meters");
        }

        CongestionStep congestionStep = congestionSteps.get(routeProgress.legIndex()).get(routeProgress.currentLegProgress().stepIndex());
        double traveled = 0;
        int segmentIndex = 0;

        ArrayList<CongestionStep.CongestionSegment> drivenCurrentSegments = new ArrayList<>();
        while (traveled < drivenStepLength) {
            CongestionStep.CongestionSegment segment = congestionStep.congestionSegments.get(segmentIndex);
            drivenCurrentSegments.add(segment);

            traveled += segment.segmentLengthMeters;

            segmentIndex++;
        }

        CongestionStep.CongestionSegment firstUpcomingSegment = null;
        if (!drivenCurrentSegments.isEmpty()) {
            CongestionStep.CongestionSegment lastSegment = drivenCurrentSegments.get(drivenCurrentSegments.size() - 1);
            //TODO: previous step should also included to have a smooth transition
            if (!location.equals(lastSegment.lineString.coordinates().get(0))) {
                LineString lastDrivenSegmentLine = TurfMisc.lineSlice(
                    lastSegment.lineString.coordinates().get(0),
                    location,
                    lastSegment.lineString
                );
                drivenCurrentSegments.set(drivenCurrentSegments.size() - 1, new CongestionStep.CongestionSegment(lastDrivenSegmentLine, TurfMeasurement.length(lastDrivenSegmentLine, "meters"), lastSegment.getCongestionValue()));

                LineString firstUpcomingSegmentLine = TurfMisc.lineSlice(
                    location,
                    lastSegment.lineString.coordinates().get(lastSegment.lineString.coordinates().size() - 1),
                    lastSegment.lineString
                );
                firstUpcomingSegment = new CongestionStep.CongestionSegment(firstUpcomingSegmentLine, TurfMeasurement.length(firstUpcomingSegmentLine, "meters"), lastSegment.getCongestionValue());
            } else {
                drivenCurrentSegments.remove(drivenCurrentSegments.size() - 1);
            }
        }

        drivenSegments.addAll(drivenCurrentSegments);

        // Upcoming path
        ArrayList<CongestionStep.CongestionSegment> upcomingSegments = new ArrayList<>();

        if (firstUpcomingSegment != null) {
            upcomingSegments.add(firstUpcomingSegment);
        }

        for (int i = segmentIndex; i < congestionStep.congestionSegments.size(); i++) {
            upcomingSegments.add(congestionStep.congestionSegments.get(i));
        }

        // Draw upcoming steps
        if (routeProgress.currentLegProgress().stepIndex() + 1 < congestionSteps.get(routeProgress.legIndex()).size()) {
            for (int i = routeProgress.currentLegProgress().stepIndex() + 1; i < congestionSteps.get(routeProgress.legIndex()).size(); i++) {
                CongestionStep upcomingCongestionStep = congestionSteps.get(routeProgress.legIndex()).get(i);
                upcomingSegments.addAll(upcomingCongestionStep.congestionSegments);
            }
        }

        // Draw upcoming legs
        if (routeProgress.legIndex() + 1 < congestionSteps.size()) {
            for (int i = routeProgress.legIndex() + 1; i < congestionSteps.size(); i++) {
                for (CongestionStep upcomingCongestionStep : congestionSteps.get(i)) {
                    drivenSegments.addAll(upcomingCongestionStep.congestionSegments);
                }
            }
        }

        drawLineStrings(
            drivenSegments,
            upcomingSegments
        );
    }

    private void drawLineStrings(@Nullable List<CongestionStep.CongestionSegment> drivenLine, List<CongestionStep.CongestionSegment> upcomingLine) {
        ArrayList<Feature> routeLineFeatures = new ArrayList<>();

        // Driven line
        if (drivenLine != null) {
            List<LineString> defaultDrivenLineStrings = new ArrayList<>();
            List<LineString> lowDrivenLineStrings = new ArrayList<>();
            List<LineString> moderateDrivenLineStrings = new ArrayList<>();
            List<LineString> heavyDrivenLineStrings = new ArrayList<>();
            List<LineString> severeDrivenLineStrings = new ArrayList<>();

            for (CongestionStep.CongestionSegment congestionLineString : drivenLine) {
                if (congestionLineString.getCongestionValue() != null) {
                    switch (congestionLineString.getCongestionValue()) {
                        case SEVERE_CONGESTION_VALUE:
                            severeDrivenLineStrings.add(congestionLineString.getLineString());
                            break;
                        case HEAVY_CONGESTION_VALUE:
                            heavyDrivenLineStrings.add(congestionLineString.getLineString());
                            break;
                        case MODERATE_CONGESTION_VALUE:
                            moderateDrivenLineStrings.add(congestionLineString.getLineString());
                            break;
                        case "low":
                            lowDrivenLineStrings.add(congestionLineString.getLineString());
                            break;
                        default:
                            defaultDrivenLineStrings.add(congestionLineString.getLineString());
                    }
                } else {
                    defaultDrivenLineStrings.add(congestionLineString.getLineString());
                }
            }

            MultiLineString defaultMultiLineString = MultiLineString.fromLineStrings(defaultDrivenLineStrings);
            Feature defaultFeature = Feature.fromGeometry(defaultMultiLineString);
            defaultFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, true);
            routeLineFeatures.add(defaultFeature);

            MultiLineString lowMultiLineString = MultiLineString.fromLineStrings(lowDrivenLineStrings);
            Feature lowFeature = Feature.fromGeometry(lowMultiLineString);
            lowFeature.addStringProperty(CONGESTION_KEY, "low");
            lowFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, true);
            routeLineFeatures.add(lowFeature);

            MultiLineString moderateMultiLineString = MultiLineString.fromLineStrings(moderateDrivenLineStrings);
            Feature moderateFeature = Feature.fromGeometry(moderateMultiLineString);
            moderateFeature.addStringProperty(CONGESTION_KEY, MODERATE_CONGESTION_VALUE);
            moderateFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, true);
            routeLineFeatures.add(moderateFeature);

            MultiLineString heavyMultiLineString = MultiLineString.fromLineStrings(heavyDrivenLineStrings);
            Feature heavyfeature = Feature.fromGeometry(heavyMultiLineString);
            heavyfeature.addStringProperty(CONGESTION_KEY, HEAVY_CONGESTION_VALUE);
            heavyfeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, true);
            routeLineFeatures.add(heavyfeature);

            MultiLineString severeMultiLineString = MultiLineString.fromLineStrings(severeDrivenLineStrings);
            Feature severeFeature = Feature.fromGeometry(severeMultiLineString);
            severeFeature.addStringProperty(CONGESTION_KEY, SEVERE_CONGESTION_VALUE);
            severeFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, true);
            routeLineFeatures.add(severeFeature);
        }

        // Upcoming line
        List<LineString> defaultUpcomingLineStrings = new ArrayList<>();
        List<LineString> lowUpcomingLineStrings = new ArrayList<>();
        List<LineString> moderateUpcomingLineStrings = new ArrayList<>();
        List<LineString> heavyUpcomingLineStrings = new ArrayList<>();
        List<LineString> severeUpcomingLineStrings = new ArrayList<>();

        for (CongestionStep.CongestionSegment congestionLineString : upcomingLine) {
            if (congestionLineString.getCongestionValue() != null) {
                switch (congestionLineString.getCongestionValue()) {
                    case SEVERE_CONGESTION_VALUE:
                        severeUpcomingLineStrings.add(congestionLineString.getLineString());
                        break;
                    case HEAVY_CONGESTION_VALUE:
                        heavyUpcomingLineStrings.add(congestionLineString.getLineString());
                        break;
                    case MODERATE_CONGESTION_VALUE:
                        moderateUpcomingLineStrings.add(congestionLineString.getLineString());
                        break;
                    case "low":
                        lowUpcomingLineStrings.add(congestionLineString.getLineString());
                        break;
                    default:
                        defaultUpcomingLineStrings.add(congestionLineString.getLineString());
                }
            } else {
                defaultUpcomingLineStrings.add(congestionLineString.getLineString());
            }
        }

        MultiLineString defaultMultiLineString = MultiLineString.fromLineStrings(defaultUpcomingLineStrings);
        Feature defaultFeature = Feature.fromGeometry(defaultMultiLineString);
        defaultFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, false);
        routeLineFeatures.add(defaultFeature);

        MultiLineString lowMultiLineString = MultiLineString.fromLineStrings(lowUpcomingLineStrings);
        Feature lowFeature = Feature.fromGeometry(lowMultiLineString);
        lowFeature.addStringProperty(CONGESTION_KEY, "low");
        lowFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, false);
        routeLineFeatures.add(lowFeature);

        MultiLineString moderateMultiLineString = MultiLineString.fromLineStrings(moderateUpcomingLineStrings);
        Feature moderateFeature = Feature.fromGeometry(moderateMultiLineString);
        moderateFeature.addStringProperty(CONGESTION_KEY, MODERATE_CONGESTION_VALUE);
        moderateFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, false);
        routeLineFeatures.add(moderateFeature);

        MultiLineString heavyMultiLineString = MultiLineString.fromLineStrings(heavyUpcomingLineStrings);
        Feature heavyfeature = Feature.fromGeometry(heavyMultiLineString);
        heavyfeature.addStringProperty(CONGESTION_KEY, HEAVY_CONGESTION_VALUE);
        heavyfeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, false);
        routeLineFeatures.add(heavyfeature);

        MultiLineString severeMultiLineString = MultiLineString.fromLineStrings(severeUpcomingLineStrings);
        Feature severeFeature = Feature.fromGeometry(severeMultiLineString);
        severeFeature.addStringProperty(CONGESTION_KEY, SEVERE_CONGESTION_VALUE);
        severeFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, false);
        routeLineFeatures.add(severeFeature);

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

    private static class CongestionStep {

        private final List<CongestionSegment> congestionSegments;

        public CongestionStep(List<CongestionSegment> congestionSegments) {
            this.congestionSegments = congestionSegments;
        }

        static class CongestionSegment {
            private final LineString lineString;

            private final double segmentLengthMeters;

            private final String congestionValue;

            public CongestionSegment(LineString lineString, double segmentLengthMeters, String congestionValue) {
                this.lineString = lineString;
                this.segmentLengthMeters = segmentLengthMeters;
                this.congestionValue = congestionValue;
            }

            public LineString getLineString() {
                return lineString;
            }

            public double getSegmentLengthMeters() {
                return segmentLengthMeters;
            }

            public String getCongestionValue() {
                return congestionValue;
            }
        }
    }
}
