package org.maplibre.navigation.android.navigation.ui.v5

import org.maplibre.geojson.common.toJvm
import org.maplibre.geojson.model.Point
import org.maplibre.geojson.Point as JvmPoint

fun List<Point>.toJvmPoints(): List<JvmPoint> = map { pt -> pt.toJvm() }
