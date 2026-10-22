package spam.blocker.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrefixUtilsTest {
    @Test
    fun testSuggestPrefixPattern() {
        assertEquals("1198765.*", PrefixUtils.suggestPrefixPattern("11987654321"))
        assertEquals("\\+55119876.*", PrefixUtils.suggestPrefixPattern("+551198765432"))
        assertEquals("12345.*", PrefixUtils.suggestPrefixPattern("12345678"))
        assertEquals("123.*", PrefixUtils.suggestPrefixPattern("123"))
        assertEquals("\\(123\\)4.*", PrefixUtils.suggestPrefixPattern("(123)4567"))
    }
}
