package com.miguelrivera.praesidiumnote.domain.repository

import com.miguelrivera.praesidiumnote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun upsertNote(note: Note)
    suspend fun deleteNote(note: Note)
}