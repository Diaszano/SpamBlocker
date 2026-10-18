package spam.blocker.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.M
import spam.blocker.ui.setting.SettingItem
import spam.blocker.ui.setting.setGlobalEnabled
import spam.blocker.ui.widgets.FabWrapper
import spam.blocker.ui.widgets.SearchBox
import spam.blocker.util.LockedDebouncer
import spam.blocker.util.TimeUtils.FreshnessColor
import spam.blocker.util.spf

object HistoryOptions {
    val showHistoryIndicator : MutableState<Boolean> = mutableStateOf(false)
    val showHistoryGeoLocation : MutableState<Boolean> = mutableStateOf(false)
    val showHistoryCarrier : MutableState<Boolean> = mutableStateOf(false)
    val forceShowSIM : MutableState<Boolean> = mutableStateOf(false)
    val showHistoryPassed : MutableState<Boolean> = mutableStateOf(false)
    val showHistoryBlocked : MutableState<Boolean> = mutableStateOf(false)
    val showHistoryTimeColor : MutableState<Boolean> = mutableStateOf(false)
    val historyTimeColors : SnapshotStateList<FreshnessColor> = mutableStateListOf()
}

@Composable
fun HistoryScreen(
    vm: HistoryViewModel,
) {
    val ctx = LocalContext.current

    // Load history options
    LaunchedEffect(true) {
        val spf = spf.HistoryOptions(ctx)
        HistoryOptions.apply {
            showHistoryIndicator.value = spf.showIndicator
            showHistoryGeoLocation.value = spf.showGeoLocation
            showHistoryCarrier.value = spf.showCarrier
            forceShowSIM.value = spf.forceShowSim
            // these two are loaded at startup for calculating the unread count on bottom bar
//            showHistoryPassed.value = spf.showPassed
//            showHistoryBlocked.value = spf.showBlocked
            showHistoryTimeColor.value = spf.showTimeColor
            historyTimeColors.apply {
                clear()
                addAll(spf.loadTimeColors())
            }
        }
    }

    // Hide FAB on scrolling to the last item
    val listState = rememberLazyListState()
    val fabVisible by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val lastItemIndex = listState.layoutInfo.totalItemsCount - 1
            lastVisibleIndex < lastItemIndex || listState.firstVisibleItemIndex == 0
        }
    }

    FabWrapper(
        fabRow = { positionModifier ->
            // Two FABs
            HistoryFabs(
                visible = fabVisible,
                vm = vm,
                modifier = positionModifier,
            )
        }
    ) {
        Column(modifier = M.padding(8.dp, 8.dp, 8.dp, 2.dp)) {
            HistoryHeader(vm = vm)
            // Use a Debouncer here as there could be LOTS of history records.
            val debouncer = remember { LockedDebouncer() }
            SearchBox(vm.searchEnabled, vm.filter) {
                debouncer.debounce {
                    vm.reload(ctx)
                }
            }
            HistoryList(
                lazyState = listState,
                vm = vm,
            )
        }
    }
}

@Composable
fun HistoryHeader(
    vm: HistoryViewModel,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val C = G.palette
    val isPaused = G.pausedUntil.value > System.currentTimeMillis()
    val remainingMinutes = if (isPaused) spam.blocker.ui.setting.PauseHelper.remainingMinutes(G.pausedUntil.value) else 0

    Column(modifier = modifier) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            SettingItem(
                modifier = Modifier.weight(1f),
                labelId = R.string.global_blocking,
                checked = G.globallyEnabled.value && !isPaused,
                onCheckedChange = { enabled ->
                    setGlobalEnabled(ctx, enabled)
                }
            )
            if (G.globallyEnabled.value && !isPaused) {
                val pauseItems = remember {
                    listOf(
                        spam.blocker.ui.widgets.LabelItem(label = ctx.getString(R.string.pause_15m)) {
                            spam.blocker.ui.setting.pauseBlocking(ctx, 15 * 60 * 1000L)
                            it.value = false
                        },
                        spam.blocker.ui.widgets.LabelItem(label = ctx.getString(R.string.pause_1h)) {
                            spam.blocker.ui.setting.pauseBlocking(ctx, 60 * 60 * 1000L)
                            it.value = false
                        },
                        spam.blocker.ui.widgets.LabelItem(label = ctx.getString(R.string.pause_8h)) {
                            spam.blocker.ui.setting.pauseBlocking(ctx, 8 * 60 * 60 * 1000L)
                            it.value = false
                        }
                    )
                }
                spam.blocker.ui.widgets.MenuButton(
                    label = spam.blocker.ui.widgets.Str(R.string.pause_blocking),
                    items = pauseItems,
                    color = C.textGrey,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        if (isPaused) {
            spam.blocker.ui.widgets.RowVCenterSpaced(8, modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)) {
                Text(
                    text = stringResource(R.string.paused_remaining, remainingMinutes),
                    color = C.warning,
                    fontSize = 11.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                spam.blocker.ui.widgets.StrokeButton(
                    label = spam.blocker.ui.widgets.Str(R.string.resume),
                    color = C.teal200,
                ) {
                    spam.blocker.ui.setting.resumeBlocking(ctx)
                }
            }
        }
        if (vm.weeklyBlockedCount.value > 0) {
            Text(
                text = stringResource(R.string.blocked_last_7_days, vm.weeklyBlockedCount.value),
                color = G.palette.textGrey,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
            )
        }
    }
}
