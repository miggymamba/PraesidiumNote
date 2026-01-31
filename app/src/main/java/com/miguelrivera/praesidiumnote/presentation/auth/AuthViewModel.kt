package com.miguelrivera.praesidiumnote.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miguelrivera.praesidiumnote.data.local.security.PassphraseManager
import com.miguelrivera.praesidiumnote.di.Dispatcher
import com.miguelrivera.praesidiumnote.di.PraesidiumDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import javax.inject.Inject

/**
 * Manages the state of the Biometric Security Gate.
 *
 * This ViewModel acts as the bridge between the [BiometricAuthenticator] results
 * and the UI's navigation logic. It does *not* hold a reference to the Activity;
 * instead, it exposes the [AuthState] which the UI observes to trigger side effects.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val passphraseManager: PassphraseManager,
    @param:Dispatcher(PraesidiumDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    /**
     * Processes the result from the Biometric Prompt.
     * Called by the View layer after the [BiometricAuthenticator] completes.
     */
    fun onAuthResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                // Offload CPU-bound key derivation to the Default dispatcher.
                // yield() ensures the Loading state is dispatched.
                _uiState.value = AuthState.Loading

                viewModelScope.launch {
                    try {
                        withContext(defaultDispatcher) {
                            yield()
                            passphraseManager.warmup()
                        }
                        _uiState.value = AuthState.Authenticated
                    } catch (e: Exception) {
                        _uiState.value = AuthState.Error("Security initialization failed. - ${e.message}")
                    }
                }
            }
            is AuthResult.HardwareUnavailable -> _uiState.value = AuthState.Error("Biometric hardware unavailable.")
            is AuthResult.NotEnrolled -> _uiState.value = AuthState.Error("No biometrics enrolled. Please check settings.")
            is AuthResult.LockedOut -> _uiState.value = AuthState.Error("Too many attempts. Sensor locked.")
            is AuthResult.UserCanceled -> _uiState.value = AuthState.Idle  // Stay on lock screen
            is AuthResult.Error -> _uiState.value = AuthState.Error(result.message)

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

    /** Cryptographic material is being derived in the background. */
    data object Loading : AuthState

    /** User has successfully verified identity. Trigger navigation to Vault. */
    data object Authenticated : AuthState

    /** Authentication failed or hardware issue. Show feedback. */
    data class Error(val message: String) : AuthState
}