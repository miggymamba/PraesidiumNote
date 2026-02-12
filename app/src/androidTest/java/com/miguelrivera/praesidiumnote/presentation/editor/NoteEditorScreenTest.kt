package com.miguelrivera.praesidiumnote.presentation.editor

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.miguelrivera.praesidiumnote.presentation.theme.PraesidiumNoteTheme
import org.junit.Rule
import org.junit.Test

/**
 * Validates the Note Editor UI.
 *
 * Covers:
 * 1. Data binding (Title/Content populated from state).
 * 2. Interaction (Save button clicks).
 * 3. Loading state.
 */
class NoteEditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun editor_populates_fields_from_state() {
        val title = "Project XYZ"
        val content = "Launch Codes: 1235"
        
        composeTestRule.setContent { 
            PraesidiumNoteTheme {
                NoteEditorContent(
                    uiState = NoteEditorUiState(
                        title = title,
                        content = content,
                        isLoading = false
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onTitleChange = {},
                    onContentChange = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }

        // BasicTextFields can be tricky to find by tag, but finding by text works if populated
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(content).assertIsDisplayed()
    }

    @Test
    fun save_button_triggers_callback() {
        var saveClicked = false

        composeTestRule.setContent {
            PraesidiumNoteTheme {
                NoteEditorContent(
                    uiState = NoteEditorUiState(),
                    snackbarHostState = SnackbarHostState(),
                    onTitleChange = {},
                    onContentChange = {},
                    onSave = { saveClicked = true },
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Save note").performClick()
        assert(saveClicked)
    }

    @Test
    fun loading_state_hides_content_fields() {
        composeTestRule.setContent {
            PraesidiumNoteTheme {
                NoteEditorContent(
                    uiState = NoteEditorUiState(isLoading = true),
                    snackbarHostState = SnackbarHostState(),
                    onTitleChange = {},
                    onContentChange = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Title").assertDoesNotExist()
    }
}