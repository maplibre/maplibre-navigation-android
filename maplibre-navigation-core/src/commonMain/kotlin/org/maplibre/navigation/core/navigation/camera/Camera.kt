package org.maplibre.navigation.core.navigation.camera

import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.navigation.MapLibreNavigation

/**
 * This class handles calculating all properties necessary to configure the camera position while
 * routing. The [MapLibreNavigation] uses
 * a [SimpleCamera] by default. If you would like to customize the camera position, create a
 * concrete implementation of this class or subclass [SimpleCamera] and set it
 * on [MapLibreNavigation] constructor.
 *
 * @since 0.10.0
 */
interface Camera {

    /**
     * Direction that the camera is pointing in, in degrees clockwise from north.
     */
    fun bearing(routeInformation: RouteInformation): Double

    /**
     * The angle, in degrees, of the camera angle from the nadir (directly facing the Earth).
     * See tilt(float) for details of restrictions on the range of values.
     */
    fun tilt(routeInformation: RouteInformation): Double

    /**
     * The location that the camera is pointing at.
     */
    fun target(routeInformation: RouteInformation): Point?


    /**
     * Zoom level near the center of the screen. See zoom(float) for the definition of the camera's
     * zoom level.
     */
    fun zoom(routeInformation: RouteInformation): Double

    /***
     * List of points that must be visible in the camera view to show full route overview.
     */
    fun overview(routeInformation: RouteInformation): List<Point>
}
