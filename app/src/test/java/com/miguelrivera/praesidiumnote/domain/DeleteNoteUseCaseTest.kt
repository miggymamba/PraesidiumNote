package com.miguelrivera.praesidiumnote.domain

import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import com.miguelrivera.praesidiumnote.domain.usecase.DeleteNoteUseCase
import com.miguelrivera.praesidiumnote.domain.usecase.NoteResult
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteNoteUseCaseTest {
    private val repository: NoteRepository = mockk()
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)

    @Test
    fun `invoke calls repository delete and returns success`() = runTest {
        // Given
        val note = Note(id = "test-id", title = "T".toCharArray(), content = "C".toCharArray())
        coJustRun { repository.deleteNote(note) }

        // When
        val result = deleteNoteUseCase(note)

        // Then
        assertThat(result).isInstanceOf(NoteResult.Success::class.java)
        coVerify(exactly = 1) { repository.deleteNote(note) }

        // Verify memory wipe after deletion
        assertThat(note.title.toList()).containsExactly('\u0000')
    }

    @Test
    fun `invoke returns generic error when repository fails`() = runTest {
        val note = Note(id = "test-id", title = charArrayOf(), content = charArrayOf())
        coEvery { repository.deleteNote(note) } throws Exception("Database error")

        val result = deleteNoteUseCase(note)

        assertThat(result).isInstanceOf(NoteResult.Error.Unknown::class.java)
    }
}