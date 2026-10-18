package spam.blocker.ui.widgets

import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.theme.AppTheme
import spam.blocker.util.spf

@Preview(name = "Section compact", widthDp = 320, fontScale = 1.3f, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SectionPreview() {
    G.palette = spf.Palette(LocalContext.current)
    AppTheme {
        Section(title = stringResource(R.string.quick_settings), isCollapsed = remember { mutableStateOf(false) }) {
            Text(stringResource(R.string.contacts))
        }
    }
}
