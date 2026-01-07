package com.miguelrivera.praesidiumnote.data.local.security

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Pbkdf2Engine @Inject constructor() {

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATION_COUNT = 600_000
        private const val KEY_LENGTH_BITS = 256
    }

    /**
     * Derives a 32-byte key.
     * * @param password Raw input source (e.g., UUID string).
     * @param salt Unique byte array to prevent pre-computed table attacks.
     * @return 256-bit stretched key.
     */
    fun derive(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            factory.generateSecret(spec).encoded
        } finally {
            // Explicitly scrub sensitive chars from heap
            spec.clearPassword()
        }
    }
}