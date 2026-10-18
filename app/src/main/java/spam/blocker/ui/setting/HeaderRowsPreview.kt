package spam.blocker.ui.setting

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
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
import spam.blocker.ui.theme.AppTheme
import spam.blocker.ui.widgets.Button
import spam.blocker.util.spf

@Preview(name = "Header rows compact German", widthDp = 320, fontScale = 1.3f, locale = "de", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Header rows compact Russian", widthDp = 320, fontScale = 1.3f, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HeaderRowsPreview() {
    G.palette = spf.Palette(LocalContext.current)
    var isCollapsed by remember { mutableStateOf(false) }

    AppTheme {
        Column {
            LabeledRow(
                labelId = R.string.workflows,
                isCollapsed = isCollapsed,
                toggleCollapse = { isCollapsed = !isCollapsed },
                helpTooltip = stringResource(R.string.help_workflows),
            ) {
                Button(
                    content = { Text(stringResource(R.string.new_)) },
                    onClick = {},
                )
            }
            LabeledRow(
                labelId = R.string.stir_include_unverified,
                helpTooltip = stringResource(R.string.help_stir),
            ) {
                Button(
                    content = { Text(stringResource(R.string.customize)) },
                    onClick = {},
                )
            }
        }
    }
}
