package org.maplibre.navigation.android.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.geojson.Point
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.navigation.android.navigation.ui.v5.NavigationLauncher
import org.maplibre.navigation.android.navigation.ui.v5.NavigationLauncherOptions
import org.maplibre.navigation.android.navigation.ui.v5.route.NavigationRoute
import org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement
import okhttp3.Request
import org.maplibre.navigation.android.example.databinding.ActivityNavigationUiBinding
import org.maplibre.navigation.android.navigation.ui.v5.route.NavigationMapRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class NavigationUIActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    MapLibreMap.OnMapClickListener {
    private lateinit var mapLibreMap: MapLibreMap

    // Navigation related variables
    private var route: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var destination: Point? = null
    private var waypoint: Point? = null
    private var locationComponent: LocationComponent? = null

    private lateinit var binding: ActivityNavigationUiBinding

    private var simulateRoute = false

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        binding = ActivityNavigationUiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@NavigationUIActivity)
        }

        binding.startRouteButton.setOnClickListener {
            route?.let { route ->
                val userLocation = mapLibreMap.locationComponent.lastKnownLocation ?: return@let

                val options = NavigationLauncherOptions.builder()
                    .directionsRoute(route)
                    .shouldSimulateRoute(simulateRoute)
                    .initialMapCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(userLocation.latitude, userLocation.longitude)).build()
                    )
                    .lightThemeResId(R.style.TestNavigationViewLight)
                    .darkThemeResId(R.style.TestNavigationViewDark)
                    .build()
                NavigationLauncher.startNavigation(this@NavigationUIActivity, options)
            }
        }

        binding.simulateRouteSwitch.setOnCheckedChangeListener { _, checked ->
            simulateRoute = checked
        }


        binding.clearPoints.setOnClickListener {
            if (::mapLibreMap.isInitialized) {
                mapLibreMap.markers.forEach {
                    mapLibreMap.removeMarker(it)
                }
            }
            destination = null
            waypoint = null
            it.visibility = View.GONE
            binding.startRouteLayout.visibility = View.GONE

            navigationMapRoute?.removeRoute()
        }
    }

    override fun onMapReady(mapLibreMap: MapLibreMap) {
        this.mapLibreMap = mapLibreMap
        mapLibreMap.setStyle(
            Style.Builder().fromUri(getString(R.string.map_style_light))
        ) { style ->
            enableLocationComponent(style)
            navigationMapRoute = NavigationMapRoute(binding.mapView, mapLibreMap)

            mapLibreMap.addOnMapClickListener(this)

            Snackbar.make(
                findViewById(R.id.container),
                "Tap map to place waypoint",
                Snackbar.LENGTH_LONG,
            ).show()
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        // Get an instance of the component
        locationComponent = mapLibreMap.locationComponent

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
        var addMarker = true
        when {
            destination == null -> destination = Point.fromLngLat(point.longitude, point.latitude)
            waypoint == null -> waypoint = Point.fromLngLat(point.longitude, point.latitude)
            else -> {
                Toast.makeText(this, "Only 2 waypoints supported", Toast.LENGTH_LONG).show()
                addMarker = false
            }
        }

        if (addMarker) {
            mapLibreMap.addMarker(MarkerOptions().position(point))
            binding.clearPoints.visibility = View.VISIBLE
        }
        calculateRoute()
        return true
    }

    private fun calculateRoute() {
        binding.startRouteLayout.visibility = View.GONE
        val userLocation = mapLibreMap.locationComponent.lastKnownLocation
        val destination = destination
        if (userLocation == null) {
            Timber.d("calculateRoute: User location is null, therefore, origin can't be set.")
            return
        }

        if (destination == null) {
            return
        }

        val origin = Point.fromLngLat(userLocation.longitude, userLocation.latitude)
        if (TurfMeasurement.distance(origin, destination, TurfConstants.UNIT_METERS) < 50) {
            binding.startRouteLayout.visibility = View.GONE
            return
        }

        val navigationRouteBuilder = NavigationRoute.builder(this).apply {
            this.accessToken(getString(R.string.mapbox_access_token))
            this.origin(origin)
            this.destination(destination)
            this.voiceUnits(DirectionsCriteria.METRIC)
            this.alternatives(true)
            // If you are using this with the GraphHopper Directions API, you need to uncomment user and profile here.
            //this.user("gh")
            //this.profile("car")
            this.baseUrl(getString(R.string.base_url))
        }

        navigationRouteBuilder.build().getRoute(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>,
            ) {
                Timber.d("Url: %s", (call.request() as Request).url.toString())
                response.body()?.let { response ->
                    if (response.routes.isNotEmpty()) {
                        val maplibreResponse = DirectionsResponse.fromJson(response.toJson());
                        this@NavigationUIActivity.route = maplibreResponse.routes.first()
                        navigationMapRoute?.addRoutes(maplibreResponse.routes)
                        binding.startRouteLayout.visibility = View.VISIBLE
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
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mapLibreMap.isInitialized) {
            mapLibreMap.removeOnMapClickListener(this)
        }
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}
