package org.maplibre.navigation.android.navigation.v5.utils

import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement
import org.maplibre.turf.TurfMisc

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
        if (lineString.coordinates().isEmpty() || usersRawLocation == lineString.coordinates()
                .first()
        ) {
            return 0.0
        }

        if (lineString.coordinates().size == 1) {
            return TurfMeasurement.distance(
                usersRawLocation,
                lineString.coordinates().first(),
                TurfConstants.UNIT_METERS
            )
        }

        val feature = TurfMisc.nearestPointOnLine(usersRawLocation, lineString.coordinates())
        val snappedPoint = feature.geometry() as Point? ?: return 0.0
        if (snappedPoint.latitude().isInfinite() || snappedPoint.longitude().isInfinite()) {
            return TurfMeasurement.distance(
                usersRawLocation,
                lineString.coordinates().first(),
                TurfConstants.UNIT_METERS
            )
        }

        val distance =
            TurfMeasurement.distance(usersRawLocation, snappedPoint, TurfConstants.UNIT_METERS)
        return if (!distance.isNaN()) distance else 0.0
    }
}

