package com.miguelrivera.praesidiumnote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.miguelrivera.praesidiumnote.data.local.database.dao.NoteDao
import com.miguelrivera.praesidiumnote.data.local.database.entity.NoteEntity

/**
 * The Room Database for this app.
 * Encrypted via SQLCipher as integrated in the [com.miguelrivera.praesidiumnote.data.di.DatabaseModule].
 */
@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
    companion object {
        /**
         * The name of the database file store in the app's internal storage.
         */
        const val DATABASE_NAME = "praesidium_notes_db"
    }
}