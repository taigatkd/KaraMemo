package com.taigatkd.karamemo.ui.feature.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme
import kotlinx.coroutines.launch

data class SongEditorRequest(
    val song: Song? = null,
    val initialArtist: String? = null,
    val initialPlaylistId: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongEditorSheet(
    request: SongEditorRequest,
    playlists: List<Playlist>,
    lastUsedArtist: String?,
    onDismiss: () -> Unit,
    onSave: suspend (
        artist: String,
        title: String,
        key: Int,
        memo: String,
        isFavorite: Boolean,
        playlistId: String?,
    ) -> Boolean,
) {
    val existingSong = request.song
    var artist by rememberSaveable(existingSong?.id, request.initialArtist, lastUsedArtist) {
        mutableStateOf(existingSong?.artist ?: request.initialArtist ?: lastUsedArtist.orEmpty())
    }
    var title by rememberSaveable(existingSong?.id) { mutableStateOf(existingSong?.title.orEmpty()) }
    var memo by rememberSaveable(existingSong?.id) { mutableStateOf(existingSong?.memo.orEmpty()) }
    var key by rememberSaveable(existingSong?.id) { mutableStateOf(existingSong?.key ?: 0) }
    var isFavorite by rememberSaveable(existingSong?.id) { mutableStateOf(existingSong?.isFavorite ?: false) }
    var selectedPlaylistId by rememberSaveable(existingSong?.id, request.initialPlaylistId) {
        mutableStateOf(existingSong?.playlistId ?: request.initialPlaylistId)
    }
    var menuExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (existingSong == null) "Add song" else "Edit song",
                style = MaterialTheme.typography.headlineSmall,
            )

            OutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = { Text("Artist") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Song title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Column {
                Text("Playlist", style = MaterialTheme.typography.labelLarge)
                OutlinedButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = playlists.firstOrNull { it.id == selectedPlaylistId }?.name ?: "Not assigned",
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Not assigned") },
                        onClick = {
                            selectedPlaylistId = null
                            menuExpanded = false
                        },
                    )
                    playlists.forEach { playlist ->
                        DropdownMenuItem(
                            text = { Text(playlist.name) },
                            onClick = {
                                selectedPlaylistId = playlist.id
                                menuExpanded = false
                            },
                        )
                    }
                }
            }

            Column {
                Text("Key ${if (key > 0) "+" else ""}$key", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = key.toFloat(),
                    onValueChange = { key = it.toInt() },
                    valueRange = -5f..5f,
                    steps = 9,
                )
            }

            OutlinedTextField(
                value = memo,
                onValueChange = { memo = it },
                label = { Text("Memo") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            Row {
                Checkbox(
                    checked = isFavorite,
                    onCheckedChange = { checked -> isFavorite = checked },
                )
                Text(
                    text = "Mark as favorite",
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        val saved = onSave(
                            artist,
                            title,
                            key,
                            memo,
                            isFavorite,
                            selectedPlaylistId,
                        )
                        isSaving = false
                        if (!saved) return@launch

                        if (existingSong != null) {
                            onDismiss()
                        } else {
                            title = ""
                            memo = ""
                            key = 0
                            isFavorite = false
                            selectedPlaylistId = request.initialPlaylistId
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text(if (existingSong == null) "Save song" else "Update song")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun SongEditorSheetPreview() {
    KaraMemoTheme {
        SongEditorSheet(
            request = SongEditorRequest(song = PreviewFixtures.songs.first()),
            playlists = PreviewFixtures.playlists,
            lastUsedArtist = PreviewFixtures.songs.first().artist,
            onDismiss = {},
            onSave = { _, _, _, _, _, _ -> true },
        )
    }
}
