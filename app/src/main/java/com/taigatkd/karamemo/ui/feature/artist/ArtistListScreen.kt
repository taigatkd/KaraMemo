package com.taigatkd.karamemo.ui.feature.artist

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.AdBanner
import com.taigatkd.karamemo.ui.feature.song.SongItemRow
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

@OptIn(ExperimentalLayoutApi::class)
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
                        val isExpanded = expandedArtists[artist] == true
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(artist, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = stringResource(
                                        R.string.label_song_count,
                                        grouped[artist].orEmpty().size,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    AssistChip(
                                        onClick = { onTogglePin(artist) },
                                        label = {
                                            Text(
                                                if (pinnedArtists.contains(artist)) {
                                                    stringResource(R.string.action_unpin)
                                                } else {
                                                    stringResource(R.string.action_pin)
                                                },
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (pinnedArtists.contains(artist)) {
                                                    Icons.Default.PushPin
                                                } else {
                                                    Icons.Outlined.PushPin
                                                },
                                                contentDescription = null,
                                            )
                                        },
                                    )
                                    AssistChip(
                                        onClick = { onAddSongForArtist(artist) },
                                        label = { Text(stringResource(R.string.action_add_song_to_artist)) },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                    )
                                    AssistChip(
                                        onClick = { expandedArtists[artist] = !isExpanded },
                                        label = {
                                            Text(
                                                if (isExpanded) {
                                                    stringResource(R.string.action_hide_songs)
                                                } else {
                                                    stringResource(R.string.action_show_songs)
                                                },
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (isExpanded) {
                                                    Icons.Default.ExpandLess
                                                } else {
                                                    Icons.Default.ExpandMore
                                                },
                                                contentDescription = null,
                                            )
                                        },
                                    )
                                    AssistChip(
                                        onClick = { deleteTarget = artist },
                                        label = { Text(stringResource(R.string.action_delete)) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                    )
                                }

                                if (isExpanded) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

        ExtendedFloatingActionButton(
            onClick = onAddArtist,
            text = { Text(stringResource(R.string.action_add_artist)) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        )
    }

    deleteTarget?.let { artist ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.title_delete_artist)) },
            text = { Text(stringResource(R.string.message_delete_artist, artist)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTarget = null
                        onDeleteArtist(artist)
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
private fun EmptyArtistState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.empty_artists),
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
