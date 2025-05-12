package org.maplibre.navigation.core.milestone

import org.maplibre.navigation.core.routeprogress.RouteProgress

/**
 * The currently support properties used for triggering a milestone.
 *
 * @since 0.4.0
 */
object TriggerProperty {

    /**
     * The Milestone will be triggered based on the duration remaining.
     *
     * @since 0.4.0
     */
    const val STEP_DURATION_REMAINING_SECONDS: Int = 0x00000000

    /**
     * The Milestone will be triggered based on the distance remaining.
     *
     * @since 0.4.0
     */
    const val STEP_DISTANCE_REMAINING_METERS: Int = 0x00000001

    /**
     * The Milestone will be triggered based on the total step distance.
     *
     * @since 0.4.0
     */
    const val STEP_DISTANCE_TOTAL_METERS: Int = 0x00000002

    /**
     * The Milestone will be triggered based on the total step duration.
     *
     * @since 0.4.0
     */
    const val STEP_DURATION_TOTAL_SECONDS: Int = 0x00000003

    const val STEP_DISTANCE_TRAVELED_METERS: Int = 0x00000009

    /**
     * The Milestone will be triggered based on the current step index.
     *
     * @since 0.4.0
     */
    const val STEP_INDEX: Int = 0x00000004

    const val NEW_STEP: Int = 0x00000005

    const val FIRST_STEP: Int = 0x00000008

    const val LAST_STEP: Int = 0x00000006

    const val NEXT_STEP_DISTANCE_METERS: Int = 0x00000007

    const val NEXT_STEP_DURATION_SECONDS: Int = 0x00000011

    const val FIRST_LEG: Int = 0x00000009

    const val LAST_LEG: Int = 0x000000010

    const val TRUE_VALUE: Int = 0x00000124

    const val FALSE_VALUE: Int = 0x00000100

    fun getSparseArray(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress
    ): HashMap<Int, Array<Number>> {
        // Build hashMap matching the trigger properties to their corresponding current values.
        return HashMap<Int, Array<Number>>(13).apply {
            val currentLegProgress = routeProgress.currentLegProgress
            val currentStep = currentLegProgress.currentStep

            put(
                STEP_DISTANCE_TOTAL_METERS,
                arrayOf(currentStep.distance)
            )

            put(
                STEP_DURATION_TOTAL_SECONDS,
                arrayOf(currentStep.duration)
            )

            put(
                STEP_DISTANCE_REMAINING_METERS,
                arrayOf(currentLegProgress.currentStepProgress.distanceRemaining)
            )

            put(
                STEP_DURATION_REMAINING_SECONDS,
                arrayOf(currentLegProgress.currentStepProgress.durationRemaining)
            )

            put(
                STEP_DISTANCE_TRAVELED_METERS,
                arrayOf(currentLegProgress.currentStepProgress.distanceTraveled)
            )

            put(
                STEP_INDEX,
                arrayOf(currentLegProgress.stepIndex)
            )

            previousRouteProgress?.currentLegProgress?.let { previousLegProgress ->
                put(
                    NEW_STEP,
                    arrayOf(
                        previousLegProgress.stepIndex,
                        currentLegProgress.stepIndex
                    )
                )
            }

            routeProgress.currentLeg?.steps?.let { steps ->
                put(
                    LAST_STEP,
                    arrayOf(
                        currentLegProgress.stepIndex,
                        steps.size - 2
                    )
                )
            }

            put(
                FIRST_STEP,
                arrayOf(currentLegProgress.stepIndex, 0)
            )

            currentLegProgress.upComingStep?.duration?.let { upComingStepDuration ->
                put(
                    NEXT_STEP_DURATION_SECONDS,
                    arrayOf(upComingStepDuration)
                )
            }

            currentLegProgress.upComingStep?.distance?.let { upComingStepDistance ->
                put(
                    NEXT_STEP_DISTANCE_METERS,
                    arrayOf(upComingStepDistance)
                )
            }

            put(FIRST_LEG, arrayOf(routeProgress.legIndex, 0))

            put(
                LAST_LEG,
                arrayOf(routeProgress.legIndex, routeProgress.directionsRoute.legs.size - 1)
            )
        }
    }
}