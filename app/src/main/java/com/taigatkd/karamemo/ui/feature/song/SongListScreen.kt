package com.taigatkd.karamemo.ui.feature.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.AdBanner
import com.taigatkd.karamemo.ui.components.KaraMemoActionIconButton
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SongListScreen(
    songs: List<Song>,
    playlistNamesById: Map<String, String>,
    query: String,
    showSearchBar: Boolean,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onOpenSort: () -> Unit,
    onOpenRandom: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddSong: () -> Unit,
    onEditSong: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KaraMemoActionIconButton(
                    onClick = onToggleSearch,
                    contentDescription = if (showSearchBar) {
                        stringResource(R.string.action_hide_search)
                    } else {
                        stringResource(R.string.action_search)
                    },
                    imageVector = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                )
                KaraMemoActionIconButton(
                    onClick = onOpenSort,
                    contentDescription = stringResource(R.string.action_sort),
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                )
                KaraMemoActionIconButton(
                    onClick = onOpenRandom,
                    contentDescription = stringResource(R.string.action_random),
                    imageVector = Icons.Default.Shuffle,
                )
                ElevatedAssistChip(
                    onClick = onOpenSettings,
                    label = { Text(stringResource(R.string.action_settings)) },
                    leadingIcon = {
                        Icon(Icons.Default.GraphicEq, contentDescription = null)
                    },
                )
            }

            if (showSearchBar) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = { Text(stringResource(R.string.label_search_song)) },
                    singleLine = true,
                )
            }

            if (songs.isEmpty()) {
                EmptySongsState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(songs, key = { it.id }) { song ->
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

            AdBanner()
        }

        FloatingActionButton(
            onClick = onAddSong,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.action_add_song),
            )
        }
    }
}

@Composable
private fun EmptySongsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.empty_songs_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = stringResource(R.string.empty_songs_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun SongListScreenPreview() {
    KaraMemoTheme {
        SongListScreen(
            songs = PreviewFixtures.songs,
            playlistNamesById = PreviewFixtures.playlistNamesById,
            query = "a",
            showSearchBar = true,
            onQueryChange = {},
            onToggleSearch = {},
            onOpenSort = {},
            onOpenRandom = {},
            onOpenSettings = {},
            onAddSong = {},
            onEditSong = {},
            onDeleteSong = {},
            onToggleFavorite = {},
        )
    }
}
