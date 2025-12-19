package com.miguelrivera.praesidiumnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.miguelrivera.praesidiumnote.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}