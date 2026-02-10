package com.miguelrivera.praesidiumnote.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Silver,
    onPrimary = CharcoalGrey,
    secondary = Silver,
    onSecondary = CharcoalGrey,
    background = CharcoalGrey,
    onBackground = OffWhite,
    surface = Obsidian,
    onSurface = OffWhite,
    error = AlertRed,
    onError = OffWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Obsidian,
    onPrimary = PureWhite,
    secondary = CharcoalGrey,
    onSecondary = PureWhite,
    background = OffWhite,
    onBackground = Obsidian,
    surface = PureWhite,
    onSurface = Obsidian,
    error = AlertRed,
    onError = PureWhite
)

@Composable
fun PraesidiumNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}