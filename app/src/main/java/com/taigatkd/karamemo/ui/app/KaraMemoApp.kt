package com.taigatkd.karamemo.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taigatkd.karamemo.R
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
    val colorScheme = MaterialTheme.colorScheme
    val noSongsMessage = stringResource(R.string.snackbar_no_songs)
    val playlistNamesById = remember(uiState.playlists) {
        uiState.playlists.associate { playlist -> playlist.id to playlist.name }
    }
    val backgroundBrush = remember(colorScheme) {
        Brush.verticalGradient(
            listOf(
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.72f),
                colorScheme.background,
            ),
        )
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                AppTab.ARTISTS -> stringResource(R.string.tab_artists)
                                AppTab.SONGS -> stringResource(R.string.tab_songs)
                                AppTab.PLAYLISTS -> stringResource(R.string.tab_playlists)
                            },
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    tonalElevation = 0.dp,
                ) {
                    NavigationBarItem(
                        selected = selectedTab == AppTab.ARTISTS,
                        onClick = { selectedTab = AppTab.ARTISTS },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_artists)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                    NavigationBarItem(
                        selected = selectedTab == AppTab.SONGS,
                        onClick = { selectedTab = AppTab.SONGS },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_songs)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                    NavigationBarItem(
                        selected = selectedTab == AppTab.PLAYLISTS,
                        onClick = { selectedTab = AppTab.PLAYLISTS },
                        icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_playlists)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
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
                    onToggleSearch = { showSearchBar = !showSearchBar },
                    onOpenSort = { showSortDialog = true },
                    onOpenRandom = {
                        scope.launch {
                            val songs = viewModel.getRandomSongs(5)
                            if (songs.isEmpty()) {
                                snackbarHostState.showSnackbar(noSongsMessage)
                            } else {
                                randomSongs = songs
                            }
                        }
                    },
                    onOpenSettings = { showSettingsSheet = true },
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
    }

    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text(stringResource(R.string.title_sort_songs)) },
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
                                text = stringResource(type.labelRes()),
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text(stringResource(R.string.action_close))
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
            playlistNamesById = playlistNamesById,
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

private fun SongSortType.labelRes(): Int =
    when (this) {
        SongSortType.DATE_DESC -> R.string.sort_newest_first
        SongSortType.DATE_ASC -> R.string.sort_oldest_first
        SongSortType.TITLE_ASC -> R.string.sort_title_order
        SongSortType.FAVORITE -> R.string.sort_favorites_first
    }
