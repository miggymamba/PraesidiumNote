package com.miguelrivera.praesidiumnote.domain.usecase

import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Domain logic for retrieving a specific note by its unique identifier.
 * Maps null results to a [NoteResult.Error.NotFound] to ensure the
 * presentation layer can trigger error states immediately without handling nullability.
 */
class GetSingleNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    /**
     * Executes the single-item lookup.
     * @param id The unique identifier of the requested note.
     * @return A [Flow] of [NoteResult]. Returns [NoteResult.Error.NotFound] if the
     * record does not exist or was deleted mid-stream.
     */
    operator fun invoke(id: String): Flow<NoteResult<Note>> {
        return repository.getNote(id)
            .map { note ->
                if (note != null) {
                    NoteResult.Success(note)
                } else {
                    NoteResult.Error.NotFound(id = id)
                }
            }
    }
}