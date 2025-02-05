package org.maplibre.navigation.android.navigation.ui.v5;

import static org.maplibre.navigation.android.navigation.ui.v5.GeoJsonExtKt.toJvmPoints;
import static org.maplibre.navigation.core.navigation.NavigationHelper.createDistancesToIntersections;
import static org.maplibre.navigation.core.navigation.NavigationHelper.createIntersectionsList;
import static org.maplibre.navigation.core.utils.Constants.PRECISION_6;

import androidx.annotation.NonNull;

import org.maplibre.navigation.core.models.DirectionsRoute;
import org.maplibre.navigation.core.models.LegStep;
import org.maplibre.navigation.core.models.StepIntersection;
import org.maplibre.geojson.Point;
import org.maplibre.geojson.utils.PolylineUtils;
import org.maplibre.navigation.core.routeprogress.RouteProgress;

import java.util.List;
import java.util.Map;

class TestRouteProgressBuilder {

    RouteProgress buildDefaultTestRouteProgress(DirectionsRoute testRoute) {
        return buildTestRouteProgress(testRoute, 100, 100,
            100, 0, 0);
    }

    RouteProgress buildTestRouteProgress(DirectionsRoute route,
                                         double stepDistanceRemaining,
                                         double legDistanceRemaining,
                                         double distanceRemaining,
                                         int stepIndex,
                                         int legIndex) {
        List<LegStep> steps = route.getLegs().get(legIndex).getSteps();
        LegStep currentStep = steps.get(stepIndex);
        List<Point> currentStepPoints = buildCurrentStepPoints(currentStep);
        int upcomingStepIndex = stepIndex + 1;
        List<Point> upcomingStepPoints = null;
        LegStep upcomingStep = null;
        if (upcomingStepIndex < steps.size()) {
            upcomingStep = steps.get(upcomingStepIndex);
            String upcomingStepGeometry = upcomingStep.getGeometry();
            upcomingStepPoints = buildStepPointsFromGeometry(upcomingStepGeometry);
        }

        List<StepIntersection> intersections = createIntersectionsList(currentStep, upcomingStep);
        Map<StepIntersection, Double> intersectionDistances = createDistancesToIntersections(
            toJvmPoints(currentStepPoints), intersections
        );

        return new RouteProgress.Builder(
            route,
            legIndex,
            distanceRemaining,
            toJvmPoints(currentStepPoints),
            stepIndex,
            legDistanceRemaining,
            stepDistanceRemaining
        )
            .withUpcomingStepPoints(upcomingStepPoints)
            .withIntersections(intersections)
            .withCurrentIntersection(intersections.get(0))
            .withIntersectionDistancesAlongStep(intersectionDistances)
            .build();
    }

    @NonNull
    private List<Point> buildCurrentStepPoints(LegStep currentStep) {
        String currentStepGeometry = currentStep.getGeometry();
        return buildStepPointsFromGeometry(currentStepGeometry);
    }

    private List<Point> buildStepPointsFromGeometry(String stepGeometry) {
        return toJvmPoints(PolylineUtils.decode(stepGeometry, PRECISION_6));
    }
}