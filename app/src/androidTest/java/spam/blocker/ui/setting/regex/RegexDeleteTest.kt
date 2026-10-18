package spam.blocker.ui.setting.regex

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.db.NumberRegexTable
import spam.blocker.db.RegexRule

@RunWith(AndroidJUnit4::class)
class RegexDeleteTest {

    private class TestTable : NumberRegexTable() {
        val deleted = mutableListOf<Long>()
        val restored = mutableListOf<RegexRule>()
        override fun deleteById(ctx: Context, id: Long): Int {
            deleted.add(id)
            return 1
        }
        override fun addWithId(ctx: Context, item: RegexRule) {
            restored.add(item)
        }
    }

    @Test
    fun testDeleteAndUndoFromMenuOrSwipe() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val table = TestTable()
        val rule1 = RegexRule(id = 11L, pattern = "foo")
        val rule2 = RegexRule(id = 22L, pattern = "bar")
        val list = mutableStateListOf(rule1, rule2)
        val scope = CoroutineScope(Dispatchers.Unconfined)

        var undoCallback: (() -> Unit)? = null
        deleteRule(
            ctx = ctx,
            forType = 0,
            rule = rule1,
            ruleList = list,
            coroutineScope = scope,
            table = table,
            snackbarHandler = { _, _, onUndo ->
                undoCallback = onUndo
            }
        )

        assertEquals(1, list.size)
        assertEquals(22L, list[0].id)
        assertEquals(listOf(11L), table.deleted)

        // Undo restores rule
        undoCallback?.invoke()
        assertEquals(2, list.size)
        assertEquals(11L, list[0].id)
        assertEquals(listOf(rule1), table.restored)
    }
}
