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
 * ### Security Note: Zero-Knowledge Hygiene
 * Since the UI layer requires [String], which are immutable and potentially persistent
 * in the heap, the references should be explicitly nullified and wipe temporary [CharArray]
 * buffers during [onCleared] to minimize the memory footprint of sensitive data.
 */
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSingleNoteUseCase: GetSingleNoteUseCase,
    private val saveNoteUseCase: SaveNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

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
                            it.copy(error = "Failed to load note.", isLoading = false)
                        }
                    }
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
    }

    fun saveNote() {
        val currentState = _uiState.value
        if (currentState.title.isBlank() && currentState.content.isBlank()) return

        viewModelScope.launch {
            // Explicit try-finally to ensure that even if
            // the save fails, it won't leak the temporary CharArrays.
            val titleArray = currentState.title.toCharArray()
            val contentArray = currentState.content.toCharArray()

            try {
                val noteToSave = if (noteId != null) {
                    Note(id = noteId, title = titleArray, content = contentArray)
                } else {
                    Note(title = titleArray, content = contentArray)
                }

                val result = saveNoteUseCase(noteToSave)
                if (result is NoteResult.Success) {
                    _uiState.update { it.copy(isSaved = true) }
                } else {
                    _uiState.update { it.copy(error = "Failed to save Note.") }
                }
            } finally {
                //  Manual wipe of the local stack arrays
                titleArray.fill('\u0000')
                contentArray.fill('\u0000')
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Executed when the user navigates away and the ViewModel is destroyed.
     * Overwrite the uiState with empty values to encourage the GC to reclaim
     * the sensitive string references.
     */
    override fun onCleared() {
        super.onCleared()
        _uiState.update {
            it.copy(
                title = "",
                content = "",
                error = null
            )
        }
    }
}

data class NoteEditorUiState(
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)