package at.schulrecht.trainer.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B5FFF),
    onPrimary = Color.White,
    secondary = Color(0xFF5B6B8C),
    tertiary = Color(0xFF0E9F6E),
    tertiaryContainer = Color(0xFFB9F0D4),
    onTertiaryContainer = Color(0xFF0A3D24)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7AA5FF),
    onPrimary = Color(0xFF0A1B3D),
    secondary = Color(0xFF9AA8C7),
    tertiary = Color(0xFF4ADE80),
    tertiaryContainer = Color(0xFF14532D),
    onTertiaryContainer = Color(0xFFBBF7D0)
)

val LightColorScheme = LightColors
val DarkColorScheme = DarkColors
