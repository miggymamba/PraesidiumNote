package com.miguelrivera.praesidiumnote.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.usecase.DeleteNoteUseCase
import com.miguelrivera.praesidiumnote.domain.usecase.GetNotesUseCase
import com.miguelrivera.praesidiumnote.domain.usecase.NoteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the data flow for the Note List Dashboard.
 *
 * Implements MVI (Model-View-Intent) where:
 * - State: [NoteListUiState] derived from the [GetNotesUseCase] stream.
 * - Intent: Explicit actions like [deleteNote].
 */
@HiltViewModel
class NoteListViewModel @Inject constructor(
    getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    /**
     * The single source of truth for the UI.
     *
     * This uses [stateIn] with [SharingStarted.WhileSubscribed] to ensure the database
     * flow is active only when the UI is visible, preventing resource waste
     * when the app is backgrounded or the user is in the Editor.
     */
    val uiState: StateFlow<NoteListUiState> = getNotesUseCase()
        .map { result ->
            when (result) {
                is NoteResult.Success -> NoteListUiState.Success(result.data)
                is NoteResult.Error.EmptyNote -> NoteListUiState.Empty
                is NoteResult.Error -> NoteListUiState.Error("Failed to load vault.")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NoteListUiState.Loading
        )

    /**
     * Stream of transient one-time events.
     * * Backed by a [SharedFlow] to guarantee events are dropped if there are no
     * active subscribers, preventing UI side effect replay on configuration changes.
     */

    private val _events = MutableSharedFlow<NoteListEvent>()
    val events: SharedFlow<NoteListEvent> = _events.asSharedFlow()

    /**
     * Deletes the specified [note] from the secure database.
     *
     * Executes asynchronously on the viewModelScope. If the underlying data
     * operation fails, it catches the exception and emits a [NoteListEvent.ShowError]
     * to notify the user via a transient Snackbar.
     *
     * @param note The domain representation of the note to permanently delete.
     */
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase(note)
            } catch (e: Exception) {
                _events.emit(NoteListEvent.ShowError("Failed to delete note."))
            }
        }
    }
}
