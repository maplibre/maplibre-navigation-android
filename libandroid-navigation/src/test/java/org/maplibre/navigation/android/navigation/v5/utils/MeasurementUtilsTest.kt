package org.maplibre.navigation.android.navigation.v5.utils

import junit.framework.Assert
import org.junit.Test
import org.maplibre.geojson.Point
import org.maplibre.geojson.utils.PolylineUtils
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.models.StepManeuver

class MeasurementUtilsTest : BaseTest() {
    @Test
    fun userTrueDistanceFromStep_returnsZeroWhenCurrentStepAndPointEqualSame() {
        val futurePoint = Point.fromLngLat(-95.367697, 29.758938)

        val geometryPoints: MutableList<Point> = ArrayList()
        geometryPoints.add(futurePoint)
        val rawLocation = doubleArrayOf(0.0, 0.0)
        val step = getLegStep(rawLocation, geometryPoints)

        val distance = MeasurementUtils.userTrueDistanceFromStep(futurePoint, step)
        Assert.assertEquals(0.0, distance, BaseTest.Companion.DELTA)
    }

    @Test
    fun userTrueDistanceFromStep_onlyOnePointInLineStringStillMeasuresDistanceCorrectly() {
        val futurePoint = Point.fromLngLat(-95.3676974, 29.7589382)

        val geometryPoints: MutableList<Point> = ArrayList()
        geometryPoints.add(Point.fromLngLat(-95.8427, 29.7757))
        val rawLocation = doubleArrayOf(0.0, 0.0)
        val step = getLegStep(rawLocation, geometryPoints)

        val distance = MeasurementUtils.userTrueDistanceFromStep(futurePoint, step)
        Assert.assertEquals(45900.73617999494, distance, BaseTest.Companion.DELTA)
    }

    @Test
    fun userTrueDistanceFromStep_onePointStepGeometryWithDifferentRawPoint() {
        val futurePoint = Point.fromLngLat(-95.3676974, 29.7589382)

        val geometryPoints: MutableList<Point> = ArrayList()
        geometryPoints.add(Point.fromLngLat(-95.8427, 29.7757))
        geometryPoints.add(futurePoint)
        val rawLocation = doubleArrayOf(0.0, 0.0)
        val step = getLegStep(rawLocation, geometryPoints)

        val distance = MeasurementUtils.userTrueDistanceFromStep(futurePoint, step)
        Assert.assertEquals(0.04457271773629306, distance, BaseTest.Companion.DELTA)
    }

    private fun getLegStep(rawLocation: DoubleArray, geometryPoints: List<Point>): LegStep {
        return LegStep.builder()
            .geometry(PolylineUtils.encode(geometryPoints, Constants.PRECISION_6))
            .mode("driving")
            .distance(0.0)
            .duration(0.0)
            .maneuver(StepManeuver.builder().rawLocation(rawLocation).build())
            .weight(0.0)
            .build()
    }
}