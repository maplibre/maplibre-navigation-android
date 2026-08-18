package org.maplibre.navigation.core.models.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A notification that is relevant to a route leg.
 * According to Mapbox Navigation Notifications API spec [Mapbox Docs](https://docs.mapbox.com/api/navigation/directions/#notification-object)
 * */
@Serializable
data class Notification(
    /**
     * The type of notifications. Notification types supported are violation and alert.
     * */
    val type: NotificationType,

    /**
     * The optional subtype of notification.
     * */
    val subtype: NotificationType.Subtype? = null,

    /**
     * The refresh type of notification to distinguish between static and dynamic notifications.
     * */
    @SerialName("refresh_type")
    val refreshType: NotificationRefreshType,

    /**
     * The optional position in the coordinate list where the notification occurred, relative to the start of the leg it's on.
     * */
    @SerialName("geometry_index")
    val geometryIndex: Int? = null,

    /**
     * The optional position in the coordinate list where the notification began, relative to the start of the leg it's on.
     * */
    @SerialName("geometry_index_start")
    val startIndex: Int? = null,

    /**
     * The optional position in the coordinate list where the notification ended, relative to the start of the leg it's on.
     * */
    @SerialName("geometry_index_end")
    val endIndex: Int? = null,

    /**
     * 	The optional details specific to the notification type and subtype.
     * */
    val details: NotificationDetails? = null
)

