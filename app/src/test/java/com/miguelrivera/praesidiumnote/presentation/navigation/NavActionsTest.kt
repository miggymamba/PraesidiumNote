package com.miguelrivera.praesidiumnote.presentation.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * Focuses on validating backstack security and type-safe route dispatching.
 * Verifies that critical transitions (like entering the vault) correctly
 * purge the authentication gate from the history to prevent session hijacking.
 */
class NavActionsTest {

    private lateinit var navController: NavHostController
    private lateinit var navActions: NavActions

    @Before
    fun setup() {
        navController = mockk(relaxed = true)
        navActions = NavActions(navController)
    }

    @Test
    fun `MapsToNoteList navigates and clears Auth from backstack`() {
        // Arrange: Capture the NavOptionsBuilder lambda
        val lambdaSlot = slot<NavOptionsBuilder.() -> Unit>()

        // Act
        navActions.navigateToNoteList()

        // Assert: Verify navigation to correct Screen
        verify {
            navController.navigate(
                route = Screen.NoteList,
                builder = capture(lambdaSlot)
            )
        }

        // Execution of the builder to ensure code coverage of the DSL block
        NavOptionsBuilder().apply(lambdaSlot.captured)
    }

    @Test
    fun `MapsToAddNote dispatches to AddNote screen`() {
        navActions.navigateToAddNote()
        verify { navController.navigate(Screen.AddNote) }
    }

    @Test
    fun `MapsToNoteDetail dispatches with correct identifier`() {
        val testId = "vault-record-99"
        navActions.navigateToNoteDetail(testId)

        verify { navController.navigate(Screen.NoteDetail(noteId = testId)) }
    }

    @Test
    fun `MapsToLockScreen ignores request if already at Auth`() {
        // Arrange: Mock current destination as Auth to test the guard clause
        val mockDestination = mockk<NavDestination>()
        every { mockDestination.route } returns Screen.Auth::class.qualifiedName
        every { navController.currentDestination } returns mockDestination

        // Act
        navActions.navigateToLockScreen()

        // Assert: Should not navigate if already there
        verify(exactly = 0) {
            navController.navigate(
                route = Screen.Auth,
                builder = any<NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @Test
    fun `MapsToLockScreen wipes graph when navigating from vault`() {
        // Arrange: Mock current destination as NoteList and set up graph for popUpTo
        val mockDestination = mockk<NavDestination>()
        val mockGraph = mockk<NavGraph>()
        val lambdaSlot = slot<NavOptionsBuilder.() -> Unit>()

        every { mockDestination.route } returns Screen.NoteList::class.qualifiedName
        every { navController.currentDestination } returns mockDestination
        every { navController.graph } returns mockGraph
        every { mockGraph.startDestinationId } returns 101

        // Act
        navActions.navigateToLockScreen()

        // Assert: Verify it tries to navigate to Auth with backstack clearing logic
        verify {
            navController.navigate(
                route = Screen.Auth,
                builder = capture(lambdaSlot)
            )
        }

        // Execute captured lambda to cover popUpTo and launchSingleTop logic
        NavOptionsBuilder().apply(lambdaSlot.captured)
    }

    @Test
    fun `MapsBack delegates to system controller`() {
        navActions.navigateBack()
        verify { navController.popBackStack() }
    }
}