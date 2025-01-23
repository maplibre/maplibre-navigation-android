package org.maplibre.navigation.core.utils

import kotlin.time.Duration.Companion.milliseconds

/**
 * Get the current system time in seconds from Android platform.
 */
internal actual fun getCurrentSystemTimeSeconds(): Long {
    return System.currentTimeMillis().milliseconds.inWholeSeconds
}