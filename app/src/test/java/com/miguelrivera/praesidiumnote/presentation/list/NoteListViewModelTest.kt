package com.miguelrivera.praesidiumnote.presentation.list

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.usecase.DeleteNoteUseCase
import com.miguelrivera.praesidiumnote.domain.usecase.GetNotesUseCase
import com.miguelrivera.praesidiumnote.domain.usecase.NoteResult
import com.miguelrivera.praesidiumnote.rules.MainDispatcherRule
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Validates the [NoteListViewModel] reactive state and user intents.
 *
 * Leverages MainDispatcherRule and Turbine to validate StateFlow transitions.
 */
class NoteListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getNotesUseCase: GetNotesUseCase = mockk()
    private val deleteNoteUseCase: DeleteNoteUseCase = mockk()

    private lateinit var viewModel: NoteListViewModel

    @Test
    fun `uiState transitions to Success when notes are retrieved`() = runTest {
        // Given
        val mockNotes = listOf(Note(id = "1", title = "T".toCharArray(), content = "C".toCharArray()))
        every { getNotesUseCase() } returns flowOf(NoteResult.Success(mockNotes))

        // When
        viewModel = NoteListViewModel(getNotesUseCase, deleteNoteUseCase)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(NoteListUiState.Success::class.java)
            assertThat((state as NoteListUiState.Success).notes).isEqualTo(mockNotes)
        }
    }

    @Test
    fun `uiState transitions to Empty when repository is empty`() = runTest {
        // Given
        every { getNotesUseCase() } returns flowOf(NoteResult.Error.EmptyNote)

        // When
        viewModel = NoteListViewModel(getNotesUseCase, deleteNoteUseCase)

        // Then
        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(NoteListUiState.Empty)
        }
    }

    @Test
    fun `deleteNote intent triggers background deletion`() = runTest {
        // Given
        val note = Note(id = "1", title = charArrayOf(), content = charArrayOf())
        every { getNotesUseCase() } returns flowOf(NoteResult.Error.EmptyNote)
        coJustRun { deleteNoteUseCase(note) }

        // When
        viewModel = NoteListViewModel(getNotesUseCase, deleteNoteUseCase)

        // Then
        viewModel.uiState.test {
            skipItems(1) // Skip initial state (Empty)
            viewModel.deleteNote(note)
            coVerify(exactly = 1) { deleteNoteUseCase(note) }
        }
    }

    @Test
    fun `uiState transitions to Error when generic NoteResult Error occurs`() = runTest {
        // Given
        every { getNotesUseCase() } returns flowOf(NoteResult.Error.NotFound("123"))

        // When
        viewModel = NoteListViewModel(getNotesUseCase, deleteNoteUseCase)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(NoteListUiState.Error::class.java)
            assertThat((state as NoteListUiState.Error).message).isEqualTo("Failed to load vault.")
        }
    }
}
