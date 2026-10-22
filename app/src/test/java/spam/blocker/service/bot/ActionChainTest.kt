package spam.blocker.service.bot

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ActionChainTest {
    @Test
    fun emptyActionListIsChainable() {
        assertTrue(emptyList<IAction>().allChainable())
    }

    @Test
    fun outputMustBeConsumedByTheNextAction() {
        assertFalse(listOf(BackupExport()).allChainable())
        assertTrue(listOf(BackupExport(), WriteFile()).allChainable())
    }

    @Test
    fun actionsWithIncompatibleInputAndOutputCannotBeChained() {
        assertFalse(listOf(WriteFile(), WriteFile()).allChainable())
    }
}
