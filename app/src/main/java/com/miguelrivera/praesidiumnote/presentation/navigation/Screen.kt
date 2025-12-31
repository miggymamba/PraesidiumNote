package com.miguelrivera.praesidiumnote.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Defines the type-safe navigation destinations for the application.
 *
 * This schema utilizes Kotlin Serialization to enforce compile-time safety across
 * the navigation graph. By representing routes as structured data classes rather
 * than raw strings, the application eliminates common runtime vulnerabilities
 * associated with string-based routing and argument passing.
 */
sealed interface Screen {

    /**
     * The primary security gateway. This destination acts as a barrier, ensuring
     * that no sensitive user data is initialized or displayed until identity
     * verification is completed.
     */
    @Serializable
    data object Auth: Screen

    /**
     * The main dashboard of the encrypted vault. Provides a high-level overview
     * of stored records using metadata-only headers to preserve memory security.
     */
    @Serializable
    data object NoteList: Screen

    /**
     * Dedicated destination for initializing a new record. This specific intent
     * ensures the editor is instantiated with a clean state, adhering to
     * zero-knowledge principles by avoiding data carry-over.
     */
    @Serializable
    data object AddNote: Screen

    /**
     * Secure view for accessing and modifying existing records.
     * * @property noteId The unique identifier required to locate and decrypt
     * the specific record payload from local storage.
     */
    @Serializable
    data class NoteDetail(val noteId: String): Screen
}