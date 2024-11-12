package org.maplibre.navigation.android.navigation.v5.location.replay

import org.junit.Test
import org.maplibre.geojson.LineString

class ReplayRouteLocationConverterTest {
    @Test
    fun testSliceRouteWithEmptyLineString() {
        val replayRouteLocationConverter = ReplayRouteLocationConverter(null, 100, 1)
        val result = replayRouteLocationConverter.sliceRoute(LineString.fromJson(""))

        assert(result.isEmpty())
    }

    @Test
    fun testSliceRouteWithNullLineString() {
        val replayRouteLocationConverter = ReplayRouteLocationConverter(null, 100, 1)
        val result = replayRouteLocationConverter.sliceRoute(null)

        assert(result.isEmpty())
    }
}