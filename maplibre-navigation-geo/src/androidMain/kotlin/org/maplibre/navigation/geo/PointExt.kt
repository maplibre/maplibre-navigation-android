package org.maplibre.navigation.geo

import org.maplibre.geojson.Point as MapLibrePoint

fun Point.toMapLibrePoint(): MapLibrePoint = MapLibrePoint.fromLngLat(this.longitude, this.latitude)

fun List<Point>.toMapLibrePoints(): List<MapLibrePoint> = this.map { p -> MapLibrePoint.fromLngLat(p.longitude, p.latitude) }
