package com.miguelrivera.praesidiumnote.presentation.list

/**
 * Defines one-time UI events originating from the Note List screen.
 *
 * Unlike standard UI State, these events are transient and must only be consumed
 * once (e.g., Snackbars, navigation triggers). This prevents events from
 * erroneously replaying upon lifecycle changes such as device rotation.
 */
sealed interface NoteListEvent {

    /**
     * Triggered when a transient error occurs that requires user visibility.
     *
     * @property message The localized error description to display.
     */
    data class ShowError(val message: String) : NoteListEvent
}