package org.maplibre.navigation.core.android.location

import org.junit.runner.RunWith
import org.maplibre.navigation.core.location.toLocation
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import android.location.Location as AndroidLocation
import org.maplibre.navigation.core.location.Location as CommonLocation

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidLocationExtTest {

    @Test
    fun `Convert minimum fields to common location`() {
        val androidLocation = AndroidLocation(null)

        assertEquals(androidLocation.toLocation(), CommonLocation(latitude = 0.0, longitude = 0.0, timeMilliseconds = 0))
    }

    @Test
    fun `Convert all fields to common location`() {
        val androidLocation = AndroidLocation("provider").apply {
            latitude = 1.0
            longitude = 2.0
            accuracy = 3.0f
            altitude = 4.0
            bearing = 5.0f
            speed = 6.0f
            time = 7
            verticalAccuracyMeters = 8.0f
            mslAltitudeMeters = 9.0
            mslAltitudeAccuracyMeters = 10.0f
        }

        assertEquals(
            androidLocation.toLocation(),
            CommonLocation(
                provider = "provider",
                latitude = 1.0,
                longitude = 2.0,
                accuracyMeters = 3.0f,
                altitude = 4.0,
                bearing = 5.0f,
                speedMetersPerSeconds = 6.0f,
                timeMilliseconds = 7,
                altitudeAccuracyMeters = 8.0f,
                mslAltitude = 9.0,
                mslAltitudeAccuracyMeters = 10.0f
            )
        )
    }
}
