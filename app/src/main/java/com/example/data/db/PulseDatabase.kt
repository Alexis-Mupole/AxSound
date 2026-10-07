package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.dao.MusicDao
import com.example.data.db.entity.FavoriteSongEntity
import com.example.data.db.entity.LoudnessGainCacheEntity
import com.example.data.db.entity.PlaybackHistoryEntity
import com.example.data.db.entity.PlaylistEntity
import com.example.data.db.entity.PlaylistItemEntity
import com.example.data.db.entity.SongMetadataCacheEntity

@Database(
    entities = [
        FavoriteSongEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        PlaybackHistoryEntity::class,
        SongMetadataCacheEntity::class,
        LoudnessGainCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: PulseDatabase? = null

        fun getInstance(context: Context): PulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PulseDatabase::class.java,
                    "pulsewave_music.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
