package com.miguelrivera.praesidiumnote.presentation.editor

/**
 * Represents the UI state for the Note Editor screen.
 *
 * This is modeled as a [data class] rather than a sealed interface because
 * the state properties (title, content, loading, error) are not mutually exclusive;
 * they can exist in various combinations (e.g., showing content while saving).
 */
data class NoteEditorUiState(
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)