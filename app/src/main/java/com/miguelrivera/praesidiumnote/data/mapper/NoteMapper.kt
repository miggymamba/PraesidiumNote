package com.miguelrivera.praesidiumnote.data.mapper

import com.miguelrivera.praesidiumnote.data.local.entity.NoteEntity
import com.miguelrivera.praesidiumnote.domain.model.Note

fun NoteEntity.toNote(): Note {
    return Note(id, title, content, timestamp, isPinned, color)
}

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(id, title, content, timestamp, isPinned, color)
}