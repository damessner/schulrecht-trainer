package at.schulrecht.trainer.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B5FFF),
    onPrimary = Color.White,
    secondary = Color(0xFF5B6B8C),
    tertiary = Color(0xFF0E9F6E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7AA5FF),
    onPrimary = Color(0xFF0A1B3D),
    secondary = Color(0xFF9AA8C7),
    tertiary = Color(0xFF4ADE80)
)

val LightColorScheme = LightColors
val DarkColorScheme = DarkColors
