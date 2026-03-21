package com.taigatkd.karamemo.ui.feature.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.ui.components.KaraMemoModalSheet
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme
import java.math.BigDecimal
import kotlinx.coroutines.launch

data class SongEditorRequest(
    val song: Song? = null,
    val initialArtist: String? = null,
    val initialPlaylistId: String? = null,
)

@Composable
fun SongEditorSheet(
    request: SongEditorRequest,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onSave: suspend (
        artist: String,
        title: String,
        key: Int,
        memo: String,
        isFavorite: Boolean,
        playlistId: String?,
        score: Double?,
    ) -> Boolean,
) {
    val existingSong = request.song
    var artist by rememberSaveable(existingSong?.id, request.initialArtist) {
        mutableStateOf(existingSong?.artist ?: request.initialArtist.orEmpty())
    }
    var title by rememberSaveable(existingSong?.id) { mutableStateOf(existingSong?.title.orEmpty()) }
    var memo by rememberSaveable(existingSong?.id) { mutableStateOf(existingSong?.memo.orEmpty()) }
    var key by rememberSaveable(existingSong?.id) { mutableStateOf(existingSong?.key ?: 0) }
    var isFavorite by rememberSaveable(existingSong?.id) { mutableStateOf(existingSong?.isFavorite ?: false) }
    var selectedPlaylistId by rememberSaveable(existingSong?.id, request.initialPlaylistId) {
        mutableStateOf(existingSong?.playlistId ?: request.initialPlaylistId)
    }
    var scoreInput by rememberSaveable(existingSong?.id) {
        mutableStateOf(existingSong?.score?.let(::formatScore).orEmpty())
    }
    var scoreErrorResId by remember { mutableStateOf<Int?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val titleRes = if (existingSong == null) R.string.title_add_song else R.string.title_edit_song
    val saveRes = if (existingSong == null) R.string.button_save_song else R.string.button_update_song

    fun parseScoreOrNull(): Double? {
        val normalizedInput = scoreInput.trim()
        if (normalizedInput.isBlank()) return null
        val parsed = normalizedInput.toDoubleOrNull()
        if (parsed == null || !parsed.isFinite() || parsed < 0.0 || parsed > 100.0) {
            scoreErrorResId = R.string.message_invalid_score
            return null
        }
        return parsed
    }

    KaraMemoModalSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineSmall,
            )

            OutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = { Text(stringResource(R.string.label_artist_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.label_song_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Column {
                Text(
                    text = stringResource(R.string.label_playlist),
                    style = MaterialTheme.typography.labelLarge,
                )
                Box {
                    OutlinedButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = playlists.firstOrNull { it.id == selectedPlaylistId }?.name
                                ?: stringResource(R.string.label_not_assigned),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.label_not_assigned)) },
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
            }

            Column {
                Text(
                    text = stringResource(R.string.label_key, formatSignedKey(key)),
                    style = MaterialTheme.typography.labelLarge,
                )
                Slider(
                    value = key.toFloat(),
                    onValueChange = { key = it.toInt() },
                    valueRange = -5f..5f,
                    steps = 9,
                )
            }

            OutlinedTextField(
                value = scoreInput,
                onValueChange = {
                    scoreInput = it
                    scoreErrorResId = null
                },
                label = { Text(stringResource(R.string.label_score)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = scoreErrorResId != null,
                supportingText = {
                    scoreErrorResId?.let { resId ->
                        Text(stringResource(resId))
                    }
                },
            )

            OutlinedTextField(
                value = memo,
                onValueChange = { memo = it },
                label = { Text(stringResource(R.string.label_memo)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            Row {
                Checkbox(
                    checked = isFavorite,
                    onCheckedChange = { checked -> isFavorite = checked },
                )
                Text(
                    text = stringResource(R.string.label_mark_favorite),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        val score = parseScoreOrNull()
                        if (scoreErrorResId != null) return@launch

                        isSaving = true
                        val saved = onSave(
                            artist,
                            title,
                            key,
                            memo,
                            isFavorite,
                            selectedPlaylistId,
                            score,
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
                            scoreInput = ""
                            scoreErrorResId = null
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text(stringResource(saveRes))
            }
        }
    }
}

private fun formatSignedKey(value: Int): String = if (value > 0) "+$value" else value.toString()

private fun formatScore(value: Double): String =
    BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun SongEditorSheetPreview() {
    KaraMemoTheme {
        SongEditorSheet(
            request = SongEditorRequest(song = PreviewFixtures.songs.first()),
            playlists = PreviewFixtures.playlists,
            onDismiss = {},
            onSave = { _, _, _, _, _, _, _ -> true },
        )
    }
}
