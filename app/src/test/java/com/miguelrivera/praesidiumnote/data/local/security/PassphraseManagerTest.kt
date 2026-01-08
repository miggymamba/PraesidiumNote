package com.miguelrivera.praesidiumnote.data.local.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.TestPraesidiumApp
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * * Note: Forced to SDK 33 to prevent java.lang.VerifyError on Java 21+
 * caused by Robolectric/Android 15 bytecode conflicts.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    application = TestPraesidiumApp::class,
    sdk = [33] // This is the critical fix for the "Bad return type" error
)
class PassphraseManagerTest {

    private lateinit var context: Context
    private val pbkdf2Engine: Pbkdf2Engine = mockk()
    private lateinit var passphraseManager: PassphraseManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        passphraseManager = PassphraseManager(context, pbkdf2Engine)

        // Mock PBKDF2 to return a stable key
        every {
            pbkdf2Engine.derive(any(), any())
        } returns ByteArray(32) { 0x01.toByte() }
    }

    @After
    fun clearMocks() {
        context.getSharedPreferences("praesidium_secure_storage", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("praesidium_fallback_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        unmockkAll()
    }

    @Test
    fun `warmup initializes cache and persists seed`() = runTest {
        // Act
        passphraseManager.warmup()

        // Assert: Verify sync access works (logic check)
        val key = passphraseManager.getPassphraseSync()
        assertThat(key).isNotNull()
        assertThat(key.size).isEqualTo(32)

        // Assert: Verify data was saved (framework check)
        // Note: Because Robolectric doesn't have a real TEE KeyStore,
        // the code will catch the exception and save to the fallback prefs.
        val securePrefs = context.getSharedPreferences("praesidium_secure_storage", Context.MODE_PRIVATE)
        val fallbackPrefs = context.getSharedPreferences("praesidium_fallback_prefs", Context.MODE_PRIVATE)

        val hasSeedGenerated = securePrefs.contains("wrapped_entropy_seed") ||
                fallbackPrefs.contains("software_seed")

        assertThat(hasSeedGenerated).isTrue()
    }

    @Test(expected = IllegalStateException::class)
    fun `getPassphraseSync throws when not warmed up`() {
        passphraseManager.getPassphraseSync()
    }

    @Test
    fun `subsequent calls return cached instance`() = runTest {
        passphraseManager.warmup()
        val firstKey = passphraseManager.getPassphrase()
        val secondKey = passphraseManager.getPassphrase()

        // Verify we aren't re-deriving (expensive) and using the RAM cache
        assertThat(firstKey).isSameInstanceAs(secondKey)
    }
}