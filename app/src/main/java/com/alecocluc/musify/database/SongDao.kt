package com.alecocluc.musify.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SongDao {
    @Insert
    suspend fun insert(song: SongEntity)

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM songs WHERE id = :songId)")
    suspend fun isSongFavorite(songId: Long): Boolean

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun delete(songId: Long)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Query("SELECT * FROM songs WHERE savedAt BETWEEN :startDate AND :endDate ORDER BY savedAt DESC")
    fun getSongsByDateRange(startDate: Long, endDate: Long): List<SongEntity>
}
