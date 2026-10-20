package spam.blocker.ui.setting.regex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import spam.blocker.db.RegexRule
import spam.blocker.def.Def

class RegexStateUnitTest {

    @Test
    fun roundTripPreservesEnabledRuleFieldsAndSchedule() {
        val rule = RegexRule(
            id = 12,
            pattern = "^555",
            patternFlags = 5,
            patternModeType = 3,
            patternExtra = "^123$",
            patternExtraFlags = 7,
            patternExtraModeType = 4,
            description = "fixture rule",
            priority = 17,
            isBlacklist = false,
            flags = Def.FLAG_FOR_CALL or Def.FLAG_FOR_NUMBER or Def.FLAG_FOR_PASSED,
            channel = "fixture-channel",
            schedule = "12345,09:00-17:00,true",
            blockType = 2,
            blockTypeConfig = "fixture-config",
            simSlot = 1,
        )

        val result = rule.toState().toRule()

        assertEquals(rule.id, result.id)
        assertEquals(rule.pattern, result.pattern)
        assertEquals(rule.patternFlags, result.patternFlags)
        assertEquals(rule.patternModeType, result.patternModeType)
        assertEquals(rule.patternExtra, result.patternExtra)
        assertEquals(rule.patternExtraFlags, result.patternExtraFlags)
        assertEquals(rule.patternExtraModeType, result.patternExtraModeType)
        assertEquals(rule.description, result.description)
        assertEquals(rule.priority, result.priority)
        assertEquals(rule.isBlacklist, result.isBlacklist)
        assertEquals(rule.flags, result.flags)
        assertEquals(rule.channel, result.channel)
        assertEquals(rule.schedule, result.schedule)
        assertEquals(rule.blockType, result.blockType)
        assertEquals(rule.blockTypeConfig, result.blockTypeConfig)
        assertEquals(rule.simSlot, result.simSlot)
    }

    @Test
    fun roundTripClearsDisabledParticularPatternAndDefaultSchedule() {
        val rule = RegexRule(
            pattern = "555",
            patternExtra = "",
            patternExtraFlags = 7,
            schedule = "",
        )

        val result = rule.toState().toRule()

        assertEquals("", result.patternExtra)
        assertEquals(Def.DefaultRegexFlags, result.patternExtraFlags)
        assertEquals("", result.schedule)
    }
}
