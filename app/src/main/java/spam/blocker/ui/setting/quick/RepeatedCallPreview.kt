package spam.blocker.ui.setting.quick

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@Preview(name = "RepeatedCall compact German", widthDp = 320, fontScale = 1.3f, locale = "de", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RepeatedCall compact Russian", widthDp = 320, fontScale = 1.3f, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RepeatedCallPreview() {
    G.palette = spf.Palette(LocalContext.current)
    var isEnabled by remember { mutableStateOf(true) }
    var times by remember { mutableIntStateOf(99) }
    var inXMin by remember { mutableIntStateOf(999) }
    var editorOpened by remember { mutableStateOf(false) }

    val minLabel = stringResource(R.string.min)
    val valueText = if (isEnabled) {
        if (times == 1) "$inXMin $minLabel" else "$times / $inXMin $minLabel"
    } else null

    AppTheme {
        Column {
            SettingItem(
                labelId = R.string.repeated_call,
                helpTooltip = stringResource(R.string.help_repeated_call),
                value = valueText,
                onValueClick = {
                    editorOpened = !editorOpened
                    times = if (times == 1) 99 else 1
                },
                checked = isEnabled,
                onCheckedChange = { isEnabled = it },
            )
            SettingItem(
                labelId = R.string.repeated_call,
                helpTooltip = stringResource(R.string.help_repeated_call),
                value = null,
                checked = false,
                onCheckedChange = {},
            )
        }
    }
}
