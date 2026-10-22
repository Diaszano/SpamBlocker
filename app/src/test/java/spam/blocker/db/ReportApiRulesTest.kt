package spam.blocker.db

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import spam.blocker.def.Def.RESULT_ALLOWED_BY_NUMBER_REGEX
import spam.blocker.def.Def.RESULT_BLOCKED_BY_MEETING_MODE
import spam.blocker.def.Def.RESULT_BLOCKED_BY_NUMBER_REGEX
import spam.blocker.def.Def.RESULT_BLOCKED_BY_STIR
import spam.blocker.service.checker.ByNonContact
import spam.blocker.service.checker.ByRegexRule
import spam.blocker.service.checker.BySTIR
import spam.blocker.service.checker.ICheckResult

class ReportApiRulesTest {
    @Test
    fun defaultTypesEnableNonContactStirAndRegexReports() {
        val api = ReportApi()

        assertTrue(api.enabledForBlockReason(ByNonContact()))
        assertTrue(api.enabledForBlockReason(BySTIR(RESULT_BLOCKED_BY_STIR, 0)))
        assertTrue(api.enabledForBlockReason(regexResult("bank")))
    }

    @Test
    fun disabledReportTypeIsNotSelected() {
        val api = ReportApi(autoReportTypes = AutoReportTypes.NonContact)

        assertFalse(api.enabledForBlockReason(BySTIR(RESULT_BLOCKED_BY_STIR, 0)))
        assertFalse(api.enabledForBlockReason(regexResult("bank")))
    }

    @Test
    fun regexReportsMatchConfiguredDescriptionFilter() {
        val api = ReportApi(
            autoReportTypes = AutoReportTypes.Regex,
            autoReportRegexFilter = "bank",
        )

        assertTrue(api.enabledForBlockReason(regexResult("bank")))
        assertFalse(api.enabledForBlockReason(regexResult("loan")))
    }

    @Test
    fun missingRegexFilterMatchesAnyRuleDescription() {
        val api = ReportApi(autoReportTypes = AutoReportTypes.Regex)

        assertTrue(api.enabledForBlockReason(regexResult("loan")))
    }

    @Test
    fun otherAndAllowedReasonsAreNotReported() {
        val api = ReportApi()
        val unrelatedBlockedReason = object : ICheckResult {
            override val byType = RESULT_BLOCKED_BY_MEETING_MODE
            override fun resultReasonStr(ctx: android.content.Context) = "meeting mode"
        }

        assertFalse(api.enabledForBlockReason(unrelatedBlockedReason))
        assertFalse(api.enabledForBlockReason(regexResult("bank", RESULT_ALLOWED_BY_NUMBER_REGEX)))
    }

    private fun regexResult(description: String, byType: Int = RESULT_BLOCKED_BY_NUMBER_REGEX) =
        ByRegexRule(byType = byType, rule = RegexRule(description = description))
}
