import UIKit

import MapLibre
import MapLibreNavigationCore
import Alamofire

class CoreOnlyViewController: UIViewController, MLNMapViewDelegate {
//    var mapView: MLNMapView!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        let mapView = MLNMapView(frame: view.bounds)
        mapView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        
        mapView.delegate = self
        mapView.styleURL = URL(string: "https://tiles.versatiles.org/assets/styles/colorful.json")
        
        view.addSubview(mapView)
    }

    func mapView(map: MLNMapView, didFinishLoading _: MLNStyle) {
    }
    
    private func loadRoute(map: MLNMapView, style: MLNStyle) {
        //        binding.tvManuever.text = "Loading..."
        
        fetchRoute()
    }
    
    
    private func fetchRoute() {
        let requestBody: [String : Any] = [
            "format": "osrm",
            "costing": "auto",
            "banner_instructions": true,
            "voice_instructions": true,
            "language": "en-US",
            "directions_options": [
                "units": "kilometers"
            ],
            "costing_options": [
                "auto" : [
                    "top_speed": 130
                ]
            ],
            "locations": [
                // Hannover, Germany
                [
                    "lon": 9.6935451,
                    "lat": 52.3758408,
                    "type": "break"
                ],
                // Hamburg, Germany
                [
                    "lon": 9.9769191,
                    "lat": 53.5426183,
                    "type": "break"
                ]
            ]
          ]
        
        AF.request(
            "https://valhalla1.openstreetmap.de/route",
            method: .post,
            parameters: requestBody,
            headers: ["User-Agent": "ML Nav - Android Sample App"],
            encoder: JSONParameterEncoder.default
        ).response { response in
            debugPrint(response)
        }
                   
                   
                   
                   
                   //        val requestBody = mapOf(
                   
                   //        )
                   //
                   //        val requestBodyJson = Gson().toJson(requestBody)
                   //        val client = OkHttpClient()
                   //
                   //        val request = Request.Builder()
                   //            .header("User-Agent", "ML Nav - Android Sample App")
                   //            .url("https://valhalla1.openstreetmap.de/route")
                   //            .post(requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
                   //            .build()
                   //
                   //        client.newCall(request).enqueue(object : okhttp3.Callback {
                   //            override fun onFailure(call: Call, e: IOException) {
                   //                continuation.resumeWithException(e)
                   //            }
                   //
                   //            override fun onResponse(call: Call, response: Response) {
                   //                val directionsResponse = DirectionsResponse.fromJson(response.body!!.string())
                   //                continuation.resume(directionsResponse)
                   //            }
                   //        })
        
//        // create the URL
//           let url = URL(string: "https://jsonplaceholder.typicode.com/todos/1")! //change the URL
//                
//           // create the session object
//           let session = URLSession.shared
//                
//           //Now create the URLRequest object using the URL object
//           let request = URLRequest(url: url)
//                
//           // create dataTask using the session object to send data to the server
//           let task = session.dataTask(with: request as URLRequest, completionHandler: { data, response, error in
//                    
//               guard error == nil else {
//                   return
//               }
//                    
//               guard let data = data else {
//                   return
//               }
//                    
//              do {
//                 //create json object from data
//                 if let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? [String: Any] {
//                    print(json)
//                 }
//              } catch let error {
//                print(error.localizedDescription)
//              }
//           })
//
//           task.resume()
    }
}


