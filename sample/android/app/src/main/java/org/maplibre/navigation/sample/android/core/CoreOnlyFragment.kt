package org.maplibre.navigation.sample.android.core

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.OnLocationCameraTransitionListener
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.common.toJvm
import org.maplibre.geojson.model.LineString
import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.location.replay.ReplayRouteLocationEngine
import org.maplibre.navigation.core.location.toAndroidLocation
import org.maplibre.navigation.core.models.BannerInstructions
import org.maplibre.navigation.core.models.DirectionsResponse
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.models.RouteOptions
import org.maplibre.navigation.core.navigation.AndroidMapLibreNavigation
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.core.utils.Constants
import org.maplibre.navigation.sample.android.databinding.FragmentCoreOnlyBinding
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

class CoreOnlyFragment : Fragment() {

    private lateinit var binding: FragmentCoreOnlyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCoreOnlyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.flOverlayContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.map.getMapAsync { map ->
            map.setStyle(
                Style.Builder()
                    .fromUri("https://tiles.versatiles.org/assets/styles/colorful/style.json")
            ) { style ->
                loadRoute(map, style)
            }
        }

        binding.map.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        binding.map.onDestroy()
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    private fun loadRoute(map: MapLibreMap, style: Style) {
        binding.tvManuever.text = "Loading..."

        lifecycleScope.launch {
            val directionsResponse = fetchRoute()
            val route = directionsResponse.routes.first().copy(
                routeOptions = RouteOptions(
                    // These dummy route options are not not used to create directions,
                    // but currently they are necessary to start the navigation
                    // and to use the banner & voice instructions.
                    // Again, this isn't ideal, but it is a requirement of the framework.
                    baseUrl = "https://valhalla.routing",
                    profile = "valhalla",
                    user = "valhalla",
                    accessToken = "valhalla",
                    voiceInstructions = true,
                    bannerInstructions = true,
                    language = "en-US",
                    coordinates = listOf(
                        Point(9.6935451, 52.3758408),
                        Point(9.9769191, 53.5426183)
                    ),
                    requestUuid = "0000-0000-0000-0000"
                )
            )

            enableLocationComponent(map, style)

            val locationEngine = ReplayRouteLocationEngine()
            val options = MapLibreNavigationOptions(
                defaultMilestonesEnabled = true
                // Do sample stuff here
            )

            val mlNavigation = AndroidMapLibreNavigation(
                context = requireContext(),
                locationEngine = locationEngine, // Disable this, to use the real-world system location engine
                options = options
            )
            mlNavigation.addProgressChangeListener { location, routeProgress ->
                // Use `toAndroidLocation()` extension to convert the generic cross-platform location to a native Android one
                map.locationComponent.forceLocationUpdate(location.toAndroidLocation())


                routeProgress.currentLegProgress.currentStep.bannerInstructions?.first()
                    ?.let { bannerInstruction: BannerInstructions ->
                        val remainingStepDistanceMeters =
                            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining
                        binding.tvManuever.text =
                            "${remainingStepDistanceMeters.roundToInt()}m : ${bannerInstruction.primary.type}+${bannerInstruction.primary.modifier} ${bannerInstruction.primary.text}"
                    }
            }

            drawRoute(style, route)
            locationEngine.assign(route)
            mlNavigation.startNavigation(route)
        }
    }

    private suspend fun fetchRoute(): DirectionsResponse = suspendCoroutine { continuation ->
        val provider = "valhalla"

        val requestBody = if provider == "graphhopper" {
            mapOf(
                "type" to "mapbox",
                "profile" to "car",
                "locale" to "en-US",
                "points" to listOf(
                    // Hannover, Germany
                    listOf(9.6935451, 52.3758408),
                    // Hamburg, Germany
                    listOf(9.9769191, 53.5426183)
                )
                // flexible options possible via "custom_model"
            )
        } else {
            mapOf(
                "format" to "osrm",
                "costing" to "auto",
                "banner_instructions" to true,
                "voice_instructions" to true,
                "language" to "en-US",
                "directions_options" to mapOf(
                    "units" to "kilometers"
                ),
                "costing_options" to mapOf(
                    "auto" to mapOf(
                        "top_speed" to 130
                    )
                ),
                "locations" to listOf(
                    // Hannover, Germany
                    mapOf(
                        "lon" to 9.6935451,
                        "lat" to 52.3758408,
                        "type" to "break"
                    ),
                    // Hamburg, Germany
                    mapOf(
                        "lon" to 9.9769191,
                        "lat" to 53.5426183,
                        "type" to "break"
                    )
                )
            )
        }

        val requestBodyJson = Gson().toJson(requestBody)
        val client = OkHttpClient()

        val url = if (provider == "valhalla") "https://valhalla1.openstreetmap.de/route"
            else "https://graphhopper.com/api/1/navigate?key=7088b84f-4cee-4059-96de-fd0cbda2fdff"

        val request = Request.Builder()
            .header("User-Agent", "ML Nav - Android Sample App")
            .url(url)
            .post(requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val directionsResponse = DirectionsResponse.fromJson(response.body!!.string())
                continuation.resume(directionsResponse)
            }
        })
    }

    private fun drawRoute(style: Style, route: DirectionsRoute) {
        val routeLine = LineString(route.geometry, Constants.PRECISION_6)

        // The `toJvm()` extension converts the LineString to the deprecated Jvm one.
        val routeSource = GeoJsonSource("route-source", routeLine.toJvm())
        style.addSource(routeSource)

        val routeLayer = LineLayer("route-layer", "route-source")
            .withProperties(
                lineWidth(5f),
                lineColor(Color.BLUE)
            )

        style.addLayer(routeLayer)
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(map: MapLibreMap, style: Style) {
        map.locationComponent.activateLocationComponent(
            LocationComponentActivationOptions.builder(requireContext(), style)
                .useDefaultLocationEngine(false)
                .build()
        )

        followLocation(map)

        map.locationComponent.isLocationComponentEnabled = true
    }

    private fun followLocation(map: MapLibreMap) {
        if (!map.locationComponent.isLocationComponentActivated) {
            return
        }

        map.locationComponent.renderMode = RenderMode.GPS
        map.locationComponent.setCameraMode(
            CameraMode.TRACKING_GPS,
            object :
                OnLocationCameraTransitionListener {
                override fun onLocationCameraTransitionFinished(cameraMode: Int) {
                    map.locationComponent.zoomWhileTracking(17.0)
                    map.locationComponent.tiltWhileTracking(60.0)
                }

                override fun onLocationCameraTransitionCanceled(cameraMode: Int) {}
            }
        )
    }
}