package com.miguelrivera.praesidiumnote.presentation.auth

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.miguelrivera.praesidiumnote.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * High-assurance identity verification utility.
 *
 * Utilizes the Android Biometric library to bridge hardware-backed TEE authentication
 * with the presentation layer. Adheres to the "Strong" biometric tier while providing
 * a system-level fallback to device credentials (PIN/Pattern) to prevent lockout.
 *
 * @param context Application context used for [BiometricManager] and resource lookups.
 */
class BiometricAuthenticator @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val biometricManager = BiometricManager.from(context)

    /**
     * Initiates the system authentication dialog.
     *
     * @param activity Must be a [FragmentActivity] to host the [BiometricPrompt] lifecycle.
     * @param onResult Callback for handling the sealed [AuthResult] states.
     */
    fun authenticate(activity: FragmentActivity, onResult: (AuthResult) -> Unit) {
        /**
         * Fallback logic: DEVICE_CREDENTIAL (PIN/Pattern) is officially supported as
         * a first-class authenticator alongside BIOMETRIC_STRONG starting from API 30.
         */
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_STRONG // Fallback for older APIs where mixed-mode is unstable
        }

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Unit
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onResult(AuthResult.NotEnrolled)
                return
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                onResult(AuthResult.HardwareUnavailable)
                return
            }

            else -> {
                onResult(AuthResult.Error(context.getString(R.string.auth_error_unknown)))
                return
            }
        }

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(AuthResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                val result = when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> AuthResult.UserCanceled

                    BiometricPrompt.ERROR_LOCKOUT -> AuthResult.LockedOut(isPermanent = false)
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> AuthResult.LockedOut(isPermanent = true)

                    else -> AuthResult.Error(errString.toString())
                }
                onResult(result)
            }
        }

        val prompt = BiometricPrompt(activity, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.auth_prompt_title))
            .setSubtitle(context.getString(R.string.auth_prompt_subtitle))
            .setAllowedAuthenticators(authenticators)
            .apply {
                // Mandatory constraint: If DEVICE_CREDENTIAL is included on API 30+,
                // setNegativeButtonText MUST NOT be called or the build() will throw.
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                    setNegativeButtonText(context.getString(android.R.string.cancel))
                }
            }
            .build()

        prompt.authenticate(promptInfo)
    }
}