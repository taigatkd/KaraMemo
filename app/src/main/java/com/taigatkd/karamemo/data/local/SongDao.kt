package com.taigatkd.karamemo.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<SongEntity>>

    @Upsert
    suspend fun upsert(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteById(songId: String)

    @Query("DELETE FROM songs WHERE artist = :artist")
    suspend fun deleteByArtist(artist: String)

    @Query("SELECT id FROM songs WHERE lower(artist) = lower(:artist) AND lower(title) = lower(:title) LIMIT 1")
    suspend fun findDuplicateId(artist: String, title: String): String?
}

