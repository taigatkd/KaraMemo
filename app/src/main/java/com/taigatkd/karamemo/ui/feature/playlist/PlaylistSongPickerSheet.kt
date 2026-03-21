package com.taigatkd.karamemo.ui.feature.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.KaraMemoModalSheet
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

@Composable
fun PlaylistSongPickerSheet(
    playlist: Playlist,
    songs: List<Song>,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedIds by remember(playlist.id, songs) {
        mutableStateOf(
            songs.filter { it.playlistId == playlist.id }.mapTo(mutableSetOf()) { it.id },
        )
    }

    val filteredSongs = songs.filter { song ->
        query.isBlank() ||
            song.artist.contains(query, ignoreCase = true) ||
            song.title.contains(query, ignoreCase = true)
    }

    KaraMemoModalSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.title_playlist_song_picker, playlist.name),
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(stringResource(R.string.label_search_song)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredSongs, key = { it.id }) { song ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = song.artist,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Checkbox(
                        checked = selectedIds.contains(song.id),
                        onCheckedChange = { checked ->
                            val next = selectedIds.toMutableSet()
                            if (checked) {
                                next.add(song.id)
                            } else {
                                next.remove(song.id)
                            }
                            selectedIds = next
                        },
                    )
                }
            }
        }

        Button(
            onClick = { onSave(selectedIds) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.button_save_selection))
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun PlaylistSongPickerSheetPreview() {
    KaraMemoTheme {
        PlaylistSongPickerSheet(
            playlist = PreviewFixtures.playlists.first(),
            songs = PreviewFixtures.songs,
            onDismiss = {},
            onSave = {},
        )
    }
}
