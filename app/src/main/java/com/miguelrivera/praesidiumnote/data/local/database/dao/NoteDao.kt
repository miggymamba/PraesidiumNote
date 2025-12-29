package com.miguelrivera.praesidiumnote.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.miguelrivera.praesidiumnote.data.local.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 *  Data Access Object for Note Persistence.
 *  getNote returns a nullable NoteEntity flow to safely handle concurrent deletions.
 */
@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note_table WHERE id = :id")
    fun getNote(id: String): Flow<NoteEntity?>

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}