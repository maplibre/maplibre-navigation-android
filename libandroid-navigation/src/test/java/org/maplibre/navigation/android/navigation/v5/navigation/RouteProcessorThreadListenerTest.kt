package org.maplibre.navigation.android.navigation.v5.navigation

import android.location.Location
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.instruction.Instruction
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone
import org.maplibre.navigation.android.navigation.v5.milestone.StepMilestone
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class RouteProcessorThreadListenerTest {
    @Test
    fun onNewRouteProgress_notificationProviderIsUpdated() {
        val provider = Mockito.mock(
            NavigationNotificationProvider::class.java
        )
        val listener = buildListener(provider)
        val routeProgress = Mockito.mock(RouteProgress::class.java)

        listener.onNewRouteProgress(Mockito.mock(Location::class.java), routeProgress)

        Mockito.verify(provider).updateNavigationNotification(ArgumentMatchers.eq(routeProgress))
    }

    @Test
    fun onNewRouteProgress_eventDispatcherProgressIsUpdated() {
        val dispatcher = Mockito.mock(
            NavigationEventDispatcher::class.java
        )
        val listener = buildListener(dispatcher)
        val location = Mockito.mock(Location::class.java)
        val routeProgress = Mockito.mock(RouteProgress::class.java)

        listener.onNewRouteProgress(location, routeProgress)

        Mockito.verify(dispatcher)
            .onProgressChange(ArgumentMatchers.eq(location), ArgumentMatchers.eq(routeProgress))
    }

    @Test
    fun onMilestoneTrigger_eventDispatcherSendsMilestone() {
        val milestones: MutableList<Milestone> = ArrayList()
        val stepMilestone = StepMilestone.Builder().build()
        milestones.add(stepMilestone)
        val eventDispatcher = Mockito.mock(
            NavigationEventDispatcher::class.java
        )
        val listener = buildListener(eventDispatcher)
        val routeProgress = Mockito.mock(RouteProgress::class.java)

        listener.onMilestoneTrigger(milestones, routeProgress)

        Mockito.verify(eventDispatcher).onMilestoneEvent(
            ArgumentMatchers.eq(routeProgress),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.eq(stepMilestone)
        )
    }

    @Test
    fun onMilestoneTrigger_correctInstructionIsBuilt() {
        val customInstruction = "Custom instruction!"
        val instruction = buildCustomInstruction(customInstruction)
        val milestones: MutableList<Milestone> = ArrayList()
        val stepMilestone = StepMilestone.Builder().setInstruction(instruction).build()
        milestones.add(stepMilestone)
        val eventDispatcher = Mockito.mock(
            NavigationEventDispatcher::class.java
        )
        val listener = buildListener(eventDispatcher)
        val routeProgress = Mockito.mock(RouteProgress::class.java)

        listener.onMilestoneTrigger(milestones, routeProgress)

        Mockito.verify(eventDispatcher).onMilestoneEvent(
            ArgumentMatchers.eq(routeProgress),
            ArgumentMatchers.eq(customInstruction),
            ArgumentMatchers.eq(stepMilestone)
        )
    }

    @Test
    fun onUserOffRouteTrue_eventDispatcherSendsEvent() {
        val dispatcher = Mockito.mock(
            NavigationEventDispatcher::class.java
        )
        val listener = buildListener(dispatcher)
        val location = Mockito.mock(Location::class.java)

        listener.onUserOffRoute(location, true)

        Mockito.verify(dispatcher).onUserOffRoute(ArgumentMatchers.eq(location))
    }

    @Test
    fun onUserOffRouteFalse_eventDispatcherDoesNotSendEvent() {
        val dispatcher = Mockito.mock(
            NavigationEventDispatcher::class.java
        )
        val listener = buildListener(dispatcher)

        listener.onUserOffRoute(Mockito.mock(Location::class.java), false)

        Mockito.verifyNoInteractions(dispatcher)
    }

    private fun buildListener(provider: NavigationNotificationProvider): RouteProcessorThreadListener {
        val eventDispatcher = Mockito.mock(
            NavigationEventDispatcher::class.java
        )
        return RouteProcessorThreadListener(eventDispatcher, provider)
    }

    private fun buildListener(eventDispatcher: NavigationEventDispatcher): RouteProcessorThreadListener {
        val provider = Mockito.mock(
            NavigationNotificationProvider::class.java
        )
        return RouteProcessorThreadListener(eventDispatcher, provider)
    }

    private fun buildCustomInstruction(customInstruction: String): Instruction {
        return object : Instruction() {
            override fun buildInstruction(routeProgress: RouteProgress): String {
                return customInstruction
            }
        }
    }
}