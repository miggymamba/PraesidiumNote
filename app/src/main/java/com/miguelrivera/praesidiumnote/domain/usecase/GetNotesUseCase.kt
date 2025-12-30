package com.miguelrivera.praesidiumnote.domain.usecase

import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Domain logic for retrieving the stream of all available notes.
 * Maps empty results to a specific domain state to allow the UI to
 * render an empty-view without manual list-checking in the ViewModel.
 */
class GetNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    /**
     * Executes the stream retrieval.
     * @return A [Flow] of [NoteResult]. Returns [NoteResult.Error.EmptyNote] if the
     * underlying database collection is empty.
     */
    operator fun invoke(): Flow<NoteResult<List<Note>>> {
        return repository.getNotes()
            .map { notes ->
                if (notes.isEmpty()) {
                    NoteResult.Error.EmptyNote
                } else {
                    NoteResult.Success(notes)
                }
            }
    }
}