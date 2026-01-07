package com.miguelrivera.praesidiumnote.data.local.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the database passphrase lifecycle using the "Wrapped Key" pattern.
 *
 * 1. Master Key (TEE): An AES-GCM key generated in AndroidKeyStore. It never leaves hardware.
 * 2. Wrapped Seed: A random 32-byte entropy seed, encrypted by the Master Key and stored in Prefs.
 * 3. Stretching (PBKDF2): The unwrapped seed is stretched via 600,000 iterations to generate
 * the final 256-bit SQLCipher key.
 */
@Singleton
class PassphraseManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pbkdf2Engine: Pbkdf2Engine
) {
    private val mutex = Mutex()
    private var cachedPassphrase: ByteArray? = null

    /**
     * Retrieves or derives the primary 256-bit database passphrase.
     * Guaranteed to be in RAM after [warmup] is called during the AuthGate.
     */
    suspend fun getPassphrase(): ByteArray = withContext(Dispatchers.IO) {
        mutex.withLock {
            cachedPassphrase?.let { return@withLock it }

            val key = try {
                val seed = getOrGenerateWrappedSeed()
                val salt = "Praesidium_v1_Entropy_Salt".toByteArray()
                pbkdf2Engine.derive(seed.map { it.toInt().toChar() }.toCharArray(), salt)
            } catch (e: Exception) {
                Log.e(TAG, "Security Critical: Hardware-backed derivation failed. Using software-only seed.", e)
                deriveSoftwareFallback()
            }

            cachedPassphrase = key
            return@withLock key
        }
    }

    /**
     * Synchronous accessor for Room/SQLCipher. Throws if not warmed up to prevent
     * accidental use of insecure defaults.
     */
    fun getPassphraseSync(): ByteArray {
        return cachedPassphrase ?: throw IllegalStateException(
            "Security Error: Passphrase must be warmed up during AuthGate before DB initialization."
        )
    }

    /**
     * Pre-calculates the passphrase to avoid UI jank during database initialization.
     */
    suspend fun warmup() {
        getPassphrase()
    }

    /**
     * Implements the Wrapped Key pattern:
     * 1. Decrypts the existing seed from SharedPreferences using the TEE Master Key.
     * 2. If no seed exists, generates a new 32-byte SecureRandom seed and wraps it.
     */
    private fun getOrGenerateWrappedSeed(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedSeedBase64 = prefs.getString(KEY_WRAPPED_SEED, null)
        val ivBase64 = prefs.getString(KEY_IV, null)

        return if (encryptedSeedBase64 != null && ivBase64 != null) {
            decryptSeed(
                Base64.decode(encryptedSeedBase64, Base64.DEFAULT),
                Base64.decode(ivBase64, Base64.DEFAULT)
            )
        } else {
            generateAndWrapNewSeed(prefs)
        }
    }

    private fun generateAndWrapNewSeed(prefs: android.content.SharedPreferences): ByteArray {
        val rawSeed = ByteArray(32).apply {
            SecureRandom().nextBytes(this)
            // SQLCipher null-byte protection: ensure no bytes are 0x00
            for (i in indices) { if (this[i] == 0.toByte()) this[i] = 1.toByte() }
        }

        val masterKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)

        val encryptedSeed = cipher.doFinal(rawSeed)
        val iv = cipher.iv

        prefs.edit()
            .putString(KEY_WRAPPED_SEED, Base64.encodeToString(encryptedSeed, Base64.DEFAULT))
            .putString(KEY_IV, Base64.encodeToString(iv, Base64.DEFAULT))
            .apply()

        return rawSeed
    }

    private fun decryptSeed(encryptedSeed: ByteArray, iv: ByteArray): ByteArray {
        val masterKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
        return cipher.doFinal(encryptedSeed)
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
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
     * Absolute fallback mechanism for devices with broken/missing Keystore implementations.
     * Still uses a high-entropy seed to ensure the DB isn't easily crackable.
     */
    private fun deriveSoftwareFallback(): ByteArray {
        val prefs = context.getSharedPreferences("praesidium_fallback_prefs", Context.MODE_PRIVATE)
        val savedFallback = prefs.getString("software_seed", null)

        val seed = if (savedFallback != null) {
            Base64.decode(savedFallback, Base64.DEFAULT)
        } else {
            val newSeed = ByteArray(32).apply { SecureRandom().nextBytes(this) }
            prefs.edit().putString("software_seed", Base64.encodeToString(newSeed, Base64.DEFAULT)).apply()
            newSeed
        }

        // Even in fallback, stretch it to maintain security standards
        return pbkdf2Engine.derive(seed.map { it.toInt().toChar() }.toCharArray(), "fallback_salt".toByteArray())
    }

    companion object {
        private const val TAG = "PassphraseManager"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "praesidium_master_key_v1"
        private const val AES_MODE = "AES/GCM/NoPadding"

        private const val PREFS_NAME = "praesidium_secure_storage"
        private const val KEY_WRAPPED_SEED = "wrapped_entropy_seed"
        private const val KEY_IV = "initialization_vector"
    }
}