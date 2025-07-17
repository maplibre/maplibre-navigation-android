package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import androidx.annotation.Nullable;

import org.maplibre.navigation.core.models.BannerInstructions;
import org.maplibre.navigation.core.models.BannerText;
import org.maplibre.navigation.core.routeprogress.RouteProgress;
import org.maplibre.navigation.android.navigation.ui.v5.utils.DistanceFormatter;

public class BannerInstructionModel extends InstructionModel {

  private final BannerText primaryBannerText;
  private final BannerText secondaryBannerText;
  private final BannerText subBannerText;

  public BannerInstructionModel(DistanceFormatter distanceFormatter, RouteProgress progress,
                                BannerInstructions instructions) {
    super(distanceFormatter, progress);
    primaryBannerText = instructions.getPrimary();
    secondaryBannerText = instructions.getSecondary();
    subBannerText = instructions.getSub();
  }

  BannerText retrievePrimaryBannerText() {
    return primaryBannerText;
  }

  BannerText retrieveSecondaryBannerText() {
    return secondaryBannerText;
  }

  BannerText retrieveSubBannerText() {
    return subBannerText;
  }

  String retrievePrimaryManeuverType() {
    return primaryBannerText.getType().getText();
  }

  @Nullable
  String retrievePrimaryManeuverModifier() {
    return primaryBannerText.getModifier() == null ? null : primaryBannerText.getModifier().getText();
  }

  @Nullable
  Double retrievePrimaryRoundaboutAngle() {
    return primaryBannerText.getDegrees();
  }
}
