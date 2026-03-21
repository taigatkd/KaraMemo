package com.taigatkd.karamemo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class KaraMemoDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        private val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `song_scores` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `songId` TEXT NOT NULL,
                            `value` REAL NOT NULL,
                            `position` INTEGER NOT NULL,
                            FOREIGN KEY(`songId`) REFERENCES `songs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_song_scores_songId` ON `song_scores` (`songId`)",
                    )
                }
            }

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE `songs` ADD COLUMN `scoreValue` REAL")
                    db.execSQL(
                        """
                        UPDATE `songs`
                        SET `scoreValue` = (
                            SELECT MAX(`value`)
                            FROM `song_scores`
                            WHERE `song_scores`.`songId` = `songs`.`id`
                        )
                        """.trimIndent(),
                    )
                }
            }

        @Volatile
        private var INSTANCE: KaraMemoDatabase? = null

        fun getInstance(context: Context): KaraMemoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KaraMemoDatabase::class.java,
                    "kara_memo.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
