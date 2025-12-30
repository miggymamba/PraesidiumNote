package com.miguelrivera.praesidiumnote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.miguelrivera.praesidiumnote.data.local.database.dao.NoteDao
import com.miguelrivera.praesidiumnote.data.local.database.entity.NoteEntity
import com.miguelrivera.praesidiumnote.data.local.database.util.Converters

/**
 * The Room Database for this app.
 * Encrypted via SQLCipher as integrated in the [com.miguelrivera.praesidiumnote.data.di.DatabaseModule].
 */
@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
    companion object {
        /**
         * The name of the database file store in the app's internal storage.
         */
        const val DATABASE_NAME = "praesidium_notes_db"
    }
}