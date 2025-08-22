package org.maplibre.navigation.core.location

import android.os.Build
import android.location.Location as AndroidLocation

/**
 * Converts our generic MapLibre location to an Android platform location.
 */
fun Location.toAndroidLocation() = AndroidLocation(provider)
    .also { androidLoc ->
        androidLoc.provider = provider
        androidLoc.latitude = latitude
        androidLoc.longitude = longitude

        bearing?.let { bearing ->
            androidLoc.bearing = bearing
        }

        speedMetersPerSeconds?.let { speed ->
            androidLoc.speed = speed
        }

        accuracyMeters?.let { accuracy ->
            androidLoc.accuracy = accuracy
        }

        altitude?.let { altitude ->
            androidLoc.altitude = altitude
        }

        bearing?.let { bearing ->
            androidLoc.bearing = bearing
        }

        speedMetersPerSeconds?.let { speed ->
            androidLoc.speed = speed
        }

        timeMilliseconds?.let { time ->
            androidLoc.time = time
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            altitudeAccuracyMeters?.let { altitudeAccuracyMeters ->
                androidLoc.verticalAccuracyMeters = altitudeAccuracyMeters
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            mslAltitude?.let { mslAltitude ->
                androidLoc.mslAltitudeMeters = mslAltitude
            }
            
            mslAltitudeAccuracyMeters?.let { mslAltitudeAccuracyMeters ->
                androidLoc.mslAltitudeAccuracyMeters = mslAltitudeAccuracyMeters
            }
        }
    }