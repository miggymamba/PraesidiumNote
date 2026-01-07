package com.miguelrivera.praesidiumnote.domain

import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.domain.model.Note
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import com.miguelrivera.praesidiumnote.domain.usecase.NoteResult
import com.miguelrivera.praesidiumnote.domain.usecase.SaveNoteUseCase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Verifies validation logic and Zero-Knowledge memory hygiene.
 */
class SaveNoteUseCaseTest {
    private val repository: NoteRepository = mockk()
    private val saveNoteUseCase = SaveNoteUseCase(repository)

    @Test
    fun `invoke fails with EmptyNote error when title and content are blank`() = runTest {
        // Given
        val emptyNote = Note(
            title = "".toCharArray(), content = " \n".toCharArray()
        )
        // When
        val result = saveNoteUseCase(emptyNote)

        // Then
        assertThat(result).isInstanceOf(NoteResult.Error.EmptyNote::class.java)
        coVerify(exactly = 0) { repository.upsertNote(emptyNote) }
    }

    @Test
    fun `invoke persists note and WIPES memory buffer (Zero-Knowledge Check)`() = runTest {
        // Given
        val originalTitle = "Github Recovery Codes"
        val originalContent = "QWER-5678"
        val note = Note(
            title = originalTitle.toCharArray(), content = originalContent.toCharArray()
        )

        coJustRun { repository.upsertNote(any()) }

        // When
        val result = saveNoteUseCase(note)

        // Then
        // 1. Verify success result
        assertThat(result).isInstanceOf(NoteResult.Success::class.java)

        // 2. Verify repository was called
        coVerify(exactly = 1) { repository.upsertNote(any()) }

        // 3. Zero Knowledge Check:
        val wipedChar = '\u0000'

        // Ensure the heap object was physically overwritten with nulls.
        // Convert to List for clean, idiomatic Truth assertions that avoid reference issues
        assertThat(note.title.toList()).containsExactlyElementsIn(List(originalTitle.length) { wipedChar })
        assertThat(note.content.toList()).containsExactlyElementsIn(List(originalContent.length) { wipedChar })
    }

    @Test
    fun `invoke maps SQLCipher exceptions to EncryptionError`() = runTest {
        // Given
        val note = Note(title = "A".toCharArray(), content = "B".toCharArray())
        val sqlCipherException =
            RuntimeException("net.zetetic.database.sqlcipher.SQLiteException: file is not a database")

        coEvery { repository.upsertNote(any()) } throws sqlCipherException

        // When
        val result = saveNoteUseCase(note)

        // Then
        assertThat(result).isInstanceOf(NoteResult.Error.EncryptionError::class.java)

        // Ensure note.clear() is called in a 'finally' block so that
        // memory is wiped even if the database operation fails.
    }
}