package org.maplibre.navigation.core.models.notification

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/***
 * The reason why notification was issued.
 * */
@Serializable
@JvmInline
value class NotificationReason(val value: String) {
    companion object {
        val OutOfOrder = NotificationReason("outOfOrder")
        val Occupied = NotificationReason("occupied")
    }
}