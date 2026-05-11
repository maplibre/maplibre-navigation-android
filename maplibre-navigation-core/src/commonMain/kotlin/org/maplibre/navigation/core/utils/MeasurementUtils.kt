package org.maplibre.navigation.core.utils

import org.maplibre.navigation.core.models.LegStep
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.polyline.PolylineEncoding
import org.maplibre.spatialk.turf.measurement.distance
import org.maplibre.spatialk.turf.misc.nearestPointTo
import org.maplibre.spatialk.units.International.Meters
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.jvm.JvmStatic

object MeasurementUtils {

    /**
     * Calculates the distance between the users current raw [android.location.Location] object
     * to the closest [Point] in the [LegStep].
     *
     * @param usersRawLocation [Point] the raw location where the user is currently located
     * @param step             [LegStep] to calculate the closest point on the step to our
     * predicted location
     * @return double in distance meters
     * @since 0.2.0
     */
    @JvmStatic
    fun userTrueDistanceFromStep(usersRawLocation: Point, step: LegStep): Double {
        // Check that the leg step contains geometry.
        if (step.geometry.isEmpty()) {
            return 0.0
        }

        // Get the lineString from the step geometry.
        val positions =
            PolylineEncoding.decode(encoded = step.geometry, precision = Constants.PRECISION_6)

        // Make sure that the step coordinates isn't less than size 2. If the points equal each other,
        // the distance is obviously zero, so return 0 to avoid executing additional unnecessary code.
        if (positions.isEmpty() || usersRawLocation == Point(positions.first())
        ) {
            return 0.0
        }

        if (positions.size == 1) {
            return distance(usersRawLocation.coordinates, positions.first()).toDouble(Meters)
        }

        val lineString = LineString(positions)
        val snappedPointFeature = lineString.nearestPointTo(usersRawLocation.coordinates)
        val snappedPoint = snappedPointFeature.geometry as Point
        if (snappedPoint.latitude.isInfinite() || snappedPoint.longitude.isInfinite()) {
            return distance(usersRawLocation, lineString).inMeters
        }

        val distance = distance(usersRawLocation, snappedPoint).inMeters
        return if (!distance.isNaN()) distance else 0.0
    }
}

