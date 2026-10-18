package spam.blocker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import spam.blocker.G


@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = darkColorScheme(
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
        ),
        content = content,
    )
}
