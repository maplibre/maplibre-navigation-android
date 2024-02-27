package com.mapbox.services.android.navigation.ui.v5.route;

import static com.mapbox.mapboxsdk.style.expressions.Expression.color;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.product;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.switchCase;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_LAYER_ID;
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
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.utils.MapUtils;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfMisc;

import java.util.ArrayList;

//TODO: public to be able to change logic?!
//TODO: add as interface?!
//TODO: `drawer` feels wrong, while we update the geo json and don't draw anything
//TODO: FPS configuration (by zoom)
//TODO: is route-eating the correct term?
public class MapPrimaryRouteDrawer {

    private Style style;
    private boolean isRouteEatingEnabled;

    private MapRouteLayerFactory routeLayerFactory;

    @Nullable
    private DirectionsRoute route;

    private Point lastLocationPoint;

    private ValueAnimator valueAnimator;

    private final double minUpdateIntervalNanoSeconds = 1E9 / Integer.MAX_VALUE; // Seconds to nano seconds

    private long lastUpdateTime = System.nanoTime();

    private long locationUpdateTimestamp;

    MapPrimaryRouteDrawer(Style style, boolean isRouteEatingEnabled, MapRouteLayerFactory routeLayerFactory) {
        this.style = style;
        this.isRouteEatingEnabled = isRouteEatingEnabled;
        this.routeLayerFactory = routeLayerFactory;
    }

    void createLayers(float routeScale,
                      @ColorInt int routeColor,
                      @ColorInt int routeShieldColor,
                      @ColorInt int drivenRouteColor,
                      @ColorInt int drivenRouteShieldColor,
                      String belowLayerId) {
        LineLayer shieldLineLayer = routeLayerFactory.createPrimaryRouteShieldLayer(routeScale, routeShieldColor, drivenRouteShieldColor);
        MapUtils.addLayerToMap(style, shieldLineLayer, belowLayerId);

        LineLayer routeLineLayer = routeLayerFactory.createPrimaryRouteLayer(routeScale, routeColor, drivenRouteColor);
        MapUtils.addLayerToMap(style, routeLineLayer, belowLayerId);
    }

    void setStyle(Style style) {
        this.style = style;

        if (route != null) {
            String routeGeometry = route.geometry();
            if (routeGeometry != null) {
                LineString routeLineString = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6);
                drawRoute(routeLineString);
            }
        }
    }

    void setRouteEatingEnabled(boolean isRouteEatingEnabled) {
        this.isRouteEatingEnabled = isRouteEatingEnabled;
    }

    void setRoute(DirectionsRoute route) {
        this.route = route;

        String routeGeometry = route.geometry();
        if (routeGeometry != null) {
            LineString routeLineString = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6);
            drawRoute(routeLineString);
        }
    }

    void updateRouteProgress(Location location, RouteProgress routeProgress) {
        if (!isRouteEatingEnabled) {
            // Route eating disabled, route will only drawn once. Skip progress updating.
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

        valueAnimator = ValueAnimator.ofFloat(0f, 1f);

//        animationDuration = (long) ((locationUpdateTimestamp - previousUpdateTimeStamp) * durationMultiplier)
//        /* make animation slightly longer with durationMultiplier, defaults to 1.1f */;

        Point startPoint = lastLocationPoint;
        Location target = location;
        valueAnimator.addUpdateListener(animation -> {
            if (System.nanoTime() - lastUpdateTime < minUpdateIntervalNanoSeconds) {
                return;
            }

            float fraction = animation.getAnimatedFraction();
            LineString routeLine = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6);

            Point routeLineStartPoint = TurfMeasurement.along(routeLine, 0, TurfConstants.UNIT_METERS);
            Point routeLineEndPoint = TurfMeasurement.along(routeLine, routeProgress.directionsRoute().distance(), TurfConstants.UNIT_METERS);
            Point animatedPoint = Point.fromLngLat(
                    startPoint.longitude() + fraction * (target.getLongitude() - startPoint.longitude()),
                    startPoint.latitude() + fraction * (target.getLatitude() - startPoint.latitude())
            );

            lastLocationPoint = animatedPoint;

            if (animatedPoint.equals(routeLineStartPoint)) {
                return;
            }

//            lastLocationPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            LineString drivenLine = TurfMisc.lineSlice(routeLineStartPoint, animatedPoint, routeLine);
            LineString upcomingLine = TurfMisc.lineSlice(animatedPoint, routeLineEndPoint, routeLine);
            drawRoute(drivenLine, upcomingLine);

            lastUpdateTime = System.nanoTime();
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                LineString routeLine = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6);
                Point routeLineStartPoint = TurfMeasurement.along(routeLine, 0, TurfConstants.UNIT_METERS);
                Point routeLineEndPoint = TurfMeasurement.along(routeLine, routeProgress.directionsRoute().distance(), TurfConstants.UNIT_METERS);

                lastLocationPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                LineString drivenLine = TurfMisc.lineSlice(routeLineStartPoint, lastLocationPoint, routeLine);
                LineString upcomingLine = TurfMisc.lineSlice(lastLocationPoint, routeLineEndPoint, routeLine);
                drawRoute(drivenLine, upcomingLine);
            }
        });

        long previousUpdateTimeStamp = locationUpdateTimestamp;
        locationUpdateTimestamp = SystemClock.elapsedRealtime();

        valueAnimator.setDuration((long) ((locationUpdateTimestamp - previousUpdateTimeStamp) * 1.1)); // 1.1 second
        valueAnimator.start();
    }

    private void drawRoute(LineString routeLine) {
        drawRoute(null, routeLine);
    }

    private void drawRoute(LineString drivenLine, LineString upcomingLine) {
        if (!style.isFullyLoaded()) {
            // The style is not available anymore. Skip processing.
            return;
        }

        ArrayList<Feature> routeLineFeatures = new ArrayList<>();

        if (drivenLine != null) {
            Feature drivenLineFeature = Feature.fromGeometry(drivenLine);
            drivenLineFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, true);
            routeLineFeatures.add(drivenLineFeature);
        }

        Feature upcomingLineFeature = Feature.fromGeometry(upcomingLine);
        upcomingLineFeature.addBooleanProperty(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY, false);
        routeLineFeatures.add(upcomingLineFeature);

        getSource(style).setGeoJson(FeatureCollection.fromFeatures(routeLineFeatures));
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


