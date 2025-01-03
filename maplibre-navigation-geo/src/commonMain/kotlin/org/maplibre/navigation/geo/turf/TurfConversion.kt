package org.maplibre.navigation.geo.turf

import kotlin.jvm.JvmOverloads
import kotlin.math.PI


/**
 * This class is made up of methods that take in an object, convert it, and then return the object
 * in the desired units or object.
 *
 * @see [Turfjs documentation](http://turfjs.org/docs/)
 *
 * @since 1.2.0
 */
object TurfConversion {

    private val factors = mapOf(
        TurfConstants.UNIT_MILES to 3960.0,
        TurfConstants.UNIT_NAUTICAL_MILES to 3441.145,
        TurfConstants.UNIT_DEGREES to 57.2957795,
        TurfConstants.UNIT_RADIANS to 1.0,
        TurfConstants.UNIT_INCHES to 250905600.0,
        TurfConstants.UNIT_YARDS to 6969600.0,
        TurfConstants.UNIT_METERS to 6373000.0,
        TurfConstants.UNIT_CENTIMETERS to 6.373e+8,
        TurfConstants.UNIT_KILOMETERS to 6373.0,
        TurfConstants.UNIT_FEET to 20908792.65,
        TurfConstants.UNIT_CENTIMETRES to 6.373e+8,
        TurfConstants.UNIT_METRES to 6373000.0,
        TurfConstants.UNIT_KILOMETRES to 6373.0,
    )

    /**
     * Converts an angle in degrees to radians.
     *
     * @param degrees angle between 0 and 360 degrees
     * @return angle in radians
     * @since 3.1.0
     */
    fun degreesToRadians(degrees: Double): Double {
        val radians = degrees % 360
        return radians * PI / 180
    }

    /**
     * Converts an angle in radians to degrees.
     *
     * @param radians angle in radians
     * @return degrees between 0 and 360 degrees
     * @since 3.0.0
     */
    fun radiansToDegrees(radians: Double): Double {
        val degrees: Double = radians % (2 * PI)
        return degrees * 180 / PI
    }

    /**
     * Convert a distance measurement (assuming a spherical Earth) from radians to a more friendly
     * unit. The units used here equals the default.
     *
     * @param radians a double using unit radian
     * @return converted radian to distance value
     * @since 1.2.0
     */
    fun radiansToLength(radians: Double, units: String = TurfConstants.UNIT_DEFAULT
    ): Double {
        return radians * factors[units]!!
    }

    /**
     * Convert a distance measurement (assuming a spherical Earth) from a real-world unit into
     * radians.
     *
     * @param distance double representing a distance value assuming the distance units is in
     * kilometers
     * @return converted distance to radians value
     * @since 1.2.0
     */
    @JvmOverloads
    fun lengthToRadians(distance: Double, units: String = TurfConstants.UNIT_DEFAULT): Double {
        return distance / factors[units]!!
    }
}