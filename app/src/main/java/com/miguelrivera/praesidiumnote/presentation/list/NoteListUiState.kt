package com.miguelrivera.praesidiumnote.presentation.list

import androidx.compose.runtime.Immutable
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
     * Represents the successful retrieval of notes from the vault.
     *
     * Annotated with [Immutable] to guarantee to the Compose compiler that the
     * underlying [notes] list will not mutate after construction. This enables
     * Compose to safely skip recomposition of list items when this state is passed
     * down the hierarchy.
     *
     * @property notes The immutable list of [Note] instances to display.
     */
    @Immutable
    data class Success(val notes: List<Note>) : NoteListUiState

    /**
     * A failure occurred during retrieval (e.g., Decryption failed, IO Error).
     * @property message User-facing error message.
     */
    data class Error(val message: String) : NoteListUiState
}