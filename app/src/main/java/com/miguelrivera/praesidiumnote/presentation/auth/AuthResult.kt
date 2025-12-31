package com.miguelrivera.praesidiumnote.presentation.auth

/**
 * Domain-aware authentication states for the Praesidium Vault.
 * * This hierarchy explicitly categorizes hardware status, user intent,
 * and system security constraints to allow the UI to provide automated
 * recovery paths (e.g., redirecting to System Settings on [NotEnrolled]).
 */
sealed class AuthResult {
    /** Authentication successfully verified by the TEE or Device Credential. */
    data object Success : AuthResult()

    /** User explicitly dismissed the prompt or clicked the negative button. */
    data object UserCanceled : AuthResult()

    /** Device hardware (Class 3) is missing or currently unavailable. */
    data object HardwareUnavailable : AuthResult()

    /** No biometrics or device credentials are set up on this device. */
    data object NotEnrolled : AuthResult()

    /** * Authentication failed due to too many attempts (Soft Lockout)
     * or sensor disabled (Permanent Lockout).
     */
    data class LockedOut(val isPermanent: Boolean) : AuthResult()

    /** Catch-all for unexpected system errors with a descriptive message. */
    data class Error(val message: String) : AuthResult()
}