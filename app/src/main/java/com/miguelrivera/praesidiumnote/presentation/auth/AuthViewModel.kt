package com.miguelrivera.praesidiumnote.presentation.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Manages the state of the Biometric Security Gate.
 *
 * This ViewModel acts as the bridge between the [BiometricAuthenticator] results
 * and the UI's navigation logic. It does *not* hold a reference to the Activity;
 * instead, it exposes the [AuthState] which the UI observes to trigger side effects.
 */
@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    /**
     * Processes the result from the Biometric Prompt.
     * Called by the View layer after the [BiometricAuthenticator] completes.
     */
    fun onAuthResult(result: AuthResult) {
        _uiState.update {
            when (result) {
                is AuthResult.Success -> AuthState.Authenticated
                is AuthResult.HardwareUnavailable -> AuthState.Error("Biometric hardware unavailable.")
                is AuthResult.NotEnrolled -> AuthState.Error("No biometrics enrolled. Please check settings.")
                is AuthResult.LockedOut -> AuthState.Error("Too many attempts. Sensor locked.")
                is AuthResult.UserCanceled -> AuthState.Idle // Stay on lock screen
                is AuthResult.Error -> AuthState.Error(result.message)
            }
        }
    }

    /**
     * Resets the state to Idle. Useful when the user logs out or the app locks.
     */
    fun resetState() {
        _uiState.value = AuthState.Idle
    }
}

/**
 * Represents the distinct states of the Authentication Screen.
 */
sealed interface AuthState {
    /** Waiting for user interaction. */
    data object Idle : AuthState

    /** User has successfully verified identity. Trigger navigation to Vault. */
    data object Authenticated : AuthState

    /** Authentication failed or hardware issue. Show feedback. */
    data class Error(val message: String) : AuthState
}