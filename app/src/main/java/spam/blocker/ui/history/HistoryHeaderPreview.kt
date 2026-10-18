package spam.blocker.ui.history

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import spam.blocker.G
import spam.blocker.ui.theme.AppTheme
import spam.blocker.util.spf

@Preview(name = "Call history header German compact", widthDp = 320, fontScale = 1.3f, locale = "de", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Call history header Russian compact", widthDp = 320, fontScale = 1.3f, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CallHistoryHeaderPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)
    G.globallyEnabled.value = true

    AppTheme {
        Column(modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 2.dp)) {
            HistoryHeader(vm = G.callVM)
        }
    }
}

@Preview(name = "SMS history header German compact", widthDp = 320, fontScale = 1.3f, locale = "de", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "SMS history header Russian compact", widthDp = 320, fontScale = 1.3f, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SmsHistoryHeaderPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)
    G.globallyEnabled.value = true

    AppTheme {
        Column(modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 2.dp)) {
            HistoryHeader(vm = G.smsVM)
        }
    }
}
