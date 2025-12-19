package com.miguelrivera.praesidiumnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
data class NoteEntity(
    @PrimaryKey val id: String, // UUID generated in Domain
    val title: String,
    val content: String,
    val timestamp: Long,
    val isPinned: Boolean,
    val color: Int
)