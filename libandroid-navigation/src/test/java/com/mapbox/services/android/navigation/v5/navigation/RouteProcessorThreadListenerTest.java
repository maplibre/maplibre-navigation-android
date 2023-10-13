package com.mapbox.services.android.navigation.v5.navigation;

import android.location.Location;
import androidx.annotation.NonNull;

import com.mapbox.services.android.navigation.v5.instruction.Instruction;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.milestone.StepMilestone;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class RouteProcessorThreadListenerTest {

  @Test
  public void onNewRouteProgress_notificationProviderIsUpdated() {
    NavigationNotificationProvider provider = mock(NavigationNotificationProvider.class);
    RouteProcessorThreadListener listener = buildListener(provider);
    RouteProgress routeProgress = mock(RouteProgress.class);

    listener.onNewRouteProgress(mock(Location.class), routeProgress);

    verify(provider).updateNavigationNotification(eq(routeProgress));
  }

  @Test
  public void onNewRouteProgress_eventDispatcherProgressIsUpdated() {
    NavigationEventDispatcher dispatcher = mock(NavigationEventDispatcher.class);
    RouteProcessorThreadListener listener = buildListener(dispatcher);
    Location location = mock(Location.class);
    RouteProgress routeProgress = mock(RouteProgress.class);

    listener.onNewRouteProgress(location, routeProgress);

    verify(dispatcher).onProgressChange(eq(location), eq(routeProgress));
  }

  @Test
  public void onMilestoneTrigger_eventDispatcherSendsMilestone() {
    List<Milestone> milestones = new ArrayList<>();
    StepMilestone stepMilestone = new StepMilestone.Builder().build();
    milestones.add(stepMilestone);
    NavigationEventDispatcher eventDispatcher = mock(NavigationEventDispatcher.class);
    RouteProcessorThreadListener listener = buildListener(eventDispatcher);
    RouteProgress routeProgress = mock(RouteProgress.class);

    listener.onMilestoneTrigger(milestones, routeProgress);

    verify(eventDispatcher).onMilestoneEvent(eq(routeProgress), anyString(), eq(stepMilestone));
  }

  @Test
  public void onMilestoneTrigger_correctInstructionIsBuilt() {
    String customInstruction = "Custom instruction!";
    Instruction instruction = buildCustomInstruction(customInstruction);
    List<Milestone> milestones = new ArrayList<>();
    Milestone stepMilestone = new StepMilestone.Builder().setInstruction(instruction).build();
    milestones.add(stepMilestone);
    NavigationEventDispatcher eventDispatcher = mock(NavigationEventDispatcher.class);
    RouteProcessorThreadListener listener = buildListener(eventDispatcher);
    RouteProgress routeProgress = mock(RouteProgress.class);

    listener.onMilestoneTrigger(milestones, routeProgress);

    verify(eventDispatcher).onMilestoneEvent(eq(routeProgress), eq(customInstruction), eq(stepMilestone));
  }

  @Test
  public void onUserOffRouteTrue_eventDispatcherSendsEvent() {
    NavigationEventDispatcher dispatcher = mock(NavigationEventDispatcher.class);
    RouteProcessorThreadListener listener = buildListener(dispatcher);
    Location location = mock(Location.class);

    listener.onUserOffRoute(location, true);

    verify(dispatcher).onUserOffRoute(eq(location));
  }

  @Test
  public void onUserOffRouteFalse_eventDispatcherDoesNotSendEvent() {
    NavigationEventDispatcher dispatcher = mock(NavigationEventDispatcher.class);
    RouteProcessorThreadListener listener = buildListener(dispatcher);

    listener.onUserOffRoute(mock(Location.class), false);

    verifyNoInteractions (dispatcher);
  }

  private RouteProcessorThreadListener buildListener(NavigationNotificationProvider provider) {
    NavigationEventDispatcher eventDispatcher = mock(NavigationEventDispatcher.class);
    return new RouteProcessorThreadListener(eventDispatcher, provider);
  }

  private RouteProcessorThreadListener buildListener(NavigationEventDispatcher eventDispatcher) {
    NavigationNotificationProvider provider = mock(NavigationNotificationProvider.class);
    return new RouteProcessorThreadListener(eventDispatcher, provider);
  }

  @NonNull
  private Instruction buildCustomInstruction(final String customInstruction) {
    return new Instruction() {
      @Override
      public String buildInstruction(RouteProgress routeProgress) {
        return customInstruction;
      }
    };
  }
}