package com.miguelrivera.praesidiumnote.presentation.navigation

import androidx.navigation.NavHostController

class NavActions(private val navController: NavHostController) {

    /**
     * Navigates to the main vault view. Clears the authentication screen from the
     * backstack to prevent unauthorized re-entry via system back navigation.
     */
    fun navigateToNoteList() {
        navController.navigate(Screen.NoteList) {
            popUpTo(Screen.Auth) { inclusive = true }
        }
    }

    /**
     * Transitions to the note creation flow.
     */
    fun navigateToAddNote() {
        navController.navigate(Screen.AddNote)
    }

    /**
     * Transitions to a specific note's detail view using the provided identifier.
     * * @param id The identifier used to retrieve the encrypted record.
     */
    fun navigateToNoteDetail(id: String) {
        navController.navigate(Screen.NoteDetail(noteId = id))
    }

    /**
     * Securely redirects to the Auth gate, wiping the backstack to prevent
     * unauthorized access via the back button.
     */
    fun navigateToLockScreen() {
        if (navController.currentDestination?.route == Screen.Auth::class.qualifiedName) return

        navController.navigate(Screen.Auth) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    /**
     * Pops the current destination from the backstack.
     */
    fun navigateBack() {
        navController.popBackStack()
    }
}