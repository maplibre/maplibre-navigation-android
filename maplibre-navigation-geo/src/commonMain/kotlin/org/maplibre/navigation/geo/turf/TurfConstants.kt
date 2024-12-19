package org.maplibre.navigation.geo.turf

/**
 * This class holds the Turf constants which are useful when specifying additional information such
 * as units prior to executing the Turf function. For example, if I intend to get the distance
 * between two GeoJson Points using [TurfMeasurement.distance] the third
 * optional parameter can define the output units.
 *
 *
 * Note that [TurfConversion.convertLength] can be used to transform
 * one unit to another, such as miles to feet.
 *
 *
 * @see [Turfjs documentation](http://turfjs.org/docs/)
 *
 * @since 1.2.0
 */
object TurfConstants {
    /**
     * The mile is an English unit of length of linear measure equal to 5,280 feet, or 1,760 yards,
     * and standardised as exactly 1,609.344 meters by international agreement in 1959.
     *
     * @since 1.2.0
     */
    const val UNIT_MILES: String = "miles"

    /**
     * The nautical mile per hour is known as the knot. Nautical miles and knots are almost
     * universally used for aeronautical and maritime navigation, because of their relationship with
     * degrees and minutes of latitude and the convenience of using the latitude scale on a map for
     * distance measuring.
     *
     * @since 1.2.0
     */
    const val UNIT_NAUTICAL_MILES: String = "nauticalmiles"

    /**
     * The kilometer (American spelling) is a unit of length in the metric system, equal to one
     * thousand meters. It is now the measurement unit used officially for expressing distances
     * between geographical places on land in most of the world; notable exceptions are the United
     * States and the road network of the United Kingdom where the statute mile is the official unit
     * used.
     *
     *
     * In many Turf calculations, if a unit is not provided, the output value will fallback onto using
     * this unit. See [.UNIT_DEFAULT] for more information.
     *
     *
     * @since 1.2.0
     */
    const val UNIT_KILOMETERS: String = "kilometers"

    /**
     * The radian is the standard unit of angular measure, used in many areas of mathematics.
     *
     * @since 1.2.0
     */
    const val UNIT_RADIANS: String = "radians"

    /**
     * A degree, is a measurement of a plane angle, defined so that a full rotation is 360 degrees.
     *
     * @since 1.2.0
     */
    const val UNIT_DEGREES: String = "degrees"

    /**
     * The inch (abbreviation: in or &quot;) is a unit of length in the (British) imperial and United
     * States customary systems of measurement now formally equal to 1/36th yard but usually
     * understood as 1/12th of a foot.
     *
     * @since 1.2.0
     */
    const val UNIT_INCHES: String = "inches"

    /**
     * The yard (abbreviation: yd) is an English unit of length, in both the British imperial and US
     * customary systems of measurement, that comprises 3 feet or 36 inches.
     *
     * @since 1.2.0
     */
    const val UNIT_YARDS: String = "yards"

    /**
     * The metre (international spelling) or meter (American spelling) is the base unit of length in
     * the International System of Units (SI).
     *
     * @since 1.2.0
     */
    const val UNIT_METERS: String = "meters"

    /**
     * A centimeter (American spelling) is a unit of length in the metric system, equal to one
     * hundredth of a meter.
     *
     * @since 1.2.0
     */
    const val UNIT_CENTIMETERS: String = "centimeters"

    /**
     * The foot is a unit of length in the imperial and US customary systems of measurement.
     *
     * @since 1.2.0
     */
    const val UNIT_FEET: String = "feet"

    /**
     * A centimetre (international spelling) is a unit of length in the metric system, equal to one
     * hundredth of a meter.
     *
     * @since 3.0.0
     */
    const val UNIT_CENTIMETRES: String = "centimetres"

    /**
     * The metre (international spelling) is the base unit of length in
     * the International System of Units (SI).
     *
     * @since 3.0.0
     */
    const val UNIT_METRES: String = "metres"

    /**
     * The kilometre (international spelling) is a unit of length in the metric system, equal to one
     * thousand metres. It is now the measurement unit used officially for expressing distances
     * between geographical places on land in most of the world; notable exceptions are the United
     * States and the road network of the United Kingdom where the statute mile is the official unit
     * used.
     *
     * @since 3.0.0
     */
    const val UNIT_KILOMETRES: String = "kilometres"

    /**
     * The default unit used in most Turf methods when no other unit is specified is kilometers.
     *
     * @since 1.2.0
     */
    const val UNIT_DEFAULT: String = UNIT_KILOMETERS
}