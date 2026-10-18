package spam.blocker.ui.history

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import spam.blocker.db.HistoryRecord
import spam.blocker.db.HistoryTable

class HistoryLoadUnitTest {

    private class FakeHistoryTable(
        var shouldThrow: Boolean = false,
        var recordsToReturn: List<HistoryRecord> = emptyList(),
    ) : HistoryTable("test_history") {
        override fun listRecords(ctx: Context): List<HistoryRecord> {
            if (shouldThrow) throw RuntimeException("Simulated DB query error")
            return recordsToReturn
        }

        override fun countBlockedSince(ctx: Context, sinceMillis: Long): Int = 0
    }

    private class MinimalContext : android.content.ContextWrapper(null)

    @BeforeEach
    fun setUp() {
        HistoryOptions.showHistoryPassed.value = true
        HistoryOptions.showHistoryBlocked.value = true
    }

    @Test
    fun testSuccessfulReloadUpdatesRecordsAndClearsError() = runBlocking {
        val table = FakeHistoryTable(recordsToReturn = listOf(HistoryRecord(id = 1L, peer = "12345")))
        val vm = HistoryViewModel(0, table, Dispatchers.Unconfined, Dispatchers.Unconfined)
        val ctx = MinimalContext()

        vm.reload(ctx).join()

        assertFalse(vm.isLoading.value)
        assertFalse(vm.loadError.value)
        assertEquals(1, vm.records.size)
        assertEquals(1L, vm.records[0].id)
    }

    @Test
    fun testFailedReloadRetainsExistingRecordsAndSetsError() = runBlocking {
        val table = FakeHistoryTable(recordsToReturn = listOf(HistoryRecord(id = 1L, peer = "12345")))
        val vm = HistoryViewModel(0, table, Dispatchers.Unconfined, Dispatchers.Unconfined)
        val ctx = MinimalContext()

        // 1. Initial success
        vm.reload(ctx).join()
        assertEquals(1, vm.records.size)
        assertFalse(vm.loadError.value)

        // 2. Failure occurs: simulate missing table
        table.shouldThrow = true
        vm.reload(ctx).join()

        assertFalse(vm.isLoading.value)
        assertTrue(vm.loadError.value)
        assertEquals(1, vm.records.size)
        assertEquals(1L, vm.records[0].id)
    }

    @Test
    fun testRetryAfterFailureSucceeds() = runBlocking {
        val table = FakeHistoryTable(shouldThrow = true)
        val vm = HistoryViewModel(0, table, Dispatchers.Unconfined, Dispatchers.Unconfined)
        val ctx = MinimalContext()

        // Initial attempt fails
        vm.reload(ctx).join()
        assertTrue(vm.loadError.value)
        assertFalse(vm.isLoading.value)
        assertTrue(vm.records.isEmpty())

        // Retry succeeds
        table.shouldThrow = false
        table.recordsToReturn = listOf(HistoryRecord(id = 42L, peer = "99999"))
        vm.reload(ctx).join()

        assertFalse(vm.isLoading.value)
        assertFalse(vm.loadError.value)
        assertEquals(1, vm.records.size)
        assertEquals(42L, vm.records[0].id)
    }
}