//class CoreOnlyFragment : Fragment() {
//
//    private lateinit var binding: FragmentCoreOnlyBinding
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentCoreOnlyBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        ViewCompat.setOnApplyWindowInsetsListener(binding.flOverlayContainer) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        binding.map.getMapAsync { map ->
//            map.setStyle(
//                Style.Builder()
//                    .fromUri("https://tiles.versatiles.org/assets/styles/colorful.json")
//            ) { style ->
//                loadRoute(map, style)
//            }
//        }
//
//        binding.map.onCreate(savedInstanceState)
//    }
//
//    override fun onDestroy() {
//        binding.map.onDestroy()
//        super.onDestroy()
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun loadRoute(map: MapLibreMap, style: Style) {
//        binding.tvManuever.text = "Loading..."
//
//        lifecycleScope.launch {
//            val directionsResponse = fetchRoute()
//            val route = directionsResponse.routes.first().copy(
//                routeOptions = RouteOptions(
//                    // These dummy route options are not not used to create directions,
//                    // but currently they are necessary to start the navigation
//                    // and to use the banner & voice instructions.
//                    // Again, this isn't ideal, but it is a requirement of the framework.
//                    baseUrl = "https://valhalla.routing",
//                    profile = "valhalla",
//                    user = "valhalla",
//                    accessToken = "valhalla",
//                    voiceInstructions = true,
//                    bannerInstructions = true,
//                    language = "en-US",
//                    coordinates = listOf(
//                        Point(9.6935451, 52.3758408),
//                        Point(9.9769191, 53.5426183)
//                    ),
//                    requestUuid = "0000-0000-0000-0000"
//                )
//            )
//
//            enableLocationComponent(map, style)
//
//            val locationEngine = ReplayRouteLocationEngine()
//            val options = MapLibreNavigationOptions(
//                defaultMilestonesEnabled = true
//                // Do sample stuff here
//            )
//
//            val mlNavigation = AndroidMapLibreNavigation(
//                context = requireContext(),
//                locationEngine = locationEngine, // Disable this, to use the real-world system location engine
//                options = options
//            )
//            mlNavigation.addProgressChangeListener { location, routeProgress ->
//                // Use `toAndroidLocation()` extension to convert the generic cross-platform location to a native Android one
//                map.locationComponent.forceLocationUpdate(location.toAndroidLocation())
//
//
//                routeProgress.currentLegProgress.currentStep.bannerInstructions?.first()
//                    ?.let { bannerInstruction: BannerInstructions ->
//                        val remainingStepDistanceMeters =
//                            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining
//                        binding.tvManuever.text =
//                            "${remainingStepDistanceMeters.roundToInt()}m : ${bannerInstruction.primary.type}+${bannerInstruction.primary.modifier} ${bannerInstruction.primary.text}"
//                    }
//            }
//
//            drawRoute(style, route)
//            locationEngine.assign(route)
//            mlNavigation.startNavigation(route)
//        }
//    }
//
//    private suspend fun fetchRoute(): DirectionsResponse = suspendCoroutine { continuation ->
//        val requestBody = mapOf(
//            "format" to "osrm",
//            "costing" to "auto",
//            "banner_instructions" to true,
//            "voice_instructions" to true,
//            "language" to "en-US",
//            "directions_options" to mapOf(
//                "units" to "kilometers"
//            ),
//            "costing_options" to mapOf(
//                "auto" to mapOf(
//                    "top_speed" to 130
//                )
//            ),
//            "locations" to listOf(
//                // Hannover, Germany
//                mapOf(
//                    "lon" to 9.6935451,
//                    "lat" to 52.3758408,
//                    "type" to "break"
//                ),
//                // Hamburg, Germany
//                mapOf(
//                    "lon" to 9.9769191,
//                    "lat" to 53.5426183,
//                    "type" to "break"
//                )
//            )
//        )
//
//        val requestBodyJson = Gson().toJson(requestBody)
//        val client = OkHttpClient()
//
//        val request = Request.Builder()
//            .header("User-Agent", "ML Nav - Android Sample App")
//            .url("https://valhalla1.openstreetmap.de/route")
//            .post(requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
//            .build()
//
//        client.newCall(request).enqueue(object : okhttp3.Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                continuation.resumeWithException(e)
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val directionsResponse = DirectionsResponse.fromJson(response.body!!.string())
//                continuation.resume(directionsResponse)
//            }
//        })
//    }
//
//    private fun drawRoute(style: Style, route: DirectionsRoute) {
//        val routeLine = LineString(route.geometry, Constants.PRECISION_6)
//
//        // The `toJvm()` extension converts the LineString to the deprecated Jvm one.
//        val routeSource = GeoJsonSource("route-source", routeLine.toJvm())
//        style.addSource(routeSource)
//
//        val routeLayer = LineLayer("route-layer", "route-source")
//            .withProperties(
//                lineWidth(5f),
//                lineColor(Color.BLUE)
//            )
//
//        style.addLayer(routeLayer)
//    }
//
//    @SuppressWarnings("MissingPermission")
//    private fun enableLocationComponent(map: MapLibreMap, style: Style) {
//        map.locationComponent.activateLocationComponent(
//            LocationComponentActivationOptions.builder(requireContext(), style)
//                .useDefaultLocationEngine(false)
//                .build()
//        )
//
//        followLocation(map)
//
//        map.locationComponent.isLocationComponentEnabled = true
//    }
//
//    private fun followLocation(map: MapLibreMap) {
//        if (!map.locationComponent.isLocationComponentActivated) {
//            return
//        }
//
//        map.locationComponent.renderMode = RenderMode.GPS
//        map.locationComponent.setCameraMode(
//            CameraMode.TRACKING_GPS,
//            object :
//                OnLocationCameraTransitionListener {
//                override fun onLocationCameraTransitionFinished(cameraMode: Int) {
//                    map.locationComponent.zoomWhileTracking(17.0)
//                    map.locationComponent.tiltWhileTracking(60.0)
//                }
//
//                override fun onLocationCameraTransitionCanceled(cameraMode: Int) {}
//            }
//        )
//    }
//}
