package org.maplibre.navigation.geo

import org.maplibre.geojson.Point as MapLibrePoint

fun MapLibrePoint.toPoint(): Point = Point(this.longitude(), this.latitude())
fun List<MapLibrePoint>.toPoints(): List<Point> = this.map { mp -> Point(mp.longitude(), mp.latitude()) }
