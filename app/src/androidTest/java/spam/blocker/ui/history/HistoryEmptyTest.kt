package spam.blocker.ui.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import spam.blocker.R
import spam.blocker.db.CallTable

class HistoryEmptyTest {

    @Test
    fun testHistoryEmptyMessageSelection() {
        // 1. Both filters off takes top precedence
        assertEquals(
            R.string.history_filters_off,
            historyEmptyMessageRes(
                passedEnabled = false,
                blockedEnabled = false,
                searchEnabled = false,
                filter = ""
            )
        )
        assertEquals(
            R.string.history_filters_off,
            historyEmptyMessageRes(
                passedEnabled = false,
                blockedEnabled = false,
                searchEnabled = true,
                filter = "spam"
            )
        )

        // 2. Active search with non-empty query takes next precedence
        assertEquals(
            R.string.history_no_matches,
            historyEmptyMessageRes(
                passedEnabled = true,
                blockedEnabled = false,
                searchEnabled = true,
                filter = "spam"
            )
        )
        assertEquals(
            R.string.history_no_matches,
            historyEmptyMessageRes(
                passedEnabled = false,
                blockedEnabled = true,
                searchEnabled = true,
                filter = "12345"
            )
        )

        // 3. Search enabled with empty/whitespace query falls back to empty
        assertEquals(
            R.string.history_empty,
            historyEmptyMessageRes(
                passedEnabled = true,
                blockedEnabled = true,
                searchEnabled = true,
                filter = "   "
            )
        )

        // 4. Default empty state when filters are active and no search query
        assertEquals(
            R.string.history_empty,
            historyEmptyMessageRes(
                passedEnabled = true,
                blockedEnabled = true,
                searchEnabled = false,
                filter = ""
            )
        )
        assertEquals(
            R.string.history_empty,
            historyEmptyMessageRes(
                passedEnabled = true,
                blockedEnabled = false,
                searchEnabled = false,
                filter = ""
            )
        )
        assertEquals(
            R.string.history_empty,
            historyEmptyMessageRes(
                passedEnabled = false,
                blockedEnabled = true,
                searchEnabled = false,
                filter = ""
            )
        )
    }

    @Test
    fun testHistoryViewModelInitialLoadingState() {
        val vm = HistoryViewModel(0, CallTable())
        assertTrue(vm.isLoading.value)
    }
}
