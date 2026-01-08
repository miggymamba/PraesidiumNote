package com.miguelrivera.praesidiumnote.presentation.list

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Validates the [NoteListViewModel] reactive state and user intents.
 *
 * Leverages [com.miguelrivera.praesidiumnote.rules.MainDispatcherRule] to control [viewModelScope] execution.
 * To test [stateIn] with [SharingStarted.WhileSubscribed], a collection should be launched
 * in the [backgroundScope]. This triggers the upstream flow and ensures the test
 * move past the 'Loading' initial state without manually mixing dispatchers.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getNotesUseCase: GetNotesUseCase = mockk()
    private val deleteNoteUseCase: DeleteNoteUseCase = mockk()

    private lateinit var viewModel: NoteListViewModel

    @Test
    fun `uiState transitions to Success when notes are retrieved`() = runTest {
        // Given
        val mockNotes =
            listOf(Note(id = "1", title = "T".toCharArray(), content = "C".toCharArray()))
        every { getNotesUseCase() } returns flowOf(NoteResult.Success(mockNotes))

        // When
        viewModel = NoteListViewModel(getNotesUseCase, deleteNoteUseCase)

        // Use backgroundScope to collect the StateFlow.
        // This triggers WhileSubscribed(5000) immediately using the Rule's dispatcher.
        backgroundScope.launch { viewModel.uiState.collect() }

        // Ensure all coroutines in the viewModelScope finish their mapping
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value).isInstanceOf(NoteListUiState.Success::class.java)
        val successState = viewModel.uiState.value as NoteListUiState.Success
        assertThat(successState.notes).isEqualTo(mockNotes)
    }

    @Test
    fun `uiState transitions to Empty when repository is empty`() = runTest {
        // Given
        every { getNotesUseCase() } returns flowOf(NoteResult.Error.EmptyNote)

        // When
        viewModel = NoteListViewModel(getNotesUseCase, deleteNoteUseCase)
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(NoteListUiState.Empty)
    }

    @Test
    fun `deleteNote intent triggers background deletion`() = runTest {
        // Given
        val note = Note(id = "1", title = charArrayOf(), content = charArrayOf())
        every { getNotesUseCase() } returns flowOf(NoteResult.Error.EmptyNote)
        coJustRun { deleteNoteUseCase(note) }

        // When
        viewModel = NoteListViewModel(getNotesUseCase, deleteNoteUseCase)
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.deleteNote(note)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { deleteNoteUseCase(note) }
    }

    @Test
    fun `uiState transitions to Error when generic NoteResult Error occurs`() = runTest {
        // NoteResult.Error.NotFound is a subclass of NoteResult.Error
        every { getNotesUseCase() } returns flowOf(NoteResult.Error.NotFound("123"))

        viewModel = NoteListViewModel(getNotesUseCase, deleteNoteUseCase)
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertThat(viewModel.uiState.value).isInstanceOf(NoteListUiState.Error::class.java)
        val errorState = viewModel.uiState.value as NoteListUiState.Error
        assertThat(errorState.message).isEqualTo("Failed to load vault.")
    }
}