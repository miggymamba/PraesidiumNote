package com.miguelrivera.praesidiumnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.miguelrivera.praesidiumnote.presentation.navigation.NavActions
import com.miguelrivera.praesidiumnote.presentation.navigation.PraesidiumNavHost
import com.miguelrivera.praesidiumnote.presentation.ui.theme.PraesidiumNoteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PraesidiumNoteTheme {
                val navController = rememberNavController()
                val navActions = remember(navController) { NavActions(navController) }

                CompositionLocalProvider(LocalNavActions provides navActions) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        PraesidiumNavHost(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PraesidiumNoteTheme {
        Greeting("Android")
    }
}