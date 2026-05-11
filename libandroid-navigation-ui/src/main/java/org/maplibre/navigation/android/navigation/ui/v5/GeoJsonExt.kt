package org.maplibre.navigation.android.navigation.ui.v5

import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.geojson.BoundingBox as MapLibreBoundingBox
import org.maplibre.geojson.Point as MapLibrePoint

@JvmName("positionsToMapLibre")
fun List<Position>.toMapLibrePoints(): List<MapLibrePoint> = map { pos -> pos.toMapLibre() }

@JvmName("pointsToMapLibre")
fun List<Point>.toMapLibrePoints(): List<MapLibrePoint> = map { pt -> pt.toMapLibre() }

fun Point.toMapLibre(): MapLibrePoint {
    return altitude?.let { alt ->
        MapLibrePoint.fromLngLat(
            longitude,
            latitude,
            alt,
            bbox?.toMapLibre()
        )
    } ?: MapLibrePoint.fromLngLat(
        longitude,
        latitude,
        bbox?.toMapLibre()
    )
}


fun Position.toMapLibre(): MapLibrePoint {
    return altitude?.let { alt ->
        return MapLibrePoint.fromLngLat(longitude, latitude, alt)
    } ?: MapLibrePoint.fromLngLat(longitude, latitude)
}

fun BoundingBox.toMapLibre(): MapLibreBoundingBox {
    return MapLibreBoundingBox.fromPoints(
        southwest.toMapLibre(),
        northeast.toMapLibre()
    )
}
