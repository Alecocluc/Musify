package com.alecocluc.musify.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val duration: Int,
    val coverUrl: String,
    val previewUrl: String
)
