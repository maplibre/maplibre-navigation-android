package org.maplibre.navigation.core.models.notification

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * The refresh type of notification to distinguish between static and dynamic notifications.
 * */
@Serializable
@JvmInline
value class NotificationRefreshType(val value: String) {
    companion object {
        val Static = NotificationRefreshType("static")
        val Dynamic = NotificationRefreshType("dynamic")
    }
}