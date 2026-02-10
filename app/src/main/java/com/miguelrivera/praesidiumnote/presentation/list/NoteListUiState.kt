package com.miguelrivera.praesidiumnote.presentation.list

import com.miguelrivera.praesidiumnote.domain.model.Note

/**
 * Represents the various UI states for the Note List Dashboard.
 *
 * Modeled as a [sealed interface] because the states are mutually exclusive
 * (you cannot be Loading and showing a List at the same time).
 */
sealed interface NoteListUiState {

    /** Initial state or when data is being refreshed. */
    data object Loading : NoteListUiState

    /** The repository returned a valid but empty list (zero records). */
    data object Empty : NoteListUiState

    /**
     * Successfully retrieved notes from the vault.
     * @property notes The encrypted notes decrypted for display.
     */
    data class Success(val notes: List<Note>) : NoteListUiState

    /**
     * A failure occurred during retrieval (e.g., Decryption failed, IO Error).
     * @property message User-facing error message.
     */
    data class Error(val message: String) : NoteListUiState
}