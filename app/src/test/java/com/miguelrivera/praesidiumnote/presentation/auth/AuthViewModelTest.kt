package com.miguelrivera.praesidiumnote.presentation.auth

import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.data.local.security.PassphraseManager
import com.miguelrivera.praesidiumnote.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Validates the AuthViewModel state machine.
 *
 * Utilizes MainDispatcherRule to override the Main dispatcher, preventing
 * crashes during viewModelScope execution. Verifies that the UI transitions
 * through the 'Loading' state during expensive cryptographic warmup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val passphraseManager: PassphraseManager = mockk()

    // Use a separate dispatcher for the background work to verify concurrency
    private val testDefaultDispatcher = StandardTestDispatcher()

    private val viewModel = AuthViewModel(
        passphraseManager = passphraseManager,
        defaultDispatcher = testDefaultDispatcher
    )

    @Test
    fun `onAuthResult Success transitions state from Loading to Authenticated`() = runTest {
        // Given
        coJustRun { passphraseManager.warmup() }

        // When
        viewModel.onAuthResult(AuthResult.Success)

        // Then: Verify immediate transition to Loading
        assertThat(viewModel.uiState.value).isEqualTo(AuthState.Loading)

        // Trigger the background work on the default dispatcher
        testDefaultDispatcher.scheduler.advanceUntilIdle()

        // Then: Verify final state and logic execution
        assertThat(viewModel.uiState.value).isEqualTo(AuthState.Authenticated)
        coVerify(exactly = 1) { passphraseManager.warmup() }
    }

    @Test
    fun `onAuthResult UserCanceled returns state to Idle`() = runTest {
        // When: User cancels the biometric prompt
        viewModel.onAuthResult(AuthResult.UserCanceled)

        // Then: State should remain Idle (or return to it) so they can try again
        assertThat(viewModel.uiState.value).isEqualTo(AuthState.Idle)
    }

    @Test
    fun `onAuthResult LockedOut transitions to Error with specific message`() = runTest {
        // When: Multiple failed attempts trigger a lockout
        viewModel.onAuthResult(AuthResult.LockedOut(isPermanent = false))

        // Then
        assertThat(viewModel.uiState.value).isInstanceOf(AuthState.Error::class.java)
        val state = viewModel.uiState.value as AuthState.Error
        // Verify we handle the lockout state logic (checking if message contains 'lockout')
        assertThat(state.message.lowercase()).contains("too many attempts")
    }

    @Test
    fun `onAuthResult Error transitions to Error state`() = runTest {
        // When
        viewModel.onAuthResult(AuthResult.Error("Fingerprint rejected"))

        // Then
        assertThat(viewModel.uiState.value).isInstanceOf(AuthState.Error::class.java)
        assertThat((viewModel.uiState.value as AuthState.Error).message).isEqualTo("Fingerprint rejected")
    }

    @Test
    fun `onAuthResult failure during warmup transitions to Error state`() = runTest {
        // Given
        coEvery { passphraseManager.warmup() } throws Exception("Keystore failure")

        // When
        viewModel.onAuthResult(AuthResult.Success)
        testDefaultDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value).isInstanceOf(AuthState.Error::class.java)
    }

    @Test
    fun `resetState returns UI to Idle`() = runTest {
        viewModel.onAuthResult(AuthResult.Success)
        viewModel.resetState()
        assertThat(viewModel.uiState.value).isEqualTo(AuthState.Idle)
    }
}

private fun coJustRun(block: suspend () -> Unit) {
    coEvery { block() } returns Unit
}