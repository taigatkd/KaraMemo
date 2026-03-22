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
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.AdBanner
import com.taigatkd.karamemo.ui.components.KaraMemoActionIconButton
import com.taigatkd.karamemo.ui.components.KaraMemoRecordCard
import com.taigatkd.karamemo.ui.components.KaraMemoSwipeToDeleteContainer
import com.taigatkd.karamemo.ui.feature.song.SongItemRow
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

@Composable
fun ArtistListScreen(
    songs: List<Song>,
    showAds: Boolean,
    pinnedArtists: Set<String>,
    onTogglePin: (String) -> Unit,
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
                        KaraMemoSwipeToDeleteContainer(
                            onDeleteRequested = { deleteTarget = artist },
                        ) {
                            KaraMemoRecordCard(
                                onClick = {
                                    expandedArtists[artist] = !isExpanded
                                },
                                modifier = Modifier.fillMaxWidth(),
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
                                                text = artist,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.label_song_count,
                                                    grouped[artist].orEmpty().size,
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            KaraMemoActionIconButton(
                                                onClick = { onTogglePin(artist) },
                                                contentDescription = if (pinnedArtists.contains(artist)) {
                                                    stringResource(R.string.action_unpin)
                                                } else {
                                                    stringResource(R.string.action_pin)
                                                },
                                                imageVector = if (pinnedArtists.contains(artist)) {
                                                    Icons.Default.PushPin
                                                } else {
                                                    Icons.Outlined.PushPin
                                                },
                                                size = 40.dp,
                                                iconTint = MaterialTheme.colorScheme.primary,
                                            )
                                            KaraMemoActionIconButton(
                                                onClick = { onAddSongForArtist(artist) },
                                                contentDescription = stringResource(R.string.action_add_song_to_artist),
                                                imageVector = Icons.Default.Add,
                                                size = 40.dp,
                                            )
                                        }
                                    }

                                    if (isExpanded) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                                        )
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(top = 8.dp),
                                        ) {
                                            grouped[artist]
                                                .orEmpty()
                                                .sortedBy { it.title.lowercase() }
                                                .forEach { song ->
                                                    SongItemRow(
                                                        song = song,
                                                        onEdit = onEditSong,
                                                        onDelete = onDeleteSong,
                                                        onToggleFavorite = onToggleFavorite,
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

            AdBanner(showAd = showAds)
        }
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
            showAds = true,
            pinnedArtists = PreviewFixtures.pinnedArtists,
            onTogglePin = {},
            onAddSongForArtist = {},
            onEditSong = {},
            onDeleteSong = {},
            onToggleFavorite = {},
            onDeleteArtist = {},
        )
    }
}
