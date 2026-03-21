package com.taigatkd.karamemo.ui.feature.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.AdBanner
import com.taigatkd.karamemo.ui.components.KaraMemoActionIconButton
import com.taigatkd.karamemo.ui.components.KaraMemoSwipeToDeleteContainer
import com.taigatkd.karamemo.ui.feature.song.SongItemRow
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

@Composable
fun PlaylistListScreen(
    playlists: List<Playlist>,
    songs: List<Song>,
    showAds: Boolean,
    pinnedPlaylists: Set<String>,
    onTogglePin: (String) -> Unit,
    onAddPlaylist: () -> Unit,
    onAddSongToPlaylist: (Playlist) -> Unit,
    onOpenPicker: (Playlist) -> Unit,
    onEditSong: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    var deleteTarget by remember { mutableStateOf<Playlist?>(null) }
    val sortedPlaylists = playlists.sortedWith(
        compareBy<Playlist> { !pinnedPlaylists.contains(it.id) }.thenBy { it.name.lowercase() },
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (sortedPlaylists.isEmpty()) {
                EmptyPlaylistState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(sortedPlaylists, key = { it.id }) { playlist ->
                        val playlistSongs = songs.filter { it.playlistId == playlist.id }
                        val isExpanded = expanded[playlist.id] == true
                        KaraMemoSwipeToDeleteContainer(
                            onDeleteRequested = { deleteTarget = playlist },
                        ) {
                            Card(
                                onClick = {
                                    expanded[playlist.id] = !isExpanded
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(
                                                text = playlist.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = stringResource(R.string.label_song_count, playlistSongs.size),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            KaraMemoActionIconButton(
                                                onClick = { onTogglePin(playlist.id) },
                                                contentDescription = if (pinnedPlaylists.contains(playlist.id)) {
                                                    stringResource(R.string.action_unpin)
                                                } else {
                                                    stringResource(R.string.action_pin)
                                                },
                                                imageVector = if (pinnedPlaylists.contains(playlist.id)) {
                                                    Icons.Default.PushPin
                                                } else {
                                                    Icons.Outlined.PushPin
                                                },
                                                size = 40.dp,
                                                iconTint = MaterialTheme.colorScheme.primary,
                                            )
                                            KaraMemoActionIconButton(
                                                onClick = { onAddSongToPlaylist(playlist) },
                                                contentDescription = stringResource(R.string.action_add_song_to_playlist),
                                                imageVector = Icons.Default.Add,
                                                size = 40.dp,
                                            )
                                            KaraMemoActionIconButton(
                                                onClick = { onOpenPicker(playlist) },
                                                contentDescription = stringResource(R.string.action_select_songs),
                                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                                size = 40.dp,
                                            )
                                        }
                                    }

                                    if (isExpanded) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (playlistSongs.isEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.empty_playlist_detail),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            } else {
                                                playlistSongs
                                                    .groupBy { it.artist }
                                                    .toSortedMap(String.CASE_INSENSITIVE_ORDER)
                                                    .forEach { (artist, artistSongs) ->
                                                        Column(
                                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                                        ) {
                                                            Text(
                                                                text = artist,
                                                                style = MaterialTheme.typography.labelLarge,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                                                            )

                                                            artistSongs
                                                                .sortedBy { it.title.lowercase() }
                                                                .forEach { song ->
                                                                    SongItemRow(
                                                                        song = song,
                                                                        onEdit = onEditSong,
                                                                        onDelete = onDeleteSong,
                                                                        onToggleFavorite = onToggleFavorite,
                                                                        compactMetadata = true,
                                                                        showArtistInfo = false,
                                                                    )
                                                                }
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdBanner(showAd = showAds)
        }

        FloatingActionButton(
            onClick = onAddPlaylist,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.action_add_playlist),
            )
        }
    }

    deleteTarget?.let { playlist ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.title_delete_playlist)) },
            text = { Text(stringResource(R.string.message_delete_playlist, playlist.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTarget = null
                        onDeletePlaylist(playlist)
                    },
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun EmptyPlaylistState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.empty_playlists),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun PlaylistListScreenPreview() {
    KaraMemoTheme {
        PlaylistListScreen(
            playlists = PreviewFixtures.playlists,
            songs = PreviewFixtures.songs,
            showAds = true,
            pinnedPlaylists = PreviewFixtures.pinnedPlaylists,
            onTogglePin = {},
            onAddPlaylist = {},
            onAddSongToPlaylist = {},
            onOpenPicker = {},
            onEditSong = {},
            onDeleteSong = {},
            onDeletePlaylist = {},
            onToggleFavorite = {},
        )
    }
}