////    private Point lastLocationPoint;
////    private ValueAnimator valueAnimator;
////
////    private final double minUpdateIntervalNanoSeconds = 1E9 / 0.5; // Seconds to nano seconds
////
////    private long lastUpdateTime = System.nanoTime();
////
////    public void updateRouteLine(Location location, RouteProgress routeProgress) {
////        if (lastLocationPoint == null) {
////            lastLocationPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
////            return;
////        }
////
////        if (valueAnimator != null) {
////            valueAnimator.cancel();
////        }
////
////        valueAnimator = ValueAnimator.ofFloat(0f, 1f);
////        valueAnimator.setDuration(1100L); // 1.1 second
////
////        Point startPoint = lastLocationPoint;
////        Location target = location;
////        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
////
////            @Override
////            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
////                float fraction = animation.getAnimatedFraction();
////                LineString routeLine = LineString.fromPolyline(directionsRoutes.get(primaryRouteIndex).geometry(), Constants.PRECISION_6);
////
////                Point routeLineStartPoint = TurfMeasurement.along(routeLine, 0, TurfConstants.UNIT_METERS);
////                Point animatedPoint = Point.fromLngLat(
////                        startPoint.longitude() + fraction * (target.getLongitude() - startPoint.longitude()),
////                        startPoint.latitude() + fraction * (target.getLatitude() - startPoint.latitude())
////                );
////
////                lastLocationPoint = animatedPoint;
////
////                if (animatedPoint.equals(routeLineStartPoint)) {
////                    return;
////                }
////
////                LineString drivenLine = TurfMisc.lineSlice(routeLineStartPoint, animatedPoint, routeLine);
////                updateDrivenLine(drivenLine);
////            }
////        });
////
////        valueAnimator.addListener(new AnimatorListenerAdapter() {
////            @Override
////            public void onAnimationEnd(Animator animation) {
////                LineString routeLine = LineString.fromPolyline(directionsRoutes.get(primaryRouteIndex).geometry(), Constants.PRECISION_6);
////                Point routeLineStartPoint = TurfMeasurement.along(routeLine, 0, TurfConstants.UNIT_METERS);
////
////                lastLocationPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
////                LineString drivenLine = TurfMisc.lineSlice(routeLineStartPoint, lastLocationPoint, routeLine);
////                updateDrivenLine(drivenLine);
////            }
////        });
////
////        valueAnimator.start();
////
////    }
////
////    private void updateDrivenLine(LineString drivenLine) {
////        if (mapboxMap.getStyle() == null)  {
////            return;
////        }
////
////        GeoJsonSource drivenRouteGeoJsonSource = (GeoJsonSource) mapboxMap.getStyle().getSource(DRIVEN_ROUTE_SOURCE_ID);
////        if (drivenRouteGeoJsonSource == null) {
////            drivenRouteGeoJsonSource = new GeoJsonSource(
////                    DRIVEN_ROUTE_SOURCE_ID,
////                    arrowShaftGeoJsonFeature,
////                    new GeoJsonOptions().withMaxZoom(16)
////            );
////
////            mapboxMap.getStyle().addSource(drivenRouteGeoJsonSource);
////        }
//////        Log.d("debug", "fabi: location " + location.getLatitude() + " " + location.getLongitude());
//////        LineString drivenLine = TurfMisc.lineSliceAlong(routeLine, 0, routeProgress.distanceTraveled(), TurfConstants.UNIT_METERS);
//////        LineString drivenLine = TurfMisc.lineSlice(routeLineStartPoint, animatedPoint, routeLine);
////        Feature feature = Feature.fromGeometry(drivenLine);
//////        feature.addStringProperty(CONGESTION_KEY, leg.annotation().congestion().get(i));
////        feature.addStringProperty(SOURCE_KEY, String.format(Locale.US, ID_FORMAT,
////                DRIVEN_ROUTE_SOURCE_ID, primaryRouteIndex));
//////        feature.addNumberProperty(INDEX_KEY, index);
////
////        drivenRouteGeoJsonSource.setGeoJson(feature);
////    }