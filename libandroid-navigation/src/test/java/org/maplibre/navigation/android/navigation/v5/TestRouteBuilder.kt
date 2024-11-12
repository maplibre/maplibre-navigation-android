package org.maplibre.navigation.android.navigation.v5

import com.google.gson.GsonBuilder
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.models.DirectionsAdapterFactory
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions
import org.maplibre.navigation.android.navigation.v5.utils.Constants
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Scanner

internal class TestRouteBuilder {
    @Throws(IOException::class)
    fun loadJsonFixture(filename: String?): String {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader!!.getResourceAsStream(filename)
        val scanner = Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else ""
    }

    @Throws(IOException::class)
    fun buildTestDirectionsRoute(fixtureName: String?): DirectionsRoute {
        var fixtureName = fixtureName
        fixtureName = checkNullFixtureName(fixtureName)
        val gson =
            GsonBuilder().registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create()
        val body = loadJsonFixture(fixtureName)
        val response = gson.fromJson(
            body,
            DirectionsResponse::class.java
        )
        val route = response.routes()[0]
        return buildRouteWithOptions(route)
    }

    @Throws(IOException::class)
    private fun buildRouteWithOptions(route: DirectionsRoute): DirectionsRoute {
        val coordinates: List<Point> = ArrayList()
        val routeOptionsWithoutVoiceInstructions = RouteOptions.builder()
            .baseUrl(Constants.BASE_API_URL)
            .user("user")
            .profile("profile")
            .accessToken(BaseTest.Companion.ACCESS_TOKEN)
            .requestUuid("uuid")
            .geometries("mocked_geometries")
            .voiceInstructions(true)
            .bannerInstructions(true)
            .coordinates(coordinates).build()

        return route.toBuilder()
            .routeOptions(routeOptionsWithoutVoiceInstructions)
            .build()
    }

    private fun checkNullFixtureName(fixtureName: String?): String {
        var fixtureName = fixtureName
        if (fixtureName == null) {
            fixtureName = DIRECTIONS_PRECISION_6
        }
        return fixtureName
    }

    companion object {
        private const val DIRECTIONS_PRECISION_6 = "directions_v5_precision_6.json"
    }
}
