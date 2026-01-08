package com.miguelrivera.praesidiumnote

import android.app.Application

/**
 * A lightweight Application class for Robolectric tests.
 * This bypasses the SQLCipher native library loading found in the main PraesidiumApp,
 * preventing UnsatisfiedLinkErrors during unit tests.
 */
class TestPraesidiumApp : Application()