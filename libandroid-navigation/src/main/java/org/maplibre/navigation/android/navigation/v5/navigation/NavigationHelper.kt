package org.maplibre.navigation.android.navigation.v5.navigation

import android.location.Location
import android.util.Pair
import androidx.core.util.component1
import androidx.core.util.component2
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.utils.PolylineUtils
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.models.RouteLeg
import org.maplibre.navigation.android.navigation.v5.models.StepIntersection
import org.maplibre.navigation.android.navigation.v5.offroute.OffRoute
import org.maplibre.navigation.android.navigation.v5.offroute.OffRouteCallback
import org.maplibre.navigation.android.navigation.v5.offroute.OffRouteDetector
import org.maplibre.navigation.android.navigation.v5.routeprogress.CurrentLegAnnotation
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.utils.Constants
import org.maplibre.navigation.android.navigation.v5.utils.MathUtils
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement
import org.maplibre.turf.TurfMisc
import timber.log.Timber

/**
 * This contains several single purpose methods that help out when a new location update occurs and
 * calculations need to be performed on it.
 */
object NavigationHelper {
    private const val FIRST_POINT = 0
    private const val FIRST_INTERSECTION = 0
    private const val ONE_INDEX = 1
    private const val INDEX_ZERO = 0
    private const val EMPTY_STRING = ""
    private const val ZERO_METERS = 0.0
    private const val TWO_POINTS = 2

    @JvmStatic
    fun buildSnappedLocation(
        mapLibreNavigation: MapLibreNavigation, snapToRouteEnabled: Boolean,
        rawLocation: Location, routeProgress: RouteProgress, userOffRoute: Boolean
    ): Location {
        val location = if (!userOffRoute && snapToRouteEnabled) {
            getSnappedLocation(mapLibreNavigation, rawLocation, routeProgress)
        } else {
            rawLocation
        }
        return location
    }

    /**
     * When a milestones triggered, it's instruction needs to be built either using the provided
     * string or an empty string.
     */
    @JvmStatic
    fun buildInstructionString(routeProgress: RouteProgress?, milestone: Milestone): String {
        if (milestone.instruction != null) {
            // Create a new custom instruction based on the Instruction packaged with the Milestone
            return milestone.instruction!!.buildInstruction(routeProgress)
        }
        return EMPTY_STRING
    }

    /**
     * Calculates the distance remaining in the step from the current users snapped position, to the
     * next maneuver position.
     *
     * If the user is more than 1km away from the route, we are returning the total step distance.
     */
    @JvmStatic
    fun stepDistanceRemaining(
        location: Location, legIndex: Int, stepIndex: Int,
        directionsRoute: DirectionsRoute, stepPoints: List<Point?>
    ): Double {
        // If the linestring coordinate size is less than 2,the distance remaining is zero.
        if (stepPoints.size < 2) {
            return 0.0
        }

        val locationToPoint = Point.fromLngLat(location.longitude, location.latitude)

        // Uses Turf's pointOnLine, which takes a Point and a LineString to calculate the closest
        // Point on the LineString.
        val feature =
            TurfMisc.nearestPointOnLine(locationToPoint, stepPoints, TurfConstants.UNIT_KILOMETERS)

        // Check distance to route line, if it's too high, it makes no sense to snap and we assume the step distance is the whole distance of the step
        val distance = feature.getNumberProperty("dist")
        if (distance != null && distance.toDouble() > 1) {
            Timber.i(
                "Distance to step is larger than 1km, so we won't advance the step, distance: %s km",
                distance.toDouble()
            )
            return TurfMeasurement.length(stepPoints, TurfConstants.UNIT_METERS)
        }

        val snappedPosition = (feature.geometry() as Point?)

        val steps = directionsRoute.legs()!![legIndex].steps()
        val nextManeuverPosition = nextManeuverPosition(
            stepIndex,
            steps!!, stepPoints
        )

        // When the coordinates are empty, no distance can be calculated
        if (nextManeuverPosition == null) {
            return 0.0
        }

        // If the users snapped position equals the next maneuver position
        if (snappedPosition == nextManeuverPosition) {
            return 0.0
        }

        val slicedLine = TurfMisc.lineSlice(
            snappedPosition!!,
            nextManeuverPosition,
            LineString.fromLngLats(stepPoints)
        )
        return TurfMeasurement.length(slicedLine, TurfConstants.UNIT_METERS)
    }

