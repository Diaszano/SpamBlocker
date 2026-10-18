package spam.blocker.ui.setting.quick

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.setting.SettingItem
import spam.blocker.ui.theme.AppTheme
import spam.blocker.util.spf

@Preview(name = "OffTime compact German", widthDp = 320, fontScale = 1.3f, locale = "de", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "OffTime compact Russian", widthDp = 320, fontScale = 1.3f, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OffTimePreview() {
    G.palette = spf.Palette(LocalContext.current)
    var isEnabled by remember { mutableStateOf(true) }
    var timeRange by remember { mutableStateOf("00:00 - 23:59") }
    var pickerOpened by remember { mutableStateOf(false) }

    AppTheme {
        Column {
            SettingItem(
                labelId = R.string.off_time,
                helpTooltip = stringResource(R.string.help_off_time),
                value = if (isEnabled) timeRange else null,
                onValueClick = {
                    pickerOpened = !pickerOpened
                    timeRange = if (timeRange == "00:00 - 23:59") "08:00 - 18:00" else "00:00 - 23:59"
                },
                checked = isEnabled,
                onCheckedChange = { isEnabled = it },
            )
            SettingItem(
                labelId = R.string.off_time,
                helpTooltip = stringResource(R.string.help_off_time),
                value = null,
                checked = false,
                onCheckedChange = {},
            )
        }
    }
}
