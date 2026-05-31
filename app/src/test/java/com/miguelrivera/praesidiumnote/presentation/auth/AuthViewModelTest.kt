package com.miguelrivera.praesidiumnote.presentation.auth

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.data.local.security.PassphraseManager
import com.miguelrivera.praesidiumnote.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Validates the AuthViewModel state machine.
 *
 * Utilizes MainDispatcherRule and Turbine to verify that the UI transitions through the 'Loading' state during expensive
 * cryptographic warmup before reaching the 'Authenticated' state.
 */
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

        viewModel.uiState.test {
            // Initial state
            assertThat(awaitItem()).isEqualTo(AuthState.Idle)

            // When
            viewModel.onAuthResult(AuthResult.Success)

            // Then: Verify transition to Loading
            assertThat(awaitItem()).isEqualTo(AuthState.Loading)

            // Trigger the background work on the default dispatcher
            testDefaultDispatcher.scheduler.advanceUntilIdle()

            // Then: Verify final Authenticated state
            assertThat(awaitItem()).isEqualTo(AuthState.Authenticated)
            coVerify(exactly = 1) { passphraseManager.warmup() }
        }
    }

    @Test
    fun `onAuthResult UserCanceled returns state to Idle`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(AuthState.Idle)

            // When: User cancels the biometric prompt
            viewModel.onAuthResult(AuthResult.UserCanceled)

            // Then: State should remain Idle (or return to it) so they can try again
            // Note: Since it was already Idle, StateFlow might not emit a new value.
            // But if it was in another state, it would. 
            // Given the logic: AuthResult.UserCanceled -> _uiState.value = AuthState.Idle
            expectNoEvents()
        }
    }

    @Test
    fun `onAuthResult LockedOut transitions to Error with specific message`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(AuthState.Idle)

            // When: Multiple failed attempts trigger a lockout
            viewModel.onAuthResult(AuthResult.LockedOut(isPermanent = false))

            // Then
            val state = awaitItem()
            assertThat(state).isInstanceOf(AuthState.Error::class.java)
            assertThat((state as AuthState.Error).message.lowercase()).contains("too many attempts")
        }
    }

    @Test
    fun `onAuthResult Error transitions to Error state`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(AuthState.Idle)

            // When
            viewModel.onAuthResult(AuthResult.Error("Fingerprint rejected"))

            // Then
            val state = awaitItem()
            assertThat(state).isInstanceOf(AuthState.Error::class.java)
            assertThat((state as AuthState.Error).message).isEqualTo("Fingerprint rejected")
        }
    }

    @Test
    fun `onAuthResult failure during warmup transitions to Error state`() = runTest {
        // Given
        coEvery { passphraseManager.warmup() } throws Exception("Keystore failure")

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(AuthState.Idle)

            // When
            viewModel.onAuthResult(AuthResult.Success)
            assertThat(awaitItem()).isEqualTo(AuthState.Loading)
            
            testDefaultDispatcher.scheduler.advanceUntilIdle()

            // Then
            assertThat(awaitItem()).isInstanceOf(AuthState.Error::class.java)
        }
    }

    @Test
    fun `resetState returns UI to Idle`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(AuthState.Idle)
            
            viewModel.onAuthResult(AuthResult.Success)
            assertThat(awaitItem()).isEqualTo(AuthState.Loading)

            // When
            viewModel.resetState()

            // Then
            assertThat(awaitItem()).isEqualTo(AuthState.Idle)
        }
    }
}

private fun coJustRun(block: suspend () -> Unit) {
    coEvery { block() } returns Unit
}
