package com.miguelrivera.praesidiumnote.domain.usecase

import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Domain logic for validating and persisting a note.
 * This use case handles business validation rules and ensures sensitive
 * memory hygiene by clearing the domain model footprint post-persistence.
 */
class SaveNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    /**
     * Executes the save operation.
     * @param note The domain model to be persisted.
     * @return [NoteResult.Success] or a validation/persistence error.
     */
    suspend operator fun invoke(note: Note): NoteResult<Unit> {
        // Validation: Business rule to prevent persistence of empty notes.
        if (note.title.all { it.isWhitespace() } && note.content.all { it.isWhitespace() }) {
            return NoteResult.Error.EmptyNote
        }

        return try {
            repository.upsertNote(note)

            /*
             * Zero-Knowledge Hygiene:
             * Once the repository (Room/SQLCipher) consumes the data,
             * we wipe the domain model's heap footprint immediately.
             */
            note.clear()

            NoteResult.Success(Unit)
        } catch (e: Exception) {
            // Map technical/cryptographic exceptions to domain-friendly results.
            if (e.message?.contains("SQLCipher", ignoreCase = true) == true) {
                NoteResult.Error.EncryptionError(e.localizedMessage ?: "Cryptographic Failure while saving Note.")
            } else {
                NoteResult.Error.Unknown(e.localizedMessage ?: "Unknown Persistence error while saving Note.")
            }
        }
    }
}