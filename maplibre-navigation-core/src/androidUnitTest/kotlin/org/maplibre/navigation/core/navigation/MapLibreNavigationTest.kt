package org.maplibre.navigation.core.navigation

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.maplibre.navigation.core.BaseTest
import org.maplibre.navigation.core.location.engine.LocationEngine
import org.maplibre.navigation.core.milestone.BannerInstructionMilestone
import org.maplibre.navigation.core.milestone.Milestone
import org.maplibre.navigation.core.milestone.StepMilestone
import org.maplibre.navigation.core.milestone.VoiceInstructionMilestone
import org.maplibre.navigation.core.offroute.OffRoute
import org.maplibre.navigation.core.snap.Snap
import org.maplibre.navigation.core.snap.SnapToRoute
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class MapLibreNavigationTest : BaseTest() {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun sanityTest() {
        val navigation = buildMapLibreNavigation()

        assertNotNull(navigation)
    }

    @Test
    fun sanityTestWithOptions() {
        val options = MapLibreNavigationOptions()
        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)

        assertNotNull(navigationWithOptions)
    }

    @Test
    @Throws(Exception::class)
    fun voiceInstructionMilestone_onInitializationDoesGetAdded() {
        val navigation = buildMapLibreNavigation()

        val identifier = searchForVoiceInstructionMilestone(navigation)

        assertEquals(NavigationConstants.VOICE_INSTRUCTION_MILESTONE_ID, identifier)
    }

    @Test
    @Throws(Exception::class)
    fun bannerInstructionMilestone_onInitializationDoesGetAdded() {
        val navigation = buildMapLibreNavigation()

        val identifier = searchForBannerInstructionMilestone(navigation)

        assertEquals(NavigationConstants.BANNER_INSTRUCTION_MILESTONE_ID, identifier)
    }

    @Test
    @Throws(Exception::class)
    fun defaultMilestones_onInitializationDoNotGetAdded() {
        val options = MapLibreNavigationOptions(
            defaultMilestonesEnabled = false
        )
        val navigationWithOptions = buildMapLibreNavigationWithOptions(options)

        assertEquals(0, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun defaultEngines_offRouteEngineDidGetInitialized() {
        val navigation = buildMapLibreNavigation()

        assertNotNull(navigation.offRouteEngine)
    }

    @Test
    @Throws(Exception::class)
    fun defaultEngines_snapEngineDidGetInitialized() {
        val navigation = buildMapLibreNavigation()

        assertNotNull(navigation.snapEngine)
    }

    @Test
    @Throws(Exception::class)
    fun addMilestone_milestoneDidGetAdded() {
        val navigation = buildMapLibreNavigation()
        val milestone: Milestone = StepMilestone(identifier = 42)

        navigation.addMilestone(milestone)

        assertTrue(navigation.milestones.contains(milestone))
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

        assertEquals(1, navigationWithOptions.milestones.size)
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

        assertEquals(0, navigationWithOptions.milestones.size)
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

        assertEquals(1, navigationWithOptions.milestones.size)
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

        assertEquals(0, navigationWithOptions.milestones.size)
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

        assertEquals(0, navigationWithOptions.milestones.size)
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

        assertEquals(1, navigationWithOptions.milestones.size)
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

        assertEquals(1, navigationWithOptions.milestones.size)
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

        assertEquals(2, navigationWithOptions.milestones.size)
    }

    @Test
    fun locationEngine_returnsCorrectLocationEngine() {
        val navigation = buildMapLibreNavigation()
        val locationEngine = mockk<LocationEngine>()
        val locationEngineInstanceNotUsed = mockk<LocationEngine>()

        navigation.locationEngine = locationEngine

        assertNotSame(locationEngineInstanceNotUsed, navigation.locationEngine)
        assertEquals(locationEngine, navigation.locationEngine)
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

        assertTrue(navigation.snapEngine !is SnapToRoute)
    }

    @Test
    @Throws(Exception::class)
    fun setOffRouteEngine_doesReplaceDefaultEngine() {
        val navigation = buildMapLibreNavigation()

        val offRoute = mockk<OffRoute>()
        navigation.offRouteEngine = offRoute

        assertEquals(offRoute, navigation.offRouteEngine)
    }

    private fun buildMapLibreNavigation(): MapLibreNavigation {
        return MapLibreNavigation(locationEngine = mockk(relaxed = true))
    }

    private fun buildMapLibreNavigationWithOptions(options: MapLibreNavigationOptions): MapLibreNavigation {
        return MapLibreNavigation(options = options, locationEngine = mockk())
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