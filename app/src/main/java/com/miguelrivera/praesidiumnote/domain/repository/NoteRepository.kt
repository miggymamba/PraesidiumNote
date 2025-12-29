package com.miguelrivera.praesidiumnote.domain.repository

import com.miguelrivera.praesidiumnote.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Domain level contract for Note data operations.
 * This interface is agnostic of the underlying persistence implementation (Room/SQLCipher).
 */
interface NoteRepository {
    /**
     * Streams all notes from the local database, sorted by `timestamp` descending.
     */
    fun getNotes(): Flow<List<Note>>

    /**
     * Streams a specific note by ID. Returns null if the note does not exist
     * in the database or was deleted while the stream was active.
     */
    fun getNote(id: String): Flow<Note?>

    /**
     * Creates or updates a note in the local database.
     */
    suspend fun upsertNote(note: Note)

    /**
     * Removes a note from the local database.
     */
    suspend fun deleteNote(note: Note)
}