package com.taigatkd.karamemo.ui.app

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.ads.AdMobManager
import com.taigatkd.karamemo.ads.NaturalBreakPoint
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.domain.model.SongSortType
import com.taigatkd.karamemo.ui.feature.artist.ArtistListScreen
import com.taigatkd.karamemo.ui.feature.playlist.PlaylistAddSheet
import com.taigatkd.karamemo.ui.feature.playlist.PlaylistListScreen
import com.taigatkd.karamemo.ui.feature.playlist.PlaylistSongPickerSheet
import com.taigatkd.karamemo.ui.feature.settings.KaraokeSettingsSheet
import com.taigatkd.karamemo.ui.feature.settings.SettingsPage
import com.taigatkd.karamemo.ui.feature.settings.SettingsBillingUi
import com.taigatkd.karamemo.ui.feature.settings.SettingsScreen
import com.taigatkd.karamemo.ui.feature.song.RandomSongsSheet
import com.taigatkd.karamemo.ui.feature.song.SongEditorRequest
import com.taigatkd.karamemo.ui.feature.song.SongEditorSheet
import com.taigatkd.karamemo.ui.feature.song.SongListScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaraMemoApp(
    viewModel: KaraMemoViewModel,
    adMobManager: AdMobManager,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    val activity = LocalContext.current.findActivity()
    val noSongsMessage = stringResource(R.string.snackbar_no_songs)
    val billingUnavailableMessage = stringResource(R.string.message_billing_unavailable)
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
    var showPlaylistAddSheet by remember { mutableStateOf(false) }
    var showKaraokeSettingsSheet by remember { mutableStateOf(false) }
    var settingsPage by remember { mutableStateOf<SettingsPage?>(null) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var randomSongs by remember { mutableStateOf<List<Song>?>(null) }
    var songEditorRequest by remember { mutableStateOf<SongEditorRequest?>(null) }
    var pickerPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var pendingBreakPoint by remember { mutableStateOf<NaturalBreakPoint?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.upgradePrompts.collect {
            showUpgradeDialog = true
        }
    }

    LaunchedEffect(pendingBreakPoint) {
        val breakPoint = pendingBreakPoint ?: return@LaunchedEffect
        viewModel.onNaturalBreak(breakPoint)
        pendingBreakPoint = null
    }

    LaunchedEffect(viewModel, adMobManager, activity) {
        val targetActivity = activity ?: return@LaunchedEffect
        viewModel.interstitialRequests.collect {
            val shown = adMobManager.showInterstitialIfAvailable(
                activity = targetActivity,
                onShown = viewModel::onInterstitialShown,
                onUnavailable = viewModel::onInterstitialNotShown,
            )
            if (!shown) {
                viewModel.onInterstitialNotShown()
            }
        }
    }

    LaunchedEffect(adMobManager) {
        withFrameNanos { }
        adMobManager.initialize()
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
                                AppTab.ARTISTS -> stringResource(R.string.title_artist_list_screen)
                                AppTab.SONGS -> stringResource(R.string.title_song_list_screen)
                                AppTab.PLAYLISTS -> stringResource(R.string.title_playlist_list_screen)
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
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    shadowElevation = 10.dp,
                    tonalElevation = 0.dp,
                ) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                            thickness = 1.dp,
                        )
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == AppTab.ARTISTS,
                                onClick = {
                                    if (selectedTab != AppTab.ARTISTS) {
                                        selectedTab = AppTab.ARTISTS
                                        viewModel.onNaturalBreak(NaturalBreakPoint.TAB_SWITCHED)
                                    }
                                },
                                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text(stringResource(R.string.tab_artists)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                            NavigationBarItem(
                                selected = selectedTab == AppTab.SONGS,
                                onClick = {
                                    if (selectedTab != AppTab.SONGS) {
                                        selectedTab = AppTab.SONGS
                                        viewModel.onNaturalBreak(NaturalBreakPoint.TAB_SWITCHED)
                                    }
                                },
                                icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                                label = { Text(stringResource(R.string.tab_songs)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                            NavigationBarItem(
                                selected = selectedTab == AppTab.PLAYLISTS,
                                onClick = {
                                    if (selectedTab != AppTab.PLAYLISTS) {
                                        selectedTab = AppTab.PLAYLISTS
                                        viewModel.onNaturalBreak(NaturalBreakPoint.TAB_SWITCHED)
                                    }
                                },
                                icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
                                label = { Text(stringResource(R.string.tab_playlists)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            when (selectedTab) {
                AppTab.ARTISTS -> ArtistListScreen(
                    modifier = Modifier.padding(innerPadding),
                    songs = uiState.songs,
                    showAds = !uiState.isProEnabled,
                    pinnedArtists = uiState.pinnedArtists,
                    onTogglePin = { artist ->
                        scope.launch { viewModel.toggleArtistPin(artist) }
                    },
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
                    showAds = !uiState.isProEnabled,
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
                    onOpenSettings = {
                        viewModel.startBilling()
                        settingsPage = SettingsPage.ROOT
                    },
                    onAddSong = {
                        songEditorRequest = SongEditorRequest()
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
                    showAds = !uiState.isProEnabled,
                    pinnedPlaylists = uiState.pinnedPlaylists,
                    onTogglePin = { playlistId ->
                        scope.launch { viewModel.togglePlaylistPin(playlistId) }
                    },
                    onAddPlaylist = { showPlaylistAddSheet = true },
                    onAddSongToPlaylist = { playlist ->
                        songEditorRequest = SongEditorRequest(initialPlaylistId = playlist.id)
                    },
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
                        val onSelect = {
                            viewModel.setSortType(type)
                            showSortDialog = false
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    role = Role.RadioButton,
                                    onClick = onSelect,
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = uiState.sortType == type,
                                onClick = onSelect,
                            )
                            Text(
                                text = stringResource(type.labelRes()),
                                modifier = Modifier.padding(start = 4.dp),
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
            onDismiss = {
                songEditorRequest = null
                pendingBreakPoint = NaturalBreakPoint.SONG_EDITOR_DISMISSED
            },
            onSave = { artist, title, key, memo, isFavorite, playlistId, score ->
                viewModel.saveSong(
                    editingSongId = request.song?.id,
                    artist = artist,
                    title = title,
                    key = key,
                    memo = memo,
                    isFavorite = isFavorite,
                    playlistId = playlistId,
                    score = score,
                )
            },
        )
    }

    if (showKaraokeSettingsSheet) {
        KaraokeSettingsSheet(
            currentMachine = uiState.currentMachine,
            settings = uiState.machineSettings,
            onDismiss = { showKaraokeSettingsSheet = false },
            onMachineSelected = { machine ->
                scope.launch { viewModel.setCurrentMachine(machine) }
            },
            onSettingsChanged = { machine, settings ->
                scope.launch { viewModel.updateMachineSettings(machine, settings) }
            },
        )
    }

    settingsPage?.let { page ->
        SettingsScreen(
            page = page,
            billingUi = SettingsBillingUi(
                isProEnabled = uiState.isProEnabled,
                hasRealProPurchase = uiState.hasRealProPurchase,
                isMockProEnabled = uiState.isMockProEnabled,
                canUseMockBilling = uiState.canUseMockBilling,
                isBillingReady = uiState.isBillingReady,
                isProductAvailable = uiState.isProductAvailable,
                isPurchaseInProgress = uiState.isPurchaseInProgress,
                proPriceLabel = uiState.proPriceLabel,
                currentSongCount = uiState.songs.size,
                freeSongLimit = uiState.freeSongLimit,
            ),
            onDismiss = { settingsPage = null },
            onOpenKaraokeSettings = { showKaraokeSettingsSheet = true },
            onOpenPage = { nextPage -> settingsPage = nextPage },
            onBuyPro = {
                val targetActivity = activity
                if (targetActivity == null) {
                    scope.launch { snackbarHostState.showSnackbar(billingUnavailableMessage) }
                } else {
                    viewModel.purchasePro(targetActivity)
                }
            },
            onRestorePurchases = viewModel::restorePurchases,
            onSetMockProEnabled = viewModel::setMockProEnabled,
        )
    }

    randomSongs?.let { songs ->
        RandomSongsSheet(
            songs = songs,
            onEditSong = { song ->
                randomSongs = null
                songEditorRequest = SongEditorRequest(song = song)
            },
            onDismiss = { randomSongs = null },
        )
    }

    pickerPlaylist?.let { playlist ->
        PlaylistSongPickerSheet(
            playlist = playlist,
            songs = uiState.songs,
            onDismiss = {
                pickerPlaylist = null
                pendingBreakPoint = NaturalBreakPoint.PLAYLIST_PICKER_DISMISSED
            },
            onSave = { selectedSongIds ->
                scope.launch {
                    viewModel.updatePlaylistMembership(playlist.id, selectedSongIds)
                    pickerPlaylist = null
                    pendingBreakPoint = NaturalBreakPoint.PLAYLIST_PICKER_DISMISSED
                }
            },
        )
    }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = { Text(stringResource(R.string.title_song_limit_reached)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.message_song_limit_dialog,
                        uiState.freeSongLimit,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUpgradeDialog = false
                        settingsPage = SettingsPage.PRO
                    },
                ) {
                    Text(stringResource(R.string.action_open_pro))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text(stringResource(R.string.action_later))
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
        SongSortType.SCORE_DESC -> R.string.sort_score_highest_first
    }

private fun android.content.Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
