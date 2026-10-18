package spam.blocker.ui.setting

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.theme.AppTheme
import spam.blocker.util.spf

@Preview(name = "German compact", widthDp = 320, fontScale = 1.3f, locale = "de", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Russian compact", widthDp = 320, fontScale = 1.3f, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingItemPreview() {
    G.palette = spf.Palette(LocalContext.current)
    var checked by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var minutes by remember { mutableStateOf(5) }
    AppTheme {
        Column {
            SettingItem(labelId = R.string.repeated_call, value = "$minutes ${stringResource(R.string.min)}", onValueClick = { minutes = if (minutes == 5) 10 else 5 }, checked = checked, onCheckedChange = { checked = it })
            SettingItem(labelId = R.string.off_time, expanded = expanded, onExpand = { expanded = !expanded })
            SettingItem(labelId = R.string.quick_tile_is_active, checked = checked, onCheckedChange = { checked = it })
        }
    }
}
