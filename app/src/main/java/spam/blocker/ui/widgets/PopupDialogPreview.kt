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

@Preview(name = "PopupDialog compact editor de", widthDp = 320, fontScale = 1.3f, locale = "de", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "PopupDialog compact editor ru", widthDp = 320, fontScale = 1.3f, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PopupDialogPreview() {
    G.palette = spf.Palette(LocalContext.current)
    AppTheme {
        PopupDialog(
            trigger = remember { mutableStateOf(true) },
            popupSize = PopupSize(maxWidthPercentage = 0.9f, minWidthDp = 340, maxWidthDp = 600),
            title = {
                Text(
                    text = stringResource(R.string.title_rule_testing),
                    color = G.palette.textGrey,
                )
            },
            buttons = {
                StrokeButton(
                    label = stringResource(R.string.save),
                    color = G.palette.teal200,
                )
            }
        ) {
            StrInputBox(
                text = ".*spam.*",
                label = { Text(stringResource(R.string.phone_number)) },
                onValueChange = {}
            )
            StrInputBox(
                text = "Sample note",
                label = { Text(stringResource(R.string.description)) },
                onValueChange = {}
            )
        }
    }
}
