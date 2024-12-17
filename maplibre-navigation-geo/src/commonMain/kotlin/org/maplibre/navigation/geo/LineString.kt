package org.maplibre.navigation.geo

import org.maplibre.navigation.geo.util.PolylineUtils

data class LineString(
    val points: List<Point>,
) {

    companion object {
        fun fromPolyline(polyline: String, precision: Int) = LineString(
            PolylineUtils.decode(polyline, precision)
        )
    }
}
