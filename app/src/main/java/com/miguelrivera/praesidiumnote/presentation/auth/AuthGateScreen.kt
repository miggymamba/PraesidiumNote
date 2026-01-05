package com.miguelrivera.praesidiumnote.presentation.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miguelrivera.praesidiumnote.R
import com.miguelrivera.praesidiumnote.presentation.navigation.NavActions
import com.miguelrivera.praesidiumnote.presentation.ui.theme.PraesidiumNoteTheme

/**
 * Entry-point security gate.
 * Orchestrates biometric verification and blocks UI composition of sensitive routes until authorized.
 */
@Composable
fun AuthGateScreen(
    navActions: NavActions,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val authenticator = remember { BiometricAuthenticator(context) }
    val onAuthenticate = {
        val activity = context.findActivity()
        if (activity != null) {
            authenticator.authenticate(activity, viewModel::onAuthResult)
        } else {
            viewModel.onAuthResult(AuthResult.Error("Host Activity not found"))
        }
    }

    // Auto-trigger biometric prompt on mount
    LaunchedEffect(Unit) {
        onAuthenticate()
    }

    // Navigation side-effect upon successful authentication
    LaunchedEffect(uiState) {
        if (uiState is AuthState.Authenticated) {
            navActions.navigateToNoteList()
            viewModel.resetState()
        }
    }

    AuthGateContent(
        uiState = uiState,
        onAuthenticate = onAuthenticate
    )
}

/**
 * Stateless UI implementation of the Auth Gate.
 * Separated from the stateful wrapper to enable high-fidelity Compose Previews.
 */
@Composable
private fun AuthGateContent(
    uiState: AuthState,
    onAuthenticate: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(AuthDimens.ScreenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val iconPainter = if (uiState is AuthState.Error) {
                    painterResource(id = R.drawable.ic_shield_error)
                } else {
                    painterResource(id = R.drawable.ic_lock_closed)
                }

                AuthHeader(
                    painter = iconPainter,
                    isError = uiState is AuthState.Error
                )

                Spacer(modifier = Modifier.height(AuthDimens.SpacingLarge))

                if (uiState is AuthState.Error) {
                    AuthErrorFeedback(message = uiState.message)
                    Spacer(modifier = Modifier.height(AuthDimens.SpacingMedium))
                }

                Button(
                    onClick = onAuthenticate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AuthDimens.ButtonHeight)
                ) {
                    Text(text = stringResource(R.string.auth_unlock_button))
                }
            }
        }
    }
}

/**
 * Visual identity and instruction header for the security gate.
 */
@Composable
private fun AuthHeader(
    painter: Painter,
    isError: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(AuthDimens.IconSize),
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(AuthDimens.SpacingMedium))
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(AuthDimens.SpacingSmall))
        Text(
            text = stringResource(R.string.auth_required_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Standardized feedback component for biometric or system failures.
 */
@Composable
private fun AuthErrorFeedback(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Recursively traverses the ContextWrapper chain to locate the hosting FragmentActivity.
 * Required for BiometricPrompt interoperability in Hilt/Theme-wrapped environments.
 */
private tailrec fun Context.findActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Design tokens encapsulated for the Auth feature set.
 */
private object AuthDimens {
    val ScreenPadding = 32.dp
    val IconSize = 80.dp
    val ButtonHeight = 60.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 40.dp
}

@Preview(showBackground = true, name = "Initial State")
@Composable
private fun AuthGateScreenPreview() {
    PraesidiumNoteTheme {
        Surface {
            AuthGateContent(
                uiState = AuthState.Idle,
                onAuthenticate = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun AuthGateScreenErrorPreview() {
    PraesidiumNoteTheme {
        Surface {
            AuthGateContent(
                uiState = AuthState.Error("Fingerprint not recognized. Please try again."),
                onAuthenticate = {}
            )
        }
    }
}