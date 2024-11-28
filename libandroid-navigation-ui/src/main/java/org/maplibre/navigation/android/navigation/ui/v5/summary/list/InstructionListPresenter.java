package org.maplibre.navigation.android.navigation.ui.v5.summary.list;

import androidx.annotation.NonNull;
import android.text.SpannableString;
import android.view.View;

import org.maplibre.navigation.android.navigation.v5.models.BannerInstructions;
import org.maplibre.navigation.android.navigation.v5.models.LegStep;
import org.maplibre.navigation.android.navigation.v5.models.ManeuverModifier;
import org.maplibre.navigation.android.navigation.v5.models.RouteLeg;
import org.maplibre.navigation.android.navigation.v5.models.StepManeuver;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteLegProgress;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;
import org.maplibre.navigation.android.navigation.v5.utils.DistanceFormatter;
import org.maplibre.navigation.android.navigation.v5.utils.RouteUtils;

import java.util.ArrayList;
import java.util.List;

class InstructionListPresenter {

  private static final int TWO_LINES = 2;
  private static final int ONE_LINE = 1;
  private static final float TWO_LINE_BIAS = 0.65f;
  private static final float ONE_LINE_BIAS = 0.5f;
  private static final int FIRST_INSTRUCTION_INDEX = 0;
  private DistanceFormatter distanceFormatter;
  private List<BannerInstructions> instructions;
  private RouteLeg currentLeg;
  private String drivingSide;
  private final RouteUtils routeUtils = new RouteUtils();

  InstructionListPresenter(DistanceFormatter distanceFormatter) {
    this.distanceFormatter = distanceFormatter;
    instructions = new ArrayList<>();
  }

  void onBindInstructionListViewAtPosition(int position, @NonNull InstructionListView listView) {
    BannerInstructions bannerInstructions = instructions.get(position);
    double distance = bannerInstructions.getDistanceAlongGeometry();
    SpannableString distanceText = distanceFormatter.formatDistance(distance);
    updateListView(listView, bannerInstructions, distanceText);
  }

  int retrieveBannerInstructionListSize() {
    return instructions.size();
  }

  boolean updateBannerListWith(RouteProgress routeProgress) {
    addBannerInstructions(routeProgress);
    return updateInstructionList(routeProgress);
  }

  void updateDistanceFormatter(DistanceFormatter distanceFormatter) {
    if (shouldUpdate(distanceFormatter)) {
      this.distanceFormatter = distanceFormatter;
    }
  }

  private boolean shouldUpdate(DistanceFormatter distanceFormatter) {
    return distanceFormatter != null
      && (this.distanceFormatter == null || !this.distanceFormatter.equals(distanceFormatter));
  }

  private void updateListView(@NonNull InstructionListView listView, BannerInstructions bannerInstructions,
                              SpannableString distanceText) {
    listView.updatePrimaryText(bannerInstructions.getPrimary().getText());
    updateSecondaryInstruction(listView, bannerInstructions);
    updateManeuverView(listView, bannerInstructions);
    listView.updateDistanceText(distanceText);
  }

  private void updateSecondaryInstruction(@NonNull InstructionListView listView,
                                          BannerInstructions bannerInstructions) {
    boolean hasSecondaryInstructions = bannerInstructions.getSecondary() != null;
    adjustListViewForSecondaryInstructions(listView, hasSecondaryInstructions);
    if (hasSecondaryInstructions) {
      listView.updateSecondaryText(bannerInstructions.getSecondary().getText());
    }
  }

  private void adjustListViewForSecondaryInstructions(InstructionListView listView, boolean hasSecondaryInstructions) {
    if (hasSecondaryInstructions) {
      hasSecondaryInstructions(listView);
    } else {
      hasNoSecondaryInstructions(listView);
    }
  }

  private void hasSecondaryInstructions(InstructionListView listView) {
    listView.updatePrimaryMaxLines(ONE_LINE);
    listView.updateSecondaryVisibility(View.VISIBLE);
    listView.updateBannerVerticalBias(TWO_LINE_BIAS);
  }

  private void hasNoSecondaryInstructions(InstructionListView listView) {
    listView.updatePrimaryMaxLines(TWO_LINES);
    listView.updateSecondaryVisibility(View.GONE);
    listView.updateBannerVerticalBias(ONE_LINE_BIAS);
  }

  private void updateManeuverView(@NonNull InstructionListView listView, BannerInstructions bannerInstructions) {
    StepManeuver.Type maneuverType = bannerInstructions.getPrimary().getType();
    if (maneuverType == null) {
      return;
    }

    ManeuverModifier.Type maneuverModifier = bannerInstructions.getPrimary().getModifier();
    listView.updateManeuverViewTypeAndModifier(maneuverType.getText(), maneuverModifier != null ? maneuverModifier.getText() : null);

    Double roundaboutDegrees = bannerInstructions.getPrimary().getDegrees();
    if (roundaboutDegrees != null) {
      listView.updateManeuverViewRoundaboutDegrees(roundaboutDegrees.floatValue());
    }
    listView.updateManeuverViewDrivingSide(drivingSide);
  }

  private void addBannerInstructions(RouteProgress routeProgress) {
    if (isNewLeg(routeProgress)) {
      instructions = new ArrayList<>();
      currentLeg = routeProgress.getCurrentLeg();
      drivingSide = routeProgress.getCurrentLegProgress().getCurrentStep().getDrivingSide();
      List<LegStep> steps = currentLeg.getSteps();
      for (LegStep step : steps) {
        List<BannerInstructions> bannerInstructions = step.getBannerInstructions();
        if (bannerInstructions != null && !bannerInstructions.isEmpty()) {
          instructions.addAll(bannerInstructions);
        }
      }
    }
  }

  private boolean isNewLeg(RouteProgress routeProgress) {
    return currentLeg == null || !currentLeg.equals(routeProgress.getCurrentLeg());
  }

  private boolean updateInstructionList(RouteProgress routeProgress) {
    if (instructions.isEmpty()) {
      return false;
    }
    RouteLegProgress legProgress = routeProgress.getCurrentLegProgress();
    LegStep currentStep = legProgress.getCurrentStep();
    double stepDistanceRemaining = legProgress.getCurrentStepProgress().getDistanceRemaining();
    BannerInstructions currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
      currentStep, stepDistanceRemaining
    );
    if (!instructions.contains(currentBannerInstructions)) {
      return false;
    }
    int currentInstructionIndex = instructions.indexOf(currentBannerInstructions);
    return removeInstructionsFrom(currentInstructionIndex);
  }

  private boolean removeInstructionsFrom(int currentInstructionIndex) {
    if (currentInstructionIndex == FIRST_INSTRUCTION_INDEX) {
      instructions.remove(FIRST_INSTRUCTION_INDEX);
      return true;
    } else if (currentInstructionIndex <= instructions.size()) {
      instructions.subList(FIRST_INSTRUCTION_INDEX, currentInstructionIndex).clear();
      return true;
    }
    return false;
  }
}
