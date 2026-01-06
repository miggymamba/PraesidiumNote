package com.miguelrivera.praesidiumnote.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.miguelrivera.praesidiumnote.LocalNavActions
import com.miguelrivera.praesidiumnote.presentation.auth.AuthGateScreen
import com.miguelrivera.praesidiumnote.presentation.editor.NoteEditorScreen
import com.miguelrivera.praesidiumnote.presentation.list.NoteListScreen

/**
 * Central navigation orchestrator for the application.
 *
 * Implements a strictly-typed navigation graph that manages transitions between
 * security gates and functional vault features. The architecture explicitly
 * separates 'Add' and 'Detail' routes to optimize resource allocation and
 * data hydration patterns.
 *
 * @param navController The navigation controller used to manage the backstack.
 * @param modifier Applied to the NavHost container for layout consistency.
 */
@Composable
fun PraesidiumNavHost(
    navController: NavHostController,
    modifier: Modifier
) {

    val navActions = LocalNavActions.current

    NavHost(
        navController = navController,
        startDestination = Screen.Auth,
        modifier = modifier
    ) {
        composable<Screen.Auth> {
            AuthGateScreen(navActions = navActions)
        }

        composable<Screen.NoteList> {
            NoteListScreen(navActions = navActions)
        }

        composable<Screen.AddNote> {
            NoteEditorScreen(navActions = navActions)
        }

        composable<Screen.NoteDetail> {
            NoteEditorScreen(navActions = navActions)
        }
    }
}