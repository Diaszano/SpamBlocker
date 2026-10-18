package spam.blocker.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ColorContrastTest {

    @Test
    fun testBlackAndWhiteContrastRatioIs21() {
        val ratio = contrastRatio(Color.White, Color.Black)
        assertEquals(21.0, ratio, 0.05)

        val inverseRatio = contrastRatio(Color.Black, Color.White)
        assertEquals(21.0, inverseRatio, 0.05)
    }

    @Test
    fun testIdenticalColorsContrastRatioIs1() {
        val ratio1 = contrastRatio(Color.Black, Color.Black)
        assertEquals(1.0, ratio1, 0.01)

        val ratio2 = contrastRatio(Color.Red, Color.Red)
        assertEquals(1.0, ratio2, 0.01)

        val ratio3 = contrastRatio(Color.White, Color.White)
        assertEquals(1.0, ratio3, 0.01)
    }

    @Test
    fun testReadableOnAll() {
        val white = Color.White
        val darkBg1 = Color(0xFF111111)
        val darkBg2 = Color(0xFF222222)
        val lightBg = Color(0xFFEEEEEE)

        assertTrue(readableOnAll(white, listOf(darkBg1, darkBg2), 4.5))
        assertFalse(readableOnAll(white, listOf(darkBg1, lightBg), 4.5))
    }

    @Test
    fun testLowContrastRejectedAndValidAccepted() {
        val darkBg = Color(0xFF111111)
        val lowContrastText = Color(0xFF222222)
        val goodContrastText = Color(0xFFDDDDDD)

        assertFalse(readableOnAll(lowContrastText, listOf(darkBg), 4.5))
        assertTrue(readableOnAll(goodContrastText, listOf(darkBg), 4.5))
    }

    @Test
    fun testBadgeContrastPairing() {
        val errorRed = Salmon
        val badgeText = badgeContentColor(errorRed)
        val ratio = contrastRatio(badgeText, errorRed)
        assertTrue(ratio >= 3.0)

        val darkBg = Color(0xFF660000)
        val darkBadgeText = badgeContentColor(darkBg)
        assertEquals(Color.White, darkBadgeText)
        assertTrue(contrastRatio(darkBadgeText, darkBg) >= 3.0)
    }
}
