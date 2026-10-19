package spam.blocker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import spam.blocker.G
import spam.blocker.ui.isLight

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val systemIsDark = isSystemInDarkTheme()
    val choice = G.currentThemeChoice.value
    val isDark = choice.isDark(
        systemIsDark = systemIsDark,
        customBackgroundIsDark = !G.palette.background.isLight()
    )

    LaunchedEffect(isDark, choice) {
        if (choice.family != ThemeFamily.CUSTOM) {
            G.palette.applyPreset(presetColors(choice.family, isDark))
        }
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = G.palette.teal200,
            secondary = G.palette.infoBlue,
            tertiary = G.palette.warning,
            error = G.palette.error,
            background = G.palette.background, // main screen bg
            surface = G.palette.background, // top/bottom systembar
            surfaceContainer = G.palette.dialogBg, // balloon tooltip bg
            onBackground = G.palette.textGrey,
            onSurface = G.palette.textGrey,
            onSurfaceVariant = G.palette.disabled,
        )
    } else {
        lightColorScheme(
            primary = G.palette.teal200,
            secondary = G.palette.infoBlue,
            tertiary = G.palette.warning,
            error = G.palette.error,
            background = G.palette.background, // main screen bg
            surface = G.palette.background, // top/bottom systembar
            surfaceContainer = G.palette.dialogBg, // balloon tooltip bg
            onBackground = G.palette.textGrey,
            onSurface = G.palette.textGrey,
            onSurfaceVariant = G.palette.disabled,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
