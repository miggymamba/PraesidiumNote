package com.miguelrivera.praesidiumnote.presentation.editor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miguelrivera.praesidiumnote.R
import com.miguelrivera.praesidiumnote.presentation.navigation.NavActions
import com.miguelrivera.praesidiumnote.presentation.ui.theme.PraesidiumNoteTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen for creating and modifying secure notes.
 * Uses BasicTextField for granular control over styling to remove default underlines.
 */
@Composable
fun NoteEditorScreen(
    navActions: NavActions,
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val noteSavedMessage = stringResource(R.string.note_saved_message)
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // Success synchronization: ensures UX continuity between persistence and navigation
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            keyboardController?.hide()

            // Snackbar is launched in a separate job to survive the popBackStack transition
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(noteSavedMessage)
            }

            // Temporal buffer to allow IME dismissal and user perception of success state
            delay(1_000)
            navActions.navigateBack()
        }
    }

    // Handle Error Side-effect
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    NoteEditorContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onTitleChange = viewModel::onTitleChange,
        onContentChange = viewModel::onContentChange,
        onSave = viewModel::saveNote,
        onBack = navActions::navigateBack
    )
}

/**
 * Stateless UI implementation.
 * Separated to allow for Previews and Screenshot testing without Hilt dependencies.
 */
@Composable
fun NoteEditorContent(
    uiState: NoteEditorUiState,
    snackbarHostState: SnackbarHostState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSave) {
                        Icon(
                            painter = painterResource(R.drawable.ic_save),
                            contentDescription = stringResource(R.string.cd_save_note)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = EditorDimens.ScreenPadding)
                ) {
                    // Title Input
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (uiState.title.isEmpty()) {
                            Text(
                                text = stringResource(R.string.hint_title),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = EditorDimens.HintAlpha)
                                )
                            )
                        }
                        BasicTextField(
                            value = uiState.title,
                            onValueChange = onTitleChange,
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(EditorDimens.SpacingMedium))

                    // Content Input
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (uiState.content.isEmpty()) {
                            Text(
                                text = stringResource(R.string.hint_content),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = EditorDimens.HintAlpha)
                                )
                            )
                        }
                        BasicTextField(
                            value = uiState.content,
                            onValueChange = onContentChange,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = EditorDimens.LineHeight
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxSize(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            )
                        )
                    }
                }
            }
        }
    }
}
private object EditorDimens {
    val ScreenPadding = 20.dp
    val SpacingMedium = 16.dp
    val LineHeight = 24.sp
    val HintAlpha = 0.5F
}

// --- PREVIEWS ---

@Preview(name = "Light Mode - Empty", showBackground = true)
@Preview(name = "Dark Mode - Empty", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun NoteEditorEmptyPreview() {
    PraesidiumNoteTheme {
        NoteEditorContent(
            uiState = NoteEditorUiState(
                title = "",
                content = "",
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onBack = {}
        )
    }
}

@Preview(name = "Light Mode - Hydrated", showBackground = true)
@Composable
private fun NoteEditorHydratedPreview() {
    PraesidiumNoteTheme {
        NoteEditorContent(
            uiState = NoteEditorUiState(
                title = "Japanese Food Ranking",
                content = "1. Ramen \n2. Sushi \n3. Oyakodon \n4. Yakiniku",
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onBack = {}
        )
    }
}