package com.taigatkd.karamemo.data.repository

import com.taigatkd.karamemo.data.local.SongDao
import com.taigatkd.karamemo.data.local.toDomain
import com.taigatkd.karamemo.data.local.toEntity
import com.taigatkd.karamemo.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SongRepository {
    fun observeSongs(): Flow<List<Song>>
    suspend fun saveSong(song: Song)
    suspend fun deleteSong(songId: String)
    suspend fun deleteSongsByArtist(artist: String)
    suspend fun isDuplicateSong(artist: String, title: String, excludeSongId: String? = null): Boolean
    suspend fun getRandomSongs(count: Int): List<Song>
}

class SongRepositoryImpl(
    private val songDao: SongDao,
) : SongRepository {
    override fun observeSongs(): Flow<List<Song>> =
        songDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveSong(song: Song) {
        songDao.upsert(song.toEntity())
    }

    override suspend fun deleteSong(songId: String) {
        songDao.deleteById(songId)
    }

    override suspend fun deleteSongsByArtist(artist: String) {
        songDao.deleteByArtist(artist)
    }

    override suspend fun isDuplicateSong(
        artist: String,
        title: String,
        excludeSongId: String?,
    ): Boolean {
        val duplicateId = songDao.findDuplicateId(artist, title)
        return duplicateId != null && duplicateId != excludeSongId
    }

    override suspend fun getRandomSongs(count: Int): List<Song> =
        observeSongs().first().shuffled().take(count)
}

