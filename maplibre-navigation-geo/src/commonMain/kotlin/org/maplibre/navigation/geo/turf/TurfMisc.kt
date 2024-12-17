package org.maplibre.navigation.geo.turf

import org.maplibre.navigation.geo.LineString
import org.maplibre.navigation.geo.Point
import kotlin.math.max


object TurfMisc {

    /**
     * Takes a line, a start [Point], and a stop point and returns the line in between those
     * points.
     *
     * @param startPt used for calculating the lineSlice
     * @param stopPt  used for calculating the lineSlice
     * @param line    geometry that should be sliced
     * @return a sliced [LineString]
     * @see [Turf Line slice documentation](http://turfjs.org/docs/.lineslice)
     *
     * @since 1.2.0
     */
    fun lineSlice(startPt: Point, stopPt: Point, line: LineString): LineString {
        val coordinates: List<Point> = line.points

        if (coordinates.size < 2) {
            throw IllegalArgumentException(
                "Turf lineSlice requires a LineString made up of at least 2 "
                        + "coordinates."
            )
        } else if (startPt == stopPt) {
            throw IllegalArgumentException("Start and stop points in Turf lineSlice cannot equal each other.")
        }

        val startVertex = nearestPointOnLineInternal(startPt, coordinates)
        val stopVertex = nearestPointOnLineInternal(stopPt, coordinates)
        val ends = mutableListOf<DistancePoint>()
        if (startVertex.index <= stopVertex.index) {
            ends.add(startVertex)
            ends.add(stopVertex)
        } else {
            ends.add(stopVertex)
            ends.add(startVertex)
        }
        val points =  mutableListOf<Point>()
        points.add(ends[0].point)
        for (i in ends[0].index + 1 until ends[1].index + 1) {
            points.add(coordinates[i])
        }
        points.add(ends[1].point)
        return LineString(points)
    }

    /**
     * Takes a [Point] and a [LineString] and calculates the closest Point on the
     * LineString.
     *
     * @param point point to snap from
     * @param coordinates line to snap to
     * @param units one of the units found inside [TurfConstants.TurfUnitCriteria]
     * can be degrees, radians, miles, or kilometers
     * @return closest point on the line to point
     * @since 4.9.0
     */
    fun nearestPointOnLine(
        point: Point,
        coordinates: List<Point>,
        units: String = TurfConstants.UNIT_KILOMETERS
    ): Point {
        return nearestPointOnLineInternal(point, coordinates, units).point
    }

    private fun nearestPointOnLineInternal(
        point: Point,
        coordinates: List<Point>,
        units: String = TurfConstants.UNIT_KILOMETERS
    ): DistancePoint {
        if (coordinates.size < 2) {
            throw IllegalArgumentException(
                "Turf nearestPointOnLine requires a List of Points "
                        + "made up of at least 2 coordinates."
            )
        }

        var closestPt = DistancePoint(
            point = Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
            distance = Double.POSITIVE_INFINITY,
            index = 0,
        )

        for (i in 0 until coordinates.size - 1) {
            val start = DistancePoint(
                point = coordinates[i],
                TurfMeasurement.distance(point, coordinates[i], units),
                index = -1,
            )
            val stop = DistancePoint(
                point = coordinates[i + 1],
                TurfMeasurement.distance(point, coordinates[i + 1], units),
                index = -1,
            )

            //perpendicular
            val heightDistance: Double = max(
                start.distance,
                stop.distance
            )
            val direction = TurfMeasurement.bearing(
                start.point,
                stop.point
            )
            val perpendicularPt1 =
                TurfMeasurement.destination(point, heightDistance, direction + 90, units)
            val perpendicularPt2 =
                TurfMeasurement.destination(point, heightDistance, direction - 90, units)

            val intersect = lineIntersects(
                perpendicularPt1.longitude,
                perpendicularPt1.latitude,
                perpendicularPt2.longitude,
                perpendicularPt2.latitude,
                start.point.longitude,
                start.point.latitude,
                stop.point.longitude,
                stop.point.latitude
            )

            var intersectDistancePoint: DistancePoint? = null
            if (intersect != null) {
                val intersectPoint = Point(longitude = intersect.horizontalIntersection!!, latitude = intersect.verticalIntersection!!)
                intersectDistancePoint = DistancePoint(
                    point = intersectPoint,
                    distance = TurfMeasurement.distance(point, intersectPoint, units),
                    index = -1,
                )
            }

            if (start.distance < closestPt.distance) {
                closestPt = start.copy(index = i)
            }

            if (stop.distance < closestPt.distance) {
                closestPt = stop.copy(index = i)
            }

            if (intersectDistancePoint != null && (intersectDistancePoint.distance < closestPt.distance)) {
                closestPt = intersectDistancePoint.copy(index = i)
            }
        }

        return closestPt
    }

