package com.miguelrivera.praesidiumnote.data.repository

import com.miguelrivera.praesidiumnote.data.di.IoDispatcher
import com.miguelrivera.praesidiumnote.data.local.database.dao.NoteDao
import com.miguelrivera.praesidiumnote.data.mapper.toNote
import com.miguelrivera.praesidiumnote.data.mapper.toNoteEntity
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Data layer implementation of [NoteRepository].
 * Map operations transforms database entities to domain models.
 * The functions operate on an IO Dispatcher to since it is a database operation.
 */
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
            .map { entities ->
                entities.map { currentEntity ->
                    currentEntity.toNote()
                }
            }
            .flowOn(ioDispatcher)
    }

    override fun getNote(id: String): Flow<Note?> {
        return noteDao.getNote(id)
            .map { noteEntity ->
                noteEntity?.toNote()
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun upsertNote(note: Note) {
        withContext(ioDispatcher) {
            noteDao.upsertNote(note.toNoteEntity())
        }
    }

    override suspend fun deleteNote(note: Note) {
        withContext(ioDispatcher) {
            noteDao.deleteNote(note.toNoteEntity())
        }
    }
}