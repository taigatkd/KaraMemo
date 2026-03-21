package com.taigatkd.karamemo.ui.app

import android.app.Activity
import com.taigatkd.karamemo.billing.BillingEvent
import com.taigatkd.karamemo.billing.BillingLaunchResult
import com.taigatkd.karamemo.billing.BillingRepository
import com.taigatkd.karamemo.billing.BillingState
import com.taigatkd.karamemo.billing.FREE_SONG_LIMIT
import com.taigatkd.karamemo.common.StringResolver
import com.taigatkd.karamemo.data.repository.MonetizationPreferences
import com.taigatkd.karamemo.data.repository.PlaylistRepository
import com.taigatkd.karamemo.data.repository.PreferencesRepository
import com.taigatkd.karamemo.data.repository.SongRepository
import com.taigatkd.karamemo.ads.NaturalBreakPoint
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.domain.model.SongSortType
import com.taigatkd.karamemo.domain.model.defaultMachineSettings
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
            billingRepository = FakeBillingRepository(),
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
            billingRepository = FakeBillingRepository(),
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
            score = null,
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
            billingRepository = FakeBillingRepository(),
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

    @Test
    fun saveSong_marksInterstitialPendingOnTenthNewSong() = runTest {
        val preferencesRepository = FakePreferencesRepository(
            monetizationPreferences = MonetizationPreferences(totalSongsAdded = 9),
        )
        val viewModel = KaraMemoViewModel(
            songRepository = FakeSongRepository(),
            playlistRepository = FakePlaylistRepository(),
            preferencesRepository = preferencesRepository,
            billingRepository = FakeBillingRepository(),
            stringResolver = FakeStringResolver(),
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val saved = viewModel.saveSong(
            editingSongId = null,
            artist = "Aimer",
            title = "Ref:rain",
            key = 0,
            memo = "",
            isFavorite = false,
            playlistId = null,
            score = null,
        )
        advanceUntilIdle()

        assertTrue(saved)
        assertEquals(10, preferencesRepository.monetizationPreferences.value.totalSongsAdded)
        assertTrue(preferencesRepository.monetizationPreferences.value.pendingInterstitial)
        collector.cancel()
    }

    @Test
    fun onNaturalBreak_emitsInterstitialAndClearsPendingAfterShown() = runTest {
        val preferencesRepository = FakePreferencesRepository(
            monetizationPreferences = MonetizationPreferences(
                totalSongsAdded = 10,
                pendingInterstitial = true,
            ),
        )
        val viewModel = KaraMemoViewModel(
            songRepository = FakeSongRepository(),
            playlistRepository = FakePlaylistRepository(),
            preferencesRepository = preferencesRepository,
            billingRepository = FakeBillingRepository(),
            stringResolver = FakeStringResolver(),
        )
        val received = mutableListOf<NaturalBreakPoint>()

        val stateCollector = launch { viewModel.uiState.collect {} }
        val requestCollector = launch {
            viewModel.interstitialRequests.collect { request ->
                received += request.breakPoint
            }
        }
        advanceUntilIdle()

        viewModel.onNaturalBreak(NaturalBreakPoint.SONG_EDITOR_DISMISSED)
        advanceUntilIdle()
        viewModel.onInterstitialShown()
        advanceUntilIdle()

        assertEquals(listOf(NaturalBreakPoint.SONG_EDITOR_DISMISSED), received)
        assertFalse(preferencesRepository.monetizationPreferences.value.pendingInterstitial)
        assertTrue(
            (preferencesRepository.monetizationPreferences.value.lastInterstitialShownAtEpochMillis ?: 0L) > 0L,
        )
        requestCollector.cancel()
        stateCollector.cancel()
    }

    @Test
    fun uiState_usesPersistedSongSortType() = runTest {
        val preferencesRepository = FakePreferencesRepository(
            songSortType = SongSortType.TITLE_ASC,
        )
        val viewModel = KaraMemoViewModel(
            songRepository = FakeSongRepository(),
            playlistRepository = FakePlaylistRepository(),
            preferencesRepository = preferencesRepository,
            billingRepository = FakeBillingRepository(),
            stringResolver = FakeStringResolver(),
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(SongSortType.TITLE_ASC, viewModel.uiState.value.sortType)
        collector.cancel()
    }

    @Test
    fun saveSong_blocksNewSongWhenFreeLimitReached() = runTest {
        val songs = (1..FREE_SONG_LIMIT).map { index ->
            song(
                id = index.toString(),
                artist = "Artist $index",
                title = "Song $index",
                createdAt = index.toLong(),
            )
        }
        val viewModel = KaraMemoViewModel(
            songRepository = FakeSongRepository(songs),
            playlistRepository = FakePlaylistRepository(),
            preferencesRepository = FakePreferencesRepository(),
            billingRepository = FakeBillingRepository(),
            stringResolver = FakeStringResolver(),
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val saved = viewModel.saveSong(
            editingSongId = null,
            artist = "New Artist",
            title = "New Song",
            key = 0,
            memo = "",
            isFavorite = false,
            playlistId = null,
            score = null,
        )

        assertFalse(saved)
        collector.cancel()
    }

    @Test
    fun saveSong_allowsNewSongWhenProIsEnabled() = runTest {
        val songs = (1..FREE_SONG_LIMIT).map { index ->
            song(
                id = index.toString(),
                artist = "Artist $index",
                title = "Song $index",
                createdAt = index.toLong(),
            )
        }
        val billingRepository = FakeBillingRepository(
            state = BillingState(isRealProEnabled = true),
        )
        val songRepository = FakeSongRepository(songs)
        val viewModel = KaraMemoViewModel(
            songRepository = songRepository,
            playlistRepository = FakePlaylistRepository(),
            preferencesRepository = FakePreferencesRepository(),
            billingRepository = billingRepository,
            stringResolver = FakeStringResolver(),
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val saved = viewModel.saveSong(
            editingSongId = null,
            artist = "New Artist",
            title = "New Song",
            key = 0,
            memo = "",
            isFavorite = false,
            playlistId = null,
            score = null,
        )
        advanceUntilIdle()

        assertTrue(saved)
        assertEquals(FREE_SONG_LIMIT + 1, songRepository.currentSongs().size)
        collector.cancel()
    }

    @Test
    fun saveSong_persistsSingleScore() = runTest {
        val songRepository = FakeSongRepository()
        val viewModel = KaraMemoViewModel(
            songRepository = songRepository,
            playlistRepository = FakePlaylistRepository(),
            preferencesRepository = FakePreferencesRepository(),
            billingRepository = FakeBillingRepository(),
            stringResolver = FakeStringResolver(),
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val saved = viewModel.saveSong(
            editingSongId = null,
            artist = "Aimer",
            title = "Ref:rain",
            key = 0,
            memo = "",
            isFavorite = false,
            playlistId = null,
            score = 95.1,
        )
        advanceUntilIdle()

        assertTrue(saved)
        assertEquals(95.1, songRepository.currentSongs().first().score ?: 0.0, 0.0001)
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
        songSortType: SongSortType = SongSortType.DATE_DESC,
        monetizationPreferences: MonetizationPreferences = MonetizationPreferences(),
    ) : PreferencesRepository {
        override val currentMachine = MutableStateFlow(KaraokeMachine.DAM)
        override val machineSettings = MutableStateFlow(defaultMachineSettings())
        override val pinnedArtists = MutableStateFlow(pinnedArtists)
        override val pinnedPlaylists = MutableStateFlow(pinnedPlaylists)
        override val lastUsedArtist = MutableStateFlow<String?>(null)
        override val songSortType = MutableStateFlow(songSortType)
        override val monetizationPreferences = MutableStateFlow(monetizationPreferences)

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

        override suspend fun setSongSortType(sortType: SongSortType) {
            songSortType.value = sortType
        }

        override suspend fun setCachedRealProEnabled(enabled: Boolean) {
            monetizationPreferences.value = monetizationPreferences.value.copy(
                cachedRealProEnabled = enabled,
            )
        }

        override suspend fun setMockProEnabled(enabled: Boolean) {
            monetizationPreferences.value = monetizationPreferences.value.copy(
                mockProEnabled = enabled,
            )
        }

        override suspend fun recordSongAdded(): MonetizationPreferences {
            val current = monetizationPreferences.value
            val nextTotal = current.totalSongsAdded + 1
            val updated = current.copy(
                totalSongsAdded = nextTotal,
                pendingInterstitial = current.pendingInterstitial || nextTotal % 10 == 0,
            )
            monetizationPreferences.value = updated
            return updated
        }

        override suspend fun recordInterstitialShown(shownAtEpochMillis: Long) {
            monetizationPreferences.value = monetizationPreferences.value.copy(
                pendingInterstitial = false,
                lastInterstitialShownAtEpochMillis = shownAtEpochMillis,
            )
        }
    }

    private class FakeBillingRepository(
        state: BillingState = BillingState(),
    ) : BillingRepository {
        override val state = MutableStateFlow(state)
        override val events = MutableSharedFlow<BillingEvent>()

        override suspend fun start() = Unit

        override suspend fun refresh() = Unit

        override suspend fun restorePurchases() = Unit

        override suspend fun launchProPurchase(activity: Activity): BillingLaunchResult =
            BillingLaunchResult.Launched

        override suspend fun setMockProEnabled(enabled: Boolean) {
            this.state.value = this.state.value.copy(isMockProEnabled = enabled)
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
        score: Double? = null,
    ) = Song(
        id = id,
        artist = artist,
        title = title,
        key = 0,
        memo = "",
        isFavorite = isFavorite,
        playlistId = playlistId,
        createdAt = Instant.ofEpochMilli(createdAt),
        score = score,
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
