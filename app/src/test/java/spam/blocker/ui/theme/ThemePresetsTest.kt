package spam.blocker.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ThemePresetsTest {

    private val allKeys = listOf(
        "text", "info", "warning", "success", "error",
        "priority", "regexFlags", "enabled", "disabled",
        "dialogBg", "background"
    )

    private fun luminance(color: Color): Double {
        fun channel(c: Float): Double {
            return if (c <= 0.04045f) c / 12.92
            else Math.pow((c + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }

    private fun contrastRatio(c1: Color, c2: Color): Double {
        val l1 = luminance(c1)
        val l2 = luminance(c2)
        val max = Math.max(l1, l2)
        val min = Math.min(l1, l2)
        return (max + 0.05) / (min + 0.05)
    }

    @Test
    fun testEveryPresetContainsAllPaletteKeys() {
        val families = listOf(ThemeFamily.TRUECALLER, ThemeFamily.CATPPUCCIN)
        val modes = listOf(false, true)

        for (family in families) {
            for (dark in modes) {
                val colors = presetColors(family, dark)
                for (key in allKeys) {
                    assertTrue(colors.containsKey(key), "Missing key '$key' in preset $family (dark=$dark)")
                    assertNotNull(colors[key], "Null color for key '$key' in preset $family (dark=$dark)")
                }
                assertEquals(allKeys.size, colors.size, "Unexpected extra or missing keys in preset $family (dark=$dark)")
            }
        }

        // Custom family should return empty map from presetColors
        val customColors = presetColors(ThemeFamily.CUSTOM, dark = false)
        assertTrue(customColors.isEmpty(), "Custom family should return empty preset map")
    }

    @Test
    fun testCatppuccinKeyColorsMatchOfficialPalette() {
        // Catppuccin Latte (Light)
        val latte = presetColors(ThemeFamily.CATPPUCCIN, dark = false)
        assertEquals(Color(0xFFEFF1F5), latte["background"], "Latte base must match #EFF1F5")
        assertEquals(Color(0xFF4C4F69), latte["text"], "Latte text must match #4C4F69")
        assertEquals(Color(0xFF1E66F5), latte["info"], "Latte blue must match #1E66F5")
        assertEquals(Color(0xFF179299), latte["enabled"], "Latte teal must match #179299")
        assertEquals(Color(0xFFD20F39), latte["error"], "Latte red must match #D20F39")
        assertEquals(Color(0xFF40A02B), latte["success"], "Latte green must match #40A02B")
        assertEquals(Color(0xFFCCD0DA), latte["dialogBg"], "Latte surface0 must match #CCD0DA")

        // Catppuccin Macchiato (Dark)
        val macchiato = presetColors(ThemeFamily.CATPPUCCIN, dark = true)
        assertEquals(Color(0xFF24273A), macchiato["background"], "Macchiato base must match #24273A")
        assertEquals(Color(0xFFCAD3F5), macchiato["text"], "Macchiato text must match #CAD3F5")
        assertEquals(Color(0xFF8AADF4), macchiato["info"], "Macchiato blue must match #8AADF4")
        assertEquals(Color(0xFF8BD5CA), macchiato["enabled"], "Macchiato teal must match #8BD5CA")
        assertEquals(Color(0xFFED8796), macchiato["error"], "Macchiato red must match #ED8796")
        assertEquals(Color(0xFFA6DA95), macchiato["success"], "Macchiato green must match #A6DA95")
        assertEquals(Color(0xFF363A4F), macchiato["dialogBg"], "Macchiato surface0 must match #363A4F")
    }

    @Test
    fun testPlannedContrastThresholds() {
        val presets = listOf(
            presetColors(ThemeFamily.TRUECALLER, dark = false),
            presetColors(ThemeFamily.TRUECALLER, dark = true),
            presetColors(ThemeFamily.CATPPUCCIN, dark = false),
            presetColors(ThemeFamily.CATPPUCCIN, dark = true),
        )

        for (colors in presets) {
            val bg = colors.getValue("background")
            val dialogBg = colors.getValue("dialogBg")
            val text = colors.getValue("text")
            val enabled = colors.getValue("enabled")
            val disabled = colors.getValue("disabled")
            val info = colors.getValue("info")
            val error = colors.getValue("error")

            // Normal text on surfaces: at least 4.5:1
            val textOnBg = contrastRatio(text, bg)
            assertTrue(textOnBg >= 4.5, "Text on background contrast $textOnBg must be >= 4.5:1")

            val textOnDialog = contrastRatio(text, dialogBg)
            assertTrue(textOnDialog >= 4.5, "Text on dialogBg contrast $textOnDialog must be >= 4.5:1")

            // Controls and significant UI on surface: at least 3.0:1
            val enabledOnBg = contrastRatio(enabled, bg)
            assertTrue(enabledOnBg >= 3.0, "Enabled control on background contrast $enabledOnBg must be >= 3.0:1")

            val disabledOnBg = contrastRatio(disabled, bg)
            assertTrue(disabledOnBg >= 3.0, "Disabled indicator on background contrast $disabledOnBg must be >= 3.0:1")

            val infoOnBg = contrastRatio(info, bg)
            assertTrue(infoOnBg >= 3.0, "Info action on background contrast $infoOnBg must be >= 3.0:1")

            val errorOnBg = contrastRatio(error, bg)
            assertTrue(errorOnBg >= 3.0, "Error indicator on background contrast $errorOnBg must be >= 3.0:1")
        }

        // Truecaller status colors contrast (>= 3.0:1)
        for (dark in listOf(false, true)) {
            val tc = presetColors(ThemeFamily.TRUECALLER, dark)
            val bg = tc.getValue("background")
            val success = tc.getValue("success")
            val warning = tc.getValue("warning")

            val successOnBg = contrastRatio(success, bg)
            assertTrue(successOnBg >= 3.0, "Truecaller success on background contrast $successOnBg must be >= 3.0:1")

            val warningOnBg = contrastRatio(warning, bg)
            assertTrue(warningOnBg >= 3.0, "Truecaller warning on background contrast $warningOnBg must be >= 3.0:1")
        }
    }
}
