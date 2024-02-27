package com.mapbox.services.android.navigation.ui.v5.route;

import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ROUTE_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_SOURCE_ID;

import android.app.Activity;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.annotation.StyleRes;

import androidx.fragment.app.Fragment;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.services.android.navigation.ui.v5.R;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide a route using {@link NavigationMapRoute#addRoutes(List)} and a route will be drawn using
 * runtime styling. The route will automatically be placed below all labels independent of specific
 * style. If the map styles changed when a routes drawn on the map, the route will automatically be
 * redrawn onto the new map style. If during a navigation session, the user gets re-routed, the
 * route line will be redrawn to reflect the new geometry.
 * <p>
 * You are given the option when first constructing an instance of this class to pass in a style
 * resource. This allows for custom colorizing and line scaling of the route. Inside your
 * applications {@code style.xml} file, you extend {@code <style name="NavigationMapRoute">} and
 * change some or all the options currently offered. If no style files provided in the constructor,
 * the default style will be used.
 *
 * @since 0.4.0
 */

public class NavigationMapRoute implements LifecycleObserver, OnRouteSelectionChangeListener {

    @StyleRes
    private final int styleRes;
    private final String belowLayer;
    private final MapboxMap mapboxMap;
    private final MapView mapView;
    private MapRouteClickListener mapRouteClickListener;
    private MapRouteProgressChangeListener mapRouteProgressChangeListener;
    private boolean isMapClickListenerAdded = false;
    private MapView.OnDidFinishLoadingStyleListener didFinishLoadingStyleListener;
    private boolean isDidFinishLoadingStyleListenerAdded = false;
    private MapboxNavigation navigation;
    //  private MapRouteLine routeLine;
    private MapRouteArrow routeArrow;
    private MapPrimaryRouteDrawer primaryRouteDrawer;
    private MapAlternativeRouteDrawer alternativeRouteDrawer;
    private List<DirectionsRoute> routes = new ArrayList<>();
    private int primaryRouteIndex = 0;
    private OnRouteSelectionChangeListener clientRouteSelectionChangeListener;

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param mapView   the MapView to apply the route to
     * @param mapboxMap the MapboxMap to apply route with
     * @since 0.4.0
     */
    public NavigationMapRoute(@NonNull MapView mapView, @NonNull MapboxMap mapboxMap) {
        this(null, mapView, mapboxMap, R.style.NavigationMapRoute);
    }

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param mapView    the MapView to apply the route to
     * @param mapboxMap  the MapboxMap to apply route with
     * @param belowLayer optionally pass in a layer id to place the route line below
     * @since 0.4.0
     */
    public NavigationMapRoute(@NonNull MapView mapView, @NonNull MapboxMap mapboxMap,
                              @Nullable String belowLayer) {
        this(null, mapView, mapboxMap, R.style.NavigationMapRoute, belowLayer);
    }

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param navigation an instance of the {@link MapboxNavigation} object. Passing in null means
     *                   your route won't consider rerouting during a navigation session.
     * @param mapView    the MapView to apply the route to
     * @param mapboxMap  the MapboxMap to apply route with
     * @since 0.4.0
     */
    public NavigationMapRoute(@Nullable MapboxNavigation navigation, @NonNull MapView mapView,
                              @NonNull MapboxMap mapboxMap) {
        this(navigation, mapView, mapboxMap, R.style.NavigationMapRoute);
    }

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param navigation an instance of the {@link MapboxNavigation} object. Passing in null means
     *                   your route won't consider rerouting during a navigation session.
     * @param mapView    the MapView to apply the route to
     * @param mapboxMap  the MapboxMap to apply route with
     * @param belowLayer optionally pass in a layer id to place the route line below
     * @since 0.4.0
     */
    public NavigationMapRoute(@Nullable MapboxNavigation navigation, @NonNull MapView mapView,
                              @NonNull MapboxMap mapboxMap, @Nullable String belowLayer) {
        this(navigation, mapView, mapboxMap, R.style.NavigationMapRoute, belowLayer);
    }

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param navigation an instance of the {@link MapboxNavigation} object. Passing in null means
     *                   your route won't consider rerouting during a navigation session.
     * @param mapView    the MapView to apply the route to
     * @param mapboxMap  the MapboxMap to apply route with
     * @param styleRes   a style resource with custom route colors, scale, etc.
     */
    public NavigationMapRoute(@Nullable MapboxNavigation navigation, @NonNull MapView mapView,
                              @NonNull MapboxMap mapboxMap, @StyleRes int styleRes) {
        this(navigation, mapView, mapboxMap, styleRes, null);
    }

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param navigation an instance of the {@link MapboxNavigation} object. Passing in null means
     *                   your route won't consider rerouting during a navigation session.
     * @param mapView    the MapView to apply the route to
     * @param mapboxMap  the MapboxMap to apply route with
     * @param styleRes   a style resource with custom route colors, scale, etc.
     * @param belowLayer optionally pass in a layer id to place the route line below
     */
    public NavigationMapRoute(@Nullable MapboxNavigation navigation, @NonNull MapView mapView,
                              @NonNull MapboxMap mapboxMap, @StyleRes int styleRes,
                              @Nullable String belowLayer) {
        this.styleRes = styleRes;
        this.belowLayer = belowLayer;
        this.mapView = mapView;
        this.mapboxMap = mapboxMap;
        this.navigation = navigation;
//    this.routeLine = buildMapRouteLine(mapView, mapboxMap, styleRes, belowLayer);
        this.routeArrow = new MapRouteArrow(mapView, mapboxMap, styleRes);
        this.primaryRouteDrawer = new MapPrimaryRouteDrawer(mapboxMap.getStyle(), true, new MapRouteLayerFactory());
        this.alternativeRouteDrawer = new MapAlternativeRouteDrawer(mapboxMap.getStyle(), new MapRouteLayerFactory());
        this.mapRouteClickListener = new MapRouteClickListener(this);
        this.mapRouteProgressChangeListener = new MapRouteProgressChangeListener(primaryRouteDrawer, routeArrow);
        initializeDidFinishLoadingStyleListener();
        addListeners();
        createLayers();
    }

    // For testing only
    NavigationMapRoute(@Nullable MapboxNavigation navigation, @NonNull MapView mapView,
                       @NonNull MapboxMap mapboxMap, @StyleRes int styleRes, @Nullable String belowLayer,
                       MapRouteClickListener mapClickListener,
                       MapView.OnDidFinishLoadingStyleListener didFinishLoadingStyleListener,
                       MapRouteProgressChangeListener progressChangeListener) {
        this.styleRes = styleRes;
        this.belowLayer = belowLayer;
        this.mapView = mapView;
        this.mapboxMap = mapboxMap;
        this.navigation = navigation;
        this.mapRouteClickListener = mapClickListener;
        this.didFinishLoadingStyleListener = didFinishLoadingStyleListener;
        this.mapRouteProgressChangeListener = progressChangeListener;
        addListeners();
        createLayers();
    }

    // For testing only
    NavigationMapRoute(@Nullable MapboxNavigation navigation, @NonNull MapView mapView,
                       @NonNull MapboxMap mapboxMap, @StyleRes int styleRes, @Nullable String belowLayer,
                       MapRouteClickListener mapClickListener,
                       MapView.OnDidFinishLoadingStyleListener didFinishLoadingStyleListener,
                       MapRouteProgressChangeListener progressChangeListener,
                       MapRouteLine routeLine,
                       MapRouteArrow routeArrow) {
        this.styleRes = styleRes;
        this.belowLayer = belowLayer;
        this.mapView = mapView;
        this.mapboxMap = mapboxMap;
        this.navigation = navigation;
        this.mapRouteClickListener = mapClickListener;
        this.didFinishLoadingStyleListener = didFinishLoadingStyleListener;
        this.mapRouteProgressChangeListener = progressChangeListener;
//    this.routeLine = routeLine;
        this.routeArrow = routeArrow;

        createLayers();
    }

    private void createLayers() {
        Context context = mapView.getContext();
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(styleRes, R.styleable.NavigationMapRoute);

            // Alternative routes
            float alternativeRouteScale = typedArray.getFloat(R.styleable.NavigationMapRoute_routeScale, 1.0f);
            int alternativeRouteColor = typedArray.getColor(R.styleable.NavigationMapRoute_alternativeRouteColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_alternative_color));
            int alternativeRouteShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_alternativeRouteShieldColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_alternative_shield_color));

            alternativeRouteDrawer.createLayers(alternativeRouteScale, alternativeRouteColor, alternativeRouteShieldColor);

            // Primary route
            float routeScale = typedArray.getFloat(R.styleable.NavigationMapRoute_routeScale, 1.0f);
            int routeColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_layer_blue));
            int routeShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeShieldColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_shield_layer_color));
            int drivenRouteColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_color));
            int drivenRouteShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteShieldColor,
                    ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_shield_color));

            primaryRouteDrawer.createLayers(routeScale, routeColor, routeShieldColor, drivenRouteColor, drivenRouteShieldColor);


        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }


