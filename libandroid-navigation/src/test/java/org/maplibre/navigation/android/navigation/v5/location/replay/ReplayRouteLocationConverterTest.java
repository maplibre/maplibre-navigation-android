package org.maplibre.navigation.android.navigation.v5.location.replay;

import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;

import org.junit.Test;
import org.maplibre.navigation.android.navigation.v5.location.replay.ReplayRouteLocationConverter;

import java.util.List;

public class ReplayRouteLocationConverterTest {


    @Test
    public void testSliceRouteWithEmptyLineString() {
        ReplayRouteLocationConverter replayRouteLocationConverter = new ReplayRouteLocationConverter(null, 100, 1);
        List<Point> result = replayRouteLocationConverter.sliceRoute(LineString.fromJson(""));

        assert (result.isEmpty());
    }

    @Test
    public void testSliceRouteWithNullLineString() {
        ReplayRouteLocationConverter replayRouteLocationConverter = new ReplayRouteLocationConverter(null, 100, 1);
        List<Point> result = replayRouteLocationConverter.sliceRoute(null);

        assert (result.isEmpty());
    }
}