package org.maplibre.navigation.core.navigation

fun interface NavigationEventListener {
    fun onRunning(running: Boolean)
}
