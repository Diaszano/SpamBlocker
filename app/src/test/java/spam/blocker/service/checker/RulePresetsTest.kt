package spam.blocker.service.checker

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
}
