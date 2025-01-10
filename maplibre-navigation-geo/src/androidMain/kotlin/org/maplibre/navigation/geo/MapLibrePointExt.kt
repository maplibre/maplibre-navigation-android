package org.maplibre.navigation.geo

import org.maplibre.geojson.Point as MapLibrePoint

fun MapLibrePoint.toPoint(): Point = Point(this.longitude(), this.latitude(), this.altitude().takeIf { this.hasAltitude() })

fun List<MapLibrePoint>.toPoints(): List<Point> = this.map { mp -> Point(mp.longitude(), mp.latitude(), mp.altitude().takeIf { mp.hasAltitude() }) }
