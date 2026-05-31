package com.miguelrivera.praesidiumnote.ui.editor

/**
 * Defines one-time UI events originating from the Note Editor screen.
 *
 * Unlike state, these events represent transient actions such as navigation
 * or error notifications that must be consumed exactly once to prevent
 * replay upon lifecycle changes.
 */
sealed interface NoteEditorEvent {
    /**
     * Triggered when the editor should be closed and the user returned
     * to the previous screen in the navigation stack.
     */
    data object NavigateBack : NoteEditorEvent

    /**
     * Triggered when a transient error occurs during a save or load operation.
     *
     * @property message The localized error description to display to the user.
     */
    data class ShowError(val message: String) : NoteEditorEvent
}