package org.maplibre.navigation.android.navigation.v5.location.replay

import com.google.gson.GsonBuilder
import org.junit.Assert
import org.junit.Test
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Scanner
import java.util.TimeZone

class ReplayRouteParserTest {
    @Test
    fun checksLongitudeParsing() {
        val json = obtainJson("reroute.json")

        val routeFromJson = GsonBuilder().create().fromJson(
            json,
            ReplayJsonRouteDto::class.java
        )

        val firstLocation = routeFromJson.locations[0]
        Assert.assertEquals(11.579233823791801, firstLocation.longitude, DELTA)
    }

    @Test
    fun checksHorizontalAccuracyParsing() {
        val json = obtainJson("reroute.json")

        val routeFromJson = GsonBuilder().create().fromJson(
            json,
            ReplayJsonRouteDto::class.java
        )

        val firstLocation = routeFromJson.locations[0]
        Assert.assertEquals(40.0, firstLocation.horizontalAccuracyMeters.toDouble(), DELTA)
    }

    @Test
    fun checksBearingParsing() {
        val json = obtainJson("reroute.json")

        val routeFromJson = GsonBuilder().create().fromJson(
            json,
            ReplayJsonRouteDto::class.java
        )

        val firstLocation = routeFromJson.locations[0]
        Assert.assertEquals(277.0355517432898, firstLocation.bearing, DELTA)
    }

    @Test
    fun checksVerticalAccuracyParsing() {
        val json = obtainJson("reroute.json")

        val routeFromJson = GsonBuilder().create().fromJson(
            json,
            ReplayJsonRouteDto::class.java
        )

        val firstLocation = routeFromJson.locations[0]
        Assert.assertEquals(10.0, firstLocation.verticalAccuracyMeters.toDouble(), DELTA)
    }

    @Test
    fun checksSpeedParsing() {
        val json = obtainJson("reroute.json")

        val routeFromJson = GsonBuilder().create().fromJson(
            json,
            ReplayJsonRouteDto::class.java
        )

        val firstLocation = routeFromJson.locations[0]
        Assert.assertEquals(14.704089336389941, firstLocation.speed, DELTA)
    }

    @Test
    fun checksLatitudeParsing() {
        val json = obtainJson("reroute.json")

        val routeFromJson = GsonBuilder().create().fromJson(
            json,
            ReplayJsonRouteDto::class.java
        )

        val firstLocation = routeFromJson.locations[0]
        Assert.assertEquals(48.1776966801359, firstLocation.latitude, DELTA)
    }

    @Test
    fun checksAltitudeParsing() {
        val json = obtainJson("reroute.json")

        val routeFromJson = GsonBuilder().create().fromJson(
            json,
            ReplayJsonRouteDto::class.java
        )

        val firstLocation = routeFromJson.locations[0]
        Assert.assertEquals(0.0, firstLocation.altitude, DELTA)
    }

    @Test
    fun checksTimestampParsing() {
        val json = obtainJson("reroute.json")

        val routeFromJson = GsonBuilder().create().fromJson(
            json,
            ReplayJsonRouteDto::class.java
        )

        val firstLocation = routeFromJson.locations[0]
        val dateFormatPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        val dateFormat = SimpleDateFormat(dateFormatPattern)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        Assert.assertEquals("2018-06-25T18:16:11.005+0000", dateFormat.format(firstLocation.date))
    }

    @Test
    fun checksRouteParsing() {
        val json = obtainJson("reroute.json")
        val routeFromJson = GsonBuilder().create().fromJson(
            json,
            ReplayJsonRouteDto::class.java
        )
        Assert.assertEquals(
            "https://api.mapbox.com/directions/v5/mapbox/driving-traffic/11.579233823791801,48.1776966801359;" +
                    "11.573521553454881,48.17812728496367.json?access_token=pk" +
                    ".eyJ1IjoibWFwYm94LW5hdmlnYXRpb24iLCJhIjoiY2plZzkxZnl4MW9tZDMzb2R2ZXlkeHlhbCJ9.L1c9Wo-gk6d3cR3oi1n9SQ&steps" +
                    "=true&overview=full&geometries=geojson", routeFromJson.routeRequest
        )
    }

    private fun obtainJson(fileName: String): String {
        val classLoader = javaClass.classLoader
        return convertStreamToString(classLoader!!.getResourceAsStream(fileName))
    }

    private fun convertStreamToString(`is`: InputStream): String {
        val s = Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    companion object {
        private const val DELTA = 1e-15
    }
}
