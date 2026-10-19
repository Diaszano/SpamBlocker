package spam.blocker.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.util.spf

@RunWith(AndroidJUnit4::class)
class ThemePersistenceTest {

    @Test
    fun testCustomColorPreservedWhenSwitchingThemes() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val palette = spf.Palette(ctx)

        // Save a custom color
        val customColor = Color(0xFF112233)
        val textDelegate = palette.allColors.first { it.key == "text" }
        textDelegate.update(customColor)
        assertTrue(palette.hasSavedCustomColors())
        assertEquals(customColor.toArgb(), palette.prefs.getInt("color_text", 0))

        // Choose TRUECALLER preset
        val truecallerPreset = presetColors(ThemeFamily.TRUECALLER, dark = false)
        palette.applyPreset(truecallerPreset)
        assertEquals(truecallerPreset["text"], textDelegate.state.value)
        assertEquals(customColor.toArgb(), palette.prefs.getInt("color_text", 0))

        // Choose CATPPUCCIN preset
        val catppuccinPreset = presetColors(ThemeFamily.CATPPUCCIN, dark = true)
        palette.applyPreset(catppuccinPreset)
        assertEquals(catppuccinPreset["text"], textDelegate.state.value)
        assertEquals(customColor.toArgb(), palette.prefs.getInt("color_text", 0))

        // Return to CUSTOM
        palette.restoreCustom()
        assertEquals(customColor.toArgb(), textDelegate.state.value.toArgb())
        assertEquals(customColor.toArgb(), palette.prefs.getInt("color_text", 0))
    }
}
