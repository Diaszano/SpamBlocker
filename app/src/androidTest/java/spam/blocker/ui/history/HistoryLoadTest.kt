package spam.blocker.ui.history

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.db.HistoryRecord
import spam.blocker.db.HistoryTable

@RunWith(AndroidJUnit4::class)
class HistoryLoadTest {

    private class MissingTable(
        var missing: Boolean = true,
        var records: List<HistoryRecord> = emptyList(),
    ) : HistoryTable("missing_table") {
        override fun listRecords(ctx: Context): List<HistoryRecord> {
            if (missing) {
                throw android.database.sqlite.SQLiteException("no such table: missing_table")
            }
            return records
        }

        override fun countBlockedSince(ctx: Context, since: Long): Int = 0
    }

    @Test
    fun testLoadingFailureRetainsRecordsAndRetrySucceeds() {
        val table = MissingTable(missing = false, records = listOf(HistoryRecord(id = 100L, peer = "12345")))
        val vm = HistoryViewModel(0, table, Dispatchers.Unconfined, Dispatchers.Unconfined)
        val ctx = ApplicationProvider.getApplicationContext<Context>()

        // 1. Initial success
        vm.reload(ctx)
        assertFalse(vm.isLoading.value)
        assertFalse(vm.loadError.value)
        assertEquals(1, vm.records.size)

        // 2. Query fails (missing table) -> keep records, mark error, finish loading
        table.missing = true
        vm.reload(ctx)
        assertFalse(vm.isLoading.value)
        assertTrue(vm.loadError.value)
        assertEquals(1, vm.records.size)
        assertEquals(100L, vm.records[0].id)

        // 3. Retry succeeds -> clear error, update records
        table.missing = false
        table.records = listOf(HistoryRecord(id = 101L, peer = "67890"))
        vm.reload(ctx)
        assertFalse(vm.isLoading.value)
        assertFalse(vm.loadError.value)
        assertEquals(1, vm.records.size)
        assertEquals(101L, vm.records[0].id)
    }
}
