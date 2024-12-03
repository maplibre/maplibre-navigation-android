package org.maplibre.navigation.android.navigation.v5.utils

import android.text.TextUtils
import org.maplibre.navigation.android.navigation.R
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.navigation.NavigationConstants

open class ManeuverUtils {

    private val maneuverResources = mutableMapOf(
        NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_UTURN to R.drawable.ic_maneuver_turn_180,
        NavigationConstants.STEP_MANEUVER_TYPE_CONTINUE + NavigationConstants.STEP_MANEUVER_MODIFIER_UTURN to R.drawable.ic_maneuver_turn_180,
        NavigationConstants.STEP_MANEUVER_TYPE_CONTINUE + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT to R.drawable.ic_maneuver_turn_0,

        NavigationConstants.STEP_MANEUVER_TYPE_ARRIVE + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to R.drawable.ic_maneuver_arrive_left,
        NavigationConstants.STEP_MANEUVER_TYPE_ARRIVE + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_arrive_right,
        NavigationConstants.STEP_MANEUVER_TYPE_ARRIVE to
                R.drawable.ic_maneuver_arrive,

        NavigationConstants.STEP_MANEUVER_TYPE_DEPART + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_depart_left,
        NavigationConstants.STEP_MANEUVER_TYPE_DEPART + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_depart_right,
        NavigationConstants.STEP_MANEUVER_TYPE_DEPART to
                R.drawable.ic_maneuver_depart,

        NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_RIGHT to
                R.drawable.ic_maneuver_turn_75,
        NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_turn_45,
        NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT to
                R.drawable.ic_maneuver_turn_30,

        NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_LEFT to
                R.drawable.ic_maneuver_turn_75_left,
        NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_turn_45_left,
        NavigationConstants.STEP_MANEUVER_TYPE_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT to
                R.drawable.ic_maneuver_turn_30_left,

        NavigationConstants.STEP_MANEUVER_TYPE_MERGE + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_merge_left,
        NavigationConstants.STEP_MANEUVER_TYPE_MERGE + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT to
                R.drawable.ic_maneuver_merge_left,
        NavigationConstants.STEP_MANEUVER_TYPE_MERGE + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_merge_right,
        NavigationConstants.STEP_MANEUVER_TYPE_MERGE + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT to
                R.drawable.ic_maneuver_merge_right,
        NavigationConstants.STEP_MANEUVER_TYPE_MERGE + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT to
                R.drawable.ic_maneuver_turn_0,

        NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_LEFT to
                R.drawable.ic_maneuver_turn_75_left,
        NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_turn_45_left,
        NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT to
                R.drawable.ic_maneuver_turn_30_left,

        NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_RIGHT to
                R.drawable.ic_maneuver_turn_75,
        NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_turn_45,
        NavigationConstants.STEP_MANEUVER_TYPE_ON_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT to
                R.drawable.ic_maneuver_turn_30,

        NavigationConstants.STEP_MANEUVER_TYPE_OFF_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_off_ramp_left,
        NavigationConstants.STEP_MANEUVER_TYPE_OFF_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT to
                R.drawable.ic_maneuver_off_ramp_slight_left,

        NavigationConstants.STEP_MANEUVER_TYPE_OFF_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_off_ramp_right,
        NavigationConstants.STEP_MANEUVER_TYPE_OFF_RAMP + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT to
                R.drawable.ic_maneuver_off_ramp_slight_right,

        NavigationConstants.STEP_MANEUVER_TYPE_FORK + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_fork_left,
        NavigationConstants.STEP_MANEUVER_TYPE_FORK + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT to
                R.drawable.ic_maneuver_fork_slight_left,
        NavigationConstants.STEP_MANEUVER_TYPE_FORK + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_fork_right,
        NavigationConstants.STEP_MANEUVER_TYPE_FORK + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT to
                R.drawable.ic_maneuver_fork_slight_right,
        NavigationConstants.STEP_MANEUVER_TYPE_FORK + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT to
                R.drawable.ic_maneuver_fork_straight,
        NavigationConstants.STEP_MANEUVER_TYPE_FORK to
                R.drawable.ic_maneuver_fork,

        NavigationConstants.STEP_MANEUVER_TYPE_END_OF_ROAD + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_end_of_road_left,
        NavigationConstants.STEP_MANEUVER_TYPE_END_OF_ROAD + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_end_of_road_right,

        NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_roundabout_left,
        NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_LEFT to
                R.drawable.ic_maneuver_roundabout_sharp_left,
        NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT to
                R.drawable.ic_maneuver_roundabout_slight_left,
        NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_roundabout_right,
        NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_RIGHT to
                R.drawable.ic_maneuver_roundabout_sharp_right,
        NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT to
                R.drawable.ic_maneuver_roundabout_slight_right,
        NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT to
                R.drawable.ic_maneuver_roundabout_straight,
        NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT to
                R.drawable.ic_maneuver_roundabout,

        NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_roundabout_left,
        NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_LEFT to
                R.drawable.ic_maneuver_roundabout_sharp_left,
        NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT to
                R.drawable.ic_maneuver_roundabout_slight_left,
        NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_roundabout_right,
        NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_RIGHT to
                R.drawable.ic_maneuver_roundabout_sharp_right,
        NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT to
                R.drawable.ic_maneuver_roundabout_slight_right,
        NavigationConstants.STEP_MANEUVER_TYPE_ROTARY + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT to
                R.drawable.ic_maneuver_roundabout_straight,
        NavigationConstants.STEP_MANEUVER_TYPE_ROTARY to
                R.drawable.ic_maneuver_roundabout,

        NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_turn_45_left,
        NavigationConstants.STEP_MANEUVER_TYPE_ROUNDABOUT_TURN + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_turn_45,

        NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_LEFT to
                R.drawable.ic_maneuver_turn_45_left,
        NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_LEFT to
                R.drawable.ic_maneuver_turn_75_left,
        NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_LEFT to
                R.drawable.ic_maneuver_turn_30_left,

        NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT to
                R.drawable.ic_maneuver_turn_45,
        NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_SHARP_RIGHT to
                R.drawable.ic_maneuver_turn_75,
        NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT to
                R.drawable.ic_maneuver_turn_30,
        NavigationConstants.STEP_MANEUVER_TYPE_NOTIFICATION + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT to
                R.drawable.ic_maneuver_turn_0,

        NavigationConstants.STEP_MANEUVER_TYPE_NEW_NAME + NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT to
                R.drawable.ic_maneuver_turn_0,
    )

    fun getManeuverResource(step: LegStep): Int {
        val maneuver = step.maneuver

        val maneuverKey = listOfNotNull(maneuver.type?.text, maneuver.modifier)
        return maneuverResources[TextUtils.join("", maneuverKey)] ?: R.drawable.ic_maneuver_turn_0
    }
}
