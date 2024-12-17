package org.maplibre.navigation.geo.turf

import org.maplibre.navigation.geo.LineString
import org.maplibre.navigation.geo.Point
import org.maplibre.navigation.geo.turf.TurfConversion
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


object TurfMeasurement {

    /**
     * Takes two [Point]s and finds the geographic bearing between them.
     *
     * @param point1 first point used for calculating the bearing
     * @param point2 second point used for calculating the bearing
     * @return bearing in decimal degrees
     * @see [Turf Bearing documentation](http://turfjs.org/docs/.bearing)
     *
     * @since 1.3.0
     */
    fun bearing(point1: Point, point2: Point): Double {
        val lon1: Double = TurfConversion.degreesToRadians(point1.longitude)
        val lon2: Double = TurfConversion.degreesToRadians(point2.longitude)
        val lat1: Double = TurfConversion.degreesToRadians(point1.latitude)
        val lat2: Double = TurfConversion.degreesToRadians(point2.latitude)
        val value1 = sin(lon2 - lon1) * cos(lat2)
        val value2 = cos(lat1) * sin(lat2) - (sin(lat1) * cos(lat2) * cos(lon2 - lon1))

        return TurfConversion.radiansToDegrees(atan2(value1, value2))
    }

    /**
     * Takes a list of points and returns a point at a specified distance along the line.
     *
     * @param lineString  that the point should be placed upon
     * @param distance along the linestring geometry which the point should be placed on
     * @param units    one of the units found inside [TurfConstants.TurfUnitCriteria]
     * @return a [Point] which is on the linestring provided and at the distance from
     * the origin of that line to the end of the distance
     * @since 5.2.0
     */
    fun along(lineString: LineString, distance: Double, units: String): Point {
        return along(lineString.points, distance, units)
    }

    /**
     * Takes a list of points and returns a point at a specified distance along the line.
     *
     * @param points   that the point should be placed upon
     * @param distance along the linestring geometry which the point should be placed on
     * @param units    one of the units found inside [TurfConstants.TurfUnitCriteria]
     * @return a [Point] which is on the linestring provided and at the distance from
     * the origin of that line to the end of the distance
     * @since 5.2.0
     */
    fun along(points: List<Point>, distance: Double, units: String): Point {
        var travelled = 0.0
        for (i in points.indices) {
            if (distance >= travelled && i == points.size - 1) {
                break
            } else if (travelled >= distance) {
                val overshot = distance - travelled
                if (overshot == 0.0) {
                    return points[i]
                } else {
                    val direction = bearing(points[i], points[i - 1]) - 180
                    return destination(points[i], overshot, direction, units)
                }
            } else {
                travelled += distance(points[i], points[i + 1], units)
            }
        }

        return points[points.size - 1]
    }

    /**
     * Takes a Point and calculates the location of a destination point given a distance in
     * degrees, radians, miles, or kilometers; and bearing in degrees. This uses the Haversine
     * formula to account for global curvature.
     *
     * @param point    starting point used for calculating the destination
     * @param distance distance from the starting point
     * @param bearing  ranging from -180 to 180 in decimal degrees
     * @param units    one of the units found inside [TurfConstants.TurfUnitCriteria]
     * @return destination [Point] result where you specified
     * @see [Turf Destination documetation](http://turfjs.org/docs/.destination)
     *
     * @since 1.2.0
     */
    fun destination(point: Point, distance: Double, bearing: Double, units: String): Point {
        val longitude1 = TurfConversion.degreesToRadians(point.longitude)
        val latitude1 = TurfConversion.degreesToRadians(point.latitude)
        val bearingRad = TurfConversion.degreesToRadians(bearing)

        val radians: Double = TurfConversion.lengthToRadians(distance, units)

        val latitude2 = asin(
            sin(latitude1) * cos(radians)
                    + cos(latitude1) * sin(radians) * cos(bearingRad)
        )
        val longitude2 = longitude1 + atan2(
            (sin(bearingRad) * sin(radians) * cos(latitude1)),
            cos(radians) - sin(latitude1) * sin(latitude2)
        )

        return Point(
            longitude = TurfConversion.radiansToDegrees(longitude2),
            latitude = TurfConversion.radiansToDegrees(latitude2)
        )
    }

