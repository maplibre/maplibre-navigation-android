package org.maplibre.navigation.android.navigation.v5.utils;

import android.text.TextUtils;

import org.maplibre.navigation.android.navigation.v5.models.LegStep;
import org.maplibre.navigation.android.navigation.v5.models.StepManeuver;
import org.maplibre.navigation.android.navigation.R;

public class ManeuverUtils {

  public static int getManeuverResource(LegStep step) {
    ManeuverMap maneuverMap = new ManeuverMap();
    if (step != null && step.maneuver() != null) {
      StepManeuver maneuver = step.maneuver();
      if (!TextUtils.isEmpty(maneuver.modifier())) {
        return maneuverMap.getManeuverResource(maneuver.type() + maneuver.modifier());
      } else {
        return maneuverMap.getManeuverResource(maneuver.type());
      }
    }
    return R.drawable.ic_maneuver_turn_0;
  }
}
