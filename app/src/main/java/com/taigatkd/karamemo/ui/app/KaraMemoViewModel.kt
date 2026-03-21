package com.taigatkd.karamemo.ui.app

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.ads.InterstitialOpportunity
import com.taigatkd.karamemo.ads.MonetizationPolicy
import com.taigatkd.karamemo.ads.NaturalBreakPoint
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
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.domain.model.SongSortType
import com.taigatkd.karamemo.domain.model.defaultMachineSettings
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KaraMemoViewModel(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val preferencesRepository: PreferencesRepository,
    private val billingRepository: BillingRepository,
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
        val songSortType: SongSortType,
        val monetizationPreferences: MonetizationPreferences,
    )

    private val searchQuery = MutableStateFlow("")
    private val messages = MutableSharedFlow<String>()
    private val interstitialOpportunities = MutableSharedFlow<InterstitialOpportunity>(extraBufferCapacity = 1)
    private val upgradePromptRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var sessionInterstitialShownCount = 0
    private var interstitialRequestInFlight = false

    val snackbarMessages = messages.asSharedFlow()
    val interstitialRequests = interstitialOpportunities.asSharedFlow()
    val upgradePrompts = upgradePromptRequests.asSharedFlow()

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
        preferencesRepository.songSortType,
    ) { pinnedArtists, pinnedPlaylists, lastUsedArtist, songSortType ->
        PreferenceSnapshot(
            pinnedArtists = pinnedArtists,
            pinnedPlaylists = pinnedPlaylists,
            lastUsedArtist = lastUsedArtist,
            songSortType = songSortType,
        )
    }

    private val monetizationState = preferencesRepository.monetizationPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MonetizationPreferences(),
    )
    private val billingState = billingRepository.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BillingState(),
    )

    private val repositorySnapshot = combine(
        songsAndPlaylists,
        machineState,
        preferenceState,
        monetizationState,
    ) { songsAndPlaylists, machineState, preferenceState, monetizationPreferences ->
        val (songs, playlists) = songsAndPlaylists
        val (currentMachine, machineSettings) = machineState
        RepositorySnapshot(
            songs = songs,
            playlists = playlists,
            currentMachine = currentMachine,
            machineSettings = machineSettings,
            pinnedArtists = preferenceState.pinnedArtists,
            pinnedPlaylists = preferenceState.pinnedPlaylists,
            lastUsedArtist = preferenceState.lastUsedArtist,
            songSortType = preferenceState.songSortType,
            monetizationPreferences = monetizationPreferences,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RepositorySnapshot(
            songs = emptyList(),
            playlists = emptyList(),
            currentMachine = KaraokeMachine.DAM,
            machineSettings = defaultMachineSettings(),
            pinnedArtists = emptySet(),
            pinnedPlaylists = emptySet(),
            lastUsedArtist = null,
            songSortType = SongSortType.DATE_DESC,
            monetizationPreferences = MonetizationPreferences(),
        ),
    )

    val uiState = combine(
        repositorySnapshot,
        searchQuery,
        billingState,
    ) { snapshot, query, billing ->
        KaraMemoUiState(
            songs = snapshot.songs,
            playlists = snapshot.playlists,
            currentMachine = snapshot.currentMachine,
            machineSettings = snapshot.machineSettings,
            pinnedArtists = snapshot.pinnedArtists,
            pinnedPlaylists = snapshot.pinnedPlaylists,
            lastUsedArtist = snapshot.lastUsedArtist,
            searchQuery = query,
            sortType = snapshot.songSortType,
            isProEnabled = billing.isProEnabled,
            hasRealProPurchase = billing.isRealProEnabled,
            isMockProEnabled = billing.isMockProEnabled,
            canUseMockBilling = billing.canUseMockBilling,
            isBillingReady = billing.isBillingReady,
            isProductAvailable = billing.isProductAvailable,
            isPurchaseInProgress = billing.isPurchaseInProgress,
            proPriceLabel = billing.proPriceLabel,
            freeSongLimit = FREE_SONG_LIMIT,
            isInitialized = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KaraMemoUiState(),
    )

    init {
        viewModelScope.launch {
            billingRepository.events.collect { event ->
                handleBillingEvent(event)
            }
        }
        startBilling()
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSortType(value: SongSortType) {
        viewModelScope.launch {
            preferencesRepository.setSongSortType(value)
        }
    }

    suspend fun saveSong(
        editingSongId: String?,
        artist: String,
        title: String,
        key: Int,
        memo: String,
        isFavorite: Boolean,
        playlistId: String?,
        score: Double?,
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

        if (
            editingSongId == null &&
            !uiState.value.isProEnabled &&
            uiState.value.songs.size >= uiState.value.freeSongLimit
        ) {
            messages.emit(
                stringResolver.get(
                    R.string.message_song_limit_reached,
                    uiState.value.freeSongLimit,
                ),
            )
            upgradePromptRequests.tryEmit(Unit)
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
            score = score,
        )

        songRepository.saveSong(song)
        preferencesRepository.setLastUsedArtist(normalizedArtist)
        if (editingSong == null) {
            preferencesRepository.recordSongAdded()
        }
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

    fun onNaturalBreak(breakPoint: NaturalBreakPoint) {
        viewModelScope.launch {
            if (uiState.value.isProEnabled) return@launch
            if (interstitialRequestInFlight) return@launch

            val preferences = repositorySnapshot.value.monetizationPreferences
            if (
                !MonetizationPolicy.canRequestInterstitial(
                    preferences = preferences,
                    sessionInterstitialShownCount = sessionInterstitialShownCount,
                    nowEpochMillis = System.currentTimeMillis(),
                )
            ) {
                return@launch
            }

            interstitialRequestInFlight = true
            interstitialOpportunities.emit(
                InterstitialOpportunity(
                    breakPoint = breakPoint,
                    totalSongsAdded = preferences.totalSongsAdded,
                ),
            )
        }
    }

    fun onInterstitialShown() {
        viewModelScope.launch {
            interstitialRequestInFlight = false
            sessionInterstitialShownCount += 1
            preferencesRepository.recordInterstitialShown(System.currentTimeMillis())
        }
    }

    fun onInterstitialNotShown() {
        interstitialRequestInFlight = false
    }

    fun startBilling() {
        viewModelScope.launch {
            billingRepository.start()
        }
    }

    fun refreshBilling() {
        viewModelScope.launch {
            billingRepository.refresh()
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            billingRepository.restorePurchases()
        }
    }

    fun purchasePro(activity: Activity) {
        viewModelScope.launch {
            when (val result = billingRepository.launchProPurchase(activity)) {
                BillingLaunchResult.Launched -> Unit
                is BillingLaunchResult.Error -> handleBillingEvent(result.event)
            }
        }
    }

    fun setMockProEnabled(enabled: Boolean) {
        viewModelScope.launch {
            billingRepository.setMockProEnabled(enabled)
        }
    }

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

    private suspend fun handleBillingEvent(event: BillingEvent) {
        val messageRes = when (event) {
            BillingEvent.PRO_PURCHASED -> R.string.message_pro_purchase_success
            BillingEvent.PRO_RESTORED -> R.string.message_pro_purchase_restored
            BillingEvent.PRO_PENDING -> R.string.message_pro_purchase_pending
            BillingEvent.PRO_CANCELLED -> R.string.message_pro_purchase_cancelled
            BillingEvent.PRO_PURCHASE_FAILED -> R.string.message_pro_purchase_failed
            BillingEvent.RESTORE_NOT_FOUND -> R.string.message_pro_restore_not_found
            BillingEvent.BILLING_UNAVAILABLE -> R.string.message_billing_unavailable
            BillingEvent.PRODUCT_UNAVAILABLE -> R.string.message_pro_product_unavailable
            BillingEvent.MOCK_PRO_ENABLED -> R.string.message_mock_pro_enabled
            BillingEvent.MOCK_PRO_DISABLED -> R.string.message_mock_pro_disabled
        }
        messages.emit(stringResolver.get(messageRes))
    }

    private data class PreferenceSnapshot(
        val pinnedArtists: Set<String>,
        val pinnedPlaylists: Set<String>,
        val lastUsedArtist: String?,
        val songSortType: SongSortType,
    )
}
