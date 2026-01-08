package com.miguelrivera.praesidiumnote.presentation.auth

import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.TestPraesidiumApp
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestPraesidiumApp::class, sdk = [33])
class BiometricAuthenticatorTest {

    private lateinit var authenticator: BiometricAuthenticator
    lateinit var activity: FragmentActivity
    private val biometricManager: BiometricManager = mockk()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(TestFragmentActivity::class.java)
            .setup()
            .get()
        mockkStatic(BiometricManager::class)
        every { BiometricManager.from(any()) } returns biometricManager

        authenticator = BiometricAuthenticator(activity)
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `returns NotEnrolled when hardware exists but no biometrics set`() {
        every { biometricManager.canAuthenticate(any()) } returns
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED

        var result: AuthResult? = null
        authenticator.authenticate(activity) { result = it }

        assertThat(result).isEqualTo(AuthResult.NotEnrolled)
    }

    @Test
    fun `returns HardwareUnavailable when sensor is missing`() {
        every { biometricManager.canAuthenticate(any()) } returns
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE

        var result: AuthResult? = null
        authenticator.authenticate(activity) { result = it }

        assertThat(result).isEqualTo(AuthResult.HardwareUnavailable)
    }

    @Test
    fun `returns HardwareUnavailable when hardware is temporarily unavailable`() {
        every { biometricManager.canAuthenticate(any()) } returns
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE

        var result: AuthResult? = null
        authenticator.authenticate(activity) { result = it }

        assertThat(result).isEqualTo(AuthResult.HardwareUnavailable)
    }

    @Test
    fun `returns Error for unknown biometric manager error`() {
        every { biometricManager.canAuthenticate(any()) } returns -999

        var result: AuthResult? = null
        authenticator.authenticate(activity) { result = it }

        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
    }
}
