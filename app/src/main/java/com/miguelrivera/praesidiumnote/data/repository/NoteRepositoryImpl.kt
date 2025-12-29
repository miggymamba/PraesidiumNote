package com.miguelrivera.praesidiumnote.data.repository

import com.miguelrivera.praesidiumnote.data.local.database.dao.NoteDao
import com.miguelrivera.praesidiumnote.data.mapper.toNote
import com.miguelrivera.praesidiumnote.data.mapper.toNoteEntity
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Data layer implementation of [NoteRepository].
 * Map operations transforms database entities to domain models.
 */
class NoteRepositoryImpl @Inject constructor(private val noteDao: NoteDao) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { currentEntity ->
                currentEntity.toNote()
            }
        }
    }

    override fun getNote(id: String): Flow<Note?> {
        return noteDao.getNote(id).map { noteEntity ->
            noteEntity?.toNote()
        }
    }

    override suspend fun upsertNote(note: Note) {
        noteDao.upsertNote(note.toNoteEntity())
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toNoteEntity())
    }
}