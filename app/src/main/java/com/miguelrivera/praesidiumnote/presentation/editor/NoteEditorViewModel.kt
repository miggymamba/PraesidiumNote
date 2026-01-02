package com.miguelrivera.praesidiumnote.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.usecase.GetSingleNoteUseCase
import com.miguelrivera.praesidiumnote.domain.usecase.NoteResult
import com.miguelrivera.praesidiumnote.domain.usecase.SaveNoteUseCase
import com.miguelrivera.praesidiumnote.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the state for Note Creation and Editing.
 *
 * ### Security Note: String vs CharArray
 * While the Domain layer strictly uses [CharArray] for Zero-Knowledge hygiene,
 * the UI layer (Compose TextField) requires [String]. This ViewModel acts as the
 * security airlock, converting UI Strings to CharArrays only at the moment of persistence.
 */
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSingleNoteUseCase: GetSingleNoteUseCase,
    private val saveNotesUseCase: SaveNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    /**
     * Probes the [SavedStateHandle] for [Screen.NoteDetail] arguments.
     * Returns null if we are in [Screen.AddNote] mode, allowing for unified logic.
     */
    private val noteId: String? = try {
        savedStateHandle.toRoute<Screen.NoteDetail>().noteId
    } catch (e: Exception) {
        null
    }

    init {
        noteId?.let(::loadNote)
    }

    private fun loadNote(id: String) {
        viewModelScope.launch {
            getSingleNoteUseCase(id).collect { result ->
                when (result) {
                    is NoteResult.Success -> {
                        val note = result.data
                        _uiState.update {
                            it.copy(
                                title = String(note.title),
                                content = String(note.content),
                                isLoading = false
                            )
                        }
                        // Immediate wipe of the domain model buffer after UI consumption
                        note.clear()
                    }

                    is NoteResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Failed to load note.",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Intent: Update the title buffer from UI input.
     */
    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    /**
     * Intent: Update the content buffer from UI input.
     */
    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
    }

    /**
     * Intent: Persist the current state to the encrypted database.
     * Converts the UI Strings into CharArrays and delegates to the UseCase.
     */
    fun saveNote() {
        val currentState = _uiState.value
        if (currentState.title.isBlank() && currentState.content.isBlank()) return

        viewModelScope.launch {
            val noteToSave = if (noteId != null) {
                Note(
                    id = noteId,
                    title = currentState.title.toCharArray(),
                    content = currentState.content.toCharArray()
                )
            } else {
                Note(
                    title = currentState.title.toCharArray(),
                    content = currentState.content.toCharArray()
                )
            }

            val result = saveNotesUseCase(noteToSave)

            when (result) {
                is NoteResult.Success -> {
                    _uiState.update { it.copy(isSaved = true) }
                }

                is NoteResult.Error -> {
                    _uiState.update { it.copy(error = "Failed to save Note.") }
                }
            }
        }
    }

    /**
     * Consumes one-time transient events or error states.
     * * Resets the error field in [NoteEditorUiState] to null, preventing redundant
     * triggers (like Snackbars) during UI recomposition or configuration changes.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * MVI State for the Editor.
 *
 * @property title The transient text for the Title field.
 * @property content The transient text for the Content field.
 * @property isSaved One-time signal to trigger navigation back.
 */
data class NoteEditorUiState(
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)