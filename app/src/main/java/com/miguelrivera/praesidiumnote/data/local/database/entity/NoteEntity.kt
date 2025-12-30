package com.miguelrivera.praesidiumnote.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persistence model for the encrypted local database.
 * * ### Security Design: SQLCipher Integration
 * This entity is stored in a database encrypted via AES-256 GCM. We use [CharArray]
 * instead of [String] to support manual memory zeroing, ensuring that sensitive
 * data doesn't persist in the Java Heap indefinitely.
 *
 * @property id Primary key.
 * @property title Encrypted headline stored as [CharArray] for heap hygiene.
 * @property content Encrypted body stored as [CharArray] for heap hygiene.
 * @property timestamp Last modification time.
 * @property isLocked Biometric lock status.
 */
@Entity(tableName = "note_table")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val title: CharArray,
    val content: CharArray,
    val timestamp: Long,
    val isLocked: Boolean
) {
    /**
     * ### Structural Equality (Diffing)
     * Manual override is mandatory for Array-based properties. This ensures Room
     * and StateFlow perform structural comparisons (content check) rather than
     * referential checks (memory address check), preventing unnecessary UI flashes.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NoteEntity
        if (id != other.id) return false
        if (!title.contentEquals(other.title)) return false
        if (!content.contentEquals(other.content)) return false
        return true
    }

    /**
     * ### High-Performance Diffing
     * Uses [contentHashCode] to ensure that database observers (Room/StateFlow)
     * accurately detect content changes in CharArrays. This prevents redundant
     * emission of identical data states to the UI layer.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.contentHashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}