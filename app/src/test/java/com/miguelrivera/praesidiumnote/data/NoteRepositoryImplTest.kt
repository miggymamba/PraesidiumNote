package com.miguelrivera.praesidiumnote.data.repository

import com.google.common.truth.Truth.assertThat
import com.miguelrivera.praesidiumnote.data.local.database.dao.NoteDao
import com.miguelrivera.praesidiumnote.data.local.database.entity.NoteEntity
import com.miguelrivera.praesidiumnote.domain.model.Note
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteRepositoryImplTest {

    private val noteDao: NoteDao = mockk(relaxed = true)

    // Use StandardTestDispatcher to verify that operations are actually dispatched
    // and not just running on the calling thread by accident.
    private val testDispatcher = StandardTestDispatcher()

    private val repository = NoteRepositoryImpl(
        noteDao = noteDao,
        ioDispatcher = testDispatcher
    )

    @Test
    fun `getNotes maps Entity to Domain model correctly`() = runTest(testDispatcher) {
        // Given
        val entityTitle = "Confidential Title"
        val entityContent = "Confidential Content"
        val entity = NoteEntity(
            id = "ABCD-1234",
            title = entityTitle.toCharArray(),
            content = entityContent.toCharArray(),
            timestamp = 84L,
            isLocked = true
        )
        every { noteDao.getAllNotes() } returns flowOf(listOf(entity))

        // When
        val result = repository.getNotes().first()

        // Then
        assertThat(result).hasSize(1)
        val domainNote = result[0]

        assertThat(String(domainNote.title)).isEqualTo(entityTitle)
        assertThat(String(domainNote.content)).isEqualTo(entityContent)

        assertThat(domainNote.title).isNotSameInstanceAs(entity.title)
    }

    @Test
    fun `upsertNote delegates to DAO on ioDispatcher`() = runTest(testDispatcher) {
        // Given
        val note = Note(
            title = "sample".toCharArray(),
            content = "test".toCharArray()
        )

        val slot = slot<NoteEntity>()

        // When
        repository.upsertNote(note)

        // Then
        coVerify { noteDao.upsertNote(capture(slot)) }
        assertThat(String(slot.captured.title)).isEqualTo(String(note.title))
    }

    @Test
    fun `upsertNote converts domain model to entity and calls dao`() = runTest(testDispatcher) {
        val note = Note(id = "123", title = "T".toCharArray(), content = "C".toCharArray())
        coJustRun { noteDao.upsertNote(any()) }

        repository.upsertNote(note)

        coVerify {
            noteDao.upsertNote(withArg { entity ->
                assertThat(entity.id).isEqualTo("123")
                assertThat(String(entity.title)).isEqualTo("T")
            })
        }
    }

    @Test
    fun `deleteNote calls dao delete`() = runTest(testDispatcher) {
        val note = Note(id = "123", title = charArrayOf(), content = charArrayOf())
        coJustRun { noteDao.deleteNote(any()) }

        repository.deleteNote(note)

        coVerify { noteDao.deleteNote(withArg { assertThat(it.id).isEqualTo("123") }) }
    }

    @Test
    fun `getNote returns domain note via flow when entity exists`() = runTest(testDispatcher) {
        // Given
        val entity = NoteEntity("1", "Title".toCharArray(), "Content".toCharArray(), 0L, false)
        every { noteDao.getNote("1") } returns flowOf(entity)

        // When
        val result = repository.getNote("1").first()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo("1")
        assertThat(String(result!!.title)).isEqualTo("Title")
    }
}