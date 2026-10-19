package spam.blocker.ui.history

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
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

enum class HistoryListState { LOADING, ERROR, EMPTY, CONTENT }

fun historyListState(
    isLoading: Boolean,
    hasError: Boolean,
    hasRecords: Boolean
): HistoryListState {
    return when {
        hasRecords -> HistoryListState.CONTENT
        isLoading -> HistoryListState.LOADING
        hasError -> HistoryListState.ERROR
        else -> HistoryListState.EMPTY
    }
}

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

    val state = historyListState(
        isLoading = vm.isLoading.value,
        hasError = vm.hasError.value,
        hasRecords = vm.records.isNotEmpty()
    )

    when (state) {
        HistoryListState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = G.palette.teal200
                )
            }
        }
        HistoryListState.ERROR -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = Str(R.string.history_error_loading),
                        color = G.palette.error,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                    )
                    StrokeButton(
                        label = Str(R.string.retry),
                        color = G.palette.teal200,
                        onClick = { vm.retry(ctx) }
                    )
                }
            }
        }
        HistoryListState.EMPTY -> {
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
        HistoryListState.CONTENT -> {
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
                    val onOpen = {
                        val i = vm.records.indexOfFirst { it.id == record.id }
                        if (i >= 0) {
                            val currRecord = vm.records[i]
                            // Navigate to the default app, open the conversation to this number.
                            if (!currRecord.read) {
                                // 1. update db
                                vm.table.markAsRead(ctx, currRecord.id)
                                // 2. update UI
                                vm.records[i] = currRecord.copy(read = true)
                            }
                            when (vm.forType) {
                                Def.ForNumber -> Launcher.openCallConversation(
                                    ctx,
                                    currRecord.peer
                                )

                                Def.ForSms -> Launcher.openSMSConversation(ctx, currRecord.peer)
                            }
                        }
                    }

                    val onDelete = {
                        val i = vm.records.indexOfFirst { it.id == record.id }
                        if (i >= 0) {
                            val rec = vm.records[i]

                            // 1. delete from db
                            vm.table.deleteById(ctx, rec.id)

                            // 2. remove from ArrayList
                            vm.records.removeAt(i)

                            // 3. show snackbar
                            SnackBar.show(
                                coroutineScope,
                                rec.peer,
                                ctx.getString(R.string.undelete),
                            ) {
                                vm.table.addWithId(ctx, rec)
                                val insertIndex = i.coerceAtMost(vm.records.size)
                                vm.records.add(insertIndex, rec)
                            }
                        }
                    }

                    HistoryContextMenuWrapper(
                        vm = vm,
                        record = record,
                        onOpen = onOpen,
                        onDelete = onDelete,
                    ) { contextMenuExpanded ->
                        // Swipe <---->
                        LeftDeleteSwipeWrapper(
                            right = SwipeInfo(
                                veto = true,
                                background = { BgLaunchApp(StartToEnd) },
                                onSwipe = onOpen
                            ),
                            left = SwipeInfo(
                                onSwipe = onDelete
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
                                ),
                                onMenuClick = {
                                    contextMenuExpanded.value = true
                                }
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
