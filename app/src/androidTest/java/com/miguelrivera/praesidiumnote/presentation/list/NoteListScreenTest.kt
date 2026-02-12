package com.miguelrivera.praesidiumnote.presentation.list

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.presentation.theme.PraesidiumNoteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Validates the Vault Dashboard UI logic.
 *
 * Covers:
 * 1. Empty State placeholder rendering.
 * 2. Populated list rendering with masked content.
 * 3. Delete confirmation dialog interaction.
 */
class NoteListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_displays_shield_placeholder() {
        composeTestRule.setContent {
            PraesidiumNoteTheme {
                NoteListContent(
                    uiState = NoteListUiState.Empty,
                    snackbarHostState = SnackbarHostState(),
                    onAddNote = {},
                    onNoteClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Verify empty or initial texts are visible
        composeTestRule.onNodeWithText("Your vault is empty").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap the button below to secure your first note.").assertIsDisplayed()
    }

    @Test
    fun successState_displays_note_with_masked_content() {
        val notes = listOf(
            Note(
                id = "1",
                title = "Wifi Passwords".toCharArray(),
                content = "Secret!".toCharArray()
            )
        )

        composeTestRule.setContent {
            PraesidiumNoteTheme {
                NoteListContent(
                    uiState = NoteListUiState.Success(notes),
                    snackbarHostState = SnackbarHostState(),
                    onAddNote = {},
                    onNoteClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Verify Title is visible
        composeTestRule.onNodeWithText("Wifi Passwords").assertIsDisplayed()

        // Verify Content is masked
        composeTestRule.onNodeWithText("•••• •••• •••• ••••").assertIsDisplayed()
    }

    @Test
    fun clicking_delete_icon_shows_confirmation_dialog() {
        val notes = listOf(
            Note(
                id = "1",
                title = "Trash Note".toCharArray(),
                content = "Not needed anymore".toCharArray()
            )
        )
        
        composeTestRule.setContent { 
            PraesidiumNoteTheme { 
                NoteListContent(
                    uiState = NoteListUiState.Success(notes),
                    snackbarHostState = SnackbarHostState(),
                    onAddNote = {},
                    onNoteClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Click trash icon
        composeTestRule.onNodeWithContentDescription("Delete note").performClick()

        // Verify Dialog appears
        composeTestRule.onNodeWithText("Purge Note?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Purge").assertIsDisplayed()
    }

    @Test
    fun confirming_delete_triggers_callback() {
        val noteToDelete = Note(id = "1", title = "A".toCharArray(), content = "B".toCharArray())
        var deletedNote: Note? = null
        
        composeTestRule.setContent { 
            PraesidiumNoteTheme {
                NoteListContent(
                    uiState = NoteListUiState.Success(listOf(noteToDelete)),
                    snackbarHostState = SnackbarHostState(),
                    onAddNote = {},
                    onNoteClick = {},
                    onDeleteClick = { deletedNote = it }
                )
            }
        }

        // Open Dialog
        composeTestRule.onNodeWithContentDescription("Delete note").performClick()

        // Confirm Purge
        composeTestRule.onNodeWithText("Purge").performClick()

        // Verify callback
        composeTestRule.runOnIdle {
            assertEquals("Expected delete callback to be invoked with the specific note.", noteToDelete, deletedNote)
        }
    }
}