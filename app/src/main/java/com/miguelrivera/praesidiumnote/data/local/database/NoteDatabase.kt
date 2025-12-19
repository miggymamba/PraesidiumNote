package com.miguelrivera.praesidiumnote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.miguelrivera.praesidiumnote.data.local.dao.NoteDao
import com.miguelrivera.praesidiumnote.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
}