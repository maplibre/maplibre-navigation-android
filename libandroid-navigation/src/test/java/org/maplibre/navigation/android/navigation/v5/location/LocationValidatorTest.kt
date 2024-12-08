package org.maplibre.navigation.core.location

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

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

    private fun buildLocationWithAccuracy(accuracyValue: Float): Location {
        val location = mockk<Location> {
            every { accuracyMeters } returns accuracyValue
        }
        return location
    }
}