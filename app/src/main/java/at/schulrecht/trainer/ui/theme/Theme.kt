package at.schulrecht.trainer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun TrainerTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val base = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = base.copy(
            tertiaryContainer = if (dark) {
                DarkColorScheme.tertiaryContainer
            } else {
                LightColorScheme.tertiaryContainer
            },
            onTertiaryContainer = if (dark) {
                DarkColorScheme.onTertiaryContainer
            } else {
                LightColorScheme.onTertiaryContainer
            }
        ),
        typography = TrainerTypography,
        content = content
    )
}
