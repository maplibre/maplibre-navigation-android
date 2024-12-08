package org.maplibre.navigation.core.location

import android.os.SystemClock
import org.maplibre.geojson.Point

//TODO fabi755: documentation
data class Location(
    val latitude: Double,
    val longitude: Double,
    // This means HorizontalAccuracyMeters
    val accuracyMeters: Float? = null,
    val speedMetersPerSeconds: Float? = null,
    val bearing: Float? = null,
    //TODO fabi755: is this correct?
    val elapsedRealtimeMilliseconds: Long = SystemClock.elapsedRealtime()
) {

    val point: Point
        get() = Point.fromLngLat(longitude, latitude)
}


//private @Nullable String mProvider;
//private long mTimeMs;
//private long mElapsedRealtimeNs;
//private double mElapsedRealtimeUncertaintyNs;
//private double mLatitudeDegrees;
//private double mLongitudeDegrees;
//private float mHorizontalAccuracyMeters;
//private double mAltitudeMeters;
//private float mAltitudeAccuracyMeters;
//private float mSpeedMetersPerSecond;
//private float mSpeedAccuracyMetersPerSecond;
//private float mBearingDegrees;
//private float mBearingAccuracyDegrees;
//private double mMslAltitudeMeters;
//private float mMslAltitudeAccuracyMeters;