package com.miguelrivera.praesidiumnote.domain

import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import com.miguelrivera.praesidiumnote.domain.usecase.GetNotesUseCase
import com.miguelrivera.praesidiumnote.domain.usecase.NoteResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetNotesUseCaseTest {
    private val repository: NoteRepository = mockk()
    private val getNotesUseCase = GetNotesUseCase(repository)

    @Test
    fun `invoke empty list maps to EmptyNote error state`() = runTest {
        // Given
        every { repository.getNotes() } returns flowOf(emptyList())

        // When
        val result = getNotesUseCase().first()

        // Then
        assertThat(result).isEqualTo(NoteResult.Error.EmptyNote)
    }

    @Test
    fun `populated list maps to Success state`() = runTest {
        // Given
        val mockNotes = listOf(Note(title = charArrayOf(), content = charArrayOf()))
        every { repository.getNotes() } returns flowOf(mockNotes)

        // When
        val result = getNotesUseCase().first()

        // Then
        assertThat(result).isInstanceOf(NoteResult.Success::class.java)
        assertThat((result as NoteResult.Success).data).hasSize(1)
    }
}