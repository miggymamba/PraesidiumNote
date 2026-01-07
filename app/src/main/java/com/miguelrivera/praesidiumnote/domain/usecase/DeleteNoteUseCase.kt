package com.miguelrivera.praesidiumnote.domain.usecase

import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Domain logic for removing a single note from persistent storage.
 * This use case ensures the UI layer is decoupled from database-level failures
 * by wrapping operations in a typed [NoteResult].
 */
class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    /**
     * Executes the deletion of a [Note].
     *
     * @param note The domain model to be removed.
     * @return [NoteResult.Success] on completion or [NoteResult.Error.Unknown] on failure.
     *
     * Threading: Execution is dispatched to the background IO thread pool
     * within the repository implementation.
     */
    suspend operator fun invoke(note: Note): NoteResult<Unit> {
        return try {
            repository.deleteNote(note)

            /*
             * Zero-Knowledge Hygiene:
             * Once the repository (Room/SQLCipher) deletes the data,
             * wipe the domain model's heap footprint immediately.
             */
            note.clear()

            NoteResult.Success(Unit)
        } catch (e: Exception){
            NoteResult.Error.Unknown(e.localizedMessage ?: "Failed to delete Note.")
        }
    }
}