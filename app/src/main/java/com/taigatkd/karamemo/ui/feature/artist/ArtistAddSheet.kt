package com.taigatkd.karamemo.ui.feature.artist

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
fun ArtistAddSheet(
    onDismiss: () -> Unit,
    onNext: (String) -> Unit,
) {
    var artistName by rememberSaveable { mutableStateOf("") }

    KaraMemoModalSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.title_add_artist),
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = artistName,
            onValueChange = { artistName = it },
            label = { Text(stringResource(R.string.label_artist_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            onClick = { onNext(artistName.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            enabled = artistName.isNotBlank(),
        ) {
            Text(stringResource(R.string.action_next))
        }
    }
}
