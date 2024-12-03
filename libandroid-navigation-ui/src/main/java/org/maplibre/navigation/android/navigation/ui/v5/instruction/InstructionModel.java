package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import android.text.SpannableString;

import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;
import org.maplibre.navigation.android.navigation.v5.utils.DistanceFormatter;

public class InstructionModel {

  private RouteProgress progress;
  private SpannableString stepDistanceRemaining;
  private String drivingSide;

  public InstructionModel(DistanceFormatter distanceFormatter, RouteProgress progress) {
    this.progress = progress;
    double distanceRemaining = progress.getCurrentLegProgress().getCurrentStepProgress().getDistanceRemaining();
    stepDistanceRemaining = distanceFormatter.formatDistance(distanceRemaining);
    this.drivingSide = progress.getCurrentLegProgress().getCurrentStep().getDrivingSide();
  }

  RouteProgress retrieveProgress() {
    return progress;
  }

  SpannableString retrieveStepDistanceRemaining() {
    return stepDistanceRemaining;
  }

  String retrieveDrivingSide() {
    return drivingSide;
  }
}
