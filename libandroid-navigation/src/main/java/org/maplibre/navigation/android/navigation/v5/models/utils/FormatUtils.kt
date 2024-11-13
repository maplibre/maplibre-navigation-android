package org.maplibre.navigation.android.navigation.v5.models.utils

import org.maplibre.geojson.Point
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Methods to convert models to Strings.
 */
object FormatUtils {
    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param delimiter the delimiter on which to split.
     * @param tokens A list of objects to be joined. Strings will be formed from the objects by
     * calling object.toString().
     * @param removeTrailingNulls true if trailing nulls should be removed.
     * @return [String]
     */
    /**
     * Returns a string containing the tokens joined by delimiters. Doesn't remove trailing nulls.
     *
     * @param delimiter the delimiter on which to split.
     * @param tokens A list of objects to be joined. Strings will be formed from the objects by
     * calling object.toString().
     * @return [String]
     */
    @JvmOverloads
    fun join(
        delimiter: CharSequence, tokens: List<*>?,
        removeTrailingNulls: Boolean = false
    ): String? {
        if (tokens == null || tokens.size < 1) {
            return null
        }

        var lastNonNullToken = tokens.size - 1
        if (removeTrailingNulls) {
            for (i in tokens.indices.reversed()) {
                val token = tokens[i]
                if (token != null) {
                    break
                } else {
                    lastNonNullToken--
                }
            }
        }

        val sb = StringBuilder()
        var firstTime = true
        for (i in 0..lastNonNullToken) {
            if (firstTime) {
                firstTime = false
            } else {
                sb.append(delimiter)
            }
            val token = tokens[i]
            if (token != null) {
                sb.append(token)
            }
        }
        return sb.toString()
    }

    /**
     * Useful to remove any trailing zeros and prevent a coordinate being over 7 significant figures.
     *
     * @param coordinate a double value representing a coordinate.
     * @return a formatted string.
     */
    fun formatCoordinate(coordinate: Double): String {
        val decimalFormat = DecimalFormat(
            "0.######",
            DecimalFormatSymbols(Locale.US)
        )
        return String.format(
            Locale.US, "%s",
            decimalFormat.format(coordinate)
        )
    }

    /**
     * Used in various APIs to format the user provided radiuses to a String matching the APIs
     * format.
     *
     * @param radiuses a list of doubles represents the radius values
     * @return a String ready for being passed into the Retrofit call
     */
    fun formatRadiuses(radiuses: List<Double?>?): String? {
        if (radiuses == null || radiuses.size == 0) {
            return null
        }

        val radiusesToJoin: MutableList<String?> = ArrayList()
        for (radius in radiuses) {
            if (radius == null) {
                radiusesToJoin.add(null)
            } else if (radius == Double.POSITIVE_INFINITY) {
                radiusesToJoin.add("unlimited")
            } else {
                radiusesToJoin.add(String.format(Locale.US, "%s", formatCoordinate(radius)))
            }
        }
        return join(";", radiusesToJoin)
    }

    /**
     * Formats the bearing variables from the raw values to a string which can than be used for the
     * request URL.
     *
     * @param bearings a List of list of doubles representing bearing values
     * @return a string with the bearing values
     */
    fun formatBearings(bearings: List<List<Double?>?>?): String? {
        if (bearings == null || bearings.isEmpty()) {
            return null
        }

        val bearingsToJoin: MutableList<String?> = ArrayList()
        for (bearing in bearings) {
            if (bearing == null) {
                bearingsToJoin.add(null)
            } else {
                if (bearing.size != 2) {
                    throw RuntimeException("Bearing size should be 2.")
                }

                val angle = bearing[0]
                val tolerance = bearing[1]
                if (angle == null || tolerance == null) {
                    bearingsToJoin.add(null)
                } else {
                    if (angle < 0 || angle > 360 || tolerance < 0 || tolerance > 360) {
                        throw RuntimeException("Angle and tolerance have to be from 0 to 360.")
                    }

                    bearingsToJoin.add(
                        String.format(
                            Locale.US, "%s,%s",
                            formatCoordinate(angle),
                            formatCoordinate(tolerance)
                        )
                    )
                }
            }
        }
        return join(";", bearingsToJoin)
    }

    /**
     * Converts the list of integer arrays to a string ready for API consumption.
     *
     * @param distributions the list of integer arrays representing the distribution
     * @return a string with the distribution values
     */
    fun formatDistributions(distributions: List<Array<Int>>?): String? {
        if (distributions == null || distributions.isEmpty()) {
            return null
        }

        val distributionsToJoin: MutableList<String?> = ArrayList()
        for (array in distributions) {
            if (array.size == 0) {
                distributionsToJoin.add(null)
            } else {
                distributionsToJoin.add(
                    String.format(
                        Locale.US, "%s,%s",
                        formatCoordinate(
                            array[0].toDouble()
                        ),
                        formatCoordinate(
                            array[1].toDouble()
                        )
                    )
                )
            }
        }
        return join(";", distributionsToJoin)
    }

    /**
     * Converts String list with approaches values to a string ready for API consumption. An approach
     * could be unrestricted, curb or null.
     *
     * @param approaches a list representing approaches to each coordinate.
     * @return a formatted string.
     */
    fun formatApproaches(approaches: List<String?>?): String? {
        if (approaches == null || approaches.isEmpty()) {
            return null
        }

        for (approach in approaches) {
            if (approach != null && approach != "unrestricted" && approach != "curb" && !approach.isEmpty()) {
                return null
            }
        }
        return join(";", approaches)
    }

    /**
     * Converts String list with waypoint_names values to a string ready for API consumption.
     *
     * @param waypointNames a string representing approaches to each coordinate.
     * @return a formatted string.
     */
    fun formatWaypointNames(waypointNames: List<String?>?): String? {
        if (waypointNames == null || waypointNames.isEmpty()) {
            return null
        }

        return join(";", waypointNames)
    }

    /**
     * Converts a list of Points to String.
     *
     * @param coordinates a list of coordinates.
     * @return a formatted string.
     */
    fun formatCoordinates(coordinates: List<Point>): String? {
        val coordinatesToJoin: MutableList<String> = ArrayList()
        for (point in coordinates) {
            coordinatesToJoin.add(
                String.format(
                    Locale.US, "%s,%s",
                    formatCoordinate(point.longitude()),
                    formatCoordinate(point.latitude())
                )
            )
        }

        return join(";", coordinatesToJoin)
    }

    /**
     * Converts array of Points with waypoint_targets values to a string ready for API consumption.
     *
     * @param points a list representing approaches to each coordinate.
     * @return a formatted string.
     */
    fun formatPointsList(points: List<Point?>?): String? {
        if (points == null || points.isEmpty()) {
            return null
        }

        val coordinatesToJoin: MutableList<String?> = ArrayList()
        for (point in points) {
            if (point == null) {
                coordinatesToJoin.add(null)
            } else {
                coordinatesToJoin.add(
                    String.format(
                        Locale.US, "%s,%s",
                        formatCoordinate(point.longitude()),
                        formatCoordinate(point.latitude())
                    )
                )
            }
        }
        return join(";", coordinatesToJoin)
    }
}
