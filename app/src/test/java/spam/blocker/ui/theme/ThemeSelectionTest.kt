package spam.blocker.ui.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ThemeSelectionTest {

    @Test
    fun testDefaultChoiceForNewInstallWithoutCustomColors() {
        val choice = resolveThemeChoice(
            savedFamily = null,
            savedAppearance = null,
            hasSavedCustomColors = false
        )
        assertEquals(ThemeFamily.TRUECALLER, choice.family)
        assertEquals(ThemeAppearance.SYSTEM, choice.appearance)
    }

    @Test
    fun testDefaultChoiceForExistingInstallWithCustomColors() {
        val choice = resolveThemeChoice(
            savedFamily = null,
            savedAppearance = null,
            hasSavedCustomColors = true
        )
        assertEquals(ThemeFamily.CUSTOM, choice.family)
        assertEquals(ThemeAppearance.SYSTEM, choice.appearance)
    }

    @Test
    fun testSavedValidChoicesRoundTrip() {
        val truecallerLight = resolveThemeChoice("TRUECALLER", "LIGHT", false)
        assertEquals(ThemeChoice(ThemeFamily.TRUECALLER, ThemeAppearance.LIGHT), truecallerLight)

        val truecallerDark = resolveThemeChoice("TRUECALLER", "DARK", false)
        assertEquals(ThemeChoice(ThemeFamily.TRUECALLER, ThemeAppearance.DARK), truecallerDark)

        val catppuccinSystem = resolveThemeChoice("CATPPUCCIN", "SYSTEM", false)
        assertEquals(ThemeChoice(ThemeFamily.CATPPUCCIN, ThemeAppearance.SYSTEM), catppuccinSystem)

        val catppuccinLight = resolveThemeChoice("CATPPUCCIN", "LIGHT", false)
        assertEquals(ThemeChoice(ThemeFamily.CATPPUCCIN, ThemeAppearance.LIGHT), catppuccinLight)

        val catppuccinDark = resolveThemeChoice("CATPPUCCIN", "DARK", false)
        assertEquals(ThemeChoice(ThemeFamily.CATPPUCCIN, ThemeAppearance.DARK), catppuccinDark)

        val customSystem = resolveThemeChoice("CUSTOM", "SYSTEM", false)
        assertEquals(ThemeChoice(ThemeFamily.CUSTOM, ThemeAppearance.SYSTEM), customSystem)

        // Saved valid family should be respected even if hasSavedCustomColors is true
        val catppuccinWithCustom = resolveThemeChoice("CATPPUCCIN", "DARK", true)
        assertEquals(ThemeChoice(ThemeFamily.CATPPUCCIN, ThemeAppearance.DARK), catppuccinWithCustom)
    }

    @Test
    fun testUnknownValuesFallBackSafely() {
        // Unknown family without custom colors falls back to TRUECALLER
        val unknownNoCustom = resolveThemeChoice("UNKNOWN_FAMILY", "SYSTEM", false)
        assertEquals(ThemeFamily.TRUECALLER, unknownNoCustom.family)
        assertEquals(ThemeAppearance.SYSTEM, unknownNoCustom.appearance)

        // Unknown family with custom colors falls back to CUSTOM
        val unknownWithCustom = resolveThemeChoice("INVALID", "SYSTEM", true)
        assertEquals(ThemeFamily.CUSTOM, unknownWithCustom.family)
        assertEquals(ThemeAppearance.SYSTEM, unknownWithCustom.appearance)

        // Unknown appearance falls back to SYSTEM
        val unknownAppearance = resolveThemeChoice("CATPPUCCIN", "NOT_AN_APPEARANCE", false)
        assertEquals(ThemeFamily.CATPPUCCIN, unknownAppearance.family)
        assertEquals(ThemeAppearance.SYSTEM, unknownAppearance.appearance)

        // Both unknown
        val bothUnknown = resolveThemeChoice("INVALID_FAMILY", "INVALID_APPEARANCE", false)
        assertEquals(ThemeFamily.TRUECALLER, bothUnknown.family)
        assertEquals(ThemeAppearance.SYSTEM, bothUnknown.appearance)

        // Empty strings
        val emptyStrings = resolveThemeChoice("", "", false)
        assertEquals(ThemeFamily.TRUECALLER, emptyStrings.family)
        assertEquals(ThemeAppearance.SYSTEM, emptyStrings.appearance)
    }

    @Test
    fun testIsDarkForPresetsFollowsAppearanceAndSystem() {
        // TRUECALLER
        val truecallerSystem = ThemeChoice(ThemeFamily.TRUECALLER, ThemeAppearance.SYSTEM)
        assertTrue(truecallerSystem.isDark(systemIsDark = true, customBackgroundIsDark = false))
        assertFalse(truecallerSystem.isDark(systemIsDark = false, customBackgroundIsDark = true))

        val truecallerLight = ThemeChoice(ThemeFamily.TRUECALLER, ThemeAppearance.LIGHT)
        assertFalse(truecallerLight.isDark(systemIsDark = true, customBackgroundIsDark = true))
        assertFalse(truecallerLight.isDark(systemIsDark = false, customBackgroundIsDark = true))

        val truecallerDark = ThemeChoice(ThemeFamily.TRUECALLER, ThemeAppearance.DARK)
        assertTrue(truecallerDark.isDark(systemIsDark = false, customBackgroundIsDark = false))
        assertTrue(truecallerDark.isDark(systemIsDark = true, customBackgroundIsDark = false))

        // CATPPUCCIN
        val catppuccinSystem = ThemeChoice(ThemeFamily.CATPPUCCIN, ThemeAppearance.SYSTEM)
        assertTrue(catppuccinSystem.isDark(systemIsDark = true, customBackgroundIsDark = false))
        assertFalse(catppuccinSystem.isDark(systemIsDark = false, customBackgroundIsDark = true))

        val catppuccinLight = ThemeChoice(ThemeFamily.CATPPUCCIN, ThemeAppearance.LIGHT)
        assertFalse(catppuccinLight.isDark(systemIsDark = true, customBackgroundIsDark = true))

        val catppuccinDark = ThemeChoice(ThemeFamily.CATPPUCCIN, ThemeAppearance.DARK)
        assertTrue(catppuccinDark.isDark(systemIsDark = false, customBackgroundIsDark = false))
    }

    @Test
    fun testIsDarkForCustomFollowsCustomBackgroundLuminance() {
        // Custom always follows customBackgroundIsDark regardless of systemIsDark or appearance
        val customSystem = ThemeChoice(ThemeFamily.CUSTOM, ThemeAppearance.SYSTEM)
        assertTrue(customSystem.isDark(systemIsDark = false, customBackgroundIsDark = true))
        assertFalse(customSystem.isDark(systemIsDark = true, customBackgroundIsDark = false))

        val customLight = ThemeChoice(ThemeFamily.CUSTOM, ThemeAppearance.LIGHT)
        assertTrue(customLight.isDark(systemIsDark = false, customBackgroundIsDark = true))
        assertFalse(customLight.isDark(systemIsDark = true, customBackgroundIsDark = false))

        val customDark = ThemeChoice(ThemeFamily.CUSTOM, ThemeAppearance.DARK)
        assertTrue(customDark.isDark(systemIsDark = false, customBackgroundIsDark = true))
        assertFalse(customDark.isDark(systemIsDark = true, customBackgroundIsDark = false))
    }
}
