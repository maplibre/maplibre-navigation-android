package org.maplibre.navigation.core.navigation

import org.maplibre.navigation.core.milestone.MilestoneEventListener
import org.maplibre.navigation.core.milestone.BannerInstructionMilestone
import kotlin.jvm.JvmField

/**
 * Navigation constants
 *
 * @since 0.1.0
 */
@Suppress("unused")
object NavigationConstants {

    /**
     * If default voice instructions are enabled, this identifier will be used to differentiate them
     * from custom milestones in the
     * [MilestoneEventListener].
     *
     * @since 0.7.0
     */
    const val VOICE_INSTRUCTION_MILESTONE_ID: Int = 1

    /**
     * This identifier will be used to
     * differentiate the [BannerInstructionMilestone]
     * from custom milestones in the [MilestoneEventListener].
     *
     * @since 0.8.0
     */
    const val BANNER_INSTRUCTION_MILESTONE_ID: Int = 2
    /**
     * NavigationLauncher key for storing initial map position in Intent
     */
    const val NAVIGATION_VIEW_INITIAL_MAP_POSITION: String = "navigation_view_initial_map_position"

    /**
     * Text to be shown in AlertView during off-route scenario.
     */
    const val REPORT_PROBLEM: String = "Report Problem"

    /**
     * Duration in which the AlertView is shown with the "Report Problem" text.
     */
    const val ALERT_VIEW_PROBLEM_DURATION: Long = 10000

    /**
     * If a set of light / dark themes been set in [android.content.SharedPreferences]
     */
    const val NAVIGATION_VIEW_PREFERENCE_SET_THEME: String = "navigation_view_theme_preference"

    /**
     * Key for the set light theme in preferences
     */
    const val NAVIGATION_VIEW_LIGHT_THEME: String = "navigation_view_light_theme"

    /**
     * Key for the set dark theme in preferences
     */
    const val NAVIGATION_VIEW_DARK_THEME: String = "navigation_view_dark_theme"

    /**
     * Defines the minimum zoom level of the displayed map.
     */
    const val NAVIGATION_MINIMUM_MAP_ZOOM: Double = 7.0

    /**
     * Maximum duration of the zoom/tilt adjustment animation while tracking.
     */
    const val NAVIGATION_MAX_CAMERA_ADJUSTMENT_ANIMATION_DURATION: Long = 1500L

    /**
     * Minimum duration of the zoom adjustment animation while tracking.
     */
    const val NAVIGATION_MIN_CAMERA_ZOOM_ADJUSTMENT_ANIMATION_DURATION: Long = 300L

    /**
     * Minimum duration of the tilt adjustment animation while tracking.
     */
    const val NAVIGATION_MIN_CAMERA_TILT_ADJUSTMENT_ANIMATION_DURATION: Long = 750L

    /**
     * 125 seconds remaining is considered a low alert level when
     * navigating along a [org.maplibre.navigation.core.models.LegStep].
     *
     * @since 0.9.0
     */
    const val NAVIGATION_LOW_ALERT_DURATION: Int = 125

    /**
     * 70 seconds remaining is considered a medium alert level when
     * navigating along a [org.maplibre.navigation.core.models.LegStep].
     *
     * @since 0.9.0
     */
    const val NAVIGATION_MEDIUM_ALERT_DURATION: Int = 70

    /**
     * 15 seconds remaining is considered a high alert level when
     * navigating along a [org.maplibre.navigation.core.models.LegStep].
     *
     * @since 0.10.1
     */
    const val NAVIGATION_HIGH_ALERT_DURATION: Int = 15

    const val NON_NULL_APPLICATION_CONTEXT_REQUIRED: String =
        "Non-null application context required."

    @JvmField
    val WAYNAME_OFFSET: Array<Float> = arrayOf(0.0f, 40.0f)
    const val MAPLIBRE_LOCATION_SOURCE: String = "maplibre-location-source"
    const val MAPLIBRE_WAYNAME_LAYER: String = "maplibre-wayname-layer"
    const val MAPLIBRE_WAYNAME_ICON: String = "maplibre-wayname-icon"

    // Bundle variable keys
    const val NAVIGATION_VIEW_ROUTE_KEY: String = "route_json"
    const val NAVIGATION_VIEW_SIMULATE_ROUTE: String = "navigation_view_simulate_route"
    const val NAVIGATION_VIEW_ROUTE_PROFILE_KEY: String = "navigation_view_route_profile"
    const val NAVIGATION_VIEW_OFF_ROUTE_ENABLED_KEY: String = "navigation_view_off_route_enabled"
    const val NAVIGATION_VIEW_SNAP_ENABLED_KEY: String = "navigation_view_snap_enabled"

    // Step Maneuver Types
    const val STEP_MANEUVER_TYPE_TURN: String = "turn"
    const val STEP_MANEUVER_TYPE_NEW_NAME: String = "new name"
    const val STEP_MANEUVER_TYPE_DEPART: String = "depart"
    const val STEP_MANEUVER_TYPE_ARRIVE: String = "arrive"
    const val STEP_MANEUVER_TYPE_MERGE: String = "merge"
    const val STEP_MANEUVER_TYPE_ON_RAMP: String = "on ramp"
    const val STEP_MANEUVER_TYPE_OFF_RAMP: String = "off ramp"
    const val STEP_MANEUVER_TYPE_FORK: String = "fork"
    const val STEP_MANEUVER_TYPE_END_OF_ROAD: String = "end of road"
    const val STEP_MANEUVER_TYPE_CONTINUE: String = "continue"
    const val STEP_MANEUVER_TYPE_ROUNDABOUT: String = "roundabout"
    const val STEP_MANEUVER_TYPE_ROTARY: String = "rotary"
    const val STEP_MANEUVER_TYPE_EXIT_ROTARY: String = "exit rotary"
    const val STEP_MANEUVER_TYPE_ROUNDABOUT_TURN: String = "roundabout turn"
    const val STEP_MANEUVER_TYPE_NOTIFICATION: String = "notification"
    const val STEP_MANEUVER_TYPE_EXIT_ROUNDABOUT: String = "exit roundabout"

    // Step Maneuver Modifiers
    const val STEP_MANEUVER_MODIFIER_UTURN: String = "uturn"
    const val STEP_MANEUVER_MODIFIER_SHARP_RIGHT: String = "sharp right"
    const val STEP_MANEUVER_MODIFIER_RIGHT: String = "right"
    const val STEP_MANEUVER_MODIFIER_SLIGHT_RIGHT: String = "slight right"
    const val STEP_MANEUVER_MODIFIER_STRAIGHT: String = "straight"
    const val STEP_MANEUVER_MODIFIER_SLIGHT_LEFT: String = "slight left"
    const val STEP_MANEUVER_MODIFIER_LEFT: String = "left"
    const val STEP_MANEUVER_MODIFIER_SHARP_LEFT: String = "sharp left"

    // Turn Lane Indication
    const val TURN_LANE_INDICATION_LEFT: String = "left"
    const val TURN_LANE_INDICATION_SLIGHT_LEFT: String = "slight left"
    const val TURN_LANE_INDICATION_STRAIGHT: String = "straight"
    const val TURN_LANE_INDICATION_RIGHT: String = "right"
    const val TURN_LANE_INDICATION_SLIGHT_RIGHT: String = "slight right"
    const val TURN_LANE_INDICATION_UTURN: String = "uturn"
}
