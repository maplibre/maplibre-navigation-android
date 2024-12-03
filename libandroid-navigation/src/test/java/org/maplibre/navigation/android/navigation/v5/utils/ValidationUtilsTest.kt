package org.maplibre.navigation.android.navigation.v5.utils

import org.junit.Test
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions
import java.io.IOException
import java.util.MissingFormatArgumentException

class ValidationUtilsTest : BaseTest() {
    @Test(expected = MissingFormatArgumentException::class)
    @Throws(Exception::class)
    fun validDirectionsRoute_isInvalidWithNullRouteOptions() {
        var route = buildTestDirectionsRoute(DIRECTIONS_WITHOUT_VOICE_INSTRUCTIONS)
        val invalidRouteOptions: RouteOptions? = null
        route = route.copy(routeOptions = invalidRouteOptions)

        ValidationUtils.validDirectionsRoute(route, true)
    }

    @Test(expected = MissingFormatArgumentException::class)
    @Throws(Exception::class)
    fun validDirectionsRoute_isInvalidWithNullInstructions() {
        val routeWithNullInstructions = buildRouteWithNullInstructions()

        ValidationUtils.validDirectionsRoute(routeWithNullInstructions, true)
    }

    @Test(expected = MissingFormatArgumentException::class)
    @Throws(Exception::class)
    fun validDirectionsRoute_isInvalidWithFalseVoiceInstructions() {
        val routeWithFalseVoiceInstructions = buildRouteWithFalseVoiceInstructions()

        ValidationUtils.validDirectionsRoute(routeWithFalseVoiceInstructions, true)
    }

    @Test(expected = MissingFormatArgumentException::class)
    @Throws(Exception::class)
    fun validDirectionsRoute_isInvalidWithFalseBannerInstructions() {
        val routeWithFalseBannerInstructions = buildRouteWithFalseBannerInstructions()

        ValidationUtils.validDirectionsRoute(routeWithFalseBannerInstructions, true)
    }

    @Throws(IOException::class)
    private fun buildRouteWithNullInstructions(): DirectionsRoute {
        val route = buildTestDirectionsRoute()
        val coordinates: List<Point> = ArrayList()
        val routeOptionsWithoutVoiceInstructions = RouteOptions(
            baseUrl = "api://",
            user = "user",
            profile = "profile",
            accessToken = ACCESS_TOKEN,
            requestUuid = "uuid",
            geometries = "mocked_geometries",
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
            bannerInstructions = null,
            voiceInstructions = null,
        )

        return route.copy(
            routeOptions = routeOptionsWithoutVoiceInstructions
        )
    }

    @Throws(IOException::class)
    private fun buildRouteWithFalseVoiceInstructions(): DirectionsRoute {
        val route = buildTestDirectionsRoute()
        val coordinates: List<Point> = ArrayList()
        val routeOptionsWithoutVoiceInstructions = RouteOptions(
            baseUrl = "api://",
            user = "user",
            profile = "profile",
            accessToken = ACCESS_TOKEN,
            requestUuid = "uuid",
            geometries = "mocked_geometries",
            voiceInstructions = false,
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
            bannerInstructions = null,
        )

        return route.copy(
            routeOptions = routeOptionsWithoutVoiceInstructions
        )
    }

    @Throws(IOException::class)
    private fun buildRouteWithFalseBannerInstructions(): DirectionsRoute {
        val route = buildTestDirectionsRoute()
        val coordinates: List<Point> = ArrayList()
        val routeOptionsWithoutVoiceInstructions = RouteOptions(
            baseUrl = "api://",
            user = "user",
            profile = "profile",
            accessToken = ACCESS_TOKEN,
            requestUuid = "uuid",
            geometries = "mocked_geometries",
            voiceInstructions = true,
            bannerInstructions = false,
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

        return route.copy(routeOptions = routeOptionsWithoutVoiceInstructions)
    }

    companion object {
        private const val DIRECTIONS_WITHOUT_VOICE_INSTRUCTIONS = "directions_v5_no_voice.json"
    }
}