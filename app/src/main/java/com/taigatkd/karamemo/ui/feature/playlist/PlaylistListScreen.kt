package com.taigatkd.karamemo.ui.feature.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.AdBanner
import com.taigatkd.karamemo.ui.components.KaraMemoActionIconButton
import com.taigatkd.karamemo.ui.feature.song.SongItemRow
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaylistListScreen(
    playlists: List<Playlist>,
    songs: List<Song>,
    pinnedPlaylists: Set<String>,
    onTogglePin: (String) -> Unit,
    onAddPlaylist: () -> Unit,
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
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = stringResource(R.string.label_song_count, playlistSongs.size),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                        iconTint = MaterialTheme.colorScheme.primary,
                                    )
                                    ElevatedAssistChip(
                                        onClick = { onOpenPicker(playlist) },
                                        label = { Text(stringResource(R.string.action_manage_songs)) },
                                        leadingIcon = {
                                            Icon(Icons.AutoMirrored.Filled.PlaylistAddCheck, contentDescription = null)
                                        },
                                    )
                                    KaraMemoActionIconButton(
                                        onClick = { expanded[playlist.id] = !isExpanded },
                                        contentDescription = if (isExpanded) {
                                            stringResource(R.string.action_hide_songs)
                                        } else {
                                            stringResource(R.string.action_show_songs)
                                        },
                                        imageVector = if (isExpanded) {
                                            Icons.Default.ExpandLess
                                        } else {
                                            Icons.Default.ExpandMore
                                        },
                                    )
                                    KaraMemoActionIconButton(
                                        onClick = { deleteTarget = playlist },
                                        contentDescription = stringResource(R.string.action_delete),
                                        imageVector = Icons.Default.Delete,
                                        iconTint = MaterialTheme.colorScheme.error,
                                    )
                                }

                                if (isExpanded) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (playlistSongs.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.empty_playlist_detail),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        } else {
                                            playlistSongs.forEach { song ->
                                                SongItemRow(
                                                    song = song,
                                                    playlistName = playlist.name,
                                                    onEdit = onEditSong,
                                                    onDelete = onDeleteSong,
                                                    onToggleFavorite = onToggleFavorite,
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

            AdBanner()
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
            pinnedPlaylists = PreviewFixtures.pinnedPlaylists,
            onTogglePin = {},
            onAddPlaylist = {},
            onOpenPicker = {},
            onEditSong = {},
            onDeleteSong = {},
            onDeletePlaylist = {},
            onToggleFavorite = {},
        )
    }
}
