package com.miguelrivera.praesidiumnote.data.mapper

import com.miguelrivera.praesidiumnote.data.local.database.entity.NoteEntity
import com.miguelrivera.praesidiumnote.domain.model.Note

/**
 * Extension functions to convert between NoteEntity (Data) and Note (Domain).
 * * We use .copyOf() for the CharArrays to make sure each layer has its own
 * version of the data. This is important so that when we clear the data
 * in the domain layer, it doesn't accidentally wipe the data being saved
 * to the database. We avoid using regular Strings here to prevent
 * sensitive information from getting stuck in the memory.
 */
fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title.copyOf(),
        content = content.copyOf(),
        timestamp = timestamp,
        isLocked = isLocked
    )
}

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title.copyOf(),
        content = content.copyOf(),
        timestamp = timestamp,
        isLocked = isLocked
    )
}