package com.taigatkd.karamemo.ui.feature.artist

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
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.AdBanner
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.feature.song.SongItemRow
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

@Composable
fun ArtistListScreen(
    songs: List<Song>,
    playlistNamesById: Map<String, String>,
    pinnedArtists: Set<String>,
    onTogglePin: (String) -> Unit,
    onAddArtist: () -> Unit,
    onAddSongForArtist: (String) -> Unit,
    onEditSong: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onDeleteArtist: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedArtists = remember { mutableStateMapOf<String, Boolean>() }
    var deleteTarget by remember { mutableStateOf<String?>(null) }
    val grouped = songs.groupBy { it.artist }
    val artists = grouped.keys.sortedWith(
        compareBy<String> { !pinnedArtists.contains(it) }.thenBy { it.lowercase() },
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (artists.isEmpty()) {
                EmptyArtistState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(artists, key = { it }) { artist ->
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(artist, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${grouped[artist].orEmpty().size} songs",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    IconButton(onClick = { onTogglePin(artist) }) {
                                        Icon(
                                            imageVector = if (pinnedArtists.contains(artist)) {
                                                Icons.Default.PushPin
                                            } else {
                                                Icons.Outlined.PushPin
                                            },
                                            contentDescription = "Toggle pin",
                                        )
                                    }
                                    IconButton(onClick = { onAddSongForArtist(artist) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Add song")
                                    }
                                    IconButton(onClick = { deleteTarget = artist }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete artist")
                                    }
                                    IconButton(
                                        onClick = {
                                            expandedArtists[artist] = !(expandedArtists[artist] ?: false)
                                        },
                                    ) {
                                        Icon(
                                            imageVector = if (expandedArtists[artist] == true) {
                                                Icons.Default.ExpandLess
                                            } else {
                                                Icons.Default.ExpandMore
                                            },
                                            contentDescription = "Expand artist",
                                        )
                                    }
                                }

                                if (expandedArtists[artist] == true) {
                                    Column(
                                        modifier = Modifier.padding(top = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        grouped[artist].orEmpty().forEach { song ->
                                            SongItemRow(
                                                song = song,
                                                playlistName = song.playlistId?.let(playlistNamesById::get),
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

            AdBanner()
        }

        FloatingActionButton(
            onClick = onAddArtist,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add artist")
        }
    }

    deleteTarget?.let { artist ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete artist songs?") },
            text = { Text("Delete every song registered under $artist?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTarget = null
                        onDeleteArtist(artist)
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
private fun EmptyArtistState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No artists yet.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun ArtistListScreenPreview() {
    KaraMemoTheme {
        ArtistListScreen(
            songs = PreviewFixtures.songs,
            playlistNamesById = PreviewFixtures.playlistNamesById,
            pinnedArtists = PreviewFixtures.pinnedArtists,
            onTogglePin = {},
            onAddArtist = {},
            onAddSongForArtist = {},
            onEditSong = {},
            onDeleteSong = {},
            onToggleFavorite = {},
            onDeleteArtist = {},
        )
    }
}
