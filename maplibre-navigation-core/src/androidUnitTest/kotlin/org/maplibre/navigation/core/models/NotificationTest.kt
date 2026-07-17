package org.maplibre.navigation.core.models

import kotlinx.serialization.SerializationException
import org.maplibre.navigation.core.json
import org.maplibre.navigation.core.models.notification.Notification
import org.maplibre.navigation.core.models.notification.NotificationDetails
import org.maplibre.navigation.core.models.notification.NotificationRefreshType
import org.maplibre.navigation.core.models.notification.NotificationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class NotificationTest {

    @Test
    fun routeLeg_fromJson_deserializesNotifications() {
        val routeLeg = json.decodeFromString<RouteLeg>(
            """
            {
              "distance": 100.0,
              "duration": 10.0,
              "steps": [],
              "notifications": [
                {
                  "type": "violation",
                  "subtype": "maxWidth",
                  "refresh_type": "static",
                  "geometry_index": 5,
                  "geometry_index_start": 4,
                  "geometry_index_end": 6,
                  "details": {
                    "requested_value": "3",
                    "actual_value": "2.5",
                    "unit": "m",
                    "message": "Width restriction"
                  }
                }
              ]
            }
            """.trimIndent()
        )

        val notification = routeLeg.notifications.single()
        assertEquals(NotificationType.Violation, notification.type)
        assertEquals(NotificationType.Subtype.MaxWidth, notification.subtype)
        assertEquals(NotificationRefreshType.Static, notification.refreshType)
        assertEquals(5, notification.geometryIndex)
        assertEquals(4, notification.startIndex)
        assertEquals(6, notification.endIndex)
        assertEquals("3", notification.details?.requestedValue)
        assertEquals("2.5", notification.details?.actualValue)
        assertEquals("m", notification.details?.unit)
        assertEquals("Width restriction", notification.details?.message)
    }

    @Test
    fun routeLeg_fromJson_deserializesPointAndRangeNotifications() {
        val routeLeg = json.decodeFromString<RouteLeg>(
            """
            {
              "distance": 100.0,
              "duration": 10.0,
              "steps": [],
              "notifications": [
                {
                  "type": "alert",
                  "subtype": "countryBorderCrossing",
                  "refresh_type": "dynamic",
                  "geometry_index": 7
                },
                {
                  "type": "violation",
                  "subtype": "ferry",
                  "refresh_type": "static",
                  "geometry_index_start": 10,
                  "geometry_index_end": 15
                }
              ]
            }
            """.trimIndent()
        )

        val pointNotification = routeLeg.notifications[0]
        assertEquals(NotificationType.Alert, pointNotification.type)
        assertEquals(NotificationType.Subtype.CountryBorderCrossing, pointNotification.subtype)
        assertEquals(NotificationRefreshType.Dynamic, pointNotification.refreshType)
        assertEquals(7, pointNotification.geometryIndex)
        assertNull(pointNotification.startIndex)
        assertNull(pointNotification.endIndex)
        assertNull(pointNotification.details)

        val rangeNotification = routeLeg.notifications[1]
        assertEquals(NotificationType.Violation, rangeNotification.type)
        assertEquals(NotificationType.Subtype.Ferry, rangeNotification.subtype)
        assertEquals(NotificationRefreshType.Static, rangeNotification.refreshType)
        assertNull(rangeNotification.geometryIndex)
        assertEquals(10, rangeNotification.startIndex)
        assertEquals(15, rangeNotification.endIndex)
    }

    @Test
    fun notification_fromJson_preservesUnknownValuesForForwardCompatibility() {
        val notification = json.decodeFromString<Notification>(
            """{"type":"newType","subtype":"newSubtype","refresh_type":"newRefreshType"}"""
        )

        assertEquals(NotificationType("newType"), notification.type)
        assertEquals(NotificationType.Subtype("newSubtype"), notification.subtype)
        assertEquals(NotificationRefreshType("newRefreshType"), notification.refreshType)
    }

    @Test
    fun notification_fromJson_requiresTypeAndRefreshType() {
        assertFailsWith<SerializationException> {
            json.decodeFromString<Notification>("""{"refresh_type":"static"}""")
        }
        assertFailsWith<SerializationException> {
            json.decodeFromString<Notification>("""{"type":"alert"}""")
        }
    }

    @Test
    fun notification_fromJson_allowsMessageOnlyDetailsAndNoSubtype() {
        val notification = json.decodeFromString<Notification>(
            """{"type":"alert","refresh_type":"dynamic","details":{"message":"Ferry service unavailable"}}"""
        )

        assertNull(notification.subtype)
        assertNull(notification.geometryIndex)
        assertNull(notification.startIndex)
        assertNull(notification.endIndex)
        assertNull(notification.details?.requestedValue)
        assertNull(notification.details?.actualValue)
        assertNull(notification.details?.unit)
        assertEquals("Ferry service unavailable", notification.details?.message)
    }

    @Test
    fun routeLeg_fromJson_withoutNotifications_returnsEmptyList() {
        val routeLeg = json.decodeFromString<RouteLeg>(
            """{"distance":100.0,"duration":10.0,"steps":[]}"""
        )

        assertEquals(emptyList(), routeLeg.notifications)
    }

    @Test
    fun routeLeg_builderAndToBuilder_preserveNotifications() {
        val notification = Notification(
            type = NotificationType.Alert,
            subtype = NotificationType.Subtype.Ferry,
            refreshType = NotificationRefreshType.Static,
        )
        val routeLeg = RouteLeg.Builder(
            distance = 100.0,
            duration = 10.0,
            steps = emptyList()
        ).withNotifications(listOf(notification)).build()

        assertEquals(listOf(notification), routeLeg.notifications)
        assertEquals(routeLeg, routeLeg.toBuilder().build())
    }

    @Test
    fun notification_toJson_usesDirectionsWireNames() {
        val notification = Notification(
            type = NotificationType.Violation,
            refreshType = NotificationRefreshType.Dynamic,
            geometryIndex = 5,
            startIndex = 4,
            endIndex = 6,
            details = NotificationDetails(
                requestedValue = "3",
                actualValue = "2.5",
            )
        )

        val serialized = json.encodeToString(notification)

        assertEquals(
            """{"type":"violation","refresh_type":"dynamic","geometry_index":5,"geometry_index_start":4,"geometry_index_end":6,"details":{"requested_value":"3","actual_value":"2.5"}}""",
            serialized
        )
        assertNull(notification.subtype)
    }
}
