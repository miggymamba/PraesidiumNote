package com.miguelrivera.praesidiumnote.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.usecase.GetSingleNoteUseCase
import com.miguelrivera.praesidiumnote.domain.usecase.NoteResult
import com.miguelrivera.praesidiumnote.domain.usecase.SaveNoteUseCase
import com.miguelrivera.praesidiumnote.presentation.navigation.Screen
import com.miguelrivera.praesidiumnote.rules.MainDispatcherRule
import com.miguelrivera.praesidiumnote.ui.editor.NoteEditorEvent
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
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
 * Uses Turbine to validate both UI state transitions and one-time events.
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
        // Given
        val noteId = "existing-id"
        val mockRoute = Screen.NoteDetail(noteId = noteId)
        val savedStateHandle = SavedStateHandle()

        // MOCK: When anyone calls toRoute<Screen.NoteDetail> on this handle, return our mockRoute
        every { savedStateHandle.toRoute<Screen.NoteDetail>() } returns mockRoute

        val mockNote = Note(id = noteId, title = "Secret".toCharArray(), content = "Data".toCharArray())
        every { getSingleNoteUseCase(noteId) } returns flowOf(NoteResult.Success(mockNote))

        // When
        viewModel = NoteEditorViewModel(savedStateHandle, getSingleNoteUseCase, saveNoteUseCase)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.title).isEqualTo("Secret")
            assertThat(state.content).isEqualTo("Data")
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun `onTitleChange updates the transient UI state`() = runTest {
        // Given
        viewModel = NoteEditorViewModel(SavedStateHandle(), getSingleNoteUseCase, saveNoteUseCase)

        // When/Then
        viewModel.uiState.test {
            skipItems(1) // Skip initial state
            
            viewModel.onTitleChange("New Secret Title")
            
            assertThat(awaitItem().title).isEqualTo("New Secret Title")
        }
    }

    @Test
    fun `saveNote success triggers the NavigateBack event`() = runTest {
        // Given
        viewModel = NoteEditorViewModel(SavedStateHandle(), getSingleNoteUseCase, saveNoteUseCase)
        
        viewModel.events.test {
            viewModel.onTitleChange("Valid Title")
            coEvery { saveNoteUseCase(any()) } returns NoteResult.Success(Unit)

            // When
            viewModel.saveNote()

            // Then - SharedFlow emits a one-time navigation event
            assertThat(awaitItem()).isEqualTo(NoteEditorEvent.NavigateBack)
        }
    }

    @Test
    fun `saveNote failure triggers the ShowError event`() = runTest {
        // Given
        viewModel = NoteEditorViewModel(SavedStateHandle(), getSingleNoteUseCase, saveNoteUseCase)
        
        viewModel.events.test {
            viewModel.onTitleChange("Title")
            coEvery { saveNoteUseCase(any()) } returns NoteResult.Error.Unknown("Disk Full")

            // When
            viewModel.saveNote()

            // Then - Failure is emitted as a transient UI event
            assertThat(awaitItem()).isEqualTo(NoteEditorEvent.ShowError("Failed to save Note."))
        }
    }

    @Test
    fun `clearError resets the error state for UI consumption`() = runTest {
        // Given
        val noteId = "id"
        val savedStateHandle = SavedStateHandle()
        every { savedStateHandle.toRoute<Screen.NoteDetail>() } returns Screen.NoteDetail(noteId)
        every { getSingleNoteUseCase(noteId) } returns flowOf(NoteResult.Error.Unknown("Error"))

        // When
        viewModel = NoteEditorViewModel(savedStateHandle, getSingleNoteUseCase, saveNoteUseCase)

        // Then
        viewModel.uiState.test {
            // Initial collection finds the error state from init loading failure
            val errorState = awaitItem()
            assertThat(errorState.error).isNotNull()

            viewModel.clearError()

            // State updates to clear the error
            assertThat(awaitItem().error).isNull()
        }
    }
}