    /**
     * Takes in the already calculated step distance and iterates through the step list from the
     * step index value plus one till the end of the leg.
     */
    @JvmStatic
    fun legDistanceRemaining(
        stepDistanceRemaining: Double, legIndex: Int, stepIndex: Int,
        directionsRoute: DirectionsRoute
    ): Double {
        var stepDistanceRemaining = stepDistanceRemaining
        val steps = directionsRoute.legs()!![legIndex].steps()
        if ((steps!!.size < stepIndex + 1)) {
            return stepDistanceRemaining
        }
        for (i in stepIndex + 1 until steps.size) {
            stepDistanceRemaining += steps[i].distance()
        }
        return stepDistanceRemaining
    }

    /**
     * Takes in the leg distance remaining value already calculated and if additional legs need to be
     * traversed along after the current one, adds those distances and returns the new distance.
     * Otherwise, if the route only contains one leg or the users on the last leg, this value will
     * equal the leg distance remaining.
     */
    @JvmStatic
    fun routeDistanceRemaining(
        legDistanceRemaining: Double, legIndex: Int,
        directionsRoute: DirectionsRoute
    ): Double {
        var legDistanceRemaining = legDistanceRemaining
        if (directionsRoute.legs()!!.size < 2) {
            return legDistanceRemaining
        }

        for (i in legIndex + 1 until directionsRoute.legs()!!.size) {
            legDistanceRemaining += directionsRoute.legs()!![i].distance()!!
        }
        return legDistanceRemaining
    }

    /**
     * Checks whether the user's bearing matches the next step's maneuver provided bearingAfter
     * variable. This is one of the criteria's required for the user location to be recognized as
     * being on the next step or potentially arriving.
     *
     *
     * If the expected turn angle is less than the max turn completion offset, this method will
     * wait for the step distance remaining to be 0.  This way, the step index does not increase
     * prematurely.
     *
     * @param userLocation          the location of the user
     * @param previousRouteProgress used for getting the most recent route information
     * @return boolean true if the user location matches (using a tolerance) the final heading
     * @since 0.2.0
     */
    @JvmStatic
    fun checkBearingForStepCompletion(
        userLocation: Location, previousRouteProgress: RouteProgress,
        stepDistanceRemaining: Double, maxTurnCompletionOffset: Double
    ): Boolean {
        if (previousRouteProgress.currentLegProgress?.upComingStep == null) {
            return false
        }

        // Bearings need to be normalized so when the bearingAfter is 359 and the user heading is 1, we
        // count this as within the MAXIMUM_ALLOWED_DEGREE_OFFSET_FOR_TURN_COMPLETION.
        val maneuver = previousRouteProgress.currentLegProgress!!.upComingStep!!.maneuver()
        val initialBearing = maneuver.bearingBefore()!!
        val initialBearingNormalized = MathUtils.wrap(initialBearing, 0.0, 360.0)
        val finalBearing = maneuver.bearingAfter()!!
        val finalBearingNormalized = MathUtils.wrap(finalBearing, 0.0, 360.0)

        val expectedTurnAngle =
            MathUtils.differenceBetweenAngles(initialBearingNormalized, finalBearingNormalized)

        val userBearingNormalized = MathUtils.wrap(userLocation.bearing.toDouble(), 0.0, 360.0)
        val userAngleFromFinalBearing =
            MathUtils.differenceBetweenAngles(finalBearingNormalized, userBearingNormalized)

        return if (expectedTurnAngle <= maxTurnCompletionOffset) {
            stepDistanceRemaining == 0.0
        } else {
            userAngleFromFinalBearing <= maxTurnCompletionOffset
        }
    }

    /**
     * This is used when a user has completed a step maneuver and the indices need to be incremented.
     * The main purpose of this class is to determine if an additional leg exist and the step index
     * has met the first legs total size, a leg index needs to occur and step index should be reset.
     * Otherwise, the step index is incremented while the leg index remains the same.
     *
     *
     * Rather than returning an int array, a new instance of Navigation Indices gets returned. This
     * provides type safety and making the code a bit more readable.
     *
     *
     * @param routeProgress   need a routeProgress in order to get the directions route leg list size
     * @param previousIndices used for adjusting the indices
     * @return a [NavigationIndices] object which contains the new leg and step indices
     */
    @JvmStatic
    fun increaseIndex(
        routeProgress: RouteProgress,
        previousIndices: NavigationIndices
    ): NavigationIndices {
        val route = routeProgress.directionsRoute
        val previousStepIndex = previousIndices.stepIndex()
        val previousLegIndex = previousIndices.legIndex()
        val routeLegSize = route.legs()!!.size
        val legStepSize = route.legs()!![routeProgress.legIndex].steps()!!.size

        val isOnLastLeg = previousLegIndex == routeLegSize - 1
        val isOnLastStep = previousStepIndex == legStepSize - 1

        if (isOnLastStep && !isOnLastLeg) {
            return NavigationIndices.create((previousLegIndex + 1), 0)
        }

        if (isOnLastStep) {
            return previousIndices
        }
        return NavigationIndices.create(previousLegIndex, (previousStepIndex + 1))
    }

