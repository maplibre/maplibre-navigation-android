package org.maplibre.navigation.android.navigation.v5.navigation

import android.content.Context
import junit.framework.Assert
import org.junit.Test
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.navigation.android.navigation.v5.BaseTest
import org.maplibre.navigation.android.navigation.v5.milestone.BannerInstructionMilestone
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone
import org.maplibre.navigation.android.navigation.v5.milestone.StepMilestone
import org.maplibre.navigation.android.navigation.v5.milestone.VoiceInstructionMilestone
import org.maplibre.navigation.android.navigation.v5.navigation.camera.SimpleCamera
import org.maplibre.navigation.android.navigation.v5.offroute.OffRoute
import org.maplibre.navigation.android.navigation.v5.snap.Snap
import org.maplibre.navigation.android.navigation.v5.snap.SnapToRoute
import org.mockito.Mockito

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
        val milestone: Milestone = StepMilestone.Builder().build()

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
        val milestone: Milestone = StepMilestone.Builder().build()
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

        val milestone: Milestone = StepMilestone.Builder().build()
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
        val milestone: Milestone = StepMilestone.Builder().build()
        navigationWithOptions.addMilestone(StepMilestone.Builder().build())
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
        navigationWithOptions.addMilestone(StepMilestone.Builder().build())
        navigationWithOptions.addMilestone(StepMilestone.Builder().build())
        navigationWithOptions.addMilestone(StepMilestone.Builder().build())
        navigationWithOptions.addMilestone(StepMilestone.Builder().build())

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
        val milestone = StepMilestone.Builder().setIdentifier(removedMilestoneIdentifier).build()
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
        navigationWithOptions.addMilestone(StepMilestone.Builder().build())
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
        val milestone = StepMilestone.Builder().setIdentifier(milestoneIdentifier).build()
        navigationWithOptions.addMilestone(milestone)
        val milestones: MutableList<Milestone> = ArrayList()
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
        val firstMilestone = StepMilestone.Builder().setIdentifier(firstMilestoneId).build()
        val secondMilestone = StepMilestone.Builder().setIdentifier(secondMilestoneId).build()
        val milestones: MutableList<Milestone> = ArrayList()
        milestones.add(firstMilestone)
        milestones.add(secondMilestone)

        navigationWithOptions.addMilestones(milestones)

        Assert.assertEquals(2, navigationWithOptions.milestones.size)
    }

    //TODO fabi755
//    @Test
//    fun locationEngine_returnsCorrectLocationEngine() {
//        val navigation = buildMapLibreNavigation()
//        val locationEngine = Mockito.mock(LocationEngine::class.java)
//        val locationEngineInstanceNotUsed = Mockito.mock(
//            LocationEngine::class.java
//        )
//
//        navigation.locationEngine = locationEngine
//
//        Assert.assertNotSame(locationEngineInstanceNotUsed, navigation.locationEngine)
//        Assert.assertEquals(locationEngine, navigation.locationEngine)
//    }

    @Test
    @Throws(Exception::class)
    fun startNavigation_doesSendTrueToNavigationEvent() {
        val navigation = buildMapLibreNavigation()
        val navigationEventListener = Mockito.mock(
            NavigationEventListener::class.java
        )

        navigation.addNavigationEventListener(navigationEventListener)
        navigation.startNavigation(buildTestDirectionsRoute()!!)

        Mockito.verify(navigationEventListener, Mockito.times(1)).onRunning(true)
    }

    //TODO fabi755
//    @Test
//    @Throws(Exception::class)
//    fun setSnapEngine_doesReplaceDefaultEngine() {
//        val navigation = buildMapLibreNavigation()
//
//        val snap = Mockito.mock(Snap::class.java)
//        navigation.snapEngine = snap
//
//        Assert.assertTrue(navigation.snapEngine !is SnapToRoute)
//    }

    //TODO fabi755
//    @Test
//    @Throws(Exception::class)
//    fun setOffRouteEngine_doesReplaceDefaultEngine() {
//        val navigation = buildMapLibreNavigation()
//
//        val offRoute = Mockito.mock(OffRoute::class.java)
//        navigation.offRouteEngine = offRoute
//
//        Assert.assertEquals(offRoute, navigation.offRouteEngine)
//    }

    private fun buildMapLibreNavigation(): MapLibreNavigation {
        val context = Mockito.mock(Context::class.java)
        Mockito.`when`(context.applicationContext).thenReturn(context)
        return MapLibreNavigation(context, locationEngine = Mockito.mock(LocationEngine::class.java))
    }

    private fun buildMapLibreNavigationWithOptions(options: MapLibreNavigationOptions): MapLibreNavigation {
        val context = Mockito.mock(Context::class.java)
        Mockito.`when`(context.applicationContext).thenReturn(context)
        return MapLibreNavigation(context, options, Mockito.mock(LocationEngine::class.java))
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