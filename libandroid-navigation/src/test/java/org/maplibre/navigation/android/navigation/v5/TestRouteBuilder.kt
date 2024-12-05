package org.maplibre.navigation.android.navigation.v5

import org.maplibre.geojson.Point
import org.maplibre.navigation.android.json
import org.maplibre.navigation.android.navigation.v5.BaseTest.Companion.ACCESS_TOKEN
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Scanner

internal class TestRouteBuilder {
    @Throws(IOException::class)
    fun loadJsonFixture(filename: String): String {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader!!.getResourceAsStream(filename)
        val scanner = Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else ""
    }

    @Throws(IOException::class)
    fun buildTestDirectionsRoute(fixtureName: String?): DirectionsRoute {
        val fixtureJsonString = loadJsonFixture(fixtureName ?: DIRECTIONS_PRECISION_6)
        val response = json.decodeFromString<DirectionsResponse>(fixtureJsonString)
        val route = response.routes[0]
        return buildRouteWithOptions(route)
    }

    @Throws(IOException::class)
    private fun buildRouteWithOptions(route: DirectionsRoute): DirectionsRoute {
        val coordinates: List<Point> = ArrayList()
        val routeOptionsWithoutVoiceInstructions = RouteOptions(
            baseUrl = "api://",
            user = "user",
            profile = "profile",
            accessToken = ACCESS_TOKEN,
            requestUuid = "uuid",
            geometries = "mocked_geometries",
            voiceInstructions = true,
            bannerInstructions = true,
            coordinates = coordinates,
            alternatives = null,
            language = null,
            radiuses = null,
            bearings = null,
            continueStraight = null,
            roundaboutExits = null,
            overview = null,
            steps = null,
            annotations = null,
            exclude = null,
            voiceUnits = null,
            approaches = null,
            waypointIndices = null,
            waypointNames = null,
            waypointTargets = null,
            walkingOptions = null,
            snappingClosures = null,
        )

        return route.copy(
            routeOptions = routeOptionsWithoutVoiceInstructions
        )
    }

    companion object {
        private const val DIRECTIONS_PRECISION_6 = "directions_v5_precision_6.json"
    }
}
