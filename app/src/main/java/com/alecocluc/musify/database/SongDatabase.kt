package com.alecocluc.musify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SongEntity::class], version = 1, exportSchema = false)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var instance: SongDatabase? = null

        fun getDatabase(context: Context): SongDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, SongDatabase::class.java, "song_database")
                .fallbackToDestructiveMigration()
                .build()
    }
}

