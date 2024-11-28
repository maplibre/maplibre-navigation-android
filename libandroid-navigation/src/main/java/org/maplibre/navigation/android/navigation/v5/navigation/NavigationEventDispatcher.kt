package org.maplibre.navigation.android.navigation.v5.navigation

import android.location.Location
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone
import org.maplibre.navigation.android.navigation.v5.milestone.MilestoneEventListener
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.offroute.OffRouteListener
import org.maplibre.navigation.android.navigation.v5.route.FasterRouteListener
import org.maplibre.navigation.android.navigation.v5.routeprogress.ProgressChangeListener
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList

open class NavigationEventDispatcher {
    private val navigationEventListeners = CopyOnWriteArrayList<NavigationEventListener>()
    private val milestoneEventListeners = CopyOnWriteArrayList<MilestoneEventListener>()
    private val progressChangeListeners = CopyOnWriteArrayList<ProgressChangeListener>()
    private val offRouteListeners = CopyOnWriteArrayList<OffRouteListener>()
    private val fasterRouteListeners = CopyOnWriteArrayList<FasterRouteListener>()

    fun addMilestoneEventListener(milestoneEventListener: MilestoneEventListener) {
        if (milestoneEventListeners.contains(milestoneEventListener)) {
            Timber.w("The specified MilestoneEventListener has already been added to the stack.")
            return
        }
        milestoneEventListeners.add(milestoneEventListener)
    }

    fun removeMilestoneEventListener(milestoneEventListener: MilestoneEventListener?) {
        if (milestoneEventListener == null) {
            milestoneEventListeners.clear()
        } else if (!milestoneEventListeners.contains(milestoneEventListener)) {
            Timber.w("The specified MilestoneEventListener isn't found in stack, therefore, cannot be removed.")
        } else {
            milestoneEventListeners.remove(milestoneEventListener)
        }
    }

    fun addProgressChangeListener(progressChangeListener: ProgressChangeListener) {
        if (progressChangeListeners.contains(progressChangeListener)) {
            Timber.w("The specified ProgressChangeListener has already been added to the stack.")
            return
        }
        progressChangeListeners.add(progressChangeListener)
    }

    fun removeProgressChangeListener(progressChangeListener: ProgressChangeListener?) {
        if (progressChangeListener == null) {
            progressChangeListeners.clear()
        } else if (!progressChangeListeners.contains(progressChangeListener)) {
            Timber.w("The specified ProgressChangeListener isn't found in stack, therefore, cannot be removed.")
        } else {
            progressChangeListeners.remove(progressChangeListener)
        }
    }

    fun addOffRouteListener(offRouteListener: OffRouteListener) {
        if (offRouteListeners.contains(offRouteListener)) {
            Timber.w("The specified OffRouteListener has already been added to the stack.")
            return
        }
        offRouteListeners.add(offRouteListener)
    }

    fun removeOffRouteListener(offRouteListener: OffRouteListener?) {
        if (offRouteListener == null) {
            offRouteListeners.clear()
        } else if (!offRouteListeners.contains(offRouteListener)) {
            Timber.w("The specified OffRouteListener isn't found in stack, therefore, cannot be removed.")
        } else {
            offRouteListeners.remove(offRouteListener)
        }
    }

    fun addNavigationEventListener(navigationEventListener: NavigationEventListener) {
        if (navigationEventListeners.contains(navigationEventListener)) {
            Timber.w("The specified NavigationEventListener has already been added to the stack.")
            return
        }
        navigationEventListeners.add(navigationEventListener)
    }

    fun removeNavigationEventListener(navigationEventListener: NavigationEventListener?) {
        if (navigationEventListener == null) {
            navigationEventListeners.clear()
        } else if (!navigationEventListeners.contains(navigationEventListener)) {
            Timber.w("The specified NavigationEventListener isn't found in stack, therefore, cannot be removed.")
        } else {
            navigationEventListeners.remove(navigationEventListener)
        }
    }

    fun addFasterRouteListener(fasterRouteListener: FasterRouteListener) {
        if (fasterRouteListeners.contains(fasterRouteListener)) {
            Timber.w("The specified FasterRouteListener has already been added to the stack.")
            return
        }
        fasterRouteListeners.add(fasterRouteListener)
    }

    fun removeFasterRouteListener(fasterRouteListener: FasterRouteListener?) {
        if (fasterRouteListener == null) {
            fasterRouteListeners.clear()
        } else if (!fasterRouteListeners.contains(fasterRouteListener)) {
            Timber.w("The specified FasterRouteListener isn't found in stack, therefore, cannot be removed.")
        } else {
            fasterRouteListeners.remove(fasterRouteListener)
        }
    }

    fun onMilestoneEvent(
        routeProgress: RouteProgress,
        instruction: String?,
        milestone: Milestone
    ) {
        for (milestoneEventListener in milestoneEventListeners) {
            milestoneEventListener.onMilestoneEvent(routeProgress, instruction, milestone)
        }
    }

    fun onProgressChange(location: Location, routeProgress: RouteProgress) {
        for (progressChangeListener in progressChangeListeners) {
            progressChangeListener.onProgressChange(location, routeProgress)
        }
    }

    fun onUserOffRoute(location: Location) {
        for (offRouteListener in offRouteListeners) {
            offRouteListener.userOffRoute(location)
        }
    }

    fun onNavigationEvent(isRunning: Boolean) {
        for (navigationEventListener in navigationEventListeners) {
            navigationEventListener.onRunning(isRunning)
        }
    }

    fun onFasterRouteEvent(directionsRoute: DirectionsRoute?) {
        for (fasterRouteListener in fasterRouteListeners) {
            fasterRouteListener.fasterRouteFound(directionsRoute)
        }
    }
}
