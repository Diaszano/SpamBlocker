package spam.blocker.service.bot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import spam.blocker.db.RegexRule
import spam.blocker.qa.QaTestEnvironment

class ParseCsvActionTest {
    @Test
    fun missingPatternHeaderFailsWithoutReplacingInput() {
        val input = "number\n12345\n".toByteArray()
        val context = ActionContext(lastOutput = input)

        assertFalse(ParseCSV().execute(QaTestEnvironment.context(), context))
        assertTrue(context.lastOutput === input)
    }

    @Test
    fun patternHeaderProducesRules() {
        val context = ActionContext(lastOutput = "pattern\n12345\n".toByteArray())

        assertTrue(ParseCSV().execute(QaTestEnvironment.context(), context))
        val rules = context.lastOutput as List<*>
        assertEquals(1, rules.size)
        assertEquals("12345", (rules.single() as RegexRule).pattern)
    }
}
