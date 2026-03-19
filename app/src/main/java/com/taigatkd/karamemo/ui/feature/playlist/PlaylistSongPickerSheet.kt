package com.taigatkd.karamemo.ui.feature.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSongPickerSheet(
    playlist: Playlist,
    songs: List<Song>,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedIds by remember {
        mutableStateOf(
            songs.filter { it.playlistId == playlist.id }.mapTo(mutableSetOf()) { it.id },
        )
    }

    val filteredSongs = songs.filter { song ->
        query.isBlank() ||
            song.artist.contains(query, ignoreCase = true) ||
            song.title.contains(query, ignoreCase = true)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Edit songs in ${playlist.name}",
                style = MaterialTheme.typography.headlineSmall,
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search songs") },
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
                            Text(song.title, style = MaterialTheme.typography.titleMedium)
                            Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            TextButton(
                onClick = { onSave(selectedIds) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text("Save selection")
            }
        }
    }
}
