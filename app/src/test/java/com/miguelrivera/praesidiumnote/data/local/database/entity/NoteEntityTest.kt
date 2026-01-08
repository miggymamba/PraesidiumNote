package com.miguelrivera.praesidiumnote.data.local.database.entity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Validates structural equality for [NoteEntity].
 * * To achieve 100% branch coverage on the manual 'equals' override, the following should be tested:
 * 1. Referential equality (same memory address).
 * 2. Class mismatch (comparing with null or different type).
 * 3. Field-level equality (ID, Title, Content).
 */
class NoteEntityTest {

    @Test
    fun `entities with same content are equal`() {
        val id = "1"
        val title = charArrayOf('A')
        val content = charArrayOf('B')

        val entity1 = NoteEntity(id, title, content, 100L, false)
        val entity2 = NoteEntity(id, title.copyOf(), content.copyOf(), 100L, false)

        // Verifies structural equality and hashCode consistency
        assertThat(entity1).isEqualTo(entity2)
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode())
    }

    @Test
    fun `entity is equal to itself`() {
        val entity = NoteEntity("1", charArrayOf('A'), charArrayOf('B'), 100L, false)

        // Triggers: if (this === other) return true
        assertThat(entity).isEqualTo(entity)
    }

    @Test
    fun `entity is not equal to null or different class`() {
        val entity = NoteEntity("1", charArrayOf('A'), charArrayOf('B'), 100L, false)

        // Triggers: if (javaClass != other?.javaClass) return false
        assertThat(entity).isNotEqualTo(null)
        assertThat(entity).isNotEqualTo("Not a NoteEntity")
    }

    @Test
    fun `entities with different content are not equal`() {
        val entity1 = NoteEntity("1", charArrayOf('A'), charArrayOf('B'), 100L, false)

        // Different ID
        assertThat(entity1).isNotEqualTo(entity1.copy(id = "2"))

        // Different Title
        assertThat(entity1).isNotEqualTo(entity1.copy(title = charArrayOf('X')))

        // Different Content
        assertThat(entity1).isNotEqualTo(entity1.copy(content = charArrayOf('Y')))
    }
}
