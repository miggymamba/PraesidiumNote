package com.miguelrivera.praesidiumnote.presentation.auth

/**
 * Represents the distinct states of the Authentication Screen.
 *
 * Modeled as a [sealed interface] to ensure exhaustive 'when' handling in the UI
 * without the memory overhead of a sealed class constructor.
 */
sealed interface AuthState {

    /** Waiting for user interaction (Biometric prompt not yet active). */
    data object Idle : AuthState

    /** Cryptographic material is being derived in the background. */
    data object Loading : AuthState

    /** User has successfully verified identity. Trigger navigation to Vault. */
    data object Authenticated : AuthState

    /** Authentication failed or hardware issue. Show feedback to user. */
    data class Error(val message: String) : AuthState
}