package com.taigatkd.karamemo.ui.feature.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.KaraMemoModalSheet
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

@Composable
fun RandomSongsSheet(
    songs: List<Song>,
    onEditSong: (Song) -> Unit,
    onDismiss: () -> Unit,
) {
    KaraMemoModalSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.title_random_picks),
            style = MaterialTheme.typography.headlineSmall,
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(songs, key = { it.id }) { song ->
                SongItemRow(
                    song = song,
                    onEdit = onEditSong,
                    onDelete = {},
                    onToggleFavorite = {},
                    showActions = false,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun RandomSongsSheetPreview() {
    KaraMemoTheme {
        RandomSongsSheet(
            songs = PreviewFixtures.songs,
            onEditSong = {},
            onDismiss = {},
        )
    }
}
