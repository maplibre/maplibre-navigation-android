package com.mapbox.services.android.navigation.testapp

//import com.mapbox.api.directions.v5.models.DirectionsResponse
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mapbox.api.directions.v5.DirectionsAdapterFactory
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
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationRoute
import com.mapbox.services.android.navigation.v5.milestone.*
import com.mapbox.services.android.navigation.v5.models.DirectionsCriteria
import com.mapbox.services.android.navigation.v5.models.DirectionsResponse
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute
import com.mapbox.services.android.navigation.v5.models.RouteOptions
import com.mapbox.services.android.navigation.v5.navigation.*
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.IOException

class ValhallaMockNavigationActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    MapboxMap.OnMapClickListener {
    private lateinit var mapboxMap: MapboxMap

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
            getMapAsync(this@ValhallaMockNavigationActivity)
        }

        binding.startRouteButton.setOnClickListener {
            route?.let { route ->
                val userLocation = mapboxMap.locationComponent.lastKnownLocation ?: return@let
                val options = NavigationLauncherOptions.builder()
                    .directionsRoute(route)
                    .shouldSimulateRoute(simulateRoute)
                    .initialMapCameraPosition(CameraPosition.Builder().target(LatLng(userLocation.latitude, userLocation.longitude)).build())
                    .lightThemeResId(R.style.TestNavigationViewLight)
                    .darkThemeResId(R.style.TestNavigationViewDark)
                    .build()
                NavigationLauncher.startNavigation(this@ValhallaMockNavigationActivity, options)
            }
        }

        binding.simulateRouteSwitch.setOnCheckedChangeListener { _, checked ->
            simulateRoute = checked
        }

        binding.clearPoints.setOnClickListener {
            if (::mapboxMap.isInitialized) {
                mapboxMap.markers.forEach {
                    mapboxMap.removeMarker(it)
                }
            }
            destination = null
            waypoint = null
            it.visibility = View.GONE
            binding.startRouteLayout.visibility = View.GONE

            navigationMapRoute?.removeRoute()
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.Builder().fromUri(getString(R.string.map_style_light))) { style ->
            enableLocationComponent(style)
        }

        navigationMapRoute = NavigationMapRoute(
            binding.mapView,
            mapboxMap
        )

        mapboxMap.addOnMapClickListener(this)
        Snackbar.make(
            findViewById(R.id.container),
            "Tap map to place waypoint",
            Snackbar.LENGTH_LONG,
        ).show()
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        // Get an instance of the component
        locationComponent = mapboxMap.locationComponent

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
            mapboxMap.addMarker(MarkerOptions().position(point))
            binding.clearPoints.visibility = View.VISIBLE
        }
        calculateRoute()
        return true
    }

    private fun calculateRoute() {
        binding.startRouteLayout.visibility = View.GONE
        val userLocation = mapboxMap.locationComponent.lastKnownLocation
        val destination = destination
        if (userLocation == null) {
            Timber.d("calculateRoute: User location is null, therefore, origin can't be set.")
            return
        }

        if (destination == null) {
            Timber.d("calculateRoute: destination is null, therefore, origin can't be set.")
            return
        }

        val origin = Point.fromLngLat(userLocation.longitude, userLocation.latitude)
        if (TurfMeasurement.distance(origin, destination, TurfConstants.UNIT_METERS) < 50) {
            Timber.d("calculateRoute: distance < 50 m")
            binding.startRouteButton.visibility = View.GONE
            return
        }

        // Construct the request body using mapOf
        val requestBody = mapOf(
            "format" to "osrm",
            "costing" to "auto",
            "banner_instructions" to true,
            "voice_instructions" to true,
            "directions_options" to mapOf(
                "units" to "kilometers"
            ),
            "costing_options" to mapOf(
                "auto" to mapOf(
                    "shortest" to true
                )
            ),
            "locations" to listOf(
                mapOf(
                    "lon" to origin.longitude(),
                    "lat" to origin.latitude(),
                    "type" to "break"
                ),
                mapOf(
                    "lon" to destination.longitude(),
                    "lat" to destination.latitude(),
                    "type" to "break"
                )
            )
        )

        // Convert the map to JSON using Gson
        val requestBodyJson = Gson().toJson(requestBody)

        // Create OkHttp client
        val client = OkHttpClient()

        // Create request object. Requires valhalla_url to be set in developer-config.xml
        // Don't use this server in production, it is for demonstration purposes only:
        // <string name="valhalla_url" translatable="false">https://valhalla1.openstreetmap.de/route</string>
        val request = Request.Builder()
            .url(getString(R.string.valhalla_url))
            .post(requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        Timber.d("calculateRoute request will be enqueued")
        Timber.d(
            "calculateRoute requestBodyJson: %s",
            requestBodyJson
        )
        client.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Handle request failure
                Timber.e(e, "calculateRoute Failed to get route from ValhallaRouting")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (response.isSuccessful) {
                        Timber.e(
                            "calculateRoute to ValhallaRouting successful with status code: %s",
                            response.code
                        )
                        Timber.d("calculateRoute ValhallaRouting body: %s", response.body)
                        val jsonResponse = response.body!!.string()
                        Timber.d("calculateRoute ValhallaRouting response: %s", jsonResponse)
                        val maplibreResponse = DirectionsResponse.fromJson(jsonResponse);
                        this@ValhallaMockNavigationActivity.route = maplibreResponse.routes()
                            .first()
                            .toBuilder()
                            .routeOptions(
                                // This is not used but currently necessary to start the navigation:
                                RouteOptions.builder()
                                    .accessToken("valhalla")
                                    .voiceUnits(DirectionsCriteria.METRIC)
                                    .voiceInstructions(true)
                                    .bannerInstructions(true)
                                    .alternatives(false)
                                    .baseUrl(getString(R.string.base_url))
                                    .profile("valhalla")
                                    .user("valhalla")
                                    .coordinates(listOf(origin, destination))
                                    .requestUuid("0000-0000-0000-0000")
                                    .build()
                            )
                            .build()

                        runOnUiThread {
                            navigationMapRoute?.addRoutes(maplibreResponse.routes())
                            binding.startRouteLayout.visibility = View.VISIBLE
                        }
                    } else {
                        Timber.e("calculateRoute Request to Valhalla failed with status code: %s: %s", response.code, response.body)
                    }
                }
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
        if (::mapboxMap.isInitialized) {
            mapboxMap.removeOnMapClickListener(this)
        }
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}
