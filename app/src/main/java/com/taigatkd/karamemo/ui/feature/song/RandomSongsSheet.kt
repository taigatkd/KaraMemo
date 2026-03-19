package com.taigatkd.karamemo.ui.feature.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomSongsSheet(
    songs: List<Song>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Random picks",
                style = MaterialTheme.typography.headlineSmall,
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(songs, key = { it.id }) { song ->
                    SongItemRow(
                        song = song,
                        onEdit = {},
                        onDelete = {},
                        onToggleFavorite = {},
                        showActions = false,
                    )
                }
            }
        }
    }
}
