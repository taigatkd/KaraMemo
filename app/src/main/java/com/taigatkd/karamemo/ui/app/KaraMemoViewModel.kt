package com.taigatkd.karamemo.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.common.StringResolver
import com.taigatkd.karamemo.data.repository.PlaylistRepository
import com.taigatkd.karamemo.data.repository.PreferencesRepository
import com.taigatkd.karamemo.data.repository.SongRepository
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.domain.model.SongSortType
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class KaraMemoViewModel(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val preferencesRepository: PreferencesRepository,
    private val stringResolver: StringResolver,
) : ViewModel() {
    private data class RepositorySnapshot(
        val songs: List<Song>,
        val playlists: List<Playlist>,
        val currentMachine: KaraokeMachine,
        val machineSettings: Map<KaraokeMachine, KaraokeMachineSettings>,
        val pinnedArtists: Set<String>,
        val pinnedPlaylists: Set<String>,
        val lastUsedArtist: String?,
    )

    private val searchQuery = MutableStateFlow("")
    private val sortType = MutableStateFlow(SongSortType.DATE_DESC)
    private val messages = MutableSharedFlow<String>()

    val snackbarMessages = messages.asSharedFlow()

    private val songsAndPlaylists = combine(
        songRepository.observeSongs(),
        playlistRepository.observePlaylists(),
    ) { songs, playlists ->
        songs to playlists
    }

    private val machineState = combine(
        preferencesRepository.currentMachine,
        preferencesRepository.machineSettings,
    ) { currentMachine, machineSettings ->
        currentMachine to machineSettings
    }

    private val preferenceState = combine(
        preferencesRepository.pinnedArtists,
        preferencesRepository.pinnedPlaylists,
        preferencesRepository.lastUsedArtist,
    ) { pinnedArtists, pinnedPlaylists, lastUsedArtist ->
        Triple(pinnedArtists, pinnedPlaylists, lastUsedArtist)
    }

    private val repositorySnapshot = combine(
        songsAndPlaylists,
        machineState,
        preferenceState,
    ) { songsAndPlaylists, machineState, preferenceState ->
        val (songs, playlists) = songsAndPlaylists
        val (currentMachine, machineSettings) = machineState
        val (pinnedArtists, pinnedPlaylists, lastUsedArtist) = preferenceState
        RepositorySnapshot(
            songs = songs,
            playlists = playlists,
            currentMachine = currentMachine,
            machineSettings = machineSettings,
            pinnedArtists = pinnedArtists,
            pinnedPlaylists = pinnedPlaylists,
            lastUsedArtist = lastUsedArtist,
        )
    }

    val uiState = combine(
        repositorySnapshot,
        searchQuery,
        sortType,
    ) { snapshot, query, sort ->
        KaraMemoUiState(
            songs = snapshot.songs,
            playlists = snapshot.playlists,
            currentMachine = snapshot.currentMachine,
            machineSettings = snapshot.machineSettings,
            pinnedArtists = snapshot.pinnedArtists,
            pinnedPlaylists = snapshot.pinnedPlaylists,
            lastUsedArtist = snapshot.lastUsedArtist,
            searchQuery = query,
            sortType = sort,
            isInitialized = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KaraMemoUiState(),
    )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSortType(value: SongSortType) {
        sortType.value = value
    }

    suspend fun saveSong(
        editingSongId: String?,
        artist: String,
        title: String,
        key: Int,
        memo: String,
        isFavorite: Boolean,
        playlistId: String?,
    ): Boolean {
        val normalizedArtist = artist.trim()
        val normalizedTitle = title.trim()
        val normalizedMemo = memo.trim()
        val normalizedPlaylistId = playlistId?.takeIf { it.isNotBlank() }

        if (normalizedArtist.isEmpty() || normalizedTitle.isEmpty()) {
            messages.emit(stringResolver.get(R.string.message_required_song))
            return false
        }

        if (songRepository.isDuplicateSong(normalizedArtist, normalizedTitle, editingSongId)) {
            messages.emit(stringResolver.get(R.string.message_duplicate_song))
            return false
        }

        val editingSong = editingSongId?.let { id ->
            uiState.value.songs.firstOrNull { song -> song.id == id }
        }

        val song = Song(
            id = editingSong?.id ?: UUID.randomUUID().toString(),
            artist = normalizedArtist,
            title = normalizedTitle,
            key = key,
            memo = normalizedMemo,
            isFavorite = isFavorite,
            playlistId = normalizedPlaylistId,
            createdAt = editingSong?.createdAt ?: Instant.now(),
        )

        songRepository.saveSong(song)
        preferencesRepository.setLastUsedArtist(normalizedArtist)
        messages.emit(
            stringResolver.get(
                if (editingSong == null) R.string.message_song_saved else R.string.message_song_updated,
            ),
        )
        return true
    }

    suspend fun deleteSong(songId: String) {
        songRepository.deleteSong(songId)
        messages.emit(stringResolver.get(R.string.message_song_deleted))
    }

    suspend fun toggleFavorite(song: Song) {
        songRepository.saveSong(song.copy(isFavorite = !song.isFavorite))
    }

    suspend fun deleteSongsByArtist(artist: String) {
        songRepository.deleteSongsByArtist(artist)
        preferencesRepository.removeArtistPin(artist)
        messages.emit(stringResolver.get(R.string.message_artist_deleted, artist))
    }

    suspend fun savePlaylist(name: String): Boolean {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) {
            messages.emit(stringResolver.get(R.string.message_required_playlist))
            return false
        }

        if (playlistRepository.isDuplicatePlaylist(normalizedName)) {
            messages.emit(stringResolver.get(R.string.message_duplicate_playlist))
            return false
        }

        playlistRepository.savePlaylist(
            Playlist(
                id = UUID.randomUUID().toString(),
                name = normalizedName,
                createdAt = Instant.now(),
            ),
        )
        messages.emit(stringResolver.get(R.string.message_playlist_created))
        return true
    }

    suspend fun deletePlaylist(playlistId: String) {
        uiState.value.songs
            .filter { song -> song.playlistId == playlistId }
            .forEach { song ->
                songRepository.saveSong(song.copy(playlistId = null))
            }
        playlistRepository.deletePlaylist(playlistId)
        preferencesRepository.removePlaylistPin(playlistId)
        messages.emit(stringResolver.get(R.string.message_playlist_deleted))
    }

    suspend fun updatePlaylistMembership(playlistId: String, selectedSongIds: Set<String>) {
        val songs = uiState.value.songs
        val targetSongs = songs.filter { song -> selectedSongIds.contains(song.id) }
        val songsToDetach = songs.filter { song ->
            song.playlistId == playlistId && !selectedSongIds.contains(song.id)
        }

        targetSongs.forEach { song ->
            if (song.playlistId != playlistId) {
                songRepository.saveSong(song.copy(playlistId = playlistId))
            }
        }

        songsToDetach.forEach { song ->
            songRepository.saveSong(song.copy(playlistId = null))
        }

        messages.emit(stringResolver.get(R.string.message_playlist_updated))
    }

    suspend fun toggleArtistPin(artistName: String) {
        preferencesRepository.toggleArtistPin(artistName)
    }

    suspend fun togglePlaylistPin(playlistId: String) {
        preferencesRepository.togglePlaylistPin(playlistId)
    }

    suspend fun setCurrentMachine(machine: KaraokeMachine) {
        preferencesRepository.setCurrentMachine(machine)
    }

    suspend fun updateMachineSettings(machine: KaraokeMachine, settings: KaraokeMachineSettings) {
        preferencesRepository.updateMachineSettings(machine, settings)
    }

    suspend fun getRandomSongs(count: Int = 5): List<Song> =
        songRepository.getRandomSongs(count)

    fun filteredSongs(source: List<Song> = uiState.value.songs): List<Song> {
        val query = uiState.value.searchQuery.trim().lowercase()
        val filtered = source.filter { song ->
            if (query.isBlank()) {
                true
            } else {
                song.artist.lowercase().contains(query) || song.title.lowercase().contains(query)
            }
        }

        return when (uiState.value.sortType) {
            SongSortType.DATE_DESC -> filtered.sortedByDescending { it.createdAt }
            SongSortType.DATE_ASC -> filtered.sortedBy { it.createdAt }
            SongSortType.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SongSortType.FAVORITE -> filtered.sortedWith(
                compareByDescending<Song> { it.isFavorite }.thenByDescending { it.createdAt },
            )
        }
    }
}
