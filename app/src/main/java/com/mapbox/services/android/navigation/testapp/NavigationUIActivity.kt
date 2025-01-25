package com.mapbox.services.android.navigation.testapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.services.android.navigation.testapp.databinding.ActivityNavigationUiBinding
import com.mapbox.services.android.navigation.ui.v5.MapRouteData
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener
import com.mapbox.services.android.navigation.ui.v5.route.NavigationRoute
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants
import com.mapbox.services.android.navigation.v5.navigation.NavigationMapRoute
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class NavigationUIActivity :
    ComponentActivity(),
    MapboxMap.OnMapClickListener, OnNavigationReadyCallback,
    NavigationListener {

    // Navigation related variables
    private var route: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var destination: Point? = null
    private var waypoint: Point? = null
    private var locationComponent: LocationComponent? = null

    private lateinit var binding: ActivityNavigationUiBinding

    private var simulateRoute = false

    private var isStarted = false

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        binding = ActivityNavigationUiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.navigationView.onCreate(this, savedInstanceState, this, "https://api.maptiler.com/maps/streets-v2-dark/style.json?key=ZkZSWT2Q0ta4f3S1VyrZ")

//        binding.mapView.apply {
//            onCreate(savedInstanceState)
//            getMapAsync(this@NavigationUIActivity)
//        }

        binding.simulateRouteSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                binding.navigationView.resetCameraPosition()
            } else {
                binding.navigationView.updateCameraRouteOverview()
            }
        }

        binding.startRouteButton.setOnClickListener {
            if (isStarted) {
                binding.navigationView.stopNavigation()
                isStarted = false
            } else {
                val points = mutableListOf<Pair<Double, Double>>()
                points.add(Pair(76.930137, 43.230361))
                points.add(Pair(76.928316, 43.236109))
                points.add(Pair(76.920187, 43.236783))
                binding.navigationView.calculateRoute(
                    MapRouteData(
                        "pk.cb243ba3-cb94-41c4-a91f-5f3074824d3a",
                        "getString(R.string.mapbox_access_token)",
                        points,
                        Pair(76.930137, 43.230361),
                        MapRouteData.DARK_THEME
                    )
                )
                isStarted = true
            }
        }

//        binding.clearPoints.setOnClickListener {
//            if (::mapboxMap.isInitialized) {
//                mapboxMap.markers.forEach {
//                    mapboxMap.removeMarker(it)
//                }
//            }
//            destination = null
//            waypoint = null
//            it.visibility = View.GONE
//            binding.startRouteLayout.visibility = View.GONE
//
//            navigationMapRoute?.removeRoute()
//        }
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        // Get an instance of the component
//        locationComponent = mapboxMap.locationComponent

        locationComponent?.let {
            // Activate with a built LocationComponentActivationOptions object
            it.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, style).build(),
            )

            // Enable to make component visible
            it.isLocationComponentEnabled = true

            // Set the component's camera mode
            it.cameraMode = CameraMode.TRACKING_GPS_NORTH

            // Set the component's render mode
            it.renderMode = RenderMode.NORMAL
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
//        var addMarker = true
//        when {
//            destination == null -> destination = Point.fromLngLat(point.longitude, point.latitude)
//            waypoint == null -> waypoint = Point.fromLngLat(point.longitude, point.latitude)
//            else -> {
//                Toast.makeText(this, "Only 2 waypoints supported", Toast.LENGTH_LONG).show()
//                addMarker = false
//            }
//        }
//
//        if (addMarker) {
//            mapboxMap.addMarker(MarkerOptions().position(point))
//            binding.clearPoints.visibility = View.VISIBLE
//        }
//        calculateRoute()
        return true
    }

    private fun calculateRoute() {
//        binding.startRouteLayout.visibility = View.GONE
//        val userLocation = mapboxMap.locationComponent.lastKnownLocation
        val destination = destination
//        if (userLocation == null) {
//            Timber.d("calculateRoute: User location is null, therefore, origin can't be set.")
//            return
//        }

        if (destination == null) {
            return
        }

//        val origin = Point.fromLngLat(userLocation.longitude, userLocation.latitude)
//        if (TurfMeasurement.distance(origin, destination, TurfConstants.UNIT_METERS) < 50) {
//            binding.startRouteLayout.visibility = View.GONE
//            return
//        }

        val navigationRouteBuilder = NavigationRoute.builder(this).apply {
            this.accessToken(getString(R.string.mapbox_access_token))
//            this.origin(origin)
            this.destination(destination)
            this.voiceUnits(DirectionsCriteria.METRIC)
            this.alternatives(true)
            // If you are using this with the GraphHopper Directions API, you need to uncomment user and profile here.
            //this.user("gh")
            //this.profile("car")
            this.baseUrl(getString(R.string.base_url))
        }


        navigationRouteBuilder.addWaypoint(
            Point.fromLngLat(76.930137, 43.230361)
        )

        navigationRouteBuilder.addWaypoint(
            Point.fromLngLat(76.928316, 43.236109)
        )

        navigationRouteBuilder.addWaypoint(
            Point.fromLngLat(76.920187, 43.236783)
        )


        navigationRouteBuilder.build().getRoute(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>,
            ) {
                Timber.d("Url: %s", (call.request() as Request).url.toString())
                response.body()?.let { response ->
                    if (response.routes().isNotEmpty()) {
                        val maplibreResponse =
                            com.mapbox.services.android.navigation.v5.models.DirectionsResponse.fromJson(
                                response.toJson()
                            );
                        this@NavigationUIActivity.route = maplibreResponse.routes().first()
                        navigationMapRoute?.addRoutes(maplibreResponse.routes())
//                        binding.startRouteLayout.visibility = View.VISIBLE
                    }
                }

            }

            override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                Timber.e(throwable, "onFailure: navigation.getRoute()")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.navigationView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.navigationView.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.navigationView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.navigationView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.navigationView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (::mapboxMap.isInitialized) {
//            mapboxMap.removeOnMapClickListener(this)
//        }
//        binding.navigationView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.navigationView.onSaveInstanceState(outState)
    }

    override fun onNavigationReady(isRunning: Boolean) {
        val options = NavigationViewOptions.builder()
        options.navigationListener(this)
        extractRoute(options)
        extractConfiguration(options)
        options.navigationOptions(MapboxNavigationOptions.builder().build())
        binding.navigationView.startNavigation(options.build())
    }

    override fun onCancelNavigation() {
        finishNavigation()
    }

    override fun onNavigationFinished() {
        finishNavigation()
    }

    override fun onNavigationRunning() {
        // Intentionally empty
    }

    override fun onMapReadyCallback() {
        // Intentionally empty
    }

    private fun extractRoute(options: NavigationViewOptions.Builder) {
        val route = NavigationLauncher.extractRoute(this)
        options.directionsRoute(route)
    }

    private fun extractConfiguration(options: NavigationViewOptions.Builder) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        options.shouldSimulateRoute(
            preferences.getBoolean(
                NavigationConstants.NAVIGATION_VIEW_SIMULATE_ROUTE,
                false
            )
        )
    }

    private fun finishNavigation() {
//        NavigationLauncher.cleanUpPreferences(this)
        binding.navigationView.onDestroy()
//        finish()
    }
}
