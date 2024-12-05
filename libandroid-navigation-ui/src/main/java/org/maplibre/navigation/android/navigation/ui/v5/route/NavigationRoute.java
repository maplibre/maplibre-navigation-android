package org.maplibre.navigation.android.navigation.ui.v5.route;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.api.directions.v5.MapboxDirections;

import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions;
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigation;
import org.maplibre.navigation.android.navigation.v5.utils.LocaleUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * The NavigationRoute class wraps the {@link MapboxDirections} class with parameters which
 * <u>must</u> be set inorder for a navigation session to successfully begin. While it is possible
 * to pass in any {@link DirectionsRoute} into
 * {@link MapLibreNavigation#startNavigation(DirectionsRoute)}, using this class will ensure your
 * request includes all the proper information needed for the navigation session to begin.
 * <p>
 * <p>
 * Developer Note: MapboxDirections cannot be directly extended since it id an AutoValue class.
 * </p>
 * 0.5.0
 */
public final class NavigationRoute {

    private final MapboxDirections mapboxDirections;

    /**
     * Private constructor used for the {@link Builder#build()} method.
     *
     * @param mapboxDirections a new instance of a {@link MapboxDirections} class
     * @since 0.5.0
     */
    private NavigationRoute(MapboxDirections mapboxDirections) {
        this.mapboxDirections = mapboxDirections;
    }

    /**
     * Build a new {@link NavigationRoute} object with the proper navigation parameters already setup.
     *
     * @return a {@link Builder} object for creating this object
     * @since 0.5.0
     */
    public static Builder builder(Context context) {
        return builder(context, new LocaleUtils());
    }

    static Builder builder(Context context, LocaleUtils localeUtils) {
        return new Builder()
            .annotations(DirectionsCriteria.ANNOTATION_CONGESTION, DirectionsCriteria.ANNOTATION_DISTANCE)
            .language(context, localeUtils)
            .voiceUnits(context, localeUtils)
            .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC);
    }

    /**
     * Call when you have constructed your navigation route with your desired parameters. A
     * {@link Callback} must be passed into the method to handle both the response and failure.
     *
     * @param callback a RetroFit callback which contains an onResponse and onFailure
     * @since 0.5.0
     */
    public void getRoute(Callback<DirectionsResponse> callback) {
        mapboxDirections.enqueueCall(toMapboxCallback(callback));
    }

    private Callback<com.mapbox.api.directions.v5.models.DirectionsResponse> toMapboxCallback(Callback<DirectionsResponse> callback) {
        return new Callback<>() {
            @Override
            public void onResponse(Call<com.mapbox.api.directions.v5.models.DirectionsResponse> call, Response<com.mapbox.api.directions.v5.models.DirectionsResponse> response) {
                callback.onResponse(toMapLibreCall(call), toMapLibreResponse(response));
            }

            @Override
            public void onFailure(Call<com.mapbox.api.directions.v5.models.DirectionsResponse> call, Throwable t) {
                callback.onFailure(toMapLibreCall(call), t);
            }
        };
    }

    private Response<DirectionsResponse> toMapLibreResponse(Response<com.mapbox.api.directions.v5.models.DirectionsResponse> response) {
        if (response.isSuccessful()) {
            return Response.success(response.code(), toMapLibreDirectionsResponse(response.body()));
        } else {
            return Response.error(response.errorBody(), response.raw());
        }
    }

    private Call<DirectionsResponse> toMapLibreCall(Call<com.mapbox.api.directions.v5.models.DirectionsResponse> call) {
        return new Call<>() {
            @Override
            public Response<DirectionsResponse> execute() throws IOException {
                return toMapLibreResponse(call.execute());
            }

            @Override
            public void enqueue(Callback<DirectionsResponse> callback) {
                call.enqueue(toMapboxCallback(callback));
            }

            @Override
            public boolean isExecuted() {
                return call.isExecuted();
            }

            @Override
            public void cancel() {
                call.cancel();
            }

            @Override
            public boolean isCanceled() {
                return call.isCanceled();
            }

            @Override
            public Call<DirectionsResponse> clone() {
                return toMapLibreCall(call.clone());
            }

            @Override
            public Request request() {
                return call.request();
            }
        };
    }

    /**
     * Wrapper method for Retrofits {@link Call#clone()} call, useful for getting call information
     * and allowing you to perform additional functions on this {@link NavigationRoute} class.
     *
     * @return cloned call
     * @since 1.0.0
     */
    public Call<DirectionsResponse> getCall() {
        return toMapLibreCall(mapboxDirections.cloneCall());
    }

    public void cancelCall() {
        getCall().cancel();
    }

    /**
     * This builder is used to create a new request to the Mapbox Directions API and removes options
     * which would cause this navigation SDK to not behave properly. At a bare minimum, your request
     * must include an access token, an origin, and a destination. All other fields can be left alone
     * inorder to use the default behaviour of the API.
     * <p>
     * By default, the directions profile is set to driving with traffic but can be changed to
     * reflect your users use-case.
     * </p>
     *
     * @since 0.5.0
     */
    public static final class Builder {

        private final MapboxDirections.Builder directionsBuilder;

        /**
         * Private constructor for initializing the raw MapboxDirections.Builder
         */
        private Builder() {
            directionsBuilder = MapboxDirections.builder();
        }

        /**
         * The username for the account that the directions engine runs on. In most cases, this should
         * always remain the default value of {@link DirectionsCriteria#PROFILE_DEFAULT_USER}.
         *
         * @param user a non-null string which will replace the default user used in the directions
         *             request
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder user(@NonNull String user) {
            directionsBuilder.user(user);
            return this;
        }

        /**
         * This selects which mode of transportation the user will be using while navigating from the
         * origin to the final destination. The options include driving, driving considering traffic,
         * walking, and cycling. Using each of these profiles will result in different routing biases.
         *
         * @param profile required to be one of the String values found in the {@link com.mapbox.api.directions.v5.DirectionsCriteria.ProfileCriteria}
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder profile(@NonNull @DirectionsCriteria.ProfileCriteria String profile) {
            directionsBuilder.profile(profile);
            return this;
        }

        /**
         * This sets the starting point on the map where the route will begin. It is one of the
         * required parameters which must be set for a successful directions response.
         *
         * @param origin a GeoJson {@link org.maplibre.geojson.Point} object representing the starting location for the route
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder origin(@NonNull org.maplibre.geojson.Point origin) {
            origin(origin, null, null);
            return this;
        }

        /**
         * This sets the starting point on the map where the route will begin. It is one of the
         * required parameters which must be set for a successful directions response.
         *
         * @param origin    a GeoJson {@link org.maplibre.geojson.Point} object representing the starting location for the
         *                  route
         * @param angle     double value used for setting the corresponding coordinate's angle of travel
         *                  when determining the route
         * @param tolerance the deviation the bearing angle can vary while determining the route,
         *                  recommended to be either 45 or 90 degree tolerance
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder origin(@NonNull org.maplibre.geojson.Point origin, @Nullable Double angle,
                              @Nullable Double tolerance) {
            directionsBuilder.origin(toMapboxPoint(origin));
            directionsBuilder.addBearing(angle, tolerance);
            return this;
        }

        /**
         * This sets the ending point on the map where the route will end. It is one of the required
         * parameters which must be set for a successful directions response.
         *
         * @param destination a GeoJson {@link org.maplibre.geojson.Point} object representing the starting location for the
         *                    route
         * @return this builder for chaining options together
         * @since 0.50
         */
        public Builder destination(@NonNull org.maplibre.geojson.Point destination) {
            destination(destination, null, null);
            return this;
        }

        /**
         * This sets the ending point on the map where the route will end. It is one of the required
         * parameters which must be set for a successful directions response.
         *
         * @param destination a GeoJson {@link org.maplibre.geojson.Point} object representing the starting location for the
         *                    route
         * @param angle       double value used for setting the corresponding coordinate's angle of travel
         *                    when determining the route
         * @param tolerance   the deviation the bearing angle can vary while determining the route,
         *                    recommended to be either 45 or 90 degree tolerance
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder destination(@NonNull org.maplibre.geojson.Point destination, @Nullable Double angle,
                                   @Nullable Double tolerance) {
            directionsBuilder.destination(toMapboxPoint(destination));
            directionsBuilder.addBearing(angle, tolerance);
            return this;
        }

        /**
         * This can be used to set up to 23 additional in-between points which will act as pit-stops
         * along the users route. Note that if you are using the
         * {@link DirectionsCriteria#PROFILE_DRIVING_TRAFFIC} that the max number of waypoints allowed
         * in the request is currently limited to 1.
         *
         * @param waypoint a {@link org.maplibre.geojson.Point} which represents the pit-stop or waypoint where you'd like
         *                 one of the {@link org.maplibre.navigation.android.navigation.v5.models.RouteLeg} to
         *                 navigate the user to
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder addWaypoint(@NonNull org.maplibre.geojson.Point waypoint) {
            directionsBuilder.addWaypoint(toMapboxPoint(waypoint));
            directionsBuilder.addBearing(null, null);
            return this;
        }

        /**
         * This can be used to set up to 23 additional in-between points which will act as pit-stops
         * along the users route. Note that if you are using the
         * {@link DirectionsCriteria#PROFILE_DRIVING_TRAFFIC} that the max number of waypoints allowed
         * in the request is currently limited to 1.
         *
         * @param waypoint  a {@link org.maplibre.geojson.Point} which represents the pit-stop or waypoint where you'd like
         *                  one of the {@link org.maplibre.navigation.android.navigation.v5.models.RouteLeg} to
         *                  navigate the user to
         * @param angle     double value used for setting the corresponding coordinate's angle of travel
         *                  when determining the route
         * @param tolerance the deviation the bearing angle can vary while determining the route,
         *                  recommended to be either 45 or 90 degree tolerance
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder addWaypoint(@NonNull org.maplibre.geojson.Point waypoint, @Nullable Double angle,
                                   @Nullable Double tolerance) {
            directionsBuilder.addWaypoint(toMapboxPoint(waypoint));
            directionsBuilder.addBearing(angle, tolerance);
            return this;
        }

        /**
         * Optionally, set which input coordinates should be treated as waypoints / separate legs.
         * Note: coordinate indices not added here act as silent waypoints
         * <p>
         * Most useful in combination with  steps=true and requests based on traces
         * with high sample rates. Can be an index corresponding to any of the input coordinates,
         * but must contain the first ( 0 ) and last coordinates' indices.
         * </p>
         *
         * @param waypointIndices a list of coordinate indices to be used as waypoints
         * @return this builder for chaining options together
         */
        public Builder waypointIndices(@NonNull List<Integer> waypointIndices) {
            directionsBuilder.waypointIndices(waypointIndices);
            return this;
        }

        /**
         * Optionally set whether to try to return alternative routes. An alternative is classified as a
         * route that is significantly different then the fastest route, but also still reasonably fast.
         * Not in all circumstances such a route exists. At the moment at most one alternative can be
         * returned.
         *
         * @param alternatives true if you'd like to receive an alternative route, otherwise false or
         *                     null to use the APIs default value
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder alternatives(@Nullable Boolean alternatives) {
            directionsBuilder.alternatives(alternatives);
            return this;
        }

        /**
         * Set the instruction language for the directions request, the default is english. Only a
         * select number of languages are currently supported, reference the table provided in the see
         * link below.
         *
         * @param language a Locale representing the language you'd like the instructions to be
         *                 written in when returned
         * @return this builder for chaining options together
         * @see <a href="https://www.mapbox.com/api-documentation/#instructions-languages">Supported
         * Languages</a>
         * @since 0.5.0
         */
        public Builder language(Locale language) {
            directionsBuilder.language(language);
            return this;
        }

        Builder language(Context context, LocaleUtils localeUtils) {
            directionsBuilder.language(localeUtils.inferDeviceLocale(context));
            return this;
        }

        /**
         * Whether or not to return additional metadata along the route. Possible values are:
         * {@link DirectionsCriteria#ANNOTATION_DISTANCE},
         * {@link DirectionsCriteria#ANNOTATION_DURATION},
         * {@link DirectionsCriteria#ANNOTATION_DURATION} and
         * {@link DirectionsCriteria#ANNOTATION_CONGESTION}. Several annotation can be used by
         * separating them with {@code ,}.
         * <p>
         * If left alone, this will automatically set Congestion to enabled
         * </p>
         *
         * @param annotations string referencing one of the annotation direction criteria's. The strings
         *                    restricted to one or multiple values inside the {@link org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria.AnnotationCriteria}
         *                    or null which will result in no annotations being used
         * @return this builder for chaining options together
         * @see <a href="https://www.mapbox.com/api-documentation/#routeleg-object">RouteLeg object
         * documentation</a>
         * @since 0.5.0
         */
        public Builder annotations(@Nullable @DirectionsCriteria.AnnotationCriteria String... annotations) {
            directionsBuilder.annotations(annotations);
            return this;
        }

        /**
         * Optionally, Use to filter the road segment the waypoint will be placed on by direction and
         * dictates the angle of approach. This option should always be used in conjunction with the
         * {@link #radiuses} parameter.
         * <p>
         * The parameter takes two values per waypoint: the first is an angle clockwise from true north
         * between 0 and 360. The second is the range of degrees the angle can deviate by. We recommend
         * a value of 45 degrees or 90 degrees for the range, as bearing measurements tend to be
         * inaccurate. This is useful for making sure we reroute vehicles on new routes that continue
         * traveling in their current direction. A request that does this would provide bearing and
         * radius values for the first waypoint and leave the remaining values empty. If provided, the
         * list of bearings must be the same length as the list of waypoints, but you can skip a
         * coordinate and show its position by passing in null value for both the angle and tolerance
         * values.
         * </p><p>
         * Each bearing value gets associated with the same order which coordinates are arranged in this
         * builder. For example, the first bearing added in this builder will be associated with the
         * origin {@code Point}, the nth bearing being associated with the nth waypoint added (if added)
         * and the last bearing being added will be associated with the destination.
         * </p><p>
         * If given the chance, you should pass in the bearing information at the same time the point is
         * passed in as a waypoint, this way it is ensured the value is matched up correctly with the
         * coordinate.
         *
         * @param angle     double value used for setting the corresponding coordinate's angle of travel
         *                  when determining the route
         * @param tolerance the deviation the bearing angle can vary while determining the route,
         *                  recommended to be either 45 or 90 degree tolerance
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder addBearing(@Nullable @FloatRange(from = 0, to = 360) Double angle,
                                  @Nullable @FloatRange(from = 0, to = 360) Double tolerance) {
            directionsBuilder.addBearing(angle, tolerance);
            return this;
        }

        /**
         * Optionally, set the maximum distance in meters that each coordinate is allowed to move when
         * snapped to a nearby road segment. There must be as many radiuses as there are coordinates in
         * the request. Values can be any number greater than 0 or they can be unlimited simply by
         * passing {@link Double#POSITIVE_INFINITY}.
         * <p>
         * If no routable road is found within the radius, a {@code NoSegment} error is returned.
         * </p>
         *
         * @param radiuses double array containing the radiuses defined in unit meters.
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder radiuses(@FloatRange(from = 0) List<Double> radiuses) {
            directionsBuilder.radiuses(radiuses);
            return this;
        }

        /**
         * Change the units used for voice announcements, this does not change the units provided in
         * other fields outside of the {@link org.maplibre.navigation.android.navigation.v5.models.VoiceInstructions}
         * object.
         *
         * @param voiceUnits one of the values found inside the {@link org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria.VoiceUnitCriteria}
         * @return this builder for chaining options together
         * @since 0.8.0
         */
        public Builder voiceUnits(@DirectionsCriteria.VoiceUnitCriteria String voiceUnits) {
            directionsBuilder.voiceUnits(voiceUnits);
            return this;
        }

        Builder voiceUnits(Context context, LocaleUtils localeUtils) {
            directionsBuilder.voiceUnits(localeUtils.getUnitTypeForDeviceLocale(context));
            return this;
        }

        /**
         * Exclude specific road classes such as highways, tolls, and more.
         *
         * @param exclude one of the values found inside the {@link DirectionsCriteria.ExcludeCriteria}
         * @return this builder for chaining options together
         * @since 0.8.0
         */
        public Builder exclude(@Nullable @DirectionsCriteria.ExcludeCriteria String exclude) {
            directionsBuilder.exclude(exclude);
            return this;
        }

        /**
         * Base package name or other simple string identifier. Used inside the calls user agent header.
         *
         * @param clientAppName base package name or other simple string identifier
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder clientAppName(@NonNull String clientAppName) {
            directionsBuilder.clientAppName(clientAppName);
            return this;
        }

        /**
         * Optionally adds and intercepter to the call.
         *
         * @param interceptor okhttp interceptor
         * @return this builder for chaining options together
         */
        public Builder interceptor(Interceptor interceptor) {
            directionsBuilder.interceptor(interceptor);
            return this;
        }

        /**
         * Required to call when this is being built. If no access token provided,
         * {@link NullPointerException} will be thrown.
         *
         * @param accessToken Mapbox access token, You must have a Mapbox account inorder to use
         *                    the Optimization API
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder accessToken(@NonNull String accessToken) {
            directionsBuilder.accessToken(accessToken);
            return this;
        }

        /**
         * Optionally change the APIs base URL to something other then the default Mapbox one.
         *
         * @param baseUrl base url used as end point
         * @return this builder for chaining options together
         * @since 0.5.0
         */
        public Builder baseUrl(String baseUrl) {
            directionsBuilder.baseUrl(baseUrl);
            return this;
        }

        /**
         * Indicates from which side of the road to approach a waypoint.
         * Accepts <tt>unrestricted</tt> (default), <tt>curb</tt> or <tt>null</tt>.
         * If set to <tt>unrestricted</tt>, the route can approach waypoints
         * from either side of the road. If set to <tt>curb</tt>, the route will be returned
         * so that on arrival, the waypoint will be found on the side that corresponds with the
         * <tt>driving_side</tt> of the region in which the returned route is located.
         * If provided, the list of approaches must be the same length as the list of waypoints.
         *
         * @param approaches null if you'd like the default approaches,
         *                   else one of the options found in
         *                   {@link DirectionsCriteria.ApproachesCriteria}.
         * @return this builder for chaining options together
         * @since 0.15.0
         */
        public Builder addApproaches(String... approaches) {
            directionsBuilder.addApproaches(approaches);
            return this;
        }

        /**
         * Custom names for waypoints used for the arrival instruction,
         * each separated by <tt>;</tt>. Values can be any string and total number of all characters cannot
         * exceed 500. If provided, the list of <tt>waypointNames</tt> must be the same length as the list of
         * coordinates, but you can skip a coordinate and show its position with the <tt>;</tt> separator.
         *
         * @param waypointNames Custom names for waypoints used for the arrival instruction.
         * @return this builder for chaining options together
         * @since 0.15.0
         */
        public Builder addWaypointNames(@Nullable String... waypointNames) {
            directionsBuilder.addWaypointNames(waypointNames);
            return this;
        }

        /**
         * Optionally create a {@link Builder} based on all variables
         * from given {@link RouteOptions}.
         * <p>
         * Note: {@link RouteOptions#bearings()} are excluded because it's better
         * to recalculate these at the time of the request, as your location bearing
         * is constantly changing.
         *
         * @param options containing all variables for request
         * @return this builder for chaining options together
         * @since 0.9.0
         */
        public Builder routeOptions(RouteOptions options) {

            if (!TextUtils.isEmpty(options.getBaseUrl())) {
                directionsBuilder.baseUrl(options.getBaseUrl());
            }

            if (!TextUtils.isEmpty(options.getLanguage())) {
                directionsBuilder.language(new Locale(options.getLanguage()));
            }

            if (options.getAlternatives() != null) {
                directionsBuilder.alternatives(options.getAlternatives());
            }

            if (!TextUtils.isEmpty(options.getProfile())) {
                directionsBuilder.profile(options.getProfile());
            }

            if (options.getAlternatives() != null) {
                directionsBuilder.alternatives(options.getAlternatives());
            }

            if (!TextUtils.isEmpty(options.getVoiceUnits())) {
                directionsBuilder.voiceUnits(options.getVoiceUnits());
            }

            if (!TextUtils.isEmpty(options.getUser())) {
                directionsBuilder.user(options.getUser());
            }

            if (!TextUtils.isEmpty(options.getAccessToken())) {
                directionsBuilder.accessToken(options.getAccessToken());
            }

            if (!TextUtils.isEmpty(options.getAnnotations())) {
                directionsBuilder.annotations(options.getAnnotations());
            }

            if (!TextUtils.isEmpty(options.getApproaches())) {
                String[] approaches = options.getApproaches().split(";");
                directionsBuilder.addApproaches(approaches);
            }

            if (!TextUtils.isEmpty(options.getWaypointNames())) {
                String[] waypointNames = options.getWaypointNames().split(";");
                directionsBuilder.addWaypointNames(waypointNames);
            }

            return this;
        }

        /**
         * This uses the provided parameters set using the {@link Builder} and adds the required
         * settings for navigation to work correctly.
         *
         * @return a new instance of Navigation Route
         * @since 0.5.0
         */
        public NavigationRoute build() {
            // Set the default values which the user cannot alter.
            directionsBuilder
                .steps(true)
                .continueStraight(true)
                .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .voiceInstructions(true)
                .bannerInstructions(true)
                .roundaboutExits(true);
            return new NavigationRoute(directionsBuilder.build());
        }
    }

    private static DirectionsResponse toMapLibreDirectionsResponse(com.mapbox.api.directions.v5.models.DirectionsResponse directionsResponse) {
        return DirectionsResponse.fromJson(directionsResponse.toJson());
    }

    private static com.mapbox.api.directions.v5.models.DirectionsResponse toMapboxDirectionsResponse(DirectionsResponse directionsResponse) {
        return com.mapbox.api.directions.v5.models.DirectionsResponse.fromJson(directionsResponse.toJson());
    }

    private static com.mapbox.geojson.Point toMapboxPoint(org.maplibre.geojson.Point point) {
        return com.mapbox.geojson.Point.fromLngLat(point.longitude(), point.latitude());
    }
}