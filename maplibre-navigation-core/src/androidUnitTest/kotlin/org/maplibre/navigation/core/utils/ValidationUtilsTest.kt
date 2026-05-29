package org.maplibre.navigation.core.utils

import org.maplibre.geojson.model.Point
import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.models.DirectionsRoute
import org.maplibre.navigation.core.models.RouteOptions
import java.io.IOException
import java.util.MissingFormatArgumentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ValidationUtilsTest : BaseTest() {

    @Test(expected = IllegalStateException::class)
    @Throws(Exception::class)
    fun validDirectionsRoute_isInvalidWithNullRouteOptions() {
        var route = buildTestDirectionsRoute(DIRECTIONS_WITHOUT_VOICE_INSTRUCTIONS)
        val invalidRouteOptions: RouteOptions? = null
        route = route.copy(routeOptions = invalidRouteOptions)

        ValidationUtils.validDirectionsRoute(route, true)
    }

    @Test(expected = IllegalStateException::class)
    @Throws(Exception::class)
    fun validDirectionsRoute_isInvalidWithNullInstructions() {
        val routeWithNullInstructions = buildRouteWithNullInstructions()

        ValidationUtils.validDirectionsRoute(routeWithNullInstructions, true)
    }

    @Test(expected = IllegalStateException::class)
    @Throws(Exception::class)
    fun validDirectionsRoute_isInvalidWithFalseVoiceInstructions() {
        val routeWithFalseVoiceInstructions = buildRouteWithFalseVoiceInstructions()

        ValidationUtils.validDirectionsRoute(routeWithFalseVoiceInstructions, true)
    }

    @Test(expected = IllegalStateException::class)
    @Throws(Exception::class)
    fun validDirectionsRoute_isInvalidWithFalseBannerInstructions() {
        val routeWithFalseBannerInstructions = buildRouteWithFalseBannerInstructions()

        ValidationUtils.validDirectionsRoute(routeWithFalseBannerInstructions, true)
    }

    @Test
    fun validDirectionsRoute_isInvalidWithoutDistance() {
        val route = buildTestDirectionsRoute("directions_two_leg_route_without_distances.json");
        val exception = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validDirectionsRoute(route, false)
        }

        assertEquals("Leg 0 has maxspeed annotation but missing distance annotation", exception.message)
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