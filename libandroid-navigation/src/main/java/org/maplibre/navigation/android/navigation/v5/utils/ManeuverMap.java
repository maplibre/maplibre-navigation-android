package org.maplibre.navigation.android.navigation.v5.utils;

import org.maplibre.navigation.android.navigation.R;

import java.util.HashMap;
import java.util.Map;

import org.maplibre.navigation.android.navigation.v5.navigation.NavigationConstants;

class ManeuverMap {

  private Map<String, Integer> maneuverMap;

  ManeuverMap() {
    maneuverMap = new HashMap<>();
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_UTURN,
      R.drawable.ic_maneuver_turn_180);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_CONTINUE + NavigationConstants.STEP_MANEUVER_MODIFIER_UTURN,
      R.drawable.ic_maneuver_turn_180);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_CONTINUE + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT,
      R.drawable.ic_maneuver_turn_0);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ARRIVE + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_arrive_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ARRIVE + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_arrive_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ARRIVE,
      R.drawable.ic_maneuver_arrive);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_DEPART + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_depart_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_DEPART + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_depart_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_DEPART, R.drawable.ic_maneuver_depart);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_RIGHT,
      R.drawable.ic_maneuver_turn_75);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_turn_45);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT,
      R.drawable.ic_maneuver_turn_30);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_LEFT,
      R.drawable.ic_maneuver_turn_75_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_turn_45_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT,
      R.drawable.ic_maneuver_turn_30_left);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_MERGE + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_merge_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_MERGE + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT,
      R.drawable.ic_maneuver_merge_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_MERGE + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_merge_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_MERGE + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT,
      R.drawable.ic_maneuver_merge_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_MERGE + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT,
      R.drawable.ic_maneuver_turn_0);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_LEFT,
      R.drawable.ic_maneuver_turn_75_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_turn_45_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT,
      R.drawable.ic_maneuver_turn_30_left);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_RIGHT,
      R.drawable.ic_maneuver_turn_75);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_turn_45);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT,
      R.drawable.ic_maneuver_turn_30);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_OFF_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_off_ramp_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_OFF_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT,
      R.drawable.ic_maneuver_off_ramp_slight_left);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_OFF_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_off_ramp_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_OFF_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT,
      R.drawable.ic_maneuver_off_ramp_slight_right);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_FORK + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_fork_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_FORK + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT,
      R.drawable.ic_maneuver_fork_slight_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_FORK + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_fork_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_FORK + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT,
      R.drawable.ic_maneuver_fork_slight_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_FORK + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT,
      R.drawable.ic_maneuver_fork_straight);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_FORK, R.drawable.ic_maneuver_fork);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_END_OF_ROAD + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_end_of_road_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_END_OF_ROAD + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_end_of_road_right);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_roundabout_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_LEFT,
      R.drawable.ic_maneuver_roundabout_sharp_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT,
      R.drawable.ic_maneuver_roundabout_slight_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_roundabout_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_RIGHT,
      R.drawable.ic_maneuver_roundabout_sharp_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT,
      R.drawable.ic_maneuver_roundabout_slight_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT,
      R.drawable.ic_maneuver_roundabout_straight);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT, R.drawable.ic_maneuver_roundabout);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_roundabout_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_LEFT,
      R.drawable.ic_maneuver_roundabout_sharp_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT,
      R.drawable.ic_maneuver_roundabout_slight_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_roundabout_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_RIGHT,
      R.drawable.ic_maneuver_roundabout_sharp_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT,
      R.drawable.ic_maneuver_roundabout_slight_right);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT,
      R.drawable.ic_maneuver_roundabout_straight);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROTARY, R.drawable.ic_maneuver_roundabout);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_turn_45_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_turn_45);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT,
      R.drawable.ic_maneuver_turn_45_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_LEFT,
      R.drawable.ic_maneuver_turn_75_left);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT,
      R.drawable.ic_maneuver_turn_30_left);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT,
      R.drawable.ic_maneuver_turn_45);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_RIGHT,
      R.drawable.ic_maneuver_turn_75);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT,
      R.drawable.ic_maneuver_turn_30);
    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT,
      R.drawable.ic_maneuver_turn_0);

    maneuverMap.put(NavigationConstants.STEP_MANEUVER_TYPE_NEW_NAME + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT,
      R.drawable.ic_maneuver_turn_0);
  }

  public int getManeuverResource(String maneuver) {
    if (maneuverMap.get(maneuver) != null) {
      return maneuverMap.get(maneuver);
    } else {
      return R.drawable.ic_maneuver_turn_0;
    }
  }
}
