package com.miguelrivera.praesidiumnote.domain

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import com.miguelrivera.praesidiumnote.domain.usecase.GetNotesUseCase
import com.miguelrivera.praesidiumnote.domain.usecase.NoteResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Validates the [GetNotesUseCase] business logic.
 *
 * Uses Turbine to ensure that the Flow not only emits the correct
 * [NoteResult] but also completes properly, ensuring no leaked observers.
 */
class GetNotesUseCaseTest {
    private val repository: NoteRepository = mockk()
    private val getNotesUseCase = GetNotesUseCase(repository)

    @Test
    fun `invoke empty list maps to EmptyNote error state`() = runTest {
        // Given
        every { repository.getNotes() } returns flowOf(emptyList())

        // When/Then
        getNotesUseCase().test {
            // Assert: The repository's empty list is mapped to our domain error
            assertThat(awaitItem()).isEqualTo(NoteResult.Error.EmptyNote)
            
            // Assert: The stream terminates after the single emission
            awaitComplete()
        }
    }

    @Test
    fun `populated list maps to Success state`() = runTest {
        // Given
        val mockNotes = listOf(Note(title = charArrayOf(), content = charArrayOf()))
        every { repository.getNotes() } returns flowOf(mockNotes)

        // When/Then
        getNotesUseCase().test {
            // Assert: Success state contains our data
            val result = awaitItem()
            assertThat(result).isInstanceOf(NoteResult.Success::class.java)
            assertThat((result as NoteResult.Success).data).hasSize(1)
            
            // Assert: Clean termination
            awaitComplete()
        }
    }
}
