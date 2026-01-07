    package com.miguelrivera.praesidiumnote.domain

    import com.google.common.truth.Truth.assertThat
    import com.miguelrivera.praesidiumnote.domain.model.Note
    import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
    import com.miguelrivera.praesidiumnote.domain.usecase.GetSingleNoteUseCase
    import com.miguelrivera.praesidiumnote.domain.usecase.NoteResult
    import io.mockk.every
    import io.mockk.mockk
    import kotlinx.coroutines.flow.first
    import kotlinx.coroutines.flow.flowOf
    import kotlinx.coroutines.test.runTest
    import org.junit.Test

    class GetSingleNoteUseCaseTest {

        private val repository: NoteRepository = mockk()
        private val getSingleNoteUseCase = GetSingleNoteUseCase(repository)

        @Test
        fun `invoke returns Success when note exists in flow`() = runTest {
            // Given
            val noteId = "existing-id"
            val mockNote = Note(id = noteId, title = "Title".toCharArray(), content = "Content".toCharArray())
            every { repository.getNote(noteId) } returns flowOf(mockNote)

            // When
            val result = getSingleNoteUseCase(noteId).first()

            // Then
            assertThat(result).isInstanceOf(NoteResult.Success::class.java)
            assertThat((result as NoteResult.Success).data).isEqualTo(mockNote)
        }

        @Test
        fun `invoke returns NotFound error when flow emits null`() = runTest {
            // Given
            val noteId = "missing-id"
            every { repository.getNote(noteId) } returns flowOf(null)

            // When
            val result = getSingleNoteUseCase(noteId).first()

            // Then
            assertThat(result).isInstanceOf(NoteResult.Error.NotFound::class.java)
            assertThat((result as NoteResult.Error.NotFound).id).isEqualTo(noteId)
        }

    }