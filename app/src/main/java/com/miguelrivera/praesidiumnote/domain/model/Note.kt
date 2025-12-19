package com.miguelrivera.praesidiumnote.domain.model

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isPinned: Boolean = false,
    val color: Int
)