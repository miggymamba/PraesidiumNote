package com.miguelrivera.praesidiumnote.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.rememberNavController
import com.miguelrivera.praesidiumnote.presentation.navigation.NavActions
import com.miguelrivera.praesidiumnote.presentation.navigation.PraesidiumNavHost
import com.miguelrivera.praesidiumnote.presentation.theme.PraesidiumNoteTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point. Extends [FragmentActivity] to support [androidx.biometric.BiometricPrompt].
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private var isVaultLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PraesidiumNoteTheme {
                val navController = rememberNavController()
                val navActions = remember(navController) { NavActions(navController) }

                // Auto-Lock Logic: Enforces re-auth when app returns from background.
                remember(navController) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_PAUSE -> isVaultLocked = true
                            Lifecycle.Event.ON_RESUME -> {
                                if (isVaultLocked) {
                                    navActions.navigateToLockScreen()
                                    isVaultLocked = false
                                }
                            }
                            else -> Unit
                        }
                    }
                    lifecycle.addObserver(observer)
                    observer
                }

                CompositionLocalProvider(LocalNavActions provides navActions) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                        PraesidiumNavHost(
                            navController = navController,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

val LocalNavActions = staticCompositionLocalOf<NavActions> {
    error("No NavActions provided! Check MainActivity implementation.")
}