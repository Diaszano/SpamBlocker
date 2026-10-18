package spam.blocker.ui.history

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import spam.blocker.G
import spam.blocker.R
import spam.blocker.def.Def
import spam.blocker.ui.M
import spam.blocker.ui.history.HistoryOptions.historyTimeColors
import spam.blocker.ui.history.HistoryOptions.showHistoryBlocked
import spam.blocker.ui.history.HistoryOptions.showHistoryIndicator
import spam.blocker.ui.history.HistoryOptions.showHistoryPassed
import spam.blocker.ui.history.HistoryOptions.showHistoryTimeColor
import spam.blocker.ui.widgets.BgLaunchApp
import spam.blocker.ui.widgets.LazyScrollbar
import spam.blocker.ui.widgets.LeftDeleteSwipeWrapper
import spam.blocker.ui.widgets.SnackBar
import spam.blocker.ui.widgets.Str
import spam.blocker.ui.widgets.StrokeButton
import spam.blocker.ui.widgets.SwipeInfo
import spam.blocker.util.Launcher
import spam.blocker.util.SimUtils

fun historyEmptyMessageRes(
    passedEnabled: Boolean,
    blockedEnabled: Boolean,
    searchEnabled: Boolean,
    filter: String
): Int {
    return when {
        !passedEnabled && !blockedEnabled -> R.string.history_filters_off
        searchEnabled && filter.trim().isNotEmpty() -> R.string.history_no_matches
        else -> R.string.history_empty
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryList(
    lazyState: LazyListState,
    vm: HistoryViewModel,
) {
    val ctx = LocalContext.current
//    val spf = spf.HistoryOptions(ctx)

    val coroutineScope = rememberCoroutineScope()

    val simCount = remember {
        SimUtils.listSimCards(ctx).size
    }

    // Just a short alias, that G.xxx it too long
    var showIndicator by remember(showHistoryIndicator.value) {
        mutableStateOf(showHistoryIndicator.value)
    }

    if (vm.records.isEmpty()) {
        if (vm.isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Str(R.string.loading),
                    color = G.palette.textGrey,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }
        } else if (vm.loadError.value) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = Str(R.string.failed_to_load),
                    color = G.palette.textGrey,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                StrokeButton(
                    label = Str(R.string.retry),
                    color = G.palette.teal200,
                    onClick = { vm.reload(ctx) }
                )
            }
        } else {
            val emptyMessageRes = historyEmptyMessageRes(
                passedEnabled = showHistoryPassed.value,
                blockedEnabled = showHistoryBlocked.value,
                searchEnabled = vm.searchEnabled.value,
                filter = vm.filter.value,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Str(emptyMessageRes),
                    color = G.palette.textGrey,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            if (vm.loadError.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Str(R.string.failed_to_load),
                        color = G.palette.error,
                        fontSize = 14.sp
                    )
                    StrokeButton(
                        label = Str(R.string.retry),
                        color = G.palette.teal200,
                        onClick = { vm.reload(ctx) }
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                LazyScrollbar(state = lazyState) {
            IndicatorsWrapper(vm) { indicatorChecker, forceRefreshIndicators ->
                LazyColumn(
                    state = lazyState,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                itemsIndexed(items = vm.records, key = { _, it -> it.id }) { index, record ->
                    val indicators = remember { mutableStateOf(listOf<Indicator>()) }

                    LaunchedEffect(record.id, showIndicator, forceRefreshIndicators) {
                        indicators.value = if (showIndicator)
                            indicatorChecker(
                                record.peer,
                                record.cnap,
                                record.extraInfo,
                                record.simSlot
                            )
                        else
                            listOf()
                    }
                    HistoryContextMenuWrapper(vm, record) { contextMenuExpanded ->
                        // Swipe <---->
                        LeftDeleteSwipeWrapper(
                            right = SwipeInfo(
                                veto = true,
                                background = { BgLaunchApp(StartToEnd) },
                                onSwipe = {
                                    val index = vm.records.indexOfFirst { it.id == record.id }
                                    val record = vm.records[index]

                                    // Navigate to the default app, open the conversation to this number.
                                    if (!record.read) {
                                        // 1. update db
                                        vm.table.markAsRead(ctx, record.id)
                                        // 2. update UI
                                        vm.records[index] = vm.records[index].copy(read = true)
                                    }
                                    when (vm.forType) {
                                        Def.ForNumber -> Launcher.openCallConversation(
                                            ctx,
                                            record.peer
                                        )

                                        Def.ForSms -> Launcher.openSMSConversation(ctx, record.peer)
                                    }
                                }
                            ),
                            left = SwipeInfo(
                                onSwipe = {
                                    val index = vm.records.indexOfFirst { it.id == record.id }
                                    val rec = vm.records[index]

                                    // 1. delete from db
                                    vm.table.deleteById(ctx, rec.id)

                                    // 2. remove from ArrayList
                                    vm.records.removeAt(index)

                                    // 3. show snackbar
                                    SnackBar.show(
                                        coroutineScope,
                                        rec.peer,
                                        ctx.getString(R.string.undelete),
                                    ) {
                                        vm.table.addWithId(ctx, rec)
                                        vm.records.add(index, rec)
                                    }
                                },
                            )
                        ) {
                            HistoryCard(
                                forType = vm.forType,
                                record = record,
                                indicators = indicators.value,
                                simCount = simCount,
                                timeColors = if (showHistoryTimeColor.value) historyTimeColors else null,
                                modifier = M.combinedClickable(
                                    onClick = {
                                        if (!record.read) {
                                            // 1. update db
                                            vm.table.markAsRead(ctx, record.id)
                                            // 2. update UI
                                            vm.records[index] = vm.records[index].copy(read = true)
                                        }

                                        // Toggle expanded
                                        val rec = vm.records[index]
                                        // 1. update db
                                        vm.table.setExpanded(ctx, record.id, !rec.expanded)
                                        // 2. update ui
                                        vm.records[index] = rec.copy(expanded = !rec.expanded)
                                    },
                                    onLongClick = {
                                        contextMenuExpanded.value = true
                                    }
                                )
                            )
                        }
                    }
                }
            }
            }
        }
    }
}
}
}
