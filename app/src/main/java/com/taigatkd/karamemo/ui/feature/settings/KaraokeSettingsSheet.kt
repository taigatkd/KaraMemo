package com.taigatkd.karamemo.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaraokeSettingsSheet(
    currentMachine: KaraokeMachine,
    settings: Map<KaraokeMachine, KaraokeMachineSettings>,
    onDismiss: () -> Unit,
    onMachineSelected: (KaraokeMachine) -> Unit,
    onSettingsChanged: (KaraokeMachine, KaraokeMachineSettings) -> Unit,
) {
    val currentSettings = settings[currentMachine] ?: KaraokeMachineSettings()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Karaoke machine settings", style = MaterialTheme.typography.headlineSmall)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KaraokeMachine.entries.forEach { machine ->
                    FilterChip(
                        selected = machine == currentMachine,
                        onClick = { onMachineSelected(machine) },
                        label = { Text(machine.name) },
                    )
                }
            }

            SettingSlider(
                label = "BGM",
                value = currentSettings.bgm,
                onValueChange = { value ->
                    onSettingsChanged(currentMachine, currentSettings.copy(bgm = value))
                },
            )
            SettingSlider(
                label = "Mic",
                value = currentSettings.mic,
                onValueChange = { value ->
                    onSettingsChanged(currentMachine, currentSettings.copy(mic = value))
                },
            )
            SettingSlider(
                label = "Echo",
                value = currentSettings.echo,
                onValueChange = { value ->
                    onSettingsChanged(currentMachine, currentSettings.copy(echo = value))
                },
            )
            SettingSlider(
                label = "Music",
                value = currentSettings.music,
                onValueChange = { value ->
                    onSettingsChanged(currentMachine, currentSettings.copy(music = value))
                },
            )
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Column {
        Text("$label $value", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..50f,
            steps = 49,
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun KaraokeSettingsSheetPreview() {
    KaraMemoTheme {
        KaraokeSettingsSheet(
            currentMachine = KaraokeMachine.DAM,
            settings = PreviewFixtures.machineSettings,
            onDismiss = {},
            onMachineSelected = {},
            onSettingsChanged = { _, _ -> },
        )
    }
}
