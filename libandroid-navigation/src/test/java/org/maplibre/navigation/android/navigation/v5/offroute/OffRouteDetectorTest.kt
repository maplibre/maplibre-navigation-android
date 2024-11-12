package org.maplibre.navigation.android.navigation.v5.offroute

import android.location.Location
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.utils.Constants
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class OffRouteDetectorTest : BaseTest() {
    @Mock
    private val mockLocation: Location? = null

    @Mock
    private val mockProgress: RouteProgress? = null

    @Mock
    private val mockCallback: OffRouteCallback? = null
    private var offRouteDetector: OffRouteDetector? = null
    private var options: MapLibreNavigationOptions? = null

    @Before
    @Throws(Exception::class)
    fun setup() {
        MockitoAnnotations.initMocks(this)

        options = MapLibreNavigationOptions.builder().build()

        offRouteDetector = OffRouteDetector()
        offRouteDetector!!.setOffRouteCallback(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun sanity() {
        Assert.assertNotNull(offRouteDetector)
    }

    @Test
    @Throws(Exception::class)
    fun invalidOffRoute_onFirstLocationUpdate() {
        Mockito.`when`(mockProgress!!.distanceRemaining).thenReturn(1000.0)

        val isUserOffRoute = offRouteDetector!!.isUserOffRoute(mockLocation, mockProgress, options)

        Assert.assertFalse(isUserOffRoute)
    }

    @Test
    @Throws(Exception::class)
    fun validOffRoute_onMinimumDistanceBeforeReroutingPassed() {
        val mapboxOffice = buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
        val routeProgress = buildDefaultTestRouteProgress()
        Mockito.`when`(mockProgress!!.distanceRemaining).thenReturn(1000.0)
        offRouteDetector!!.isUserOffRoute(mockLocation, mockProgress, options)
        val target = buildPointAwayFromLocation(
            mapboxOffice!!,
            options!!.minimumDistanceBeforeRerouting() + 1
        )
        val locationOverMinimumDistance =
            buildDefaultLocationUpdate(target.longitude(), target.latitude())

        val validOffRoute =
            offRouteDetector!!.isUserOffRoute(locationOverMinimumDistance, routeProgress, options)

        Assert.assertTrue(validOffRoute)
    }

    @Test
    fun isUserOffRoute_AssertTrueWhenTooFarFromStep() {
            val routeProgress = buildDefaultTestRouteProgress()
            val stepManeuverPoint: Point =
                routeProgress!!.directionsRoute!!.legs()!!.get(0).steps()!!.get(0).maneuver().location()

            val firstUpdate =
                buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
            offRouteDetector!!.isUserOffRoute(firstUpdate, routeProgress, options)

            val offRoutePoint =
                buildPointAwayFromPoint(stepManeuverPoint, 100.0, 90.0)
            val secondUpdate =
                buildDefaultLocationUpdate(offRoutePoint.longitude(), offRoutePoint.latitude())

            val isUserOffRoute =
                offRouteDetector!!.isUserOffRoute(secondUpdate, routeProgress, options)
            Assert.assertTrue(isUserOffRoute)
        }

    @Test
    fun isUserOffRoute_StepPointSize() {
            val routeProgress = buildDefaultTestRouteProgress()
            val stepManeuverPoint: Point =
                routeProgress!!.directionsRoute.legs()!!.get(0).steps()!!.get(0).maneuver().location()
            removeAllButOneStepPoints(routeProgress)
            val firstUpdate =
                buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
            offRouteDetector!!.isUserOffRoute(firstUpdate, routeProgress, options)
            val offRoutePoint =
                buildPointAwayFromPoint(stepManeuverPoint, 50.0, 90.0)
            val secondUpdate =
                buildDefaultLocationUpdate(offRoutePoint.longitude(), offRoutePoint.latitude())

            val isUserOffRoute =
                offRouteDetector!!.isUserOffRoute(secondUpdate, routeProgress, options)

            Assert.assertFalse(isUserOffRoute)
        }

    @Test
    fun isUserOffRoute_AssertFalseWhenOnStep() {
            val routeProgress = buildDefaultTestRouteProgress()
            val stepManeuverPoint: Point =
                routeProgress!!.directionsRoute.legs()!!.get(0).steps()!!.get(0).maneuver().location()

            val firstUpdate =
                buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
            offRouteDetector!!.isUserOffRoute(firstUpdate, routeProgress, options)

            val offRoutePoint =
                buildPointAwayFromPoint(stepManeuverPoint, 10.0, 90.0)
            val secondUpdate =
                buildDefaultLocationUpdate(offRoutePoint.longitude(), offRoutePoint.latitude())

            val isUserOffRoute =
                offRouteDetector!!.isUserOffRoute(secondUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRoute)
        }

    @Test
    fun isUserOffRoute_AssertFalseWhenWithinRadiusAndStepLocationHasBadAccuracy() {
            val routeProgress = buildDefaultTestRouteProgress()
            val stepManeuverPoint: Point =
                routeProgress!!.directionsRoute.legs()!!.get(0).steps()!!.get(0).maneuver().location()

            val firstUpdate =
                buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
            offRouteDetector!!.isUserOffRoute(firstUpdate, routeProgress, options)

            val offRoutePoint =
                buildPointAwayFromPoint(stepManeuverPoint, 250.0, 90.0)
            val secondUpdate =
                buildDefaultLocationUpdate(offRoutePoint.longitude(), offRoutePoint.latitude())
            Mockito.`when`(secondUpdate!!.accuracy).thenReturn(300f)

            val isUserOffRoute =
                offRouteDetector!!.isUserOffRoute(secondUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRoute)
        }

    @Test
    fun isUserOffRoute_AssertFalseWhenOffRouteButCloseToUpcomingStep() {
            val routeProgress = buildDefaultTestRouteProgress()
            val upcomingStepManeuverPoint: Point =
                routeProgress!!.currentLegProgress!!.upComingStep!!.maneuver().location()

            val firstUpdate =
                buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
            offRouteDetector!!.isUserOffRoute(firstUpdate, routeProgress, options)

            val offRoutePoint =
                buildPointAwayFromPoint(upcomingStepManeuverPoint, 30.0, 180.0)
            val secondUpdate =
                buildDefaultLocationUpdate(offRoutePoint.longitude(), offRoutePoint.latitude())

            val isUserOffRoute =
                offRouteDetector!!.isUserOffRoute(secondUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRoute)
            Mockito.verify(mockCallback, Mockito.times(1))!!.onShouldIncreaseIndex()
        }

    @Test
    fun isUserOffRoute_AssertTrueWhenOnRouteButMovingAwayFromManeuver() {
            val routeProgress = buildDefaultTestRouteProgress()
            val currentStep: LegStep =
                routeProgress!!.currentLegProgress!!.currentStep!!

            val lineString =
                LineString.fromPolyline(
                    currentStep.geometry()!!,
                    Constants.PRECISION_6
                )
            val coordinates =
                lineString.coordinates()

            val firstLocationUpdate =
                buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
            offRouteDetector!!.isUserOffRoute(firstLocationUpdate, routeProgress, options)

            val lastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val secondLocationUpdate = buildDefaultLocationUpdate(
                lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFirstTry =
                offRouteDetector!!.isUserOffRoute(secondLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFirstTry)

            val secondLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val thirdLocationUpdate = buildDefaultLocationUpdate(
                secondLastPointInCurrentStep.longitude(), secondLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteSecondTry =
                offRouteDetector!!.isUserOffRoute(thirdLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteSecondTry)

            val thirdLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val fourthLocationUpdate = buildDefaultLocationUpdate(
                thirdLastPointInCurrentStep.longitude(), thirdLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteThirdTry =
                offRouteDetector!!.isUserOffRoute(fourthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteThirdTry)

            val fourthLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val fifthLocationUpdate = buildDefaultLocationUpdate(
                fourthLastPointInCurrentStep.longitude(), fourthLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFourthTry =
                offRouteDetector!!.isUserOffRoute(fifthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFourthTry)

            val fifthLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val sixthLocationUpdate = buildDefaultLocationUpdate(
                fifthLastPointInCurrentStep.longitude(), fifthLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFifthTry =
                offRouteDetector!!.isUserOffRoute(sixthLocationUpdate, routeProgress, options)
            Assert.assertTrue(isUserOffRouteFifthTry)
        }

    @Test
    fun isUserOffRoute_AssertFalseWhenOnRouteMovingAwayButNotFarEnoughFromManeuver() {
            val routeProgress = buildDefaultTestRouteProgress()
            val currentStep: LegStep =
                routeProgress!!.currentLegProgress!!.currentStep!!

            val lineString =
                LineString.fromPolyline(
                    currentStep.geometry()!!,
                    Constants.PRECISION_6
                )
            val coordinates =
                lineString.coordinates()

            val firstLocationUpdate =
                buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
            offRouteDetector!!.isUserOffRoute(firstLocationUpdate, routeProgress, options)

            val lastPointInCurrentStep = coordinates[7]
            val secondLocationUpdate = buildDefaultLocationUpdate(
                lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFirstTry =
                offRouteDetector!!.isUserOffRoute(secondLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFirstTry)

            val pointSix = coordinates[6]
            val thirdLocationUpdate = buildDefaultLocationUpdate(
                pointSix.longitude(), pointSix.latitude()
            )
            val isUserOffRouteSecondTry =
                offRouteDetector!!.isUserOffRoute(thirdLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteSecondTry)

            val fourthLocationUpdate = buildDefaultLocationUpdate(
                pointSix.longitude(), pointSix.latitude()
            )
            val isUserOffRouteThirdTry =
                offRouteDetector!!.isUserOffRoute(fourthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteThirdTry)

            val fifthLocationUpdate = buildDefaultLocationUpdate(
                pointSix.longitude(), pointSix.latitude()
            )
            val isUserOffRouteFourthTry =
                offRouteDetector!!.isUserOffRoute(fifthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFourthTry)

            val sixthLocationUpdate = buildDefaultLocationUpdate(
                pointSix.longitude(), pointSix.latitude()
            )
            val isUserOffRouteFifthTry =
                offRouteDetector!!.isUserOffRoute(sixthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFifthTry)

            val pointFive = coordinates[5]
            val seventhLocationUpdate = buildDefaultLocationUpdate(
                pointFive.longitude(), pointFive.latitude()
            )
            val isUserOffRouteSixthTry =
                offRouteDetector!!.isUserOffRoute(seventhLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteSixthTry)

            val pointFour = coordinates[4]
            val eighthLocationUpdate = buildDefaultLocationUpdate(
                pointFour.longitude(), pointFour.latitude()
            )
            val isUserOffRouteSeventhTry =
                offRouteDetector!!.isUserOffRoute(eighthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteSeventhTry)
        }

    @Test
    fun isUserOffRoute_AssertTrueWhenOnRouteMovingAwayWithRightDirectionTraveling() {
            val routeProgress = buildDefaultTestRouteProgress()
            val currentStep: LegStep =
                routeProgress!!.currentLegProgress!!.currentStep!!

            val lineString =
                LineString.fromPolyline(
                    currentStep.geometry()!!,
                    Constants.PRECISION_6
                )
            val coordinates =
                lineString.coordinates()

            val firstLocationUpdate =
                buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
            offRouteDetector!!.isUserOffRoute(firstLocationUpdate, routeProgress, options)

            val lastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val secondLocationUpdate = buildDefaultLocationUpdate(
                lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFirstTry =
                offRouteDetector!!.isUserOffRoute(secondLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFirstTry)

            val secondLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val thirdLocationUpdate = buildDefaultLocationUpdate(
                secondLastPointInCurrentStep.longitude(), secondLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteSecondTry =
                offRouteDetector!!.isUserOffRoute(thirdLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteSecondTry)

            val thirdLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val fourthLocationUpdate = buildDefaultLocationUpdate(
                thirdLastPointInCurrentStep.longitude(), thirdLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteThirdTry =
                offRouteDetector!!.isUserOffRoute(fourthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteThirdTry)

            val fourthLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val fifthLocationUpdate = buildDefaultLocationUpdate(
                fourthLastPointInCurrentStep.longitude(), fourthLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFourthTry =
                offRouteDetector!!.isUserOffRoute(fifthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFourthTry)

            val eighthLocationUpdate = buildDefaultLocationUpdate(
                secondLastPointInCurrentStep.longitude(), secondLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteSeventhTry =
                offRouteDetector!!.isUserOffRoute(eighthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteSeventhTry)

            val fifthLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val sixthLocationUpdate = buildDefaultLocationUpdate(
                fifthLastPointInCurrentStep.longitude(), fifthLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFifthTry =
                offRouteDetector!!.isUserOffRoute(sixthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFifthTry)
        }

    @Test
    fun isUserOffRoute_AssertTrueWhenOnRouteMovingAwayWithNotEnoughRightDirectionTraveling() {
            val options = options!!.toBuilder()
                .offRouteMinimumDistanceMetersBeforeRightDirection(60.0)
                .build()

            val routeProgress = buildDefaultTestRouteProgress()
            val currentStep: LegStep =
                routeProgress!!.currentLegProgress!!.currentStep!!

            val lineString =
                LineString.fromPolyline(
                    currentStep.geometry()!!,
                    Constants.PRECISION_6
                )
            val coordinates =
                lineString.coordinates()

            val firstLocationUpdate =
                buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
            offRouteDetector!!.isUserOffRoute(firstLocationUpdate, routeProgress, options)

            val lastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val secondLocationUpdate = buildDefaultLocationUpdate(
                lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFirstTry =
                offRouteDetector!!.isUserOffRoute(secondLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFirstTry)

            val secondLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val thirdLocationUpdate = buildDefaultLocationUpdate(
                secondLastPointInCurrentStep.longitude(), secondLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteSecondTry =
                offRouteDetector!!.isUserOffRoute(thirdLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteSecondTry)

            val thirdLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val fourthLocationUpdate = buildDefaultLocationUpdate(
                thirdLastPointInCurrentStep.longitude(), thirdLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteThirdTry =
                offRouteDetector!!.isUserOffRoute(fourthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteThirdTry)

            val fourthLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val fifthLocationUpdate = buildDefaultLocationUpdate(
                fourthLastPointInCurrentStep.longitude(), fourthLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFourthTry =
                offRouteDetector!!.isUserOffRoute(fifthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFourthTry)

            val eighthLocationUpdate = buildDefaultLocationUpdate(
                secondLastPointInCurrentStep.longitude(), secondLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteSeventhTry =
                offRouteDetector!!.isUserOffRoute(eighthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteSeventhTry)

            val fifthLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val sixthLocationUpdate = buildDefaultLocationUpdate(
                fifthLastPointInCurrentStep.longitude(), fifthLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFifthTry =
                offRouteDetector!!.isUserOffRoute(sixthLocationUpdate, routeProgress, options)
            Assert.assertTrue(isUserOffRouteFifthTry)
        }

    @Test
    fun isUserOffRoute_AssertFalseTwoUpdatesAwayFromManeuverThenOneTowards() {
            val routeProgress = buildDefaultTestRouteProgress()
            val currentStep: LegStep =
                routeProgress!!.currentLegProgress!!.currentStep!!

            val lineString =
                LineString.fromPolyline(
                    currentStep.geometry()!!,
                    Constants.PRECISION_6
                )
            val coordinates =
                lineString.coordinates()

            val firstLocationUpdate =
                buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637)
            offRouteDetector!!.isUserOffRoute(firstLocationUpdate, routeProgress, options)

            val lastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val secondLocationUpdate = buildDefaultLocationUpdate(
                lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
            )
            val isUserOffRouteFirstTry =
                offRouteDetector!!.isUserOffRoute(secondLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteFirstTry)

            val secondLastPointInCurrentStep =
                coordinates.removeAt(coordinates.size - 1)
            val thirdLocationUpdate = buildDefaultLocationUpdate(
                secondLastPointInCurrentStep.longitude(), secondLastPointInCurrentStep.latitude()
            )
            val isUserOffRouteSecondTry =
                offRouteDetector!!.isUserOffRoute(thirdLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteSecondTry)

            val fourthLocationUpdate = buildDefaultLocationUpdate(
                lastPointInCurrentStep.longitude(), lastPointInCurrentStep.latitude()
            )
            val isUserOffRouteThirdTry =
                offRouteDetector!!.isUserOffRoute(fourthLocationUpdate, routeProgress, options)
            Assert.assertFalse(isUserOffRouteThirdTry)
        }

    @Test
    fun isUserOffRoute_assertTrueWhenRouteDistanceRemainingIsZero() {
            val location =
                Mockito.mock(Location::class.java)
            val routeProgress =
                Mockito.mock(RouteProgress::class.java)
            Mockito.`when`(routeProgress.distanceRemaining).thenReturn(0.0)

            val isOffRoute =
                offRouteDetector!!.isUserOffRoute(location, routeProgress, options)

            Assert.assertTrue(isOffRoute)
        }

    private fun removeAllButOneStepPoints(routeProgress: RouteProgress) {
        //TODO fabi755, list is here mutated, this not working after kotlin migration
        for (i in routeProgress.currentStepPoints!!.size - 2 downTo 0) {
            routeProgress.currentStepPoints!!.toMutableList().removeAt(i)
        }
    }
}
