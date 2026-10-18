package spam.blocker.ui.history

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import spam.blocker.G
import spam.blocker.ui.theme.AppTheme
import spam.blocker.util.spf

@Preview(name = "History empty filters off compact dark", widthDp = 320, fontScale = 1.3f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryEmptyFiltersOffPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)
    HistoryOptions.showHistoryPassed.value = false
    HistoryOptions.showHistoryBlocked.value = false
    G.callVM.searchEnabled.value = false
    G.callVM.filter.value = ""
    G.callVM.records.clear()
    G.callVM.isLoading.value = false

    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            HistoryList(
                lazyState = rememberLazyListState(),
                vm = G.callVM,
            )
        }
    }
}

@Preview(name = "History empty search miss compact dark", widthDp = 320, fontScale = 1.3f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryEmptySearchMissPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)
    HistoryOptions.showHistoryPassed.value = true
    HistoryOptions.showHistoryBlocked.value = true
    G.callVM.searchEnabled.value = true
    G.callVM.filter.value = "12345"
    G.callVM.records.clear()
    G.callVM.isLoading.value = false

    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            HistoryList(
                lazyState = rememberLazyListState(),
                vm = G.callVM,
            )
        }
    }
}

@Preview(name = "History empty no records compact dark", widthDp = 320, fontScale = 1.3f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryEmptyNoRecordsPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)
    HistoryOptions.showHistoryPassed.value = true
    HistoryOptions.showHistoryBlocked.value = true
    G.callVM.searchEnabled.value = false
    G.callVM.filter.value = ""
    G.callVM.records.clear()
    G.callVM.isLoading.value = false

    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            HistoryList(
                lazyState = rememberLazyListState(),
                vm = G.callVM,
            )
        }
    }
}

@Preview(name = "History FABs action labels compact dark", widthDp = 320, fontScale = 1.3f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryFabsPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)

    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            HistoryFabs(
                visible = true,
                vm = G.callVM,
                modifier = Modifier,
            )
        }
    }
}
