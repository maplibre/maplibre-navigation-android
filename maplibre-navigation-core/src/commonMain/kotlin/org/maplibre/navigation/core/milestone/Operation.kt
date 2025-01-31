package org.maplibre.navigation.core.milestone

/**
 * Extracted operation methods are found in this class and are fundamental to how Triggers work.
 *
 * @since 0.4.0
 */
object Operation {

    fun greaterThan(valueOne: Array<Number>, valueTwo: Number): Boolean {
        if (valueOne.size > 1) {
            return if (valueTwo == TriggerProperty.TRUE_VALUE) {
                valueOne[0].toDouble() > valueOne[1].toDouble()
            } else {
                valueOne[0].toDouble() <= valueOne[1].toDouble()
            }
        }
        return valueOne[0].toDouble() > valueTwo.toDouble()
    }

    fun lessThan(valueOne: Array<Number>, valueTwo: Number): Boolean {
        if (valueOne.size > 1) {
            return if (valueTwo == TriggerProperty.TRUE_VALUE) {
                valueOne[0].toDouble() < valueOne[1].toDouble()
            } else {
                valueOne[0].toDouble() >= valueOne[1].toDouble()
            }
        }
        return valueOne[0].toDouble() < valueTwo.toDouble()
    }

    fun notEqual(valueOne: Array<Number>, valueTwo: Number): Boolean {
        if (valueOne.size > 1) {
            return if (valueTwo == TriggerProperty.TRUE_VALUE) {
                valueOne[0] != valueOne[1]
            } else {
                valueOne[0] == valueOne[1]
            }
        }
        return valueOne[0] != valueTwo
    }

    fun equal(valueOne: Array<Number>, valueTwo: Number): Boolean {
        if (valueOne.size > 1) {
            return if (valueTwo == TriggerProperty.TRUE_VALUE) {
                valueOne[0] == valueOne[1]
            } else {
                valueOne[0] != valueOne[1]
            }
        }
        return valueOne[0] == valueTwo
    }

    fun greaterThanEqual(valueOne: Array<Number>, valueTwo: Number): Boolean {
        if (valueOne.size > 1) {
            return if (valueTwo == TriggerProperty.TRUE_VALUE) {
                valueOne[0].toDouble() >= valueOne[1].toDouble()
            } else {
                valueOne[0].toDouble() < valueOne[1].toDouble()
            }
        }
        return valueOne[0].toDouble() >= valueTwo.toDouble()
    }

    fun lessThanEqual(valueOne: Array<Number>, valueTwo: Number): Boolean {
        if (valueOne.size > 1) {
            return if (valueTwo == TriggerProperty.TRUE_VALUE) {
                valueOne[0].toDouble() <= valueOne[1].toDouble()
            } else {
                valueOne[0].toDouble() > valueOne[1].toDouble()
            }
        }
        return valueOne[0].toDouble() <= valueTwo.toDouble()
    }
}
