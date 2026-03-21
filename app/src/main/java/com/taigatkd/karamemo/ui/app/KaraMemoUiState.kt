package com.taigatkd.karamemo.ui.app

import com.taigatkd.karamemo.billing.FREE_SONG_LIMIT
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.domain.model.SongSortType
import com.taigatkd.karamemo.domain.model.defaultMachineSettings

data class KaraMemoUiState(
    val songs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val currentMachine: KaraokeMachine = KaraokeMachine.DAM,
    val machineSettings: Map<KaraokeMachine, KaraokeMachineSettings> = defaultMachineSettings(),
    val pinnedArtists: Set<String> = emptySet(),
    val pinnedPlaylists: Set<String> = emptySet(),
    val lastUsedArtist: String? = null,
    val searchQuery: String = "",
    val sortType: SongSortType = SongSortType.DATE_DESC,
    val isProEnabled: Boolean = false,
    val hasRealProPurchase: Boolean = false,
    val isMockProEnabled: Boolean = false,
    val canUseMockBilling: Boolean = false,
    val isBillingReady: Boolean = false,
    val isProductAvailable: Boolean = false,
    val isPurchaseInProgress: Boolean = false,
    val proPriceLabel: String? = null,
    val freeSongLimit: Int = FREE_SONG_LIMIT,
    val isInitialized: Boolean = false,
) {
    val remainingFreeSongs: Int
        get() = (freeSongLimit - songs.size).coerceAtLeast(0)
}
