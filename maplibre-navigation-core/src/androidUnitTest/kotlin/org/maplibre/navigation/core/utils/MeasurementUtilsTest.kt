package org.maplibre.navigation.core.utils

import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.models.LegStep
import org.maplibre.navigation.core.models.StepManeuver
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.polyline.PolylineEncoding
import kotlin.test.Test
import kotlin.test.assertEquals

class MeasurementUtilsTest : BaseTest() {

    @Test
    fun userTrueDistanceFromStep_returnsZeroWhenCurrentStepAndPointEqualSame() {
        val futurePoint = Position(-95.367697, 29.758938)

        val geometryPoints: MutableList<Position> = ArrayList()
        geometryPoints.add(futurePoint)
        val step = getLegStep(Position(0.0, 0.0), geometryPoints)

        val distance = MeasurementUtils.userTrueDistanceFromStep(futurePoint, step)
        assertEquals(0.0, distance, DELTA)
    }

    @Test
    fun userTrueDistanceFromStep_onlyOnePointInLineStringStillMeasuresDistanceCorrectly() {
        val futurePoint = Position(-95.3676974, 29.7589382)

        val geometryPoints: MutableList<Position> = ArrayList()
        geometryPoints.add(Position(-95.8427, 29.7757))
        val step = getLegStep(Position(0.0, 0.0), geometryPoints)

        val distance = MeasurementUtils.userTrueDistanceFromStep(futurePoint, step)
        assertEquals(45886.3, distance, LARGE_DELTA)
    }

    @Test
    fun userTrueDistanceFromStep_onePointStepGeometryWithDifferentRawPoint() {
        val futurePoint = Position(-95.3676974, 29.7589382)

        val geometryPoints: MutableList<Position> = ArrayList()
        geometryPoints.add(Position(-95.8427, 29.7757))
        geometryPoints.add(futurePoint)
        val step = getLegStep(Position(0.0, 0.0), geometryPoints)

        val distance = MeasurementUtils.userTrueDistanceFromStep(futurePoint, step)
        assertEquals(0.04, distance, LARGE_DELTA)
    }

    private fun getLegStep(location: Position, geometryPoints: List<Position>): LegStep {
        return LegStep(
            geometry = PolylineEncoding.encode(geometryPoints, Constants.PRECISION_6),
            mode = "driving",
            distance = 0.0,
            duration = 0.0,
            maneuver = StepManeuver(
                location = location,
                bearingBefore = 0.0,
                bearingAfter = 0.0,
                instruction = null,
                type = null,
                modifier = null,
                exit = null
            ),
            weight = 0.0,
            durationTypical = null,
            speedLimitUnit = null,
            speedLimitSign = null,
            name = null,
            ref = null,
            destinations = null,
            pronunciation = null,
            rotaryName = null,
            rotaryPronunciation = null,
            voiceInstructions = null,
            bannerInstructions = null,
            drivingSide = null,
            intersections = null,
            exits = null,
        )
    }
}