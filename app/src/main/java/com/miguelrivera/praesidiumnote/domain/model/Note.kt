package com.miguelrivera.praesidiumnote.domain.model

import java.util.UUID

/**
 * Core domain representation of a user note.
 *
 * ### Security Design: Zero-Knowledge Hygiene
 * By utilizing [CharArray], the app circumvents the immutability of JVM Strings.
 * This allows the app to overwrite sensitive content in memory as soon as
 * the persistence or display operation is complete.
 *
 * @property id Unique identifier.
 * @property title Encrypted headline. Uses [CharArray] for manual memory wiping.
 * @property content Encrypted body. Uses [CharArray] for manual memory wiping.
 * @property timestamp Epoch time of last modification.
 * @property isLocked Indicates if biometric re-authentication is required.
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: CharArray,
    val content: CharArray,
    val timestamp: Long = System.currentTimeMillis(),
    val isLocked: Boolean = true
) {
    /**
     * Overridden to handle [CharArray] content equality and exclude metadata
     * ([timestamp], [isLocked]) from UI diffing/recomposition triggers.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Note
        if (id != other.id) return false
        if (!title.contentEquals(other.title)) return false
        if (!content.contentEquals(other.content)) return false
        return true
    }

    /**
     * ### High-Performance Diffing
     * Uses [contentHashCode] to ensure that list updates in Compose [LazyColumn]
     * are triggered by data changes, not just memory reference shifts.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.contentHashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }

    /**
     * ### Memory Wipe
     * Overwrites sensitive data in the Java Heap with null characters.
     * This minimizes the window of exposure for memory-scraping attacks.
     */
    fun clear() {
        title.fill('\u0000')
        content.fill('\u0000')
    }
}