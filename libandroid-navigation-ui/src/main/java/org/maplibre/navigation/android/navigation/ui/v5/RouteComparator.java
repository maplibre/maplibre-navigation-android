package org.maplibre.navigation.android.navigation.ui.v5;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.maplibre.navigation.core.models.DirectionsResponse;
import org.maplibre.navigation.core.models.DirectionsRoute;
import org.maplibre.navigation.core.models.RouteLeg;

import java.util.List;

class RouteComparator {

  private static final int FIRST_ROUTE = 0;
  private static final int ONE_ROUTE = 1;
  private final NavigationViewRouter navigationViewRouter;

  RouteComparator(NavigationViewRouter navigationViewRouter) {
    this.navigationViewRouter = navigationViewRouter;
  }

  void compare(@NonNull DirectionsResponse response, @Nullable DirectionsRoute chosenRoute) {
    if (isValidRoute(response)) {
      List<DirectionsRoute> routes = response.getRoutes();
      DirectionsRoute bestRoute = routes.get(FIRST_ROUTE);
      if (isNavigationRunning(chosenRoute)) {
        bestRoute = findMostSimilarRoute(routes, bestRoute, chosenRoute);
      }
      navigationViewRouter.updateCurrentRoute(bestRoute);
    }
  }

  private DirectionsRoute findMostSimilarRoute(List<DirectionsRoute> routes, DirectionsRoute currentBestRoute,
                                               DirectionsRoute chosenRoute) {
    DirectionsRoute mostSimilarRoute = currentBestRoute;
    if (routes.size() > ONE_ROUTE) {
      mostSimilarRoute = compareRoutes(chosenRoute, routes);
    }
    return mostSimilarRoute;
  }

  private DirectionsRoute compareRoutes(DirectionsRoute chosenRoute, List<DirectionsRoute> routes) {
    int routeIndex = 0;
    String chosenRouteLegDescription = obtainRouteLegDescriptionFrom(chosenRoute);
    int minSimilarity = Integer.MAX_VALUE;
    for (int index = 0; index < routes.size(); index++) {
      String routeLegDescription = obtainRouteLegDescriptionFrom(routes.get(index));
      int currentSimilarity = DamerauLevenshteinAlgorithm.execute(chosenRouteLegDescription, routeLegDescription);
      if (currentSimilarity < minSimilarity) {
        minSimilarity = currentSimilarity;
        routeIndex = index;
      }
    }
    return routes.get(routeIndex);
  }

  private String obtainRouteLegDescriptionFrom(DirectionsRoute route) {
    List<RouteLeg> routeLegs = route.getLegs();
    StringBuilder routeLegDescription = new StringBuilder();
    for (RouteLeg leg : routeLegs) {
      routeLegDescription.append(leg.getSummary());
    }
    return routeLegDescription.toString();
  }

  private boolean isValidRoute(DirectionsResponse response) {
    return response != null && !response.getRoutes().isEmpty();
  }

  private boolean isNavigationRunning(DirectionsRoute chosenRoute) {
    return chosenRoute != null;
  }
}