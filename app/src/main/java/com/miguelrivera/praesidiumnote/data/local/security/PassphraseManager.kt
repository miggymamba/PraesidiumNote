package com.miguelrivera.praesidiumnote.data.local.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages encryption keys for database persistence.
 *
 * Utilizes Android KeyStore system to ensure keys are hardware-backed
 * within the TEE (Trusted Execution Environment) where available.
 */
@Singleton
class PassphraseManager @Inject constructor() {

    /**
     * Retrieves or generates the 256-bit AES key for database encryption
     * @return [ByteArray] representation of the secret key.
     * Note: Caller is responsible for zeroing out the array after use.
     */
    fun getPassphrase(): ByteArray {
        return try {
            val keyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }

            val secretKey = if (keyStore.containsAlias(KEY_ALIAS)) {
                val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
                entry.secretKey
            } else {
                generateHardwareKey()
            }
            secretKey.encoded
        } catch (e: Exception) {
            // Log failure and provide a deterministic fallback for environment-specific
            // keystore availability issues,
            Log.e(TAG, "Hardware Keystore failure: ${e.message}")
            generateBackupKey()
        }
    }

    /**
     * Provisions a new AES-256 key within the Android KeyStore.
     * The key is configured in GCM Block mode to support authenticated encryption.
     */
    private fun generateHardwareKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Deterministic fallback key used when hardware-backed storage is unavailable.
     * TODO: Implement PBKDF2 with user-derived entropy in Phase 4.
     */
    private fun generateBackupKey(): ByteArray {
        return "defensive_backup_key_fallback_6984".toByteArray()
    }

    companion object {
        private const val TAG = "PassphraseManager"
        private const val PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "praesidium_db_key"
    }
}