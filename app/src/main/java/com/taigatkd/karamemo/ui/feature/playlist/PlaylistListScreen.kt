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
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.AdBanner
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.feature.song.SongItemRow
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

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
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${playlistSongs.size} songs",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    IconButton(onClick = { onTogglePin(playlist.id) }) {
                                        Icon(
                                            imageVector = if (pinnedPlaylists.contains(playlist.id)) {
                                                Icons.Default.PushPin
                                            } else {
                                                Icons.Outlined.PushPin
                                            },
                                            contentDescription = "Toggle pin",
                                        )
                                    }
                                    IconButton(onClick = { onOpenPicker(playlist) }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                            contentDescription = "Edit playlist songs",
                                        )
                                    }
                                    IconButton(onClick = { deleteTarget = playlist }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete playlist")
                                    }
                                    IconButton(onClick = { expanded[playlist.id] = !(expanded[playlist.id] ?: false) }) {
                                        Icon(
                                            imageVector = if (expanded[playlist.id] == true) {
                                                Icons.Default.ExpandLess
                                            } else {
                                                Icons.Default.ExpandMore
                                            },
                                            contentDescription = "Expand playlist",
                                        )
                                    }
                                }

                                if (expanded[playlist.id] == true) {
                                    Column(
                                        modifier = Modifier.padding(top = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        if (playlistSongs.isEmpty()) {
                                            Text(
                                                text = "This playlist has no songs yet.",
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
            Icon(Icons.Default.Add, contentDescription = "Create playlist")
        }
    }

    deleteTarget?.let { playlist ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete playlist?") },
            text = { Text("Delete ${playlist.name} and remove it from any assigned songs?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTarget = null
                        onDeletePlaylist(playlist)
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun EmptyPlaylistState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No playlists yet.",
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
