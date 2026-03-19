package com.taigatkd.karamemo.ui.app

import com.taigatkd.karamemo.common.StringResolver
import com.taigatkd.karamemo.data.repository.PlaylistRepository
import com.taigatkd.karamemo.data.repository.PreferencesRepository
import com.taigatkd.karamemo.data.repository.SongRepository
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.domain.model.SongSortType
import com.taigatkd.karamemo.domain.model.defaultMachineSettings
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KaraMemoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun filteredSongs_sortsFavoritesFirst() = runTest {
        val songRepository = FakeSongRepository(
            listOf(
                song(id = "1", artist = "Aimer", title = "Ref:rain", isFavorite = false, createdAt = 1),
                song(id = "2", artist = "Ado", title = "Show", isFavorite = true, createdAt = 2),
                song(id = "3", artist = "YOASOBI", title = "Idol", isFavorite = false, createdAt = 3),
            ),
        )
        val viewModel = KaraMemoViewModel(
            songRepository = songRepository,
            playlistRepository = FakePlaylistRepository(),
            preferencesRepository = FakePreferencesRepository(),
            stringResolver = FakeStringResolver(),
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.setSearchQuery("")
        viewModel.setSortType(SongSortType.FAVORITE)
        advanceUntilIdle()

        val filtered = viewModel.filteredSongs(songRepository.currentSongs())

        assertEquals(listOf("Show", "Idol", "Ref:rain"), filtered.map { it.title })
        collector.cancel()
    }

    @Test
    fun saveSong_rejectsDuplicateIgnoringCase() = runTest {
        val songRepository = FakeSongRepository(
            listOf(song(id = "1", artist = "Aimer", title = "Ref:rain")),
        )
        val viewModel = KaraMemoViewModel(
            songRepository = songRepository,
            playlistRepository = FakePlaylistRepository(),
            preferencesRepository = FakePreferencesRepository(),
            stringResolver = FakeStringResolver(),
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val saved = viewModel.saveSong(
            editingSongId = null,
            artist = "aimer",
            title = "REF:RAIN",
            key = 0,
            memo = "",
            isFavorite = false,
            playlistId = null,
        )

        assertFalse(saved)
        assertEquals(1, songRepository.currentSongs().size)
        collector.cancel()
    }

    @Test
    fun deletePlaylist_detachesSongsAndClearsPin() = runTest {
        val playlistRepository = FakePlaylistRepository(
            listOf(playlist(id = "pl-1", name = "Favorites")),
        )
        val songRepository = FakeSongRepository(
            listOf(
                song(id = "1", artist = "Aimer", title = "Ref:rain", playlistId = "pl-1"),
                song(id = "2", artist = "Ado", title = "Show"),
            ),
        )
        val preferencesRepository = FakePreferencesRepository(
            pinnedPlaylists = setOf("pl-1"),
        )
        val viewModel = KaraMemoViewModel(
            songRepository = songRepository,
            playlistRepository = playlistRepository,
            preferencesRepository = preferencesRepository,
            stringResolver = FakeStringResolver(),
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.deletePlaylist("pl-1")
        advanceUntilIdle()

        assertTrue(playlistRepository.currentPlaylists().isEmpty())
        assertNull(songRepository.currentSongs().first { it.id == "1" }.playlistId)
        assertFalse(preferencesRepository.pinnedPlaylists.value.contains("pl-1"))
        collector.cancel()
    }

    private class FakeSongRepository(initialSongs: List<Song> = emptyList()) : SongRepository {
        private val songs = MutableStateFlow(initialSongs)

        override fun observeSongs(): Flow<List<Song>> = songs

        override suspend fun saveSong(song: Song) {
            songs.value = songs.value
                .filterNot { it.id == song.id }
                .plus(song)
                .sortedByDescending { it.createdAt }
        }

        override suspend fun deleteSong(songId: String) {
            songs.value = songs.value.filterNot { it.id == songId }
        }

        override suspend fun deleteSongsByArtist(artist: String) {
            songs.value = songs.value.filterNot { it.artist == artist }
        }

        override suspend fun isDuplicateSong(artist: String, title: String, excludeSongId: String?): Boolean =
            songs.value.any { song ->
                song.id != excludeSongId &&
                    song.artist.equals(artist, ignoreCase = true) &&
                    song.title.equals(title, ignoreCase = true)
            }

        override suspend fun getRandomSongs(count: Int): List<Song> = songs.value.take(count)

        fun currentSongs(): List<Song> = songs.value
    }

    private class FakePlaylistRepository(initialPlaylists: List<Playlist> = emptyList()) : PlaylistRepository {
        private val playlists = MutableStateFlow(initialPlaylists)

        override fun observePlaylists(): Flow<List<Playlist>> = playlists

        override suspend fun savePlaylist(playlist: Playlist) {
            playlists.value = playlists.value
                .filterNot { it.id == playlist.id }
                .plus(playlist)
                .sortedBy { it.name.lowercase() }
        }

        override suspend fun deletePlaylist(playlistId: String) {
            playlists.value = playlists.value.filterNot { it.id == playlistId }
        }

        override suspend fun isDuplicatePlaylist(name: String, excludePlaylistId: String?): Boolean =
            playlists.value.any { playlist ->
                playlist.id != excludePlaylistId && playlist.name.equals(name, ignoreCase = true)
            }

        fun currentPlaylists(): List<Playlist> = playlists.value
    }

    private class FakePreferencesRepository(
        pinnedArtists: Set<String> = emptySet(),
        pinnedPlaylists: Set<String> = emptySet(),
    ) : PreferencesRepository {
        override val currentMachine = MutableStateFlow(KaraokeMachine.DAM)
        override val machineSettings = MutableStateFlow(defaultMachineSettings())
        override val pinnedArtists = MutableStateFlow(pinnedArtists)
        override val pinnedPlaylists = MutableStateFlow(pinnedPlaylists)
        override val lastUsedArtist = MutableStateFlow<String?>(null)

        override suspend fun setCurrentMachine(machine: KaraokeMachine) {
            currentMachine.value = machine
        }

        override suspend fun updateMachineSettings(
            machine: KaraokeMachine,
            settings: KaraokeMachineSettings,
        ) {
            machineSettings.value = machineSettings.value + (machine to settings)
        }

        override suspend fun toggleArtistPin(artistName: String) {
            pinnedArtists.value = pinnedArtists.value.toMutableSet().apply {
                if (!add(artistName)) {
                    remove(artistName)
                }
            }
        }

        override suspend fun togglePlaylistPin(playlistId: String) {
            pinnedPlaylists.value = pinnedPlaylists.value.toMutableSet().apply {
                if (!add(playlistId)) {
                    remove(playlistId)
                }
            }
        }

        override suspend fun removeArtistPin(artistName: String) {
            pinnedArtists.value = pinnedArtists.value - artistName
        }

        override suspend fun removePlaylistPin(playlistId: String) {
            pinnedPlaylists.value = pinnedPlaylists.value - playlistId
        }

        override suspend fun setLastUsedArtist(artistName: String) {
            lastUsedArtist.value = artistName
        }
    }

    private class FakeStringResolver : StringResolver {
        override fun get(resId: Int, vararg formatArgs: Any): String = "message:$resId"
    }

    private fun song(
        id: String,
        artist: String,
        title: String,
        isFavorite: Boolean = false,
        playlistId: String? = null,
        createdAt: Long = 0,
    ) = Song(
        id = id,
        artist = artist,
        title = title,
        key = 0,
        memo = "",
        isFavorite = isFavorite,
        playlistId = playlistId,
        createdAt = Instant.ofEpochMilli(createdAt),
    )

    private fun playlist(
        id: String,
        name: String,
    ) = Playlist(
        id = id,
        name = name,
        createdAt = Instant.EPOCH,
    )
}
