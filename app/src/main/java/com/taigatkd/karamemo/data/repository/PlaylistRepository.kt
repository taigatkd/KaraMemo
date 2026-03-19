package com.taigatkd.karamemo.data.repository

import com.taigatkd.karamemo.data.local.PlaylistDao
import com.taigatkd.karamemo.data.local.toDomain
import com.taigatkd.karamemo.data.local.toEntity
import com.taigatkd.karamemo.domain.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PlaylistRepository {
    fun observePlaylists(): Flow<List<Playlist>>
    suspend fun savePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: String)
    suspend fun isDuplicatePlaylist(name: String, excludePlaylistId: String? = null): Boolean
}

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
) : PlaylistRepository {
    override fun observePlaylists(): Flow<List<Playlist>> =
        playlistDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun savePlaylist(playlist: Playlist) {
        playlistDao.upsert(playlist.toEntity())
    }

    override suspend fun deletePlaylist(playlistId: String) {
        playlistDao.deleteById(playlistId)
    }

    override suspend fun isDuplicatePlaylist(name: String, excludePlaylistId: String?): Boolean {
        val duplicateId = playlistDao.findDuplicateId(name)
        return duplicateId != null && duplicateId != excludePlaylistId
    }
}

