package com.alecocluc.musify.models

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Int,
    val coverUrl: String,
    val previewUrl: String,
    var isFavorite: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)