    /**
     * Calculates the distance between two points in degress, radians, miles, or kilometers. This
     * uses the Haversine formula to account for global curvature.
     *
     * @param point1 first point used for calculating the bearing
     * @param point2 second point used for calculating the bearing
     * @param units  one of the units found inside [TurfConstants.TurfUnitCriteria]
     * @return distance between the two points in kilometers
     * @see [Turf distance documentation](http://turfjs.org/docs/.distance)
     *
     * @since 1.2.0
     */
    fun distance(point1: Point, point2: Point, units: String): Double {
        val difLat = TurfConversion.degreesToRadians((point2.latitude - point1.latitude))
        val difLon = TurfConversion.degreesToRadians((point2.longitude - point1.longitude))
        val lat1 = TurfConversion.degreesToRadians(point1.latitude)
        val lat2 = TurfConversion.degreesToRadians(point2.latitude)

        val value = sin(difLat / 2).pow(2.0) + sin(difLon / 2).pow(2.0) * cos(lat1) * cos(lat2)

        return TurfConversion.radiansToLength(2 * atan2(sqrt(value), sqrt(1 - value)), units)
    }

    /**
     * Takes a [LineString] and measures its length in the specified units.
     *
     * @param lineString geometry to measure
     * @param units      one of the units found inside [TurfConstants.TurfUnitCriteria]
     * @return length of the input line in the units specified
     * @see [Turf Line Distance documentation](http://turfjs.org/docs/.linedistance)
     *
     * @since 1.2.0
     */
    fun length(lineString: LineString, units: String): Double {
        return length(lineString.points, units)
    }

//    /**
//     * Takes a [MultiLineString] and measures its length in the specified units.
//     *
//     * @param multiLineString geometry to measure
//     * @param units           one of the units found inside [TurfConstants.TurfUnitCriteria]
//     * @return length of the input lines combined, in the units specified
//     * @see [Turf Line Distance documentation](http://turfjs.org/docs/.linedistance)
//     *
//     * @since 1.2.0
//     */
//    fun length(multiLineString: MultiLineString, units: String?
//    ): Double {
//        var len = 0.0
//        for (points in multiLineString.coordinates()) {
//            len += length(points, units)
//        }
//        return len
//    }

//    /**
//     * Takes a [Polygon] and measures its perimeter in the specified units. if the polygon
//     * contains holes, the perimeter will also be included.
//     *
//     * @param polygon geometry to measure
//     * @param units   one of the units found inside [TurfConstants.TurfUnitCriteria]
//     * @return total perimeter of the input polygon in the units specified
//     * @see [Turf Line Distance documentation](http://turfjs.org/docs/.linedistance)
//     *
//     * @since 1.2.0
//     */
//    fun length(
//        @NonNull polygon: Polygon,
//        @NonNull @TurfUnitCriteria units: String?
//    ): Double {
//        var len = 0.0
//        for (points in polygon.coordinates()) {
//            len += length(points, units)
//        }
//        return len
//    }

//    /**
//     * Takes a [MultiPolygon] and measures each polygons perimeter in the specified units. if
//     * one of the polygons contains holes, the perimeter will also be included.
//     *
//     * @param multiPolygon geometry to measure
//     * @param units        one of the units found inside [TurfConstants.TurfUnitCriteria]
//     * @return total perimeter of the input polygons combined, in the units specified
//     * @see [Turf Line Distance documentation](http://turfjs.org/docs/.linedistance)
//     *
//     * @since 1.2.0
//     */
//    fun length(
//        @NonNull multiPolygon: MultiPolygon,
//        @NonNull @TurfUnitCriteria units: String?
//    ): Double {
//        var len = 0.0
//        val coordinates: List<List<List<Point?>?>> = multiPolygon.coordinates()
//        for (coordinate in coordinates) {
//            for (theCoordinate in coordinate) {
//                len += length(theCoordinate, units)
//            }
//        }
//        return len
//    }

    /**
     * Takes a [List] of [Point] and measures its length in the specified units.
     *
     * @param coords geometry to measure
     * @param units  one of the units found inside [TurfConstants.TurfUnitCriteria]
     * @return length of the input line in the units specified
     * @see [Turf Line Distance documentation](http://turfjs.org/docs/.linedistance)
     *
     * @since 5.2.0
     */
    fun length(points: List<Point>, units: String): Double {
        var travelled = 0.0
        var prevCoords = points[0]
        var curCoords: Point?
        for (i in 1 until points.size) {
            curCoords = points[i]
            travelled += distance(prevCoords, curCoords, units)
            prevCoords = curCoords
        }
        return travelled
    }
}