    /**
     * Takes a reference point and a list of [Point] geometries and returns the point from the
     * set point list closest to the reference. This calculation is geodesic.
     *
     * @param targetPoint the reference point
     * @param points      set list of points to run against the input point
     * @return the closest point in the set to the reference point
     * @since 3.0.0
     */
    fun nearestPoint(targetPoint: Point, points: List<Point>): Point {
        if (points.isEmpty()) {
            return targetPoint
        }
        var nearestPoint = points[0]
        var minDist = Double.POSITIVE_INFINITY
        for (point in points) {
            val distanceToPoint = TurfMeasurement.distance(targetPoint, point, TurfConstants.UNIT_METERS)
            if (distanceToPoint < minDist) {
                nearestPoint = point
                minDist = distanceToPoint
            }
        }
        return nearestPoint
    }

    private fun lineIntersects(
        line1StartX: Double,
        line1StartY: Double,
        line1EndX: Double,
        line1EndY: Double,
        line2StartX: Double,
        line2StartY: Double,
        line2EndX: Double,
        line2EndY: Double
    ): LineIntersects? {
        // If the lines intersect, the result contains the x and y of the intersection
        // (treating the lines as infinite) and booleans for whether line segment 1 or line
        // segment 2 contain the point
        var result = LineIntersects(
            onLine1 = false,
            onLine2 = false
        )

        val denominator = (((line2EndY - line2StartY) * (line1EndX - line1StartX))
                - ((line2EndX - line2StartX) * (line1EndY - line1StartY)))
        if (denominator == 0.0) {
            return if (result.horizontalIntersection != null && result.verticalIntersection != null) {
                result
            } else {
                null
            }
        }
        var varA = line1StartY - line2StartY
        var varB = line1StartX - line2StartX
        val numerator1 = ((line2EndX - line2StartX) * varA) - ((line2EndY - line2StartY) * varB)
        val numerator2 = ((line1EndX - line1StartX) * varA) - ((line1EndY - line1StartY) * varB)
        varA = numerator1 / denominator
        varB = numerator2 / denominator

        // if we cast these lines infinitely in both directions, they intersect here:
        result = result.copy(
            horizontalIntersection = line1StartX + (varA * (line1EndX - line1StartX)),
            verticalIntersection = line1StartY + (varA * (line1EndY - line1StartY))
        )

        // if line1 is a segment and line2 is infinite, they intersect if:
        if (varA > 0 && varA < 1) {
            result = result.copy(onLine1 = true)
        }
        // if line2 is a segment and line1 is infinite, they intersect if:
        if (varB > 0 && varB < 1) {
            result = result.copy(onLine2 = true)
        }
        // if line1 and line2 are segments, they intersect if both of the above are true
        return if (result.onLine1 && result.onLine2) {
            result
        } else {
            null
        }
    }

    private data class DistancePoint(
        val point: Point,
        val distance: Double,
        val index: Int,
    )

    private data class LineIntersects(
        val horizontalIntersection: Double? = null,
        val verticalIntersection: Double? = null,
        val onLine1: Boolean,
        val onLine2: Boolean
    )
}