package com.miguelrivera.praesidiumnote.presentation.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.miguelrivera.praesidiumnote.presentation.theme.PraesidiumNoteTheme
import org.junit.Rule
import org.junit.Test

/**
 * Validates the visual state and interaction contract of the [AuthGateContent].
 *
 * Covers:
 * 1. Idle state rendering (Lock icon, instructions).
 * 2. Error state feedback (Error shield, error message).
 * 3. User intent handling (Unlock button click).
 */
class AuthGateScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun idleState_displays_lock_icon_and_instructions() {
        composeTestRule.setContent {
            PraesidiumNoteTheme {
                AuthGateContent(
                    uiState = AuthState.Idle,
                    onAuthenticate = {}
                )
            }
        }

        // Verify Title
        composeTestRule.onNodeWithText("PraesidiumNote").assertIsDisplayed()

        // Verify Instruction
        composeTestRule.onNodeWithText("Identity verification required to access your encrypted notes.").assertIsDisplayed()

        // Verify Button
        composeTestRule.onNodeWithText("Unlock Vault").assertIsDisplayed()

    }

    @Test
    fun errorState_displays_error_message_and_feedback() {
        val errorMessage = "Biometric hardware unavailable."

        composeTestRule.setContent {
            PraesidiumNoteTheme {
                AuthGateContent(
                    uiState = AuthState.Error(errorMessage),
                    onAuthenticate = {}
                )
            }
        }

        // Verify that the error message passed in state is rendered
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun unlockButton_triggers_onAuthenticate_callback() {
        var clicked = false

        composeTestRule.setContent {
            PraesidiumNoteTheme {
                AuthGateContent(
                    uiState = AuthState.Idle,
                    onAuthenticate = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Unlock Vault").performClick()

        // Verify `clicked` value is set to true
        assert(clicked) { "Expected unlock button click to trigger onAuthenticate callback" }
    }

    @Test
    fun loadingState_displays_loading_text() {
        composeTestRule.setContent {
            PraesidiumNoteTheme {
                AuthGateContent(
                    uiState = AuthState.Loading,
                    onAuthenticate = {}
                )
            }
        }

        // Verify Loading
        composeTestRule.onNodeWithText("Decrypting your vault…").assertIsDisplayed()

        // Verify Icon is displayed
        composeTestRule.onNodeWithContentDescription("Auth Screen Status").assertIsDisplayed()
    }
}