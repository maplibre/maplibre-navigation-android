package org.maplibre.navigation.core.navigation

import org.maplibre.navigation.core.location.engine.AppleLocationEngine
import org.maplibre.navigation.core.location.engine.LocationEngine
import org.maplibre.navigation.core.navigation.camera.Camera
import org.maplibre.navigation.core.navigation.camera.SimpleCamera
import org.maplibre.navigation.core.offroute.OffRoute
import org.maplibre.navigation.core.offroute.OffRouteDetector
import org.maplibre.navigation.core.route.FasterRoute
import org.maplibre.navigation.core.route.FasterRouteDetector
import org.maplibre.navigation.core.snap.Snap
import org.maplibre.navigation.core.snap.SnapToRoute
import org.maplibre.navigation.core.utils.RouteUtils

/**
 * A Android platform specific wrapper for [MapLibreNavigation].
 *
 * You can also use [MapLibreNavigation] directly, but this leads to more configuration.
 *
 * Currently the only difference is, that the location engine is created depending on
 * your dependencies.
 */
class IOSMapLibreNavigation(
    options: MapLibreNavigationOptions = MapLibreNavigationOptions(),
    locationEngine: LocationEngine = AppleLocationEngine(),
    cameraEngine: Camera = SimpleCamera(),
    snapEngine: Snap = SnapToRoute(),
    offRouteEngine: OffRoute = OffRouteDetector(),
    fasterRouteEngine: FasterRoute = FasterRouteDetector(options),
    routeUtils: RouteUtils = RouteUtils(),
) : MapLibreNavigation(
    options = options,
    locationEngine = locationEngine,
    cameraEngine = cameraEngine,
    snapEngine = snapEngine,
    offRouteEngine = offRouteEngine,
    fasterRouteEngine = fasterRouteEngine,
    routeUtils = routeUtils
)
