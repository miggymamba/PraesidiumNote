package com.miguelrivera.praesidiumnote.data.local.database.util

import androidx.room.TypeConverter

/**
 * Type converters to handle CharArray persistence.
 *
 * ### Architectural Decision: Intermediary Mapping
 * While the [NoteEntity] uses [CharArray] for memory security (heap hygiene),
 * Room requires a primitive mapping for SQLite. We map to [String] during
 * the persistence phase.
 * * Note: While [String] creation is temporary, SQLCipher encrypts this data
 * before it is written to disk, ensuring the 'Zero-Knowledge' contract.
 */
class Converters {

    @TypeConverter
    fun fromCharArray(value: CharArray?): String? {
        return value?.let { String(it) }
    }

    @TypeConverter
    fun toCharArray(value: String?): CharArray? {
        return value?.toCharArray()
    }
}