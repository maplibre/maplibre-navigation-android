package org.maplibre.navigation.core.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * Get the current system time in seconds from iOS platform.
 */
internal actual fun getCurrentSystemTimeSeconds(): Long {
    return (NSDate().timeIntervalSince1970).toLong()
}