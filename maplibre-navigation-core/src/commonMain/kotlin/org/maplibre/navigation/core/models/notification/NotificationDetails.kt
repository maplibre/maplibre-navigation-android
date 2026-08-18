package org.maplibre.navigation.core.models.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDetails(
    /**
     * The optional requested value in the request. For example, it is 3 (meters) if we specify `max_width=3` in the request.
     * */
    @SerialName("requested_value")
    val requestedValue: String? = null,
    /**
     * The optional actual value associated with the property of the road. For example, it is 2.5 (meters) if maximum road width is 2.5 meters.
     * */
    @SerialName("actual_value")
    val actualValue: String? = null,
    /**
     * The optional unit of measure associated with `details.actual_value` and `details.requested_value`.
     * */
    val unit: String? = null,
    /**
     * The optional message associated with the notification.
     * */
    val message: String? = null,
)