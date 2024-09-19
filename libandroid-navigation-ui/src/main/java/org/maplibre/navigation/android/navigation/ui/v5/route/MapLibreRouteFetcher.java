package org.maplibre.navigation.android.navigation.ui.v5.route;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.geojson.Point;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions;
import org.maplibre.navigation.android.navigation.v5.route.RouteFetcher;
import org.maplibre.navigation.android.navigation.v5.route.RouteListener;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;
import org.maplibre.navigation.android.navigation.v5.utils.RouteUtils;
import org.maplibre.navigation.android.navigation.v5.utils.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * This class can be used to fetch new routes given a {@link Location} origin and
 * {@link RouteOptions} provided by a {@link RouteProgress}.
 */
public class MapLibreRouteFetcher extends RouteFetcher {

    private static final double BEARING_TOLERANCE = 90d;
    private static final String SEMICOLON = ";";
    private static final int ORIGIN_APPROACH_THRESHOLD = 1;
    private static final int ORIGIN_APPROACH = 0;
    private static final int FIRST_POSITION = 0;
    private static final int SECOND_POSITION = 1;
    private final WeakReference<Context> contextWeakReference;

    private RouteProgress routeProgress;
    private RouteUtils routeUtils;

    private NavigationRoute navigationRoute;

    public MapLibreRouteFetcher(Context context) {
        contextWeakReference = new WeakReference<>(context);
        routeUtils = new RouteUtils();
    }

    /**
     * Calculates a new {@link DirectionsRoute} given
     * the current {@link Location} and {@link RouteProgress} along the route.
     * <p>
     * Uses {@link RouteOptions#coordinates()} and {@link RouteProgress#remainingWaypoints()}
     * to determine the amount of remaining waypoints there are along the given route.
     *
     * @param location      current location of the device
     * @param routeProgress for remaining waypoints along the route
     * @since 0.13.0
     */

    public void findRouteFromRouteProgress(Location location, RouteProgress routeProgress) {
        this.routeProgress = routeProgress;
        NavigationRoute.Builder builder = buildRequest(location, routeProgress);
        findRouteWith(builder);
    }

    @Nullable
    public NavigationRoute.Builder buildRequest(Location location, RouteProgress progress) {
        Context context = contextWeakReference.get();
        if (invalid(context, location, progress)) {
            return null;
        }
        Point origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
        Double bearing = location.hasBearing() ? Float.valueOf(location.getBearing()).doubleValue() : null;
        RouteOptions options = progress.directionsRoute().routeOptions();
        NavigationRoute.Builder builder = NavigationRoute.builder(context)
                .origin(toMapLibrePoint(origin), bearing, BEARING_TOLERANCE)
                .routeOptions(options);

        List<Point> remainingWaypoints = toMapboxPointList(routeUtils.calculateRemainingWaypoints(progress));
        if (remainingWaypoints == null) {
            Timber.e("An error occurred fetching a new route");
            return null;
        }
        addDestination(remainingWaypoints, builder);
        addWaypoints(remainingWaypoints, builder);
        addWaypointNames(progress, builder);
        addApproaches(progress, builder);
        return builder;
    }

    private void addDestination(List<Point> remainingWaypoints, NavigationRoute.Builder builder) {
        if (!remainingWaypoints.isEmpty()) {
            builder.destination(toMapLibrePoint(retrieveDestinationWaypoint(remainingWaypoints)));
        }
    }

    private Point retrieveDestinationWaypoint(List<Point> remainingWaypoints) {
        int lastWaypoint = remainingWaypoints.size() - 1;
        return remainingWaypoints.remove(lastWaypoint);
    }

    private void addWaypoints(List<Point> remainingCoordinates, NavigationRoute.Builder builder) {
        if (!remainingCoordinates.isEmpty()) {
            for (Point coordinate : remainingCoordinates) {
                builder.addWaypoint(toMapLibrePoint(coordinate));
            }
        }
    }

    private void addWaypointNames(RouteProgress progress, NavigationRoute.Builder builder) {
        String[] remainingWaypointNames = routeUtils.calculateRemainingWaypointNames(progress);
        if (remainingWaypointNames != null) {
            builder.addWaypointNames(remainingWaypointNames);
        }
    }

    private void addApproaches(RouteProgress progress, NavigationRoute.Builder builder) {
        String[] remainingApproaches = calculateRemainingApproaches(progress);
        if (remainingApproaches != null) {
            builder.addApproaches(remainingApproaches);
        }
    }

    private String[] calculateRemainingApproaches(RouteProgress routeProgress) {
        RouteOptions routeOptions = routeProgress.directionsRoute().routeOptions();
        if (routeOptions == null || TextUtils.isEmpty(routeOptions.approaches())) {
            return null;
        }
        String allApproaches = routeOptions.approaches();
        String[] splitApproaches = allApproaches.split(SEMICOLON);
        int coordinatesSize = routeProgress.directionsRoute().routeOptions().coordinates().size();
        String[] remainingApproaches = Arrays.copyOfRange(splitApproaches,
                coordinatesSize - routeProgress.remainingWaypoints(), coordinatesSize);
        String[] approaches = new String[remainingApproaches.length + ORIGIN_APPROACH_THRESHOLD];
        approaches[ORIGIN_APPROACH] = splitApproaches[ORIGIN_APPROACH];
        System.arraycopy(remainingApproaches, FIRST_POSITION, approaches, SECOND_POSITION, remainingApproaches.length);
        return approaches;
    }

    /**
     * Cancels the Directions API call if it has not been executed yet.
     */
    public void cancelRouteCall() {
        if (navigationRoute != null) {
            navigationRoute.cancelCall();
        }
    }

    /**
     * Executes the given NavigationRoute builder, eventually triggering
     * any {@link RouteListener} that has been added via {@link MapLibreRouteFetcher#addRouteListener(RouteListener)}.
     *
     * @param builder to be executed
     */
    public void findRouteWith(NavigationRoute.Builder builder) {
        if (builder != null) {
            navigationRoute = builder.build();
            navigationRoute.getRoute(directionsResponseCallback);
        }
    }

    private boolean invalid(Context context, Location location, RouteProgress routeProgress) {
        return context == null || location == null || routeProgress == null;
    }

    private Callback<DirectionsResponse> directionsResponseCallback = new Callback<>() {
        @Override
        public void onResponse(@NonNull Call<DirectionsResponse> call,
                @NonNull Response<DirectionsResponse> response) {
            if (!response.isSuccessful()) {
                return;
            }
            /*
            This part here is critical. We use Mapbox Directions API SDK to fetch a route from Mapbox. Now we convert it from Mapbox to our internal model for navigation.
            The json is similar to the Mapbox Directions API, therefore we can do this easily.
            Be aware, the fromJson -> toJson handling should work fine usually, for production use, there might be better options
             */
            DirectionsResponse maplibreResponse = DirectionsResponse.fromJson(response.body().toJson());
            updateListeners(maplibreResponse, routeProgress);
        }

        @Override
        public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable throwable) {
            updateListenersWithError(throwable);
        }
    };

    private void updateListeners(DirectionsResponse response, RouteProgress routeProgress) {
        for (RouteListener listener : routeListeners) {
            listener.onResponseReceived(response, routeProgress);
        }
    }

    private void updateListenersWithError(Throwable throwable) {
        for (RouteListener listener : routeListeners) {
            listener.onErrorReceived(throwable);
        }
    }

    private List<Point> toMapboxPointList(List<org.maplibre.geojson.Point> pointList) {
        List<Point> mapboxPointList = new ArrayList<>();
        for (org.maplibre.geojson.Point point : pointList) {
            mapboxPointList.add(toMapboxPoint(point));
        }
        return mapboxPointList;
    }

    private Point toMapboxPoint(org.maplibre.geojson.Point point) {
        return Point.fromLngLat(point.longitude(), point.latitude());
    }

    private List<org.maplibre.geojson.Point> toMapLibrePointList(List<Point> pointList) {
        List<org.maplibre.geojson.Point> mapboxPointList = new ArrayList<>();
        for (Point point : pointList) {
            mapboxPointList.add(toMapLibrePoint(point));
        }
        return mapboxPointList;
    }

    private org.maplibre.geojson.Point toMapLibrePoint(Point point) {
        return org.maplibre.geojson.Point.fromLngLat(point.longitude(), point.latitude());
    }
}
