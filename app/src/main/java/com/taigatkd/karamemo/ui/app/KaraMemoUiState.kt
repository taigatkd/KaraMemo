package com.taigatkd.karamemo.ui.app

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
    val isInitialized: Boolean = false,
)

