package org.maplibre.navigation.android.navigation.v5.utils;

import android.text.TextUtils;

import org.maplibre.navigation.android.navigation.v5.models.LegStep;
import org.maplibre.navigation.android.navigation.v5.models.StepManeuver;
import org.maplibre.navigation.android.navigation.R;

public class ManeuverUtils {

  public static int getManeuverResource(LegStep step) {
    ManeuverMap maneuverMap = new ManeuverMap();
    if (step != null && step.getManeuver() != null) {
      StepManeuver maneuver = step.getManeuver();
      if (!TextUtils.isEmpty(maneuver.getModifier())) {
        return maneuverMap.getManeuverResource(maneuver.getType().getText() + maneuver.getModifier());
      } else {
        return maneuverMap.getManeuverResource(maneuver.getType().getText());
      }
    }
    return R.drawable.ic_maneuver_turn_0;
  }
}
