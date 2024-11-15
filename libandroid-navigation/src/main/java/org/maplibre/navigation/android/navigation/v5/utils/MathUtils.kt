package org.maplibre.navigation.android.navigation.v5.utils

import org.maplibre.android.utils.MathUtils as MapLibreMathUtils
import kotlin.math.abs

object MathUtils {

    /**
     * Test a value in specified range, returning minimum if it's below, and maximum if it's above
     *
     * @param value Value to test
     * @param min   Minimum value of range
     * @param max   Maximum value of range
     * @return value if it's between min and max, min if it's below, max if it's above
     */
    @JvmStatic
    fun clamp(value: Double, min: Double, max: Double): Double {
        return MapLibreMathUtils.clamp(value, min, max)
    }

    /**
     * Constrains value to the given range (including min, excluding max) via modular arithmetic.
     *
     * Same formula as used in Core GL (wrap.hpp)
     * std::fmod((std::fmod((value - min), d) + d), d) + min;
     *
     * @param value Value to wrap
     * @param min   Minimum value
     * @param max   Maximum value
     * @return Wrapped value
     */
    @JvmStatic
    fun wrap(value: Double, min: Double, max: Double): Double {
        return MapLibreMathUtils.wrap(value, min, max)
    }

    /**
     * Returns the smallest angle between two angles.
     *
     * @param alpha First angle in degrees
     * @param beta  Second angle in degrees
     * @return Smallest angle between two angles.
     */
    fun differenceBetweenAngles(alpha: Double, beta: Double): Double {
        val phi = abs(beta - alpha) % 360
        return if (phi > 180) 360 - phi else phi
    }
}