//    // Primary route attributes
//    routeDefaultColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeColor,
//            ContextCompat.getColor(context, R.color.mapbox_navigation_route_layer_blue));
//    routeModerateColor = typedArray.getColor(
//            R.styleable.NavigationMapRoute_routeModerateCongestionColor,
//            ContextCompat.getColor(context, R.color.mapbox_navigation_route_layer_congestion_yellow));
//    routeSevereColor = typedArray.getColor(
//            R.styleable.NavigationMapRoute_routeSevereCongestionColor,
//            ContextCompat.getColor(context, R.color.mapbox_navigation_route_layer_congestion_red));
//    routeShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeShieldColor,
//            ContextCompat.getColor(context, R.color.mapbox_navigation_route_shield_layer_color));
//    routeScale = typedArray.getFloat(R.styleable.NavigationMapRoute_routeScale, 1.0f);
//    roundedLineCap = typedArray.getBoolean(R.styleable.NavigationMapRoute_roundedLineCap, true);
//
//    // Driven route attributes
//    drivenRouteColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteColor,
//            ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_color));
//    drivenRouteShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_drivenRouteShieldColor,
//            ContextCompat.getColor(context, R.color.mapbox_navigation_route_driven_shield_color));
//
//    // Alternative routes attributes
//    alternativeRouteDefaultColor = typedArray.getColor(
//            R.styleable.NavigationMapRoute_alternativeRouteColor,
//            ContextCompat.getColor(context, R.color.mapbox_navigation_route_alternative_color));
//    alternativeRouteModerateColor = typedArray.getColor(
//            R.styleable.NavigationMapRoute_alternativeRouteModerateCongestionColor,
//            ContextCompat.getColor(context, R.color.mapbox_navigation_route_alternative_congestion_yellow));
//    alternativeRouteSevereColor = typedArray.getColor(
//            R.styleable.NavigationMapRoute_alternativeRouteSevereCongestionColor,
//            ContextCompat.getColor(context, R.color.mapbox_navigation_route_alternative_congestion_red));
//    alternativeRouteShieldColor = typedArray.getColor(
//            R.styleable.NavigationMapRoute_alternativeRouteShieldColor,
//            ContextCompat.getColor(context, R.color.mapbox_navigation_route_alternative_shield_color));
//    alternativeRouteScale = typedArray.getFloat(
//            R.styleable.NavigationMapRoute_alternativeRouteScale, 1.0f);
//
//    GeoJsonOptions wayPointGeoJsonOptions = new GeoJsonOptions().withMaxZoom(16);
//    drawnWaypointsFeatureCollection = waypointsFeatureCollection;
//    wayPointSource = sourceProvider.build(WAYPOINT_SOURCE_ID, drawnWaypointsFeatureCollection, wayPointGeoJsonOptions);
//    style.addSource(wayPointSource);
//
//    GeoJsonOptions routeLineGeoJsonOptions = new GeoJsonOptions().withMaxZoom(16);
//    drawnRouteFeatureCollection = routesFeatureCollection;
//    routeLineSource = sourceProvider.build(ROUTE_SOURCE_ID, drawnRouteFeatureCollection, routeLineGeoJsonOptions);
//    style.addSource(routeLineSource);
//
//    // Waypoint attributes
//    int originWaypointIcon = typedArray.getResourceId(
//            R.styleable.NavigationMapRoute_originWaypointIcon, R.drawable.ic_route_origin);
//    int destinationWaypointIcon = typedArray.getResourceId(
//            R.styleable.NavigationMapRoute_destinationWaypointIcon, R.drawable.ic_route_destination);
//    typedArray.recycle();
//
//    Drawable originIcon = drawableProvider.retrieveDrawable(originWaypointIcon);
//    Drawable destinationIcon = drawableProvider.retrieveDrawable(destinationWaypointIcon);
//    belowLayer = findRouteBelowLayerId(belowLayer, style);
//
//    initializeLayers(style, layerProvider, mapRouteLayerFactory, originIcon, destinationIcon, belowLayer);
//
//    this.directionsRoutes.addAll(directionsRoutes);
//    this.routeFeatureCollections.addAll(routeFeatureCollections);
//    this.routeLineStrings.putAll(routeLineStrings);
//
//    updateAlternativeVisibilityTo(alternativesVisible);
//    updateRoutesFor(primaryRouteIndex);
//    updateVisibilityTo(isVisible);
    }

    /**
     * Allows adding a single primary route for the user to traverse along. No alternative routes will
     * be drawn on top of the map.
     *
     * @param directionsRoute the directions route which you'd like to display on the map
     * @since 0.4.0
     */
    public void addRoute(DirectionsRoute directionsRoute) {
        List<DirectionsRoute> routes = new ArrayList<>();
        routes.add(directionsRoute);
        addRoutes(routes);
    }

    /**
     * Provide a list of {@link DirectionsRoute}s, the primary route will default to the first route
     * in the directions route list. All other routes in the list will be drawn on the map using the
     * alternative route style.
     *
     * @param directionsRoutes a list of direction routes, first one being the primary and the rest of
     *                         the routes are considered alternatives.
     * @since 0.8.0
     */
    public void addRoutes(@NonNull @Size(min = 1) List<DirectionsRoute> directionsRoutes) {
        routes.addAll(directionsRoutes);
        mapRouteClickListener.addRoutes(directionsRoutes);

        primaryRouteDrawer.setRoute(directionsRoutes.get(primaryRouteIndex));

        List<DirectionsRoute> alternativeRoutes = new ArrayList<>();
        for (DirectionsRoute route : directionsRoutes) {
            String routeIndexString = directionsRoutes.get(primaryRouteIndex).routeIndex();
            if (route.routeIndex() != null && routeIndexString != null && Integer.parseInt(route.routeIndex()) != Integer.parseInt(routeIndexString)) {
                alternativeRoutes.add(route);
            }
        }
        alternativeRouteDrawer.setRoutes(alternativeRoutes);
    }

    /**
     * Hides all routes / route arrows on the map drawn by this class.
     *
     * @deprecated you can now use a combination of {@link NavigationMapRoute#updateRouteVisibilityTo(boolean)}
     * and {@link NavigationMapRoute#updateRouteArrowVisibilityTo(boolean)} to hide the route line and arrow.
     */
    @Deprecated
    public void removeRoute() {
        updateRouteVisibilityTo(false);
        updateRouteArrowVisibilityTo(false);
    }

    /**
     * Hides all routes on the map drawn by this class.
     *
     * @param isVisible true to show routes, false to hide
     */
    public void updateRouteVisibilityTo(boolean isVisible) {
//    routeLine.updateVisibilityTo(isVisible);
        mapRouteProgressChangeListener.updateVisibility(isVisible);
    }


    /**
     * Hides the progress arrow on the map drawn by this class.
     *
     * @param isVisible true to show routes, false to hide
     */
    public void updateRouteArrowVisibilityTo(boolean isVisible) {
        routeArrow.updateVisibilityTo(isVisible);
    }

    /**
     * Add a {@link OnRouteSelectionChangeListener} to know which route the user has currently
     * selected as their primary route.
     *
     * @param onRouteSelectionChangeListener a listener which lets you know when the user has changed
     *                                       the primary route and provides the current direction
     *                                       route which the user has selected
     * @since 0.8.0
     */
    public void setOnRouteSelectionChangeListener(
            @Nullable OnRouteSelectionChangeListener onRouteSelectionChangeListener) {
//    mapRouteClickListener.setOnRouteSelectionChangeListener(onRouteSelectionChangeListener);
    }

    /**
     * Toggle whether or not you'd like the map to display the alternative routes. This options great
     * for when the user actually begins the navigation session and alternative routes aren't needed
     * anymore.
     *
     * @param alternativesVisible true if you'd like alternative routes to be displayed on the map,
     *                            else false
     * @since 0.8.0
     */
    public void showAlternativeRoutes(boolean alternativesVisible) {
//    mapRouteClickListener.updateAlternativesVisible(alternativesVisible);
//    routeLine.toggleAlternativeVisibilityWith(alternativesVisible);
    }

    /**
     * This method will allow this class to listen to new routes based on
     * the progress updates from {@link MapboxNavigation}.
     * <p>
     * If a new route is given to {@link MapboxNavigation#startNavigation(DirectionsRoute)}, this
     * class will automatically draw the new route.
     *
     * @param navigation to add the progress change listener
     */
    public void addProgressChangeListener(MapboxNavigation navigation) {
        this.navigation = navigation;
        navigation.addProgressChangeListener(mapRouteProgressChangeListener);
    }


    /**
     * Should be called if {@link NavigationMapRoute#addProgressChangeListener(MapboxNavigation)} was
     * called to prevent leaking.
     *
     * @param navigation to remove the progress change listener
     */
    public void removeProgressChangeListener(MapboxNavigation navigation) {
        if (navigation != null) {
            navigation.removeProgressChangeListener(mapRouteProgressChangeListener);
        }
    }

    /**
     * This method should be added in your {@link Activity#onStart()} or
     * {@link Fragment#onStart()} to handle adding and removing of listeners,
     * preventing memory leaks.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        addListeners();
    }

    /**
     * This method should be added in your {@link Activity#onStop()} or {@link Fragment#onStop()}
     * to handle adding and removing of listeners, preventing memory leaks.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        removeListeners();
    }

    private MapRouteLine buildMapRouteLine(@NonNull MapView mapView, @NonNull MapboxMap mapboxMap,
                                           @StyleRes int styleRes, @Nullable String belowLayer) {
        Context context = mapView.getContext();
        MapRouteDrawableProvider drawableProvider = new MapRouteDrawableProvider(context);
        MapRouteSourceProvider sourceProvider = new MapRouteSourceProvider();
        MapRouteLayerProvider layerProvider = new MapRouteLayerProvider();
        MapRouteLayerFactory layerFactory = new MapRouteLayerFactory();
        Handler handler = new Handler(context.getMainLooper());
        return new MapRouteLine(context, mapboxMap.getStyle(), styleRes, belowLayer,
                drawableProvider, sourceProvider, layerProvider, layerFactory, handler
        );
    }

    private void initializeDidFinishLoadingStyleListener() {
        didFinishLoadingStyleListener = new MapView.OnDidFinishLoadingStyleListener() {
            @Override
            public void onDidFinishLoadingStyle() {
                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        redraw(style);
                    }
                });
            }
        };
    }

    private void addListeners() {
        if (!isMapClickListenerAdded) {
            mapboxMap.addOnMapClickListener(mapRouteClickListener);
            isMapClickListenerAdded = true;
        }
        if (navigation != null) {
            navigation.addProgressChangeListener(mapRouteProgressChangeListener);
        }
        if (!isDidFinishLoadingStyleListenerAdded) {
            mapView.addOnDidFinishLoadingStyleListener(didFinishLoadingStyleListener);
            isDidFinishLoadingStyleListenerAdded = true;
        }
    }

    private void removeListeners() {
        if (isMapClickListenerAdded) {
            mapboxMap.removeOnMapClickListener(mapRouteClickListener);
            isMapClickListenerAdded = false;
        }
        if (navigation != null) {
            navigation.removeProgressChangeListener(mapRouteProgressChangeListener);
        }
        if (isDidFinishLoadingStyleListenerAdded) {
            mapView.removeOnDidFinishLoadingStyleListener(didFinishLoadingStyleListener);
            isDidFinishLoadingStyleListenerAdded = false;
        }
    }

    private void redraw(Style style) {
        createLayers();
        routeArrow = new MapRouteArrow(mapView, mapboxMap, styleRes);
        recreateRouteLine(style);
    }

    @Override
    public void onNewPrimaryRouteSelected(DirectionsRoute directionsRoute) {
        primaryRouteIndex = Integer.parseInt(directionsRoute.routeIndex()); //TODO: do we need the index?!?!?
        primaryRouteDrawer.setRoute(directionsRoute);

        List<DirectionsRoute> alternativeRoutes = new ArrayList<>();
        for (DirectionsRoute route : routes) {
            if (route.routeIndex() != null && Integer.parseInt(route.routeIndex()) != primaryRouteIndex) {
                alternativeRoutes.add(route);
            }
        }
        alternativeRouteDrawer.setRoutes(alternativeRoutes);

        if (clientRouteSelectionChangeListener != null) {
            clientRouteSelectionChangeListener.onNewPrimaryRouteSelected(directionsRoute);
        }
    }

    private void recreateRouteLine(Style style) {
//    Context context = mapView.getContext();
//    MapRouteDrawableProvider drawableProvider = new MapRouteDrawableProvider(context);
//    MapRouteSourceProvider sourceProvider = new MapRouteSourceProvider();
//    MapRouteLayerProvider layerProvider = new MapRouteLayerProvider();
//    MapRouteLayerFactory layerFactory = new MapRouteLayerFactory();
//    Handler handler = new Handler(context.getMainLooper());
//
//    routeLine = new MapRouteLine(
//            context,
//            style,
//            styleRes,
//            belowLayer,
//            drawableProvider,
//            sourceProvider,
//            layerProvider,
//            layerFactory,
//            routeLine.retrieveDrawnRouteFeatureCollections(),
//            routeLine.retrieveDrawnWaypointsFeatureCollections(),
//            routeLine.retrieveDirectionsRoutes(),
//            routeLine.retrieveRouteFeatureCollections(),
//            routeLine.retrieveRouteLineStrings(),
//            routeLine.retrievePrimaryRouteIndex(),
//            routeLine.retrieveVisibility(),
//            routeLine.retrieveAlternativesVisible(),
//            handler
//    );
//    mapboxMap.removeOnMapClickListener(mapRouteClickListener);
//    mapRouteClickListener = new MapRouteClickListener(this);
//    mapboxMap.addOnMapClickListener(mapRouteClickListener);
//    mapRouteProgressChangeListener = new MapRouteProgressChangeListener(routeLine, primaryRouteDrawer, routeArrow);
    }
}