    /**
     * Given the current [DirectionsRoute] and leg / step index,
     * return a list of [Point] representing the current step.
     *
     *
     * This method is only used on a per-step basis as [PolylineUtils.decode]
     * can be a heavy operation based on the length of the step.
     *
     *
     * Returns null if index is invalid.
     *
     * @param directionsRoute for list of steps
     * @param legIndex        to get current step list
     * @param stepIndex       to get current step
     * @return list of [Point] representing the current step
     */
    @JvmStatic
    fun decodeStepPoints(
        directionsRoute: DirectionsRoute, currentPoints: List<Point>,
        legIndex: Int, stepIndex: Int
    ): List<Point> {
        val legs = directionsRoute.legs()
        if (hasInvalidLegs(legs)) {
            return currentPoints
        }
        val steps = legs!![legIndex].steps()
        if (hasInvalidSteps(steps)) {
            return currentPoints
        }
        val invalidStepIndex = stepIndex < 0 || stepIndex > steps!!.size - 1
        if (invalidStepIndex) {
            return currentPoints
        }
        val step = steps!![stepIndex]
            ?: return currentPoints
        val stepGeometry = step.geometry()
        if (stepGeometry != null) {
            return PolylineUtils.decode(stepGeometry, Constants.PRECISION_6)
        }
        return currentPoints
    }

    /**
     * Given a current and upcoming step, this method assembles a list of [StepIntersection]
     * consisting of all of the current step intersections, as well as the first intersection of
     * the upcoming step (if the upcoming step isn't null).
     *
     * @param currentStep  for intersections list
     * @param upcomingStep for first intersection, if not null
     * @return complete list of intersections
     * @since 0.13.0
     */
    @JvmStatic
    fun createIntersectionsList(
        currentStep: LegStep,
        upcomingStep: LegStep?
    ): List<StepIntersection> {
        val intersectionsWithNextManeuver: MutableList<StepIntersection> = ArrayList()
        intersectionsWithNextManeuver.addAll(currentStep.intersections()!!)
        if (upcomingStep != null && !upcomingStep.intersections()!!.isEmpty()) {
            intersectionsWithNextManeuver.add(upcomingStep.intersections()!![FIRST_POINT])
        }
        return intersectionsWithNextManeuver
    }

    /**
     * Creates a list of pairs [StepIntersection] and double distance in meters along a step.
     *
     *
     * Each pair represents an intersection on the given step and its distance along the step geometry.
     *
     *
     * The first intersection is the same point as the first point of the list of step points, so will
     * always be zero meters.
     *
     * @param stepPoints    representing the step geometry
     * @param intersections along the step to be measured
     * @return list of measured intersection pairs
     * @since 0.13.0
     */
    @JvmStatic
    fun createDistancesToIntersections(
        stepPoints: List<Point>,
        intersections: List<StepIntersection>
    ): List<Pair<StepIntersection, Double>> {
        val lessThanTwoStepPoints = stepPoints.size < TWO_POINTS
        val noIntersections = intersections.isEmpty()
        if (lessThanTwoStepPoints || noIntersections) {
            return emptyList()
        }

        val stepLineString = LineString.fromLngLats(stepPoints)
        val firstStepPoint = stepPoints[FIRST_POINT]
        val distancesToIntersections: MutableList<Pair<StepIntersection, Double>> = ArrayList()

        for (intersection in intersections) {
            val intersectionPoint = intersection.location()
            if (firstStepPoint == intersectionPoint) {
                distancesToIntersections.add(Pair(intersection, ZERO_METERS))
            } else {
                val beginningLineString =
                    TurfMisc.lineSlice(firstStepPoint, intersectionPoint, stepLineString)
                val distanceToIntersectionInMeters =
                    TurfMeasurement.length(beginningLineString, TurfConstants.UNIT_METERS)
                distancesToIntersections.add(Pair(intersection, distanceToIntersectionInMeters))
            }
        }
        return distancesToIntersections
    }

    /**
     * Based on the list of measured intersections and the step distance traveled, finds
     * the current intersection a user is traveling along.
     *
     * @param intersections         along the step
     * @param measuredIntersections measured intersections along the step
     * @param stepDistanceTraveled  how far the user has traveled along the step
     * @return the current step intersection
     * @since 0.13.0
     */
    @JvmStatic
    fun findCurrentIntersection(
        intersections: List<StepIntersection>,
        measuredIntersections: List<Pair<StepIntersection, Double>>,
        stepDistanceTraveled: Double
    ): StepIntersection? {
        for (measuredIntersection in measuredIntersections) {
            if (measuredIntersection.first == null) return intersections[0]
            val intersectionDistance = measuredIntersection.second
            val intersectionIndex = measuredIntersections.indexOf(measuredIntersection)
            val nextIntersectionIndex = intersectionIndex + ONE_INDEX
            val measuredIntersectionSize = measuredIntersections.size
            val hasValidNextIntersection = nextIntersectionIndex < measuredIntersectionSize

            if (hasValidNextIntersection) {
                val nextIntersectionDistance = measuredIntersections[nextIntersectionIndex].second
                if (stepDistanceTraveled > intersectionDistance && stepDistanceTraveled < nextIntersectionDistance) {
                    return measuredIntersection.first
                }
            } else if (stepDistanceTraveled > measuredIntersection.second) {
                return measuredIntersection.first
            } else {
                return measuredIntersections[FIRST_INTERSECTION].first
            }
        }
        return intersections[FIRST_INTERSECTION]
    }

    /**
     * Based on the current intersection index, add one and try to get the upcoming.
     *
     *
     * If there is not an upcoming intersection on the step, check for an upcoming step and
     * return the first intersection from the upcoming step.
     *
     * @param intersections       for the current step
     * @param upcomingStep        for the first intersection if needed
     * @param currentIntersection being traveled along
     * @return the upcoming intersection on the step
     * @since 0.13.0
     */
    @JvmStatic
    fun findUpcomingIntersection(
        intersections: List<StepIntersection?>,
        upcomingStep: LegStep?,
        currentIntersection: StepIntersection?
    ): StepIntersection? {
        val intersectionIndex = intersections.indexOf(currentIntersection)
        val nextIntersectionIndex = intersectionIndex + ONE_INDEX
        val intersectionSize = intersections.size
        val isValidUpcomingIntersection = nextIntersectionIndex < intersectionSize
        if (isValidUpcomingIntersection) {
            return intersections[nextIntersectionIndex]
        } else if (upcomingStep != null) {
            val upcomingIntersections = upcomingStep.intersections()
            if (upcomingIntersections != null && !upcomingIntersections.isEmpty()) {
                return upcomingIntersections[FIRST_INTERSECTION]
            }
        }
        return null
    }

    /**
     * Given a list of distance annotations, find the current annotation index.  This index retrieves the
     * current annotation from any provided annotation list in [LegAnnotation].
     *
     * @param currentLegAnnotation current annotation being traveled along
     * @param leg                  holding each list of annotations
     * @param legDistanceRemaining to determine the new set of annotations
     * @return a current set of annotation data for the user's position along the route
     */
    @JvmStatic
    fun createCurrentAnnotation(
        currentLegAnnotation: CurrentLegAnnotation?,
        leg: RouteLeg, legDistanceRemaining: Double
    ): CurrentLegAnnotation? {
        val legAnnotation = leg.annotation() ?: return null
        val distanceList = legAnnotation.distance()
        if (distanceList == null || distanceList.isEmpty()) {
            return null
        }

        val (annotationIndex, distanceToAnnotation) = findAnnotationIndex(
            currentLegAnnotation, leg, legDistanceRemaining, distanceList
        )
        return CurrentLegAnnotation(
            index = annotationIndex,
            distance = distanceList[annotationIndex],
            distanceToAnnotation = distanceToAnnotation,
            duration = legAnnotation.duration()?.get(annotationIndex),
            speed = legAnnotation.speed()?.get(annotationIndex),
            maxSpeed = legAnnotation.maxspeed()?.get(annotationIndex),
            congestion = legAnnotation.congestion()?.get(annotationIndex),
        )
    }

    /**
     * This method runs through the list of milestones in [MapLibreNavigation.getMilestones]
     * and returns a list of occurring milestones (if any), based on their individual criteria.
     *
     * @param previousRouteProgress for checking if milestone is occurring
     * @param routeProgress         for checking if milestone is occurring
     * @param mapLibreNavigation      for list of milestones
     * @return list of occurring milestones
     */
    @JvmStatic
    fun checkMilestones(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress,
        mapLibreNavigation: MapLibreNavigation
    ): List<Milestone> {
        val milestones: MutableList<Milestone> = ArrayList()
        for (milestone in mapLibreNavigation.milestones) {
            if (milestone.isOccurring(previousRouteProgress, routeProgress)) {
                milestones.add(milestone)
            }
        }
        return milestones
    }

    /**
     * This method checks if off route detection is enabled or disabled.
     *
     *
     * If enabled, the off route engine is retrieved from [MapLibreNavigation] and
     * [OffRouteDetector.isUserOffRoute] is called
     * to determine if the location is on or off route.
     *
     * @param navigationLocationUpdate containing new location and navigation objects
     * @param routeProgress    to be used in off route check
     * @param callback         only used if using our default [OffRouteDetector]
     * @return true if on route, false otherwise
     */
    @JvmStatic
    fun isUserOffRoute(
        navigationLocationUpdate: NavigationLocationUpdate, routeProgress: RouteProgress?,
        callback: OffRouteCallback
    ): Boolean {
        val options = navigationLocationUpdate.mapLibreNavigation().options()
        if (!options.enableOffRouteDetection()) {
            return false
        }
        val offRoute = navigationLocationUpdate.mapLibreNavigation().offRouteEngine
        setOffRouteDetectorCallback(offRoute, callback)
        val location = navigationLocationUpdate.location()
        return offRoute.isUserOffRoute(location, routeProgress, options)
    }

    @JvmStatic
    fun shouldCheckFasterRoute(
        navigationLocationUpdate: NavigationLocationUpdate?,
        routeProgress: RouteProgress?
    ): Boolean {
        if (navigationLocationUpdate == null) return false
        val fasterRoute = navigationLocationUpdate.mapLibreNavigation().fasterRouteEngine
        return fasterRoute.shouldCheckFasterRoute(
            navigationLocationUpdate.location(),
            routeProgress
        )
    }

    /**
     * Retrieves the next steps maneuver position if one exist, otherwise it decodes the current steps
     * geometry and uses the last coordinate in the position list.
     */
    @JvmStatic
    fun nextManeuverPosition(stepIndex: Int, steps: List<LegStep>, coords: List<Point?>): Point? {
        // If there is an upcoming step, use it's maneuver as the position.
        if (steps.size > (stepIndex + 1)) {
            return steps[stepIndex + 1].maneuver().location()
        }
        return if (!coords.isEmpty()) coords[coords.size - 1] else null
    }

    private fun findAnnotationIndex(
        currentLegAnnotation: CurrentLegAnnotation?, leg: RouteLeg,
        legDistanceRemaining: Double, distanceAnnotationList: List<Double>
    ): Pair<Int, Double> {
        val legDistances: List<Double> = ArrayList(distanceAnnotationList)
        val totalLegDistance = leg.distance()
        val distanceTraveled = totalLegDistance!! - legDistanceRemaining

        var distanceIndex = 0
        var annotationDistancesTraveled = 0.0
        if (currentLegAnnotation != null) {
            distanceIndex = currentLegAnnotation.index
            annotationDistancesTraveled = currentLegAnnotation.distanceToAnnotation
        }

        for (i in distanceIndex until legDistances.size) {
            val distance = legDistances[i]
            annotationDistancesTraveled += distance
            if (annotationDistancesTraveled > distanceTraveled || i == legDistances.size - 1) {
                val distanceToAnnotation = annotationDistancesTraveled - distance
                return Pair(i, distanceToAnnotation)
            }
        }

        //TODO fabi755: is 0 distance right here?
        return Pair(INDEX_ZERO, 0.0)
    }

    private fun getSnappedLocation(
        mapLibreNavigation: MapLibreNavigation, location: Location,
        routeProgress: RouteProgress
    ): Location {
        val snap = mapLibreNavigation.snapEngine
        return snap.getSnappedLocation(location, routeProgress)
    }

    private fun setOffRouteDetectorCallback(offRoute: OffRoute, callback: OffRouteCallback) {
        if (offRoute is OffRouteDetector) {
            offRoute.setOffRouteCallback(callback)
        }
    }

    private fun hasInvalidLegs(legs: List<RouteLeg>?): Boolean {
        return legs == null || legs.isEmpty()
    }

    private fun hasInvalidSteps(steps: List<LegStep>?): Boolean {
        return steps == null || steps.isEmpty()
    }
}
