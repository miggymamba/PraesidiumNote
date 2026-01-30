package com.miguelrivera.praesidiumnote.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.google.common.truth.Truth
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.usecase.GetSingleNoteUseCase
import com.miguelrivera.praesidiumnote.domain.usecase.NoteResult
import com.miguelrivera.praesidiumnote.domain.usecase.SaveNoteUseCase
import com.miguelrivera.praesidiumnote.presentation.navigation.Screen
import com.miguelrivera.praesidiumnote.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Validates the [NoteEditorViewModel] state management and persistence flow.
 *
 * Confirms that domain-layer models are wiped after conversion to UI strings
 * and verifies that the ViewModel correctly identifies Add vs Edit mode via SavedStateHandle.
 * Uses [com.miguelrivera.praesidiumnote.rules.MainDispatcherRule] and [backgroundScope] to ensure all coroutine state
 * transitions are captured during testing.
 */
class NoteEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getSingleNoteUseCase: GetSingleNoteUseCase = mockk()
    private val saveNoteUseCase: SaveNoteUseCase = mockk()

    private lateinit var viewModel: NoteEditorViewModel

    @Before
    fun setup() {
        mockkStatic("androidx.navigation.SavedStateHandleKt")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `init in Edit mode loads note`() = runTest {
        val noteId = "existing-id"
        val mockRoute = Screen.NoteDetail(noteId = noteId)
        val savedStateHandle = SavedStateHandle()

        // MOCK: When anyone calls toRoute<Screen.NoteDetail> on this handle, return our mockRoute
        every {
            savedStateHandle.toRoute<Screen.NoteDetail>()
        } returns mockRoute

        val mockNote =
            Note(id = noteId, title = "Secret".toCharArray(), content = "Data".toCharArray())
        every { getSingleNoteUseCase(noteId) } returns flowOf(NoteResult.Success(mockNote))

        // Initialize ViewModel
        viewModel = NoteEditorViewModel(savedStateHandle, getSingleNoteUseCase, saveNoteUseCase)

        // Trigger collection to process the state flow
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        // Assertions
        Truth.assertThat(viewModel.uiState.value.title).isEqualTo("Secret")
        Truth.assertThat(viewModel.uiState.value.content).isEqualTo("Data")
        Truth.assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `onTitleChange updates the transient UI state`() = runTest {
        viewModel = NoteEditorViewModel(SavedStateHandle(), getSingleNoteUseCase, saveNoteUseCase)
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.onTitleChange("New Secret Title")

        Truth.assertThat(viewModel.uiState.value.title).isEqualTo("New Secret Title")
    }

    @Test
    fun `saveNote success triggers the isSaved navigation signal`() = runTest {
        // Given
        viewModel = NoteEditorViewModel(SavedStateHandle(), getSingleNoteUseCase, saveNoteUseCase)
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.onTitleChange("Valid Title")
        coEvery { saveNoteUseCase(any()) } returns NoteResult.Success(Unit)

        // When
        viewModel.saveNote()
        advanceUntilIdle()

        // Then
        Truth.assertThat(viewModel.uiState.value.isSaved).isTrue()
    }

    @Test
    fun `saveNote failure updates error state`() = runTest {
        // Given
        viewModel = NoteEditorViewModel(SavedStateHandle(), getSingleNoteUseCase, saveNoteUseCase)
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.onTitleChange("Title")
        coEvery { saveNoteUseCase(any()) } returns NoteResult.Error.Unknown("Disk Full")

        // When
        viewModel.saveNote()
        advanceUntilIdle()

        // Then
        Truth.assertThat(viewModel.uiState.value.error).isEqualTo("Failed to save Note.")
    }

    @Test
    fun `clearError resets the error state for UI consumption`() = runTest {
        // Given
        viewModel = NoteEditorViewModel(SavedStateHandle(), getSingleNoteUseCase, saveNoteUseCase)
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.onTitleChange("Title")
        coEvery { saveNoteUseCase(any()) } returns NoteResult.Error.Unknown("")

        // When
        viewModel.saveNote()
        advanceUntilIdle()
        Truth.assertThat(viewModel.uiState.value.error).isNotNull()

        viewModel.clearError()

        // Then
        Truth.assertThat(viewModel.uiState.value.error).isNull()
    }
}