package org.maplibre.navigation.android.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
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
import org.maplibre.navigation.android.example.databinding.ActivityNavigationUiBinding
import org.maplibre.navigation.android.navigation.ui.v5.NavigationLauncher
import org.maplibre.navigation.android.navigation.ui.v5.NavigationLauncherOptions
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions
import org.maplibre.navigation.android.navigation.v5.navigation.*
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.IOException
import java.util.Locale

class ValhallaNavigationActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    MapLibreMap.OnMapClickListener {
    private lateinit var mapLibreMap: MapLibreMap

    // Navigation related variables
    private var language = Locale.getDefault().language
    private var route: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var destination: Point? = null
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
            getMapAsync(this@ValhallaNavigationActivity)
        }

        binding.startRouteButton.setOnClickListener {
            route?.let { route ->
                val userLocation = mapLibreMap.locationComponent.lastKnownLocation ?: return@let
                val options = NavigationLauncherOptions.builder()
                    .directionsRoute(route)
                    .shouldSimulateRoute(simulateRoute)
                    .initialMapCameraPosition(CameraPosition.Builder().target(LatLng(userLocation.latitude, userLocation.longitude)).build())
                    .lightThemeResId(R.style.TestNavigationViewLight)
                    .darkThemeResId(R.style.TestNavigationViewDark)
                    .build()
                NavigationLauncher.startNavigation(this@ValhallaNavigationActivity, options)
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
            it.visibility = View.GONE
            binding.startRouteLayout.visibility = View.GONE

            navigationMapRoute?.removeRoute()
        }
    }

    override fun onMapReady(mapLibreMap: MapLibreMap) {
        this.mapLibreMap = mapLibreMap
        mapLibreMap.setStyle(Style.Builder().fromUri(getString(R.string.map_style_light))) { style ->
            enableLocationComponent(style)
        }

        navigationMapRoute = NavigationMapRoute(
            binding.mapView,
            mapLibreMap
        )

        mapLibreMap.addOnMapClickListener(this)
        Snackbar.make(
            findViewById(R.id.container),
            "Tap map to place destination",
            Snackbar.LENGTH_LONG,
        ).show()
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
        destination = Point.fromLngLat(point.longitude, point.latitude)

        mapLibreMap.addMarker(MarkerOptions().position(point))
        binding.clearPoints.visibility = View.VISIBLE
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
            Timber.d("calculateRoute: destination is null, therefore, destination can't be set.")
            return
        }

        val origin = Point.fromLngLat(userLocation.longitude, userLocation.latitude)
        if (TurfMeasurement.distance(origin, destination, TurfConstants.UNIT_METERS) < 50) {
            Timber.d("calculateRoute: distance < 50 m")
            binding.startRouteButton.visibility = View.GONE
            return
        }

        // The full Valhalla API is documented here:
        // https://valhalla.github.io/valhalla/api/turn-by-turn/api-reference/

        // It would be better if there was a proper ValhallaService which uses retrofit to
        // generate the API call similar to the DirectionsService for the Mapbox API:
        // https://github.com/mapbox/mapbox-java/blob/main/services-directions/src/main/java/com/mapbox/api/directions/v5/DirectionsService.java
        // That would allow us to skip adding fake attributes further down as well.
        // But this is the first step to show how the newly added banner_instructions
        // and voice_instructions of Valhalla can be used to generate directions directly:
        val requestBody = mapOf(
            "format" to "osrm",
            "costing" to "auto",
            "banner_instructions" to true,
            "voice_instructions" to true,
            "language" to language,
            "directions_options" to mapOf(
                "units" to "kilometers"
            ),
            "costing_options" to mapOf(
                "auto" to mapOf(
                    "top_speed" to 130
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

        val requestBodyJson = Gson().toJson(requestBody)
        val client = OkHttpClient()

        // Create request object. Requires valhalla_url to be set in developer-config.xml
        // Don't use the following server in production, it is for demonstration purposes only:
        // <string name="valhalla_url" translatable="false">https://valhalla1.openstreetmap.de/route</string>
        val request = Request.Builder()
            .header("User-Agent", "MapLibre Android Navigation SDK Demo App")
            .url(getString(R.string.valhalla_url))
            .post(requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        Timber.d("calculateRoute enqueued requestBodyJson: %s", requestBodyJson)
        client.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Timber.e(e, "calculateRoute Failed to get route from ValhallaRouting")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (response.isSuccessful) {
                        Timber.e("calculateRoute to ValhallaRouting successful with status code: %s", response.code)
                        val responseBodyJson = response.body!!.string()
                        Timber.d("calculateRoute ValhallaRouting responseBodyJson: %s", responseBodyJson)
                        val maplibreResponse = DirectionsResponse.fromJson(responseBodyJson);
                        this@ValhallaNavigationActivity.route = maplibreResponse.routes()
                            .first()
                            .toBuilder()
                            .routeOptions(
                                // These dummy route options are not not used to create directions,
                                // but currently they are necessary to start the navigation
                                // and to use the voice instructions.
                                // Again, this isn't ideal, but it is a requirement of the framework.
                                RouteOptions.builder()
                                    .baseUrl("https://valhalla.routing")
                                    .profile("valhalla")
                                    .user("valhalla")
                                    .accessToken("valhalla")
                                    .voiceInstructions(true)
                                    .bannerInstructions(true)
                                    .language(language)
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
