package com.taigatkd.karamemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = AmpGold,
    onPrimary = NightInk,
    secondary = SpotlightCoral,
    onSecondary = StageWhite,
    tertiary = MintEcho,
    onTertiary = NightInk,
    background = NightInk,
    onBackground = StageWhite,
    surface = DeepWave,
    onSurface = StageWhite,
    surfaceVariant = MidnightStage,
    onSurfaceVariant = ColorTokens.onSurfaceMutedDark,
    outline = VinylBlue,
)

private val LightColors = lightColorScheme(
    primary = SpotlightCoral,
    onPrimary = StageWhite,
    secondary = VinylBlue,
    onSecondary = StageWhite,
    tertiary = AmpGold,
    onTertiary = NightInk,
    background = CreamRoom,
    onBackground = NightInk,
    surface = StageWhite,
    onSurface = NightInk,
    surfaceVariant = PaperWarm,
    onSurfaceVariant = CaptionGray,
    outline = ColorTokens.outlineLight,
)

private object ColorTokens {
    val onSurfaceMutedDark = StageWhite.copy(alpha = 0.78f)
    val outlineLight = VinylBlue.copy(alpha = 0.28f)
}

@Composable
fun KaraMemoTheme(
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = KaraMemoShapes,
        content = content,
    )
}
