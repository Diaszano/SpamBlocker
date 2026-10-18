package spam.blocker.ui.widgets

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import spam.blocker.G
import spam.blocker.ui.theme.AppTheme
import spam.blocker.util.spf

@Preview(name = "Switch compact", widthDp = 320, fontScale = 1.3f, locale = "de", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwitchBoxPreview() {
    G.palette = spf.Palette(LocalContext.current)
    val checked = remember { mutableStateOf(true) }
    AppTheme { SwitchBox(checked = checked.value, onCheckedChange = { checked.value = it }) }
}
