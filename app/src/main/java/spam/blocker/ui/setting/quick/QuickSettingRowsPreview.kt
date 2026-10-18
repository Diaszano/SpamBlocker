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

@Preview(name = "Quick rows compact German", widthDp = 320, fontScale = 1.3f, locale = "de", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Quick rows compact Russian", widthDp = 320, fontScale = 1.3f, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QuickSettingRowsPreview() {
    G.palette = spf.Palette(LocalContext.current)
    var dialedEnabled by remember { mutableStateOf(true) }
    var dialedDays by remember { mutableIntStateOf(30) }

    var answeredEnabled by remember { mutableStateOf(true) }
    var answeredDays by remember { mutableIntStateOf(14) }

    var callerIdEnabled by remember { mutableStateOf(true) }
    var previewClicked by remember { mutableStateOf(false) }

    AppTheme {
        Column {
            SettingItem(
                labelId = R.string.dialed_number,
                helpTooltip = stringResource(R.string.help_dialed).format(dialedDays),
                value = if (dialedEnabled) "$dialedDays d" else null,
                onValueClick = { dialedDays = if (dialedDays == 30) 7 else 30 },
                checked = dialedEnabled,
                onCheckedChange = { dialedEnabled = it },
            )
            SettingItem(
                labelId = R.string.answered_number,
                helpTooltip = stringResource(R.string.help_answered).format("", answeredDays, 5),
                value = if (answeredEnabled) "$answeredDays d" else null,
                onValueClick = { answeredDays = if (answeredDays == 14) 3 else 14 },
                checked = answeredEnabled,
                onCheckedChange = { answeredEnabled = it },
            )
            SettingItem(
                labelId = R.string.caller_id,
                helpTooltip = stringResource(R.string.help_caller_id),
                value = if (callerIdEnabled) stringResource(R.string.preview) else null,
                onValueClick = { previewClicked = !previewClicked },
                checked = callerIdEnabled,
                onCheckedChange = { callerIdEnabled = it },
            )
        }
    }
}
