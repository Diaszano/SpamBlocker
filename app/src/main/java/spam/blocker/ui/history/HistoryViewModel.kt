package spam.blocker.ui.history

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import spam.blocker.db.CallTable
import spam.blocker.db.HistoryRecord
import spam.blocker.db.HistoryTable
import spam.blocker.db.SmsTable
import spam.blocker.def.Def
import spam.blocker.ui.history.HistoryOptions.showHistoryBlocked
import spam.blocker.ui.history.HistoryOptions.showHistoryPassed
import spam.blocker.util.Contacts
import spam.blocker.util.FuzzyFilter

/*
  To simplify the code, this view model is used in GlobalVariables instead of viewModel<...>().
 */
open class HistoryViewModel(
    val forType: Int,
    val table: HistoryTable,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ViewModel() {
    val records = mutableStateListOf<HistoryRecord>()
    val searchEnabled = mutableStateOf(false)
    val filter = mutableStateOf("")
    val isLoading = mutableStateOf(true)
    val loadError = mutableStateOf(false)
    val weeklyBlockedCount = mutableIntStateOf(0)

    private var reloadCounter = 0
    var reloadJob: Job? = null
        internal set

    fun reload(ctx: Context): Job {
        val token = ++reloadCounter
        reloadJob?.cancel()
        isLoading.value = true
        val job = CoroutineScope(ioDispatcher).launch {
            try {
                // Fuzzy search
                val filter = FuzzyFilter(filter.value)

                val newRecords = table.listRecords(ctx).filter {
                    isVisible(ctx, it, filter)
                }

                val sevenDaysAgo = spam.blocker.util.Now.currentMillis() - 7 * 24 * 3600 * 1000L
                val count = table.countBlockedSince(ctx, sevenDaysAgo)

                withContext(mainDispatcher) {
                    if (token == reloadCounter) {
                        records.clear()
                        records.addAll(newRecords)
                        weeklyBlockedCount.intValue = count
                        loadError.value = false
                    }
                }
            } catch (e: CancellationException) {
                return@launch
            } catch (e: Throwable) {
                withContext(mainDispatcher) {
                    if (token == reloadCounter) {
                        loadError.value = true
                    }
                }
            } finally {
                withContext(mainDispatcher) {
                    if (token == reloadCounter) {
                        isLoading.value = false
                    }
                }
            }
        }
        reloadJob = job
        return job
    }

    fun isVisible(
        ctx: Context,
        record: HistoryRecord,

        // provide this param when calling this function repetitively (for better performance)
        filter: FuzzyFilter ?= null
    ) : Boolean {
        // 1. show or not
        val show = (showHistoryPassed.value && record.isNotBlocked()) || (showHistoryBlocked.value && record.isBlocked())
        if (!show)
            return false

        // 2. fuzzy filter by keywords
        return if(!searchEnabled.value) { // not filtering
            true
        } else {
            if (filter == null) {
                true
            } else {
                val contactName = Contacts.cache.findContactByRawNumber(ctx, record.peer)?.name ?: ""
                val allText = record.peer + contactName + (record.extraInfo ?: "") + record.reason
                filter.matches(text = allText)
            }
        }
    }
    fun updateRecord(recordId: Long, changes: HistoryRecord.() -> HistoryRecord) {
        val index = records.indexOfFirst { it.id == recordId }
        if (index != -1) {
            records[index] = records[index].let(changes)
        }
    }

    fun markAllAsRead(ctx: Context) {
        val read = records.map { it.copy(read = true) }
        records.apply {
            clear()
            addAll(read)
        }
        table.markAllAsRead(ctx)
    }
}

class CallViewModel(
    table: HistoryTable = CallTable(),
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : HistoryViewModel(Def.ForNumber, table, ioDispatcher, mainDispatcher)

class SmsViewModel(
    table: HistoryTable = SmsTable(),
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : HistoryViewModel(Def.ForSms, table, ioDispatcher, mainDispatcher)
