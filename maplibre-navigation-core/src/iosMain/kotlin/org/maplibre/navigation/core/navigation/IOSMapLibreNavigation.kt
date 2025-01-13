package org.maplibre.navigation.core.navigation

import org.maplibre.navigation.core.location.engine.AppleLocationEngine
import org.maplibre.navigation.core.location.engine.LocationEngine
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions.Defaults
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions.RoundingIncrement
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions.TimeFormat
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
 * A iOS platform specific wrapper for [MapLibreNavigation].
 *
 * You can also use [MapLibreNavigation] directly, but this leads to more configuration.
 *
 * Currently the only difference is, that the location engine is set to the [AppleLocationEngine]
 * by default.
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
) {

    fun toBuilder(): Builder {
        return Builder()
            .withOptions(options)
            .withLocationEngine(locationEngine)
            .withCameraEngine(cameraEngine)
            .withSnapEngine(snapEngine)
            .withOffRouteEngine(offRouteEngine)
            .withFasterRouteEngine(fasterRouteEngine)
            .withRouteUtils(routeUtils)
    }

    class Builder {
        private var options: MapLibreNavigationOptions = MapLibreNavigationOptions()
        private var locationEngine: LocationEngine = AppleLocationEngine()
        private var cameraEngine: Camera = SimpleCamera()
        private var snapEngine: Snap = SnapToRoute()
        private var offRouteEngine: OffRoute = OffRouteDetector()
        private var fasterRouteEngine: FasterRoute = FasterRouteDetector(options)
        private var routeUtils: RouteUtils = RouteUtils()

        fun withOptions(options: MapLibreNavigationOptions) = apply { this.options = options }
        fun withLocationEngine(locationEngine: LocationEngine) =
            apply { this.locationEngine = locationEngine }

        fun withCameraEngine(cameraEngine: Camera) = apply { this.cameraEngine = cameraEngine }
        fun withSnapEngine(snapEngine: Snap) = apply { this.snapEngine = snapEngine }
        fun withOffRouteEngine(offRouteEngine: OffRoute) =
            apply { this.offRouteEngine = offRouteEngine }

        fun withFasterRouteEngine(fasterRouteEngine: FasterRoute) =
            apply { this.fasterRouteEngine = fasterRouteEngine }

        fun withRouteUtils(routeUtils: RouteUtils) = apply { this.routeUtils = routeUtils }

        fun build(): IOSMapLibreNavigation {
            return IOSMapLibreNavigation(
                options = options,
                locationEngine = locationEngine,
                cameraEngine = cameraEngine,
                snapEngine = snapEngine,
                offRouteEngine = offRouteEngine,
                fasterRouteEngine = fasterRouteEngine,
                routeUtils = routeUtils
            )
        }
    }
}
