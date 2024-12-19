package org.maplibre.navigation.core.offroute

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.maplibre.navigation.geo.LineString
import org.maplibre.navigation.geo.Point
import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.models.LegStep
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.core.routeprogress.RouteProgress
import org.maplibre.navigation.core.utils.Constants
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class OffRouteDetectorTest : BaseTest() {

    private val defaultOptions = MapLibreNavigationOptions()

    @Test
    @Throws(Exception::class)
    fun invalidOffRoute_onFirstLocationUpdate() {
        val routeProgress = mockk<RouteProgress> {
            every { distanceRemaining } returns 1000.0
        }

        val isUserOffRoute = OffRouteDetector().isUserOffRoute(
            mockk(relaxed = true),
            routeProgress,
            MapLibreNavigationOptions()
        )

        assertFalse(isUserOffRoute)
    }

    @Test
    @Throws(Exception::class)
    fun validOffRoute_onMinimumDistanceBeforeReroutingPassed() {
        val mapboxOffice = buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        val firstRouteProgress = mockk<RouteProgress> {
            every { distanceRemaining } returns 1000.0
        }
        val secondRouteProgress = buildDefaultTestRouteProgress()
        val mapLibreNavigationOptions = MapLibreNavigationOptions()
        val offRouteDetector = OffRouteDetector()
        offRouteDetector.isUserOffRoute(
            mockk(relaxed = true),
            firstRouteProgress,
            mapLibreNavigationOptions
        )

        val target = buildPointAwayFromLocation(
            mapboxOffice,
            mapLibreNavigationOptions.offRouteMinimumDistanceMetersBeforeWrongDirection + 1
        )
        val locationOverMinimumDistance =
            buildDefaultLocationUpdate(target.longitude, target.latitude)

        val validOffRoute = offRouteDetector.isUserOffRoute(
            locationOverMinimumDistance,
            secondRouteProgress,
            mapLibreNavigationOptions
        )

        assertTrue(validOffRoute)
    }

    @Test
    fun isUserOffRoute_AssertTrueWhenTooFarFromStep() {
        val routeProgress = buildDefaultTestRouteProgress()
        val stepManeuverPoint: Point =
            routeProgress.directionsRoute.legs[0].steps[0].maneuver.location
        val firstUpdate =
            buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        val offRouteDetector = OffRouteDetector()
        offRouteDetector.isUserOffRoute(firstUpdate, routeProgress, defaultOptions)

        val offRoutePoint =
            buildPointAwayFromPoint(stepManeuverPoint, 100.0, 90.0)
        val secondUpdate =
            buildDefaultLocationUpdate(offRoutePoint.longitude, offRoutePoint.latitude)

        val isUserOffRoute =
            offRouteDetector.isUserOffRoute(secondUpdate, routeProgress, defaultOptions)
        assertTrue(isUserOffRoute)
    }

    @Test
    fun isUserOffRoute_StepPointSize() {
        val routeProgress = with(buildDefaultTestRouteProgress()) {
            copy(
                currentStepPoints = currentStepPoints
                    .subList(currentStepPoints.size - 2, currentStepPoints.size)
            )
        }

        val stepManeuverPoint: Point =
            routeProgress.directionsRoute.legs[0].steps[0].maneuver.location

        val offRouteDetector = OffRouteDetector()
        val firstUpdate =
            buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        offRouteDetector.isUserOffRoute(firstUpdate, routeProgress, defaultOptions)
        val offRoutePoint =
            buildPointAwayFromPoint(stepManeuverPoint, 50.0, 90.0)
        val secondUpdate =
            buildDefaultLocationUpdate(offRoutePoint.longitude, offRoutePoint.latitude)

        val isUserOffRoute = offRouteDetector.isUserOffRoute(
            secondUpdate,
            routeProgress,
            defaultOptions
        )

        assertFalse(isUserOffRoute)
    }

    @Test
    fun isUserOffRoute_AssertFalseWhenOnStep() {
        val routeProgress = buildDefaultTestRouteProgress()
        val stepManeuverPoint: Point =
            routeProgress.directionsRoute.legs[0].steps[0].maneuver.location
        val offRouteDetector = OffRouteDetector()
        val firstUpdate =
            buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        offRouteDetector.isUserOffRoute(firstUpdate, routeProgress, defaultOptions)

        val offRoutePoint =
            buildPointAwayFromPoint(stepManeuverPoint, 10.0, 90.0)
        val secondUpdate =
            buildDefaultLocationUpdate(offRoutePoint.longitude, offRoutePoint.latitude)

        val isUserOffRoute =
            offRouteDetector.isUserOffRoute(secondUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRoute)
    }

    @Test
    fun isUserOffRoute_AssertFalseWhenWithinRadiusAndStepLocationHasBadAccuracy() {
        val routeProgress = buildDefaultTestRouteProgress()
        val stepManeuverPoint: Point =
            routeProgress.directionsRoute.legs[0].steps[0].maneuver.location
        val offRouteDetector = OffRouteDetector()
        val firstUpdate =
            buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        offRouteDetector.isUserOffRoute(firstUpdate, routeProgress, defaultOptions)

        val offRoutePoint =
            buildPointAwayFromPoint(stepManeuverPoint, 250.0, 90.0)
        val secondUpdate = buildDefaultLocationUpdate(offRoutePoint.longitude, offRoutePoint.latitude).copy(
            accuracyMeters = 300f
        )

        val isUserOffRoute =
            offRouteDetector.isUserOffRoute(secondUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRoute)
    }

    @Test
    fun isUserOffRoute_AssertFalseWhenOffRouteButCloseToUpcomingStep() {
        val routeProgress = buildDefaultTestRouteProgress()
        val upcomingStepManeuverPoint: Point =
            routeProgress.currentLegProgress.upComingStep!!.maneuver.location
        val callback = mockk<OffRouteCallback>(relaxed = true)
        val offRouteDetector = OffRouteDetector(callback)

        val firstUpdate = buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        offRouteDetector.isUserOffRoute(firstUpdate, routeProgress, defaultOptions)

        val offRoutePoint =
            buildPointAwayFromPoint(upcomingStepManeuverPoint, 30.0, 180.0)
        val secondUpdate =
            buildDefaultLocationUpdate(offRoutePoint.longitude, offRoutePoint.latitude)

        val isUserOffRoute =
            offRouteDetector.isUserOffRoute(secondUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRoute)

        verify {
            callback.onShouldIncreaseIndex()
        }
    }

    @Test
    fun isUserOffRoute_AssertTrueWhenOnRouteButMovingAwayFromManeuver() {
        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep: LegStep =
            routeProgress.currentLegProgress.currentStep
        val offRouteDetector = OffRouteDetector()
        val lineString = LineString.fromPolyline(
            currentStep.geometry,
            Constants.PRECISION_6
        )
        val coordinates = lineString.points.toMutableList()

        val firstLocationUpdate =
            buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        offRouteDetector.isUserOffRoute(firstLocationUpdate, routeProgress, defaultOptions)

        val lastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val secondLocationUpdate = buildDefaultLocationUpdate(
            lastPointInCurrentStep.longitude, lastPointInCurrentStep.latitude
        )
        val isUserOffRouteFirstTry =
            offRouteDetector.isUserOffRoute(secondLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteFirstTry)

        val secondLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val thirdLocationUpdate = buildDefaultLocationUpdate(
            secondLastPointInCurrentStep.longitude, secondLastPointInCurrentStep.latitude
        )
        val isUserOffRouteSecondTry =
            offRouteDetector.isUserOffRoute(thirdLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteSecondTry)

        val thirdLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val fourthLocationUpdate = buildDefaultLocationUpdate(
            thirdLastPointInCurrentStep.longitude, thirdLastPointInCurrentStep.latitude
        )
        val isUserOffRouteThirdTry =
            offRouteDetector.isUserOffRoute(fourthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteThirdTry)

        val fourthLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val fifthLocationUpdate = buildDefaultLocationUpdate(
            fourthLastPointInCurrentStep.longitude, fourthLastPointInCurrentStep.latitude
        )
        val isUserOffRouteFourthTry =
            offRouteDetector.isUserOffRoute(fifthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteFourthTry)

        val fifthLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val sixthLocationUpdate = buildDefaultLocationUpdate(
            fifthLastPointInCurrentStep.longitude, fifthLastPointInCurrentStep.latitude
        )
        val isUserOffRouteFifthTry =
            offRouteDetector.isUserOffRoute(sixthLocationUpdate, routeProgress, defaultOptions)
        assertTrue(isUserOffRouteFifthTry)
    }

    @Test
    fun isUserOffRoute_AssertFalseWhenOnRouteMovingAwayButNotFarEnoughFromManeuver() {
        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep: LegStep =
            routeProgress.currentLegProgress.currentStep
        val offRouteDetector = OffRouteDetector()
        val lineString =
            LineString.fromPolyline(
                currentStep.geometry,
                Constants.PRECISION_6
            )
        val coordinates = lineString.points.toMutableList()

        val firstLocationUpdate =
            buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        offRouteDetector.isUserOffRoute(firstLocationUpdate, routeProgress, defaultOptions)

        val lastPointInCurrentStep = coordinates[7]
        val secondLocationUpdate = buildDefaultLocationUpdate(
            lastPointInCurrentStep.longitude, lastPointInCurrentStep.latitude
        )
        val isUserOffRouteFirstTry =
            offRouteDetector.isUserOffRoute(secondLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteFirstTry)

        val pointSix = coordinates[6]
        val thirdLocationUpdate = buildDefaultLocationUpdate(
            pointSix.longitude, pointSix.latitude
        )
        val isUserOffRouteSecondTry =
            offRouteDetector.isUserOffRoute(thirdLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteSecondTry)

        val fourthLocationUpdate = buildDefaultLocationUpdate(
            pointSix.longitude, pointSix.latitude
        )
        val isUserOffRouteThirdTry =
            offRouteDetector.isUserOffRoute(fourthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteThirdTry)

        val fifthLocationUpdate = buildDefaultLocationUpdate(
            pointSix.longitude, pointSix.latitude
        )
        val isUserOffRouteFourthTry =
            offRouteDetector.isUserOffRoute(fifthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteFourthTry)

        val sixthLocationUpdate = buildDefaultLocationUpdate(
            pointSix.longitude, pointSix.latitude
        )
        val isUserOffRouteFifthTry =
            offRouteDetector.isUserOffRoute(sixthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteFifthTry)

        val pointFive = coordinates[5]
        val seventhLocationUpdate = buildDefaultLocationUpdate(
            pointFive.longitude, pointFive.latitude
        )
        val isUserOffRouteSixthTry =
            offRouteDetector.isUserOffRoute(seventhLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteSixthTry)

        val pointFour = coordinates[4]
        val eighthLocationUpdate = buildDefaultLocationUpdate(
            pointFour.longitude, pointFour.latitude
        )
        val isUserOffRouteSeventhTry =
            offRouteDetector.isUserOffRoute(eighthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteSeventhTry)
    }

    @Test
    fun isUserOffRoute_AssertTrueWhenOnRouteMovingAwayWithRightDirectionTraveling() {
        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep: LegStep =
            routeProgress.currentLegProgress.currentStep
        val offRouteDetector = OffRouteDetector()
        val lineString =
            LineString.fromPolyline(
                currentStep.geometry,
                Constants.PRECISION_6
            )
        val coordinates = lineString.points.toMutableList()

        val firstLocationUpdate =
            buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        offRouteDetector.isUserOffRoute(firstLocationUpdate, routeProgress, defaultOptions)

        val lastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val secondLocationUpdate = buildDefaultLocationUpdate(
            lastPointInCurrentStep.longitude, lastPointInCurrentStep.latitude
        )
        val isUserOffRouteFirstTry =
            offRouteDetector.isUserOffRoute(secondLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteFirstTry)

        val secondLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val thirdLocationUpdate = buildDefaultLocationUpdate(
            secondLastPointInCurrentStep.longitude, secondLastPointInCurrentStep.latitude
        )
        val isUserOffRouteSecondTry =
            offRouteDetector.isUserOffRoute(thirdLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteSecondTry)

        val thirdLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val fourthLocationUpdate = buildDefaultLocationUpdate(
            thirdLastPointInCurrentStep.longitude, thirdLastPointInCurrentStep.latitude
        )
        val isUserOffRouteThirdTry =
            offRouteDetector.isUserOffRoute(fourthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteThirdTry)

        val fourthLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val fifthLocationUpdate = buildDefaultLocationUpdate(
            fourthLastPointInCurrentStep.longitude, fourthLastPointInCurrentStep.latitude
        )
        val isUserOffRouteFourthTry =
            offRouteDetector.isUserOffRoute(fifthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteFourthTry)

        val eighthLocationUpdate = buildDefaultLocationUpdate(
            secondLastPointInCurrentStep.longitude, secondLastPointInCurrentStep.latitude
        )
        val isUserOffRouteSeventhTry =
            offRouteDetector.isUserOffRoute(eighthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteSeventhTry)

        val fifthLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val sixthLocationUpdate = buildDefaultLocationUpdate(
            fifthLastPointInCurrentStep.longitude, fifthLastPointInCurrentStep.latitude
        )
        val isUserOffRouteFifthTry =
            offRouteDetector.isUserOffRoute(sixthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteFifthTry)
    }

    @Test
    fun isUserOffRoute_AssertTrueWhenOnRouteMovingAwayWithNotEnoughRightDirectionTraveling() {
        val options = defaultOptions.copy(
            offRouteMinimumDistanceMetersBeforeRightDirection = 60.0
        )

        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep: LegStep = routeProgress.currentLegProgress.currentStep
        val offRouteDetector = OffRouteDetector()
        val lineString =
            LineString.fromPolyline(
                currentStep.geometry,
                Constants.PRECISION_6
            )
        val coordinates = lineString.points.toMutableList()

        val firstLocationUpdate =
            buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        offRouteDetector.isUserOffRoute(firstLocationUpdate, routeProgress, options)

        val lastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val secondLocationUpdate = buildDefaultLocationUpdate(
            lastPointInCurrentStep.longitude, lastPointInCurrentStep.latitude
        )
        val isUserOffRouteFirstTry =
            offRouteDetector.isUserOffRoute(secondLocationUpdate, routeProgress, options)
        assertFalse(isUserOffRouteFirstTry)

        val secondLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val thirdLocationUpdate = buildDefaultLocationUpdate(
            secondLastPointInCurrentStep.longitude, secondLastPointInCurrentStep.latitude
        )
        val isUserOffRouteSecondTry =
            offRouteDetector.isUserOffRoute(thirdLocationUpdate, routeProgress, options)
        assertFalse(isUserOffRouteSecondTry)

        val thirdLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val fourthLocationUpdate = buildDefaultLocationUpdate(
            thirdLastPointInCurrentStep.longitude, thirdLastPointInCurrentStep.latitude
        )
        val isUserOffRouteThirdTry =
            offRouteDetector.isUserOffRoute(fourthLocationUpdate, routeProgress, options)
        assertFalse(isUserOffRouteThirdTry)

        val fourthLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val fifthLocationUpdate = buildDefaultLocationUpdate(
            fourthLastPointInCurrentStep.longitude, fourthLastPointInCurrentStep.latitude
        )
        val isUserOffRouteFourthTry =
            offRouteDetector.isUserOffRoute(fifthLocationUpdate, routeProgress, options)
        assertFalse(isUserOffRouteFourthTry)

        val eighthLocationUpdate = buildDefaultLocationUpdate(
            secondLastPointInCurrentStep.longitude, secondLastPointInCurrentStep.latitude
        )
        val isUserOffRouteSeventhTry =
            offRouteDetector.isUserOffRoute(eighthLocationUpdate, routeProgress, options)
        assertFalse(isUserOffRouteSeventhTry)

        val fifthLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val sixthLocationUpdate = buildDefaultLocationUpdate(
            fifthLastPointInCurrentStep.longitude, fifthLastPointInCurrentStep.latitude
        )
        val isUserOffRouteFifthTry =
            offRouteDetector.isUserOffRoute(sixthLocationUpdate, routeProgress, options)
        assertTrue(isUserOffRouteFifthTry)
    }

    @Test
    fun isUserOffRoute_AssertFalseTwoUpdatesAwayFromManeuverThenOneTowards() {
        val routeProgress = buildDefaultTestRouteProgress()
        val currentStep: LegStep =
            routeProgress.currentLegProgress.currentStep
        val offRouteDetector = OffRouteDetector()
        val lineString = LineString.fromPolyline(
            currentStep.geometry,
            Constants.PRECISION_6
        )
        val coordinates = lineString.points.toMutableList()

        val firstLocationUpdate =
            buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        offRouteDetector.isUserOffRoute(firstLocationUpdate, routeProgress, defaultOptions)

        val lastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val secondLocationUpdate = buildDefaultLocationUpdate(
            lastPointInCurrentStep.longitude, lastPointInCurrentStep.latitude
        )
        val isUserOffRouteFirstTry =
            offRouteDetector.isUserOffRoute(secondLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteFirstTry)

        val secondLastPointInCurrentStep =
            coordinates.removeAt(coordinates.size - 1)
        val thirdLocationUpdate = buildDefaultLocationUpdate(
            secondLastPointInCurrentStep.longitude, secondLastPointInCurrentStep.latitude
        )
        val isUserOffRouteSecondTry =
            offRouteDetector.isUserOffRoute(thirdLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteSecondTry)

        val fourthLocationUpdate = buildDefaultLocationUpdate(
            lastPointInCurrentStep.longitude, lastPointInCurrentStep.latitude
        )
        val isUserOffRouteThirdTry =
            offRouteDetector.isUserOffRoute(fourthLocationUpdate, routeProgress, defaultOptions)
        assertFalse(isUserOffRouteThirdTry)
    }

    @Test
    fun isUserOffRoute_assertTrueWhenRouteDistanceRemainingIsZero() {
        val location = Location(provider = "test", latitude = 0.0, longitude = 0.0)
        val routeProgress = mockk<RouteProgress>()
        every { routeProgress.distanceRemaining } returns 0.0

        val offRouteDetector = OffRouteDetector()
        val isOffRoute = offRouteDetector.isUserOffRoute(location, routeProgress, defaultOptions)

        assertTrue(isOffRoute)
    }
}
