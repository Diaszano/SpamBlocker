package spam.blocker.ui.setting

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.R
import spam.blocker.ui.main.MainActivity
import spam.blocker.ui.theme.ThemeAppearance
import spam.blocker.ui.theme.ThemeFamily
import spam.blocker.util.spf

@RunWith(AndroidJUnit4::class)
class SettingsFlowsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsSectionsAreReachableAndThemeChoiceCanBeChanged() {
        compose.onNodeWithText(compose.activity.getString(R.string.tab_settings), useUnmergedTree = true)
            .performClick()

        listOf(
            R.string.instant_query,
            R.string.report_number,
            R.string.automation,
            R.string.miscellaneous,
        ).forEach { id ->
            compose.onNodeWithText(compose.activity.getString(id), substring = true)
                .performScrollTo()
                .assertExists()
        }

        val ctx = compose.activity
        compose.onNodeWithText(ctx.getString(R.string.theme), substring = true)
            .performScrollTo()
            .assertExists()

        val themePrefs = spf.Theme(ctx)
        val selectedFamilyLabel = when (themePrefs.themeFamily) {
            ThemeFamily.CATPPUCCIN.name -> R.string.theme_catppuccin
            ThemeFamily.CUSTOM.name -> R.string.custom
            else -> R.string.theme_truecaller
        }
        compose.onNodeWithText(ctx.getString(selectedFamilyLabel))
            .performClick()
        compose.onNodeWithText(ctx.getString(R.string.theme_catppuccin))
            .performClick()
        assertEquals(ThemeFamily.CATPPUCCIN.name, themePrefs.themeFamily)

        val selectedAppearanceLabel = when (themePrefs.themeAppearance) {
            ThemeAppearance.DARK.name -> R.string.appearance_dark
            ThemeAppearance.LIGHT.name -> R.string.appearance_light
            else -> R.string.appearance_system
        }
        compose.onAllNodesWithText(ctx.getString(selectedAppearanceLabel))[1]
            .performClick()
        compose.onNodeWithText(ctx.getString(R.string.appearance_dark))
            .performClick()
        assertEquals("DARK", spf.Theme(ctx).themeAppearance)
    }
}
