package com.miguelrivera.praesidiumnote.presentation.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miguelrivera.praesidiumnote.R
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.presentation.navigation.NavActions
import com.miguelrivera.praesidiumnote.presentation.ui.theme.PraesidiumNoteTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Vault Dashboard.
 *
 * Implements a high-assurance list view utilizing "Masked-by-Default" UX.
 * This implementation avoids bulky external icon libraries by utilizing local
 * vector drawables, significantly reducing binary size while maintaining visual fidelity.
 *
 * @param navActions Encapsulates navigation events to keep the UI layer stateless.
 * @param viewModel State holder, injected via Hilt for business logic orchestration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    navActions: NavActions,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is NoteListUiState.Error) {
            snackbarHostState.showSnackbar((uiState as NoteListUiState.Error).message)
        }
    }

    NoteListContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAddNote = navActions::navigateToAddNote,
        onNoteClick = { id -> navActions.navigateToNoteDetail(id)},
        onDeleteClick = viewModel::deleteNote
    )
}


/**
 * Stateless UI implementation.
 * Separated to allow for Previews and Screenshot testing without Hilt dependencies.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListContent(
    uiState: NoteListUiState,
    snackbarHostState: SnackbarHostState,
    onAddNote: () -> Unit,
    onNoteClick: (String) -> Unit,
    onDeleteClick: (Note) -> Unit
) {
    val scope = rememberCoroutineScope()
    var notePendingDeletion by remember { mutableStateOf<Note?>(null) }
    val notePurgedMessage = stringResource(R.string.note_purged_message)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNote,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.cd_add_note)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)){
            when (uiState){
                is NoteListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NoteListUiState.Empty -> {
                    EmptyVaultPlaceholder()
                }
                is NoteListUiState.Success -> {
                    NoteLazyList(
                        notes = uiState.notes,
                        onNoteClick = { note -> onNoteClick(note.id) },
                        onDeleteClick = { notePendingDeletion = it }
                    )
                }
                is NoteListUiState.Error -> {
                    ErrorVaultView(message = uiState.message)
                }
            }
        }
    }

    DeleteConfirmationDialog(
        note = notePendingDeletion,
        onDismiss = { notePendingDeletion = null },
        onConfirm = { note ->
            onDeleteClick(note)
            notePendingDeletion = null
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val snackbarJob = launch {
                    snackbarHostState.showSnackbar(
                        message = notePurgedMessage,
                        duration = SnackbarDuration.Indefinite
                    )
                }
                delay(2_000)
                snackbarJob.cancel()
            }
        }
    )
}

/**
 * High-assurance destructive action confirmation.
 * Extracted to isolate dialog logic and maintain clean composition trees.
 */
@Composable
private fun DeleteConfirmationDialog(
    note: Note?,
    onDismiss: () -> Unit,
    onConfirm: (Note) -> Unit
) {
    note?.let {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = { onConfirm(note) }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

/**
 * Renders the scrollable list of encrypted note summaries.
 *
 * @param notes The list of decrypted metadata headers to display.
 * @param onNoteClick Callback triggered when a user selects a note for full decryption.
 * @param onDeleteClick Callback triggered to initiate the record destruction flow.
 */
@Composable
private fun NoteLazyList(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(ListDimens.PaddingLarge),
        verticalArrangement = Arrangement.spacedBy(ListDimens.SpacingMedium),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = notes, key = { it.id }) { note ->
            NoteCard(
                note = note,
                onClick = { onNoteClick(note) },
                onDelete = { onDeleteClick(note) }
            )
        }
    }
}

/**
 * A specialized card component for masked note data.
 *
 * @param note The domain model containing encrypted buffers.
 * @param onClick Interaction handler for navigation.
 * @param onDelete Interaction handler for data removal.
 */
@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(ListDimens.PaddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String(note.title).ifEmpty { stringResource(R.string.untitled_note) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(ListDimens.IconSmall)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.cd_delete_note),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(modifier = Modifier.height(ListDimens.SpacingSmall))

            Text(
                text = stringResource(R.string.masked_content_pattern),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(ListDimens.SpacingMedium))

            Text(
                text = formatTimestamp(note.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Visual placeholder for zero-record states.
 */
@Composable
private fun EmptyVaultPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ListDimens.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_shield),
            contentDescription = null,
            modifier = Modifier.size(ListDimens.IconLarge),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(ListDimens.SpacingMedium))
        Text(
            text = stringResource(R.string.vault_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.vault_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * State-specific view for handling data hydration failures.
 *
 * @param message The localized error description.
 */
@Composable
private fun ErrorVaultView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ListDimens.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lock_closed),
            contentDescription = null,
            modifier = Modifier.size(ListDimens.IconLarge),
            tint = MaterialTheme.colorScheme.errorContainer
        )
        Spacer(modifier = Modifier.height(ListDimens.SpacingMedium))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(timestamp))
}

/**
 * Design tokens for the Note List feature.
 * Managed internally to maintain component-level encapsulation.
 */
private object ListDimens {
    val PaddingMedium = 16.dp
    val PaddingLarge = 20.dp
    val SpacingSmall = 4.dp
    val SpacingMedium = 12.dp
    val IconSmall = 24.dp
    val IconLarge = 72.dp
}

// --- PREVIEWS ---

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun NoteListContentPreview() {
    val mockNotes = listOf(
        Note(
            id = "ABCD-1234",
            title = "Bank Credentials".toCharArray(),
            content = charArrayOf(),
            timestamp = System.currentTimeMillis()
        ),
        Note(
            id = "EFGH-5678",
            title = "Github Backup Codes".toCharArray(),
            content = charArrayOf(),
            timestamp = System.currentTimeMillis() - 3600000
        )
    )
    PraesidiumNoteTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            NoteLazyList(
                notes = mockNotes,
                onNoteClick = {},
                onDeleteClick = {}
            )
        }
    }
}


@Preview(name = "Empty State", showBackground = true)
@Composable
private fun EmptyVaultPreview() {
    PraesidiumNoteTheme {
        EmptyVaultPlaceholder()
    }
}