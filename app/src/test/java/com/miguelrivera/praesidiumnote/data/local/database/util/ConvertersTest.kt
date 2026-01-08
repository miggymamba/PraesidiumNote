package com.miguelrivera.praesidiumnote.data.local.database.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun `fromCharArray converts correctly`() {
        val input = charArrayOf('T', 'e', 's', 't')
        assertThat(converters.fromCharArray(input)).isEqualTo("Test")
    }

    @Test
    fun `toCharArray converts correctly`() {
        val input = "Test"
        assertThat(converters.toCharArray(input)).isEqualTo(charArrayOf('T', 'e', 's', 't'))
    }

    @Test
    fun `handles null values`() {
        assertThat(converters.fromCharArray(null)).isNull()
        assertThat(converters.toCharArray(null)).isNull()
    }
}