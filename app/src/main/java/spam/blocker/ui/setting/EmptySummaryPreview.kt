package spam.blocker.ui.setting

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.setting.api.ApiSummary
import spam.blocker.ui.setting.bot.BotSummary
import spam.blocker.ui.theme.AppTheme
import spam.blocker.ui.widgets.Fab
import spam.blocker.ui.widgets.Str
import spam.blocker.util.spf

@Preview(name = "Empty ApiSummary compact dark", widthDp = 320, fontScale = 1.3f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyApiSummaryPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)
    G.apiQueryVM.apis.clear()

    AppTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ApiSummary(vm = G.apiQueryVM)
        }
    }
}

@Preview(name = "Empty BotSummary compact dark", widthDp = 320, fontScale = 1.3f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyBotSummaryPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)
    G.botVM.bots.clear()

    AppTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BotSummary(vm = G.botVM)
        }
    }
}

@Preview(name = "Testing FAB icon action compact dark", widthDp = 320, fontScale = 1.3f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TestingFabPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)

    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Fab(
                visible = true,
                contentDescription = Str(R.string.title_rule_testing),
                iconId = R.drawable.ic_tube,
                iconColor = Color.White,
                bgColor = G.palette.teal200,
            ) {}
        }
    }
}
