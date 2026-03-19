package com.taigatkd.karamemo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class KaraMemoDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: KaraMemoDatabase? = null

        fun getInstance(context: Context): KaraMemoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KaraMemoDatabase::class.java,
                    "kara_memo.db",
                ).build().also { INSTANCE = it }
            }
    }
}

