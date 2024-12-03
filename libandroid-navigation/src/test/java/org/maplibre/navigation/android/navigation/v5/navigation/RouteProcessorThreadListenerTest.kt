package org.maplibre.navigation.android.navigation.v5.navigation

import android.location.Location
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.instruction.Instruction
import org.maplibre.navigation.android.navigation.v5.milestone.StepMilestone
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

class RouteProcessorThreadListenerTest {

    @Test
    fun onNewRouteProgress_notificationProviderIsUpdated() {
        val provider = mockk<NavigationNotificationProvider>(relaxed = true)
        val listener = buildListener(provider)
        val routeProgress = mockk<RouteProgress>(relaxed = true)

        listener.onNewRouteProgress(mockk(relaxed = true), routeProgress)

        verify { provider.updateNavigationNotification(routeProgress) }
    }

    @Test
    fun onNewRouteProgress_eventDispatcherProgressIsUpdated() {
        val dispatcher = mockk<NavigationEventDispatcher>(relaxed = true)
        val listener = buildListener(dispatcher)
        val location = mockk<Location>(relaxed = true)
        val routeProgress = mockk<RouteProgress>(relaxed = true)

        listener.onNewRouteProgress(location, routeProgress)

        verify { dispatcher.onProgressChange(location, routeProgress) }
    }

    @Test
    fun onMilestoneTrigger_eventDispatcherSendsMilestone() {
        val stepMilestone = StepMilestone(identifier = 1)
        val eventDispatcher = mockk<NavigationEventDispatcher>(relaxed = true)
        val listener = buildListener(eventDispatcher)
        val routeProgress = mockk<RouteProgress>(relaxed = true)

        listener.onMilestoneTrigger(listOf(stepMilestone), routeProgress)

        verify {
            eventDispatcher.onMilestoneEvent(routeProgress, any(), stepMilestone)
        }
    }

    @Test
    fun onMilestoneTrigger_correctInstructionIsBuilt() {
        val customInstruction = "Custom instruction!"
        val instruction = buildCustomInstruction(customInstruction)
        val stepMilestone = StepMilestone(identifier = 1, instruction = instruction)
        val eventDispatcher = mockk<NavigationEventDispatcher>(relaxed = true)
        val listener = buildListener(eventDispatcher)
        val routeProgress = mockk<RouteProgress>(relaxed = true)

        listener.onMilestoneTrigger(listOf(stepMilestone), routeProgress)

        verify {
            eventDispatcher.onMilestoneEvent(
                routeProgress,
                customInstruction,
                stepMilestone
            )
        }
    }

    @Test
    fun onUserOffRouteTrue_eventDispatcherSendsEvent() {
        val dispatcher = mockk<NavigationEventDispatcher>(relaxed = true)
        val listener = buildListener(dispatcher)
        val location = mockk<Location>(relaxed = true)

        listener.onUserOffRoute(location, true)

        verify {
            dispatcher.onUserOffRoute(location)
        }
    }

    @Test
    fun onUserOffRouteFalse_eventDispatcherDoesNotSendEvent() {
        val dispatcher = mockk<NavigationEventDispatcher>(relaxed = true)
        val listener = buildListener(dispatcher)

        listener.onUserOffRoute(mockk<Location>(relaxed = true), false)

        verify {
            dispatcher.wasNot(Called)
        }
    }

    private fun buildListener(provider: NavigationNotificationProvider): RouteProcessorThreadListener {
        val eventDispatcher = mockk<NavigationEventDispatcher>(relaxed = true)
        return RouteProcessorThreadListener(eventDispatcher, provider)
    }

    private fun buildListener(eventDispatcher: NavigationEventDispatcher): RouteProcessorThreadListener {
        val provider = mockk<NavigationNotificationProvider>(relaxed = true)
        return RouteProcessorThreadListener(eventDispatcher, provider)
    }

    private fun buildCustomInstruction(customInstruction: String): Instruction {
        return Instruction { customInstruction }
    }
}