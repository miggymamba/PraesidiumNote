package com.miguelrivera.praesidiumnote.data.mapper

import com.miguelrivera.praesidiumnote.data.local.database.entity.NoteEntity
import com.miguelrivera.praesidiumnote.domain.model.Note

/**
 * Extension functions to bridge the Data and Domain layers.
 * Note: Domain models should remain pure and unaware of persistence logic.
 */
fun NoteEntity.toNote(): Note {
    return Note(id, title, content, timestamp, isPinned, color)
}

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(id, title, content, timestamp, isPinned, color)
}