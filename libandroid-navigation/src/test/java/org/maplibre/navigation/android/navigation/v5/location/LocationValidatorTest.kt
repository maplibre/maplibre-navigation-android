package org.maplibre.navigation.android.navigation.v5.location

import android.location.Location
import junit.framework.Assert
import org.junit.Test
import org.mockito.Mockito

class LocationValidatorTest {
    @Test
    fun isValidUpdate_trueOnFirstUpdate() {
        val location = buildLocationWithAccuracy(10f)
        val accuracyThreshold = 100
        val validator = LocationValidator(accuracyThreshold)

        val isValid = validator.isValidUpdate(location)

        Assert.assertTrue(isValid)
    }

    @Test
    fun isValidUpdate_trueWhenUnder100MeterAccuracyThreshold() {
        val location = buildLocationWithAccuracy(90f)
        val validator = buildValidatorWithUpdate()

        val isValid = validator.isValidUpdate(location)

        Assert.assertTrue(isValid)
    }

    @Test
    fun isValidUpdate_falseWhenOver100MeterAccuracyThreshold() {
        val location = buildLocationWithAccuracy(110f)
        val validator = buildValidatorWithUpdate()

        val isValid = validator.isValidUpdate(location)

        Assert.assertFalse(isValid)
    }

    private fun buildValidatorWithUpdate(): LocationValidator {
        val location = buildLocationWithAccuracy(10f)
        val accuracyThreshold = 100
        val validator = LocationValidator(accuracyThreshold)
        validator.isValidUpdate(location)
        return validator
    }

    private fun buildLocationWithAccuracy(accuracy: Float): Location {
        val location = Mockito.mock(Location::class.java)
        Mockito.`when`(location.accuracy).thenReturn(accuracy)
        return location
    }
}