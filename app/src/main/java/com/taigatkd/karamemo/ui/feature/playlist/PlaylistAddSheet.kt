package com.taigatkd.karamemo.ui.feature.playlist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.ui.components.KaraMemoModalSheet

@Composable
fun PlaylistAddSheet(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var playlistName by rememberSaveable { mutableStateOf("") }

    KaraMemoModalSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.title_create_playlist),
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = playlistName,
            onValueChange = { playlistName = it },
            label = { Text(stringResource(R.string.label_playlist_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            onClick = { onSave(playlistName.trim()) },
            enabled = playlistName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Text(stringResource(R.string.action_save))
        }
    }
}
