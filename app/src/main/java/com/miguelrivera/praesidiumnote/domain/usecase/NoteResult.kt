package com.miguelrivera.praesidiumnote.domain.usecase

/**
 * A sealed hierarchy representing the outcome of a Domain-layer operation.
 * * Unlike standard try-catch blocks, this "Result" pattern forces the
 * Presentation layer to explicitly handle all logical states (Success, Empty, Error),
 * ensuring a predictable UI state and preventing unhandled exceptions
 * from leaking to the user.
 */
sealed class NoteResult<out T> {

    /** Represents a successful operation containing the resulting [data]. */
    data class Success<out T>(val data: T) : NoteResult<T>()

    /** * Base class for all domain-specific failures.
     * Using a sealed sub-hierarchy allows for exhaustive 'when' expressions in ViewModels.
     */
    sealed class Error : NoteResult<Nothing>() {

        /** * Represents an empty state across two primary contexts:
         * 1. Retrieval (GetNotes): Indicates the database collection contains no records.
         * 2. Persistence (SaveNote): Indicates a validation failure where both title
         * and content are empty or contain only whitespace.
         */
        object EmptyNote : Error()

        /** Indicates a failure in the SQLCipher/TEE encryption handshake. */
        data class EncryptionError(val message: String) : Error()

        /** Triggered when a specific record lookup fails for the provided [id]. */
        data class NotFound(val id: String) : Error()

        /** A catch-all for non-domain specific failures (e.g., IO exceptions). */
        data class Unknown(val message: String) : Error()
    }
}