package org.maplibre.navigation.core.utils

import org.maplibre.navigation.geo.LineString
import org.maplibre.navigation.geo.Point
import org.maplibre.navigation.core.models.LegStep
import org.maplibre.navigation.geo.turf.TurfConstants
import org.maplibre.navigation.geo.turf.TurfMeasurement
import org.maplibre.navigation.geo.turf.TurfMisc
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
        val lineString = LineString.fromPolyline(step.geometry, Constants.PRECISION_6)

        // Make sure that the step coordinates isn't less than size 2. If the points equal each other,
        // the distance is obviously zero, so return 0 to avoid executing additional unnecessary code.
        if (lineString.points.isEmpty() || usersRawLocation == lineString.points
                .first()
        ) {
            return 0.0
        }

        if (lineString.points.size == 1) {
            return TurfMeasurement.distance(
                usersRawLocation,
                lineString.points.first(),
                TurfConstants.UNIT_METERS
            )
        }

        val snappedPoint = TurfMisc.nearestPointOnLine(usersRawLocation, lineString.points)
        if (snappedPoint.latitude.isInfinite() || snappedPoint.longitude.isInfinite()) {
            return TurfMeasurement.distance(
                usersRawLocation,
                lineString.points.first(),
                TurfConstants.UNIT_METERS
            )
        }

        val distance =
            TurfMeasurement.distance(usersRawLocation, snappedPoint, TurfConstants.UNIT_METERS)
        return if (!distance.isNaN()) distance else 0.0
    }
}

