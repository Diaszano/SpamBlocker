package spam.blocker.ui.setting.regex

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import spam.blocker.db.NumberRegexTable
import spam.blocker.db.RegexRule

class RegexDeleteUnitTest {

    private class FakeRegexTable : NumberRegexTable() {
        val deletedIds = mutableListOf<Long>()
        val addedRules = mutableListOf<RegexRule>()

        override fun deleteById(ctx: Context, id: Long): Int {
            deletedIds.add(id)
            return 1
        }

        override fun addWithId(ctx: Context, item: RegexRule) {
            addedRules.add(item)
        }
    }

    private class MinimalContext : android.content.ContextWrapper(null)

    @Test
    fun testDeleteRuleRemovesFromListAndTable() {
        val ctx = MinimalContext()
        val table = FakeRegexTable()
        val rule1 = RegexRule(id = 10L, pattern = "^123.*")
        val rule2 = RegexRule(id = 20L, pattern = "^456.*")
        val list = mutableStateListOf(rule1, rule2)
        val scope = CoroutineScope(Dispatchers.Unconfined)

        deleteRule(ctx, 0, rule1, list, scope, table = table)

        assertEquals(1, list.size)
        assertEquals(20L, list[0].id)
        assertEquals(listOf(10L), table.deletedIds)
    }

    @Test
    fun testDeleteAbsentRuleIsNoOp() {
        val ctx = MinimalContext()
        val table = FakeRegexTable()
        val rule1 = RegexRule(id = 10L, pattern = "^123.*")
        val rule2 = RegexRule(id = 20L, pattern = "^456.*")
        val list = mutableStateListOf(rule1)
        val scope = CoroutineScope(Dispatchers.Unconfined)

        deleteRule(ctx, 0, rule2, list, scope, table = table)

        assertEquals(1, list.size)
        assertTrue(table.deletedIds.isEmpty())
    }

    @Test
    fun testDeleteAndUndoRestoresRule() {
        val ctx = MinimalContext()
        val table = FakeRegexTable()
        val rule1 = RegexRule(id = 10L, pattern = "^123.*")
        val rule2 = RegexRule(id = 20L, pattern = "^456.*")
        val list = mutableStateListOf(rule1, rule2)
        val scope = CoroutineScope(Dispatchers.Unconfined)

        var undoTriggered = false
        var undoAction: (() -> Unit)? = null

        deleteRule(
            ctx = ctx,
            forType = 0,
            rule = rule1,
            ruleList = list,
            coroutineScope = scope,
            table = table,
            snackbarHandler = { _, _, onUndo ->
                undoAction = onUndo
            }
        )

        assertEquals(1, list.size)
        assertEquals(20L, list[0].id)

        // Invoke undo
        undoAction?.invoke()

        assertEquals(2, list.size)
        assertEquals(10L, list[0].id)
        assertEquals(listOf(rule1), table.addedRules)
    }
}
