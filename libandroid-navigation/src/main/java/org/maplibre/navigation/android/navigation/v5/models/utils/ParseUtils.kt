package org.maplibre.navigation.android.navigation.v5.models.utils

import org.maplibre.geojson.Point
import java.util.Arrays

/**
 * Methods to convert Strings to Lists of objects.
 */
object ParseUtils {
    private const val SEMICOLON = ";"
    private const val COMMA = ","
    private const val UNLIMITED = "unlimited"
    private const val TRUE = "true"
    private const val FALSE = "false"

    /**
     * Parse a String to a list of Integers.
     *
     * @param original an original String.
     * @return List of Integers
     */
    fun parseToIntegers(original: String?): List<Int?>? {
        if (original == null) {
            return null
        }

        val integers: MutableList<Int?> = ArrayList()
        val strings =
            original.split(SEMICOLON.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (index in strings) {
            if (index != null) {
                if (index.isEmpty()) {
                    integers.add(null)
                } else {
                    integers.add(index.toInt())
                }
            }
        }

        return integers
    }

    /**
     * Parse a String to a list of Strings.
     *
     * @param original an original String.
     * @param separator a String used as a separator.
     * @return List of Strings
     */
    /**
     * Parse a String to a list of Strings using ";" as a separator.
     *
     * @param original an original String.
     * @return List of Strings
     */
    @JvmOverloads
    fun parseToStrings(original: String?, separator: String = SEMICOLON): List<String?>? {
        if (original == null) {
            return null
        }

        val result: MutableList<String?> = ArrayList()
        val strings = original.split(separator.toRegex()).toTypedArray()
        for (str in strings) {
            if (str != null) {
                if (str.isEmpty()) {
                    result.add(null)
                } else {
                    result.add(str)
                }
            }
        }

        return result
    }

    /**
     * Parse a String to a list of Points.
     *
     * @param original an original String.
     * @return List of Points
     */
    fun parseToPoints(original: String?): List<Point?>? {
        if (original == null) {
            return null
        }

        val points: MutableList<Point?> = ArrayList()
        val targets = original.split(SEMICOLON.toRegex()).toTypedArray()
        for (target in targets) {
            if (target != null) {
                if (target.isEmpty()) {
                    points.add(null)
                } else {
                    val point =
                        target.split(COMMA.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    points.add(Point.fromLngLat(point[0].toDouble(), point[1].toDouble()))
                }
            }
        }

        return points
    }

    /**
     * Parse a String to a list of Points.
     *
     * @param original an original String.
     * @return List of Doubles
     */
    fun parseToDoubles(original: String?): List<Double?>? {
        if (original == null) {
            return null
        }

        val doubles: MutableList<Double?> = ArrayList()
        val strings = original.split(SEMICOLON.toRegex()).toTypedArray()
        for (str in strings) {
            if (str != null) {
                if (str.isEmpty()) {
                    doubles.add(null)
                } else if (str == UNLIMITED) {
                    doubles.add(Double.POSITIVE_INFINITY)
                } else {
                    doubles.add(str.toDouble())
                }
            }
        }

        return doubles
    }

    /**
     * Parse a String to a list of list of Doubles.
     *
     * @param original an original String.
     * @return List of List of Doubles
     */
    fun parseToListOfListOfDoubles(original: String?): List<List<Double>?>? {
        if (original == null) {
            return null
        }

        val result: MutableList<List<Double>?> = ArrayList()
        val pairs = original.split(SEMICOLON.toRegex()).toTypedArray()
        for (pair in pairs) {
            if (pair.isEmpty()) {
                result.add(null)
            } else {
                val values =
                    pair.split(COMMA.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (values.size == 2) {
                    result.add(Arrays.asList(values[0].toDouble(), values[1].toDouble()))
                }
            }
        }

        return result
    }

    /**
     * Parse a String to a list of Boolean.
     *
     * @param original an original String.
     * @return List of Booleans
     */
    fun parseToBooleans(original: String?): List<Boolean?>? {
        if (original == null) {
            return null
        }

        val booleans: MutableList<Boolean?> = ArrayList()
        if (original.isEmpty()) {
            return booleans
        }

        val strings = original.split(SEMICOLON.toRegex()).toTypedArray()
        for (str in strings) {
            if (str != null) {
                if (str.isEmpty()) {
                    booleans.add(null)
                } else if (str.equals(TRUE, ignoreCase = true)) {
                    booleans.add(true)
                } else if (str.equals(FALSE, ignoreCase = true)) {
                    booleans.add(false)
                } else {
                    booleans.add(null)
                }
            }
        }

        return booleans
    }
}
