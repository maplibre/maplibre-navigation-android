package org.maplibre.navigation.android.navigation.v5.navigation

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.milestone.BannerInstructionMilestone
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone
import org.maplibre.navigation.android.navigation.v5.milestone.StepMilestone
import org.maplibre.navigation.android.navigation.v5.milestone.VoiceInstructionMilestone
import org.maplibre.navigation.android.navigation.v5.offroute.OffRoute
import org.maplibre.navigation.android.navigation.v5.snap.Snap
import org.maplibre.navigation.android.navigation.v5.snap.SnapToRoute

class MapLibreNavigationTest : BaseTest() {
    @Test
    fun sanityTest() {
        val navigation = buildMapLibreNavigation()

        Assert.assertNotNull(navigation)
    }

    @Test
    fun sanityTestWithOptions() {
        val options = MapLibreNavigationOptions()
        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)

        Assert.assertNotNull(navigationWithOptions)
    }

    @Test
    @Throws(Exception::class)
    fun voiceInstructionMilestone_onInitializationDoesGetAdded() {
        val navigation = buildMapLibreNavigation()

        val identifier = searchForVoiceInstructionMilestone(navigation)

        Assert.assertEquals(NavigationConstants.VOICE_INSTRUCTION_MILESTONE_ID, identifier)
    }

    @Test
    @Throws(Exception::class)
    fun bannerInstructionMilestone_onInitializationDoesGetAdded() {
        val navigation = buildMapLibreNavigation()

        val identifier = searchForBannerInstructionMilestone(navigation)

        Assert.assertEquals(NavigationConstants.BANNER_INSTRUCTION_MILESTONE_ID, identifier)
    }

    @Test
    @Throws(Exception::class)
    fun defaultMilestones_onInitializationDoNotGetAdded() {
        val options = MapLibreNavigationOptions(
            defaultMilestonesEnabled = false
        )
        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)

        Assert.assertEquals(0, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun defaultEngines_offRouteEngineDidGetInitialized() {
        val navigation = buildMapLibreNavigation()

        Assert.assertNotNull(navigation.offRouteEngine)
    }

    @Test
    @Throws(Exception::class)
    fun defaultEngines_snapEngineDidGetInitialized() {
        val navigation = buildMapLibreNavigation()

        Assert.assertNotNull(navigation.snapEngine)
    }

    @Test
    @Throws(Exception::class)
    fun addMilestone_milestoneDidGetAdded() {
        val navigation = buildMapLibreNavigation()
        val milestone: Milestone = StepMilestone(identifier = 42)

        navigation.addMilestone(milestone)

        Assert.assertTrue(navigation.milestones.contains(milestone))
    }

    @Test
    @Throws(Exception::class)
    fun addMilestone_milestoneOnlyGetsAddedOnce() {
        val options = MapLibreNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)
        val milestone: Milestone = StepMilestone(identifier = 42)
        navigationWithOptions.addMilestone(milestone)
        navigationWithOptions.addMilestone(milestone)

        Assert.assertEquals(1, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestone_milestoneDidGetRemoved() {
        val options = MapLibreNavigationOptions(
            defaultMilestonesEnabled = false
        )
        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)

        val milestone: Milestone = StepMilestone(identifier = 1)
        navigationWithOptions.addMilestone(milestone)
        navigationWithOptions.removeMilestone(milestone)

        Assert.assertEquals(0, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestone_milestoneDoesNotExist() {
        val options = MapLibreNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)
        val milestone: Milestone = StepMilestone(identifier = 1)
        navigationWithOptions.addMilestone(StepMilestone(identifier = 2))
        navigationWithOptions.removeMilestone(milestone)

        Assert.assertEquals(1, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestone_nullRemovesAllMilestones() {
        val options = MapLibreNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)
        navigationWithOptions.addMilestone(StepMilestone(identifier = 1))
        navigationWithOptions.addMilestone(StepMilestone(identifier = 2))
        navigationWithOptions.addMilestone(StepMilestone(identifier = 3))
        navigationWithOptions.addMilestone(StepMilestone(identifier = 4))

        navigationWithOptions.removeMilestone(null)

        Assert.assertEquals(0, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestone_correctMilestoneWithIdentifierGetsRemoved() {
        val options = MapLibreNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)
        val removedMilestoneIdentifier = 5678
        val milestone = StepMilestone(identifier = removedMilestoneIdentifier)
        navigationWithOptions.addMilestone(milestone)

        navigationWithOptions.removeMilestone(removedMilestoneIdentifier)

        Assert.assertEquals(0, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestone_noMilestoneWithIdentifierFound() {
        val options = MapLibreNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)
        navigationWithOptions.addMilestone(StepMilestone(identifier = 1234))
        val removedMilestoneIdentifier = 5678

        navigationWithOptions.removeMilestone(removedMilestoneIdentifier)

        Assert.assertEquals(1, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun addMilestoneList_duplicateIdentifiersAreIgnored() {
        val options = MapLibreNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)
        val milestoneIdentifier = 5678
        val milestone = StepMilestone(identifier = milestoneIdentifier)
        navigationWithOptions.addMilestone(milestone)
        val milestones: MutableList<Milestone> = java.util.ArrayList()
        milestones.add(milestone)
        milestones.add(milestone)
        milestones.add(milestone)

        navigationWithOptions.addMilestones(milestones)

        Assert.assertEquals(1, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun addMilestoneList_allMilestonesAreAdded() {
        val options = MapLibreNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)
        val firstMilestoneId = 5678
        val secondMilestoneId = 5679
        val firstMilestone = StepMilestone(identifier = firstMilestoneId)
        val secondMilestone = StepMilestone(identifier = secondMilestoneId)
        val milestones: MutableList<Milestone> = ArrayList()
        milestones.add(firstMilestone)
        milestones.add(secondMilestone)

        navigationWithOptions.addMilestones(milestones)

        Assert.assertEquals(2, navigationWithOptions.milestones.size)
    }

    @Test
    fun locationEngine_returnsCorrectLocationEngine() {
        val navigation = buildMapLibreNavigation()
        val locationEngine = mockk<LocationEngine>()
        val locationEngineInstanceNotUsed = mockk<LocationEngine>()

        navigation.locationEngine = locationEngine

        Assert.assertNotSame(locationEngineInstanceNotUsed, navigation.locationEngine)
        Assert.assertEquals(locationEngine, navigation.locationEngine)
    }

    @Test
    @Throws(Exception::class)
    fun startNavigation_doesSendTrueToNavigationEvent() {
        val navigation = buildMapLibreNavigation()
        val navigationEventListener = mockk<NavigationEventListener>(relaxed = true)

        navigation.addNavigationEventListener(navigationEventListener)
        navigation.startNavigation(buildTestDirectionsRoute())

        verify {
            navigationEventListener.onRunning(true)
        }
    }

    @Test
    @Throws(Exception::class)
    fun setSnapEngine_doesReplaceDefaultEngine() {
        val navigation = buildMapLibreNavigation()

        val snap = mockk<Snap>()
        navigation.snapEngine = snap

        Assert.assertTrue(navigation.snapEngine !is SnapToRoute)
    }

    @Test
    @Throws(Exception::class)
    fun setOffRouteEngine_doesReplaceDefaultEngine() {
        val navigation = buildMapLibreNavigation()

        val offRoute = mockk<OffRoute>()
        navigation.offRouteEngine = offRoute

        Assert.assertEquals(offRoute, navigation.offRouteEngine)
    }

    private fun buildMapLibreNavigation(): MapLibreNavigation {
        val context = mockk<Context>(relaxed = true) {
            every { applicationContext } returns this
        }
        return MapLibreNavigation(context, locationEngine = mockk())
    }

    private fun buildMapLibreNavigationWithOptions(options: MapLibreNavigationOptions): MapLibreNavigation {
        val context = mockk<Context> {
            every { applicationContext } returns this
        }
        return MapLibreNavigation(context, options, mockk())
    }

    private fun searchForVoiceInstructionMilestone(navigation: MapLibreNavigation): Int {
        var identifier = -1
        for (milestone in navigation.milestones) {
            if (milestone is VoiceInstructionMilestone) {
                identifier = milestone.identifier
            }
        }
        return identifier
    }

    private fun searchForBannerInstructionMilestone(navigation: MapLibreNavigation): Int {
        var identifier = -1
        for (milestone in navigation.milestones) {
            if (milestone is BannerInstructionMilestone) {
                identifier = milestone.identifier
            }
        }
        return identifier
    }
}