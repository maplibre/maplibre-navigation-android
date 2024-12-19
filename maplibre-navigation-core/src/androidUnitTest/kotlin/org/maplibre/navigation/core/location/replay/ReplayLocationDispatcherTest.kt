package org.maplibre.navigation.core.location.replay

import org.maplibre.navigation.core.location.Location
import kotlin.test.Test

class ReplayLocationDispatcherTest {

    @Test(expected = IllegalArgumentException::class)
    fun checksNonNullLocationListRequired() {
        ReplayLocationDispatcher(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun checksNonEmptyLocationListRequired() {
        val empty = emptyList<Location>()

        ReplayLocationDispatcher(empty)
    }

    @Test(expected = IllegalArgumentException::class)
    fun checksNonEmptyLocationListRequiredWhenUpdate() {
        val theReplayLocationDispatcher = ReplayLocationDispatcher(mutableListOf())
        val empty = emptyList<Location>()

        theReplayLocationDispatcher.update(empty)
    }
}