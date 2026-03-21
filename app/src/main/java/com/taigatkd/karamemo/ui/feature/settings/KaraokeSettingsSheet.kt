package com.taigatkd.karamemo.ui.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.domain.model.MAX_KARAOKE_MACHINE_SETTING
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.ui.components.KaraMemoModalSheet
import com.taigatkd.karamemo.ui.preview.PreviewFixtures
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
    var isDialInteracting by remember { mutableStateOf(false) }

    KaraMemoModalSheet(
        onDismissRequest = onDismiss,
        sheetGesturesEnabled = !isDialInteracting,
    ) {
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

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularSettingDial(
                    modifier = Modifier.weight(1f),
                    labelRes = R.string.label_bgm,
                    value = currentSettings.bgm,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onInteractionChanged = { isDialInteracting = it },
                    onValueChange = { value ->
                        onSettingsChanged(currentMachine, currentSettings.copy(bgm = value))
                    },
                )
                CircularSettingDial(
                    modifier = Modifier.weight(1f),
                    labelRes = R.string.label_mic,
                    value = currentSettings.mic,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onInteractionChanged = { isDialInteracting = it },
                    onValueChange = { value ->
                        onSettingsChanged(currentMachine, currentSettings.copy(mic = value))
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularSettingDial(
                    modifier = Modifier.weight(1f),
                    labelRes = R.string.label_echo,
                    value = currentSettings.echo,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    onInteractionChanged = { isDialInteracting = it },
                    onValueChange = { value ->
                        onSettingsChanged(currentMachine, currentSettings.copy(echo = value))
                    },
                )
                CircularSettingDial(
                    modifier = Modifier.weight(1f),
                    labelRes = R.string.label_music,
                    value = currentSettings.music,
                    accentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                    onInteractionChanged = { isDialInteracting = it },
                    onValueChange = { value ->
                        onSettingsChanged(currentMachine, currentSettings.copy(music = value))
                    },
                )
            }
        }
    }
}

@Composable
private fun CircularSettingDial(
    modifier: Modifier = Modifier,
    labelRes: Int,
    value: Int,
    accentColor: Color,
    onInteractionChanged: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit,
) {
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
    val surfaceHighlight = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
    val centerKnobColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(surfaceHighlight, CircleShape)
            .pointerInput(onValueChange, onInteractionChanged) {
                detectTapGestures(
                    onPress = {
                        onInteractionChanged(true)
                        try {
                            tryAwaitRelease()
                        } finally {
                            onInteractionChanged(false)
                        }
                    },
                    onTap = { offset ->
                        onValueChange(offset.toMachineSettingValue(size))
                    },
                )
            }
            .pointerInput(onValueChange, onInteractionChanged) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onInteractionChanged(true)
                        onValueChange(offset.toMachineSettingValue(size))
                    },
                    onDragEnd = {
                        onInteractionChanged(false)
                    },
                    onDragCancel = {
                        onInteractionChanged(false)
                    },
                    onDrag = { change: PointerInputChange, _ ->
                        onValueChange(change.position.toMachineSettingValue(size))
                        change.consume()
                    },
                )
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val knobRadius = 7.dp.toPx()
            val arcInset = strokeWidth / 2f + 6.dp.toPx()
            val arcSize = Size(
                width = size.width - arcInset * 2,
                height = size.height - arcInset * 2,
            )
            val sweepAngle = (value / MAX_KARAOKE_MACHINE_SETTING.toFloat()) * 360f

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(arcInset, arcInset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(arcInset, arcInset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            val angleRadians = Math.toRadians((sweepAngle - 90f).toDouble())
            val knobDistance = arcSize.width / 2f
            val knobCenter = Offset(
                x = size.width / 2f + cos(angleRadians).toFloat() * knobDistance,
                y = size.height / 2f + sin(angleRadians).toFloat() * knobDistance,
            )
            drawCircle(
                color = accentColor,
                radius = knobRadius,
                center = knobCenter,
            )
            drawCircle(
                color = centerKnobColor,
                radius = knobRadius / 2.6f,
                center = knobCenter,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "/$MAX_KARAOKE_MACHINE_SETTING",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun Offset.toMachineSettingValue(size: IntSize): Int {
    val center = Offset(size.width / 2f, size.height / 2f)
    val angle = (
        Math.toDegrees(
            atan2(
                (y - center.y).toDouble(),
                (x - center.x).toDouble(),
            ),
        )
            + 450.0
        ) % 360.0
    return ((angle / 360.0) * MAX_KARAOKE_MACHINE_SETTING)
        .roundToInt()
        .coerceIn(0, MAX_KARAOKE_MACHINE_SETTING)
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
