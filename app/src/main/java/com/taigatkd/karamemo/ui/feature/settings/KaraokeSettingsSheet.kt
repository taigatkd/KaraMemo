package com.taigatkd.karamemo.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.ui.components.KaraMemoModalSheet
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KaraokeSettingsSheet(
    currentMachine: KaraokeMachine,
    settings: Map<KaraokeMachine, KaraokeMachineSettings>,
    onDismiss: () -> Unit,
    onMachineSelected: (KaraokeMachine) -> Unit,
    onSettingsChanged: (KaraokeMachine, KaraokeMachineSettings) -> Unit,
) {
    val currentSettings = settings[currentMachine] ?: KaraokeMachineSettings()

    KaraMemoModalSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.title_karaoke_settings),
            style = MaterialTheme.typography.headlineSmall,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KaraokeMachine.entries.forEach { machine ->
                FilterChip(
                    selected = machine == currentMachine,
                    onClick = { onMachineSelected(machine) },
                    label = { Text(stringResource(machine.labelRes())) },
                )
            }
        }

        SettingSlider(
            labelRes = R.string.label_bgm,
            value = currentSettings.bgm,
            onValueChange = { value ->
                onSettingsChanged(currentMachine, currentSettings.copy(bgm = value))
            },
        )
        SettingSlider(
            labelRes = R.string.label_mic,
            value = currentSettings.mic,
            onValueChange = { value ->
                onSettingsChanged(currentMachine, currentSettings.copy(mic = value))
            },
        )
        SettingSlider(
            labelRes = R.string.label_echo,
            value = currentSettings.echo,
            onValueChange = { value ->
                onSettingsChanged(currentMachine, currentSettings.copy(echo = value))
            },
        )
        SettingSlider(
            labelRes = R.string.label_music,
            value = currentSettings.music,
            onValueChange = { value ->
                onSettingsChanged(currentMachine, currentSettings.copy(music = value))
            },
        )
    }
}

@Composable
private fun SettingSlider(
    labelRes: Int,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.label_setting_value, stringResource(labelRes), value),
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..50f,
            steps = 49,
        )
    }
}

private fun KaraokeMachine.labelRes(): Int =
    when (this) {
        KaraokeMachine.DAM -> R.string.machine_dam
        KaraokeMachine.JOYSOUND -> R.string.machine_joysound
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
