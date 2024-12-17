package org.maplibre.navigation.core.utils

import kotlin.jvm.JvmStatic
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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
        return max(min, min(max, value));
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
        val delta = max - min
        val firstMod = (value - min) % delta
        val secondMod = (firstMod + delta) % delta
        return secondMod + min
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
