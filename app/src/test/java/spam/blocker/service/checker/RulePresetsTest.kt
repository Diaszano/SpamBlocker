package spam.blocker.service.checker

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import spam.blocker.def.Def

class RulePresetsTest {
    @Test
    fun testBrazilTelemarketing0303Matches() {
        val pattern = RulePresets.PAT_BR_TELEMARKETING.toRegex()
        assertTrue(pattern.containsMatchIn("03031234567"))
        assertTrue(pattern.containsMatchIn("+553031234567"))
        assertTrue(pattern.containsMatchIn("03049876543"))
        assertTrue(pattern.containsMatchIn("+5503031234567"))
    }

    @Test
    fun testSuspiciousUrlPatternMatches() {
        val pattern = RulePresets.PAT_SMS_SUSPICIOUS_URL.toRegex(RegexOption.IGNORE_CASE)
        assertTrue(pattern.containsMatchIn("Acesse bit.ly/promo agora"))
        assertTrue(pattern.containsMatchIn("Seu pacote: http://192.168.1.1/track"))
        assertTrue(pattern.containsMatchIn("Clique aqui: tinyurl.com/xyz"))
        assertTrue(pattern.containsMatchIn("Acesse cutt.ly/promo"))
    }

    @Test
    fun satellitePremiumPatternMatchesSupportedPrefixesOnly() {
        val pattern = RulePresets.PAT_SATELLITE_PREMIUM.toRegex()

        listOf("+881123", "+882123", "+883123", "+870123").forEach {
            assertTrue(pattern.containsMatchIn(it), it)
        }
        assertFalse(pattern.containsMatchIn("+880123"))
        assertFalse(pattern.containsMatchIn("881123"))
    }

    @Test
    fun presetFamiliesKeepTheirExpectedRuleTypesAndFlags() {
        val numberPresets = RulePresets.getNumberPresets()
        val contentPresets = RulePresets.getContentPresets()

        assertEquals(2, numberPresets.size)
        assertTrue(numberPresets.all { it.isBlacklist && it.flags == Def.FLAG_REGEX_RAW_NUMBER })
        assertEquals(1, contentPresets.size)
        assertTrue(contentPresets.single().isBlacklist)
        assertEquals(Def.DefaultRegexFlags, contentPresets.single().flags)
    }
}
