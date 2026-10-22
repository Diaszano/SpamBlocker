package spam.blocker.ui.setting.regex

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import spam.blocker.util.Csv

class CsvRuleImportTest {
    @Test
    fun rejectsCsvWithoutPatternHeader() {
        val csv = Csv(listOf("number"), listOf(listOf("12345")))

        assertNull(csv.toRegexRulesOrNull())
    }

}
