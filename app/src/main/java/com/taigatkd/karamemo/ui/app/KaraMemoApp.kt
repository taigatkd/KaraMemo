package com.taigatkd.karamemo.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.domain.model.SongSortType
import com.taigatkd.karamemo.ui.feature.artist.ArtistAddSheet
import com.taigatkd.karamemo.ui.feature.artist.ArtistListScreen
import com.taigatkd.karamemo.ui.feature.playlist.PlaylistAddSheet
import com.taigatkd.karamemo.ui.feature.playlist.PlaylistListScreen
import com.taigatkd.karamemo.ui.feature.playlist.PlaylistSongPickerSheet
import com.taigatkd.karamemo.ui.feature.settings.KaraokeSettingsSheet
import com.taigatkd.karamemo.ui.feature.song.RandomSongsSheet
import com.taigatkd.karamemo.ui.feature.song.SongEditorRequest
import com.taigatkd.karamemo.ui.feature.song.SongEditorSheet
import com.taigatkd.karamemo.ui.feature.song.SongListScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaraMemoApp(
    viewModel: KaraMemoViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val playlistNamesById = remember(uiState.playlists) {
        uiState.playlists.associate { playlist -> playlist.id to playlist.name }
    }

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.SONGS) }
    var showSearchBar by rememberSaveable { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showArtistAddSheet by remember { mutableStateOf(false) }
    var showPlaylistAddSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var randomSongs by remember { mutableStateOf<List<Song>?>(null) }
    var songEditorRequest by remember { mutableStateOf<SongEditorRequest?>(null) }
    var pickerPlaylist by remember { mutableStateOf<Playlist?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            AppTab.ARTISTS -> "Artists"
                            AppTab.SONGS -> "Songs"
                            AppTab.PLAYLISTS -> "Playlists"
                        },
                    )
                },
                actions = {
                    if (selectedTab == AppTab.SONGS) {
                        IconButton(onClick = { showSettingsSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.SettingsInputComponent,
                                contentDescription = "Open karaoke settings",
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val songs = viewModel.getRandomSongs(5)
                                    if (songs.isEmpty()) {
                                        snackbarHostState.showSnackbar("No songs have been added yet.")
                                    } else {
                                        randomSongs = songs
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Pick random songs",
                            )
                        }
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Change sort order",
                            )
                        }
                        IconButton(onClick = { showSearchBar = !showSearchBar }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Toggle search",
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == AppTab.ARTISTS,
                    onClick = { selectedTab = AppTab.ARTISTS },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Artists") },
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.SONGS,
                    onClick = { selectedTab = AppTab.SONGS },
                    icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                    label = { Text("Songs") },
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.PLAYLISTS,
                    onClick = { selectedTab = AppTab.PLAYLISTS },
                    icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
                    label = { Text("Playlists") },
                )
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            AppTab.ARTISTS -> ArtistListScreen(
                modifier = Modifier.padding(innerPadding),
                songs = uiState.songs,
                playlistNamesById = playlistNamesById,
                pinnedArtists = uiState.pinnedArtists,
                onTogglePin = { artist ->
                    scope.launch { viewModel.toggleArtistPin(artist) }
                },
                onAddArtist = { showArtistAddSheet = true },
                onAddSongForArtist = { artist ->
                    songEditorRequest = SongEditorRequest(initialArtist = artist)
                },
                onEditSong = { song -> songEditorRequest = SongEditorRequest(song = song) },
                onDeleteSong = { song ->
                    scope.launch { viewModel.deleteSong(song.id) }
                },
                onToggleFavorite = { song ->
                    scope.launch { viewModel.toggleFavorite(song) }
                },
                onDeleteArtist = { artist ->
                    scope.launch { viewModel.deleteSongsByArtist(artist) }
                },
            )

            AppTab.SONGS -> SongListScreen(
                modifier = Modifier.padding(innerPadding),
                songs = viewModel.filteredSongs(),
                playlistNamesById = playlistNamesById,
                query = uiState.searchQuery,
                showSearchBar = showSearchBar,
                onQueryChange = viewModel::setSearchQuery,
                onAddSong = {
                    songEditorRequest = SongEditorRequest(initialArtist = uiState.lastUsedArtist)
                },
                onEditSong = { song -> songEditorRequest = SongEditorRequest(song = song) },
                onDeleteSong = { song ->
                    scope.launch { viewModel.deleteSong(song.id) }
                },
                onToggleFavorite = { song ->
                    scope.launch { viewModel.toggleFavorite(song) }
                },
            )

            AppTab.PLAYLISTS -> PlaylistListScreen(
                modifier = Modifier.padding(innerPadding),
                playlists = uiState.playlists,
                songs = uiState.songs,
                pinnedPlaylists = uiState.pinnedPlaylists,
                onTogglePin = { playlistId ->
                    scope.launch { viewModel.togglePlaylistPin(playlistId) }
                },
                onAddPlaylist = { showPlaylistAddSheet = true },
                onOpenPicker = { playlist -> pickerPlaylist = playlist },
                onEditSong = { song -> songEditorRequest = SongEditorRequest(song = song) },
                onDeleteSong = { song ->
                    scope.launch { viewModel.deleteSong(song.id) }
                },
                onDeletePlaylist = { playlist ->
                    scope.launch { viewModel.deletePlaylist(playlist.id) }
                },
                onToggleFavorite = { song ->
                    scope.launch { viewModel.toggleFavorite(song) }
                },
            )
        }
    }

    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort songs") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SongSortType.entries.forEach { type ->
                        Row {
                            RadioButton(
                                selected = uiState.sortType == type,
                                onClick = {
                                    viewModel.setSortType(type)
                                    showSortDialog = false
                                },
                            )
                            Text(
                                text = type.label(),
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Close")
                }
            },
        )
    }

    if (showArtistAddSheet) {
        ArtistAddSheet(
            onDismiss = { showArtistAddSheet = false },
            onNext = { artistName ->
                showArtistAddSheet = false
                songEditorRequest = SongEditorRequest(initialArtist = artistName)
            },
        )
    }

    if (showPlaylistAddSheet) {
        PlaylistAddSheet(
            onDismiss = { showPlaylistAddSheet = false },
            onSave = { name ->
                scope.launch {
                    if (viewModel.savePlaylist(name)) {
                        showPlaylistAddSheet = false
                    }
                }
            },
        )
    }

    songEditorRequest?.let { request ->
        SongEditorSheet(
            request = request,
            playlists = uiState.playlists,
            lastUsedArtist = uiState.lastUsedArtist,
            onDismiss = { songEditorRequest = null },
            onSave = { artist, title, key, memo, isFavorite, playlistId ->
                viewModel.saveSong(
                    editingSongId = request.song?.id,
                    artist = artist,
                    title = title,
                    key = key,
                    memo = memo,
                    isFavorite = isFavorite,
                    playlistId = playlistId,
                )
            },
        )
    }

    if (showSettingsSheet) {
        KaraokeSettingsSheet(
            currentMachine = uiState.currentMachine,
            settings = uiState.machineSettings,
            onDismiss = { showSettingsSheet = false },
            onMachineSelected = { machine ->
                scope.launch { viewModel.setCurrentMachine(machine) }
            },
            onSettingsChanged = { machine, settings ->
                scope.launch { viewModel.updateMachineSettings(machine, settings) }
            },
        )
    }

    randomSongs?.let { songs ->
        RandomSongsSheet(
            songs = songs,
            onDismiss = { randomSongs = null },
        )
    }

    pickerPlaylist?.let { playlist ->
        PlaylistSongPickerSheet(
            playlist = playlist,
            songs = uiState.songs,
            onDismiss = { pickerPlaylist = null },
            onSave = { selectedSongIds ->
                scope.launch {
                    viewModel.updatePlaylistMembership(playlist.id, selectedSongIds)
                    pickerPlaylist = null
                }
            },
        )
    }
}

private fun SongSortType.label(): String =
    when (this) {
        SongSortType.DATE_DESC -> "Newest first"
        SongSortType.DATE_ASC -> "Oldest first"
        SongSortType.TITLE_ASC -> "Title order"
        SongSortType.FAVORITE -> "Favorites first"
    }
