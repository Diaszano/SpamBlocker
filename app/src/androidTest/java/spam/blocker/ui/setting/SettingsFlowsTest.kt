package spam.blocker.ui.setting
import android.Manifest

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.R
import spam.blocker.G
import spam.blocker.ui.main.MainActivity
import spam.blocker.ui.theme.ThemeAppearance
import spam.blocker.ui.theme.ThemeFamily
import spam.blocker.ui.theme.presetColors
import spam.blocker.util.spf
import spam.blocker.service.bot.BotTemplates
import spam.blocker.db.BotTable
import spam.blocker.db.Bot
import spam.blocker.db.PushAlertTable
import spam.blocker.ui.setting.regex.PushAlertViewModel
import spam.blocker.db.QueryApiTable
import spam.blocker.db.NumberRegexTable
import spam.blocker.db.RegexRule
import spam.blocker.qa.QaTestEnvironment
import spam.blocker.util.Permission
import spam.blocker.qa.QaDocumentProvider
import spam.blocker.config.Category
import spam.blocker.config.CategorySelection
import spam.blocker.config.Configs
import spam.blocker.config.NumberRules

@RunWith(AndroidJUnit4::class)
class SettingsFlowsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()
@get:Rule
val receiveSmsPermission = GrantPermissionRule.grant(Manifest.permission.RECEIVE_SMS)

    @Before
    fun startFromCallsTab() {
        val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        compose.onNode(tab.and(hasText(compose.activity.getString(R.string.tab_settings)))).performClick()
        compose.onNode(tab.and(hasText(compose.activity.getString(R.string.call)))).performClick()
    }

@Before
@After
fun clearQaApiAndBotRecords() {
val context = QaTestEnvironment.context()
QueryApiTable().clearAll(context)
BotTable.clearAll(context)
}

    @After
    fun resetSettingsSections() {
        spf.SettingSections(compose.activity).apply {
            isInstantQueryCollapsed = false
            isReportNumberCollapsed = false
            isAutomationCollapsed = false
            isMiscCollapsed = false
        }
    }

    @Test
    fun smsAlertAndSmsBombSwitchesPersistInQaPreferences() {
        val context = QaTestEnvironment.context()
        val smsAlert = spf.SmsAlert(context)
        val smsBomb = spf.SmsBomb(context)
        val originalAlertEnabled = smsAlert.isEnabled
        val originalBombEnabled = smsBomb.isEnabled
val originalAlertDuration = smsAlert.duration
val originalAlertRegex = smsAlert.regexStr
val originalBombInterval = smsBomb.interval
val originalBombRegex = smsBomb.regexStr
val originalLockscreenProtection = smsBomb.isLockScreenProtectionEnabled
        val originalSmsPermission = Permission.receiveSMS.isGranted
        val originalBatteryPermission = Permission.batteryUnRestricted.isGranted
        try {
            smsAlert.isEnabled = true
            smsBomb.isEnabled = true
            spf.SettingSections(context).apply {
                isScreeningSectionCollapsed = false
                isRegexSettingsCollapsed = false
            }
            Permission.receiveSMS.isGranted = true
Permission.batteryUnRestricted.isGranted = true
compose.activityRule.scenario.recreate()
            compose.onNodeWithText(context.getString(R.string.protection), useUnmergedTree = true)
                .performClick()

            assertTrue(smsAlert.isEnabled)
            assertTrue(smsBomb.isEnabled)
            compose.onNodeWithText("${smsAlert.duration} ${context.getString(R.string.seconds_short)}")
                .performScrollTo().performClick()
            val patternLabel = context.getString(R.string.sms_content_pattern)
fun dialogField(matcher: SemanticsMatcher): SemanticsNodeInteraction {
    val nodes = compose.onAllNodes(matcher).fetchSemanticsNodes()
    val index = nodes.indices.minBy { nodes[it].boundsInRoot.top }
    return compose.onAllNodes(matcher)[index]
}
val alertPattern = dialogField(hasSetTextAction().and(hasText(patternLabel)))
alertPattern.performTextClearance()
alertPattern.performTextInput("[")
compose.runOnIdle { assertEquals(originalAlertRegex, smsAlert.regexStr) }
alertPattern.performTextClearance()
alertPattern.performTextInput("qa-alert")
compose.runOnIdle { assertEquals("qa-alert", smsAlert.regexStr) }
val durationLabel = context.getString(R.string.within_seconds)
val alertDuration = dialogField(hasSetTextAction().and(hasText(durationLabel)))
alertDuration.performTextClearance()
alertDuration.performTextInput("12")
compose.runOnIdle { assertEquals(12, smsAlert.duration) }
pressBack()
pressBack()
compose.onNodeWithText(context.getString(R.string.sms_alert)).performScrollTo().performClick()
compose.onNodeWithText(context.getString(R.string.sms_bomb)).performScrollTo().performClick()
compose.onNodeWithText("${smsBomb.interval} ${context.getString(R.string.seconds_short)}")
.performScrollTo().performClick()
val bombPattern = dialogField(hasSetTextAction().and(hasText(patternLabel)))
bombPattern.performTextClearance()
bombPattern.performTextInput("[")
compose.runOnIdle { assertEquals(originalBombRegex, smsBomb.regexStr) }
bombPattern.performTextClearance()
bombPattern.performTextInput("qa-bomb")
compose.runOnIdle { assertEquals("qa-bomb", smsBomb.regexStr) }
val bombInterval = dialogField(hasSetTextAction().and(hasText(durationLabel)))
bombInterval.performTextClearance()
bombInterval.performTextInput("15")
compose.runOnIdle { assertEquals(15, smsBomb.interval) }
val lockscreenLabel = context.getString(R.string.lockscreen_protection)
compose.onNodeWithText(lockscreenLabel).performScrollTo()
val lockscreenSwitch = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)
.and(hasContentDescription(lockscreenLabel).or(hasText(lockscreenLabel)))
compose.onNode(lockscreenSwitch).performClick()
compose.runOnIdle { assertEquals(!originalLockscreenProtection, smsBomb.isLockScreenProtectionEnabled) }
compose.waitForIdle()
pressBack()
pressBack()
            compose.waitForIdle()
            assertTrue(
                "SMS Bomb editor still open after dismissal",
                compose.onAllNodes(hasText(patternLabel)).fetchSemanticsNodes().isEmpty(),
            )

listOf(
                R.string.sms_alert to { smsAlert.isEnabled },
R.string.sms_bomb to { smsBomb.isEnabled },
).forEach { (labelId, isEnabled) ->
val label = context.getString(labelId)
compose.waitForIdle()
            val switchNode = compose.onNodeWithTag(
                if (labelId == R.string.sms_alert) "sms_alert_switch" else "sms_bomb_switch"
            ).performScrollTo()
            switchNode.assertIsOn()
            val bombCollapsedBeforeSwitch = smsBomb.isCollapsed
            switchNode.performClick()
compose.waitForIdle()
            assertEquals(bombCollapsedBeforeSwitch, smsBomb.isCollapsed)
            switchNode.assertIsOff()
            val switchedOff = runCatching { compose.waitUntil(5_000) { !isEnabled() } }.isSuccess
            assertTrue(
                "$label did not turn off; stored=${isEnabled()}, permission=${Permission.receiveSMS.isGranted}",
                switchedOff && !isEnabled(),
            )
            }
            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
            assertFalse(smsAlert.isEnabled)
            assertFalse(smsBomb.isEnabled)
            compose.onNodeWithTag("sms_alert_switch").assertIsOff()
            compose.onNodeWithTag("sms_bomb_switch").assertIsOff()
        } finally {
            smsAlert.isEnabled = originalAlertEnabled
            smsBomb.isEnabled = originalBombEnabled
smsAlert.duration = originalAlertDuration
smsAlert.regexStr = originalAlertRegex
smsBomb.interval = originalBombInterval
smsBomb.regexStr = originalBombRegex
smsBomb.isLockScreenProtectionEnabled = originalLockscreenProtection
            Permission.receiveSMS.isGranted = originalSmsPermission
            Permission.batteryUnRestricted.isGranted = originalBatteryPermission
            compose.activityRule.scenario.recreate()
        }
    }

    @Test
    fun pushAlertCanBeCreatedEditedAndDeletedWithQaAppSelection() {
        val context = QaTestEnvironment.context()
        val table = PushAlertTable
        val originalRecords = table.listAll(context)
        val settings = spf.PushAlert(context)
        val originalCollapsed = settings.isCollapsed
        val originalListCollapsed = PushAlertViewModel.listCollapsed.value
        val originalRegexCollapsed = spf.SettingSections(context).isRegexSettingsCollapsed
        val originalNotificationAccess = Permission.notificationAccess.isGranted
        val originalBatteryPermission = Permission.batteryUnRestricted.isGranted
        val qaPackage = context.packageName
        try {
            table.clearAll(context)
            PushAlertViewModel.reloadDb(context)
            settings.isCollapsed = false
            PushAlertViewModel.listCollapsed.value = false
            spf.SettingSections(context).isRegexSettingsCollapsed = false
            spf.SettingSections(context).isScreeningSectionCollapsed = false
            compose.activityRule.scenario.recreate()
            compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
                .performClick()
            compose.onNodeWithText(context.getString(R.string.protection), useUnmergedTree = true)
                .performClick()
            compose.onNodeWithText(context.getString(R.string.push_alert)).performScrollTo()
Permission.notificationAccess.isGranted = true
Permission.batteryUnRestricted.isGranted = true
            compose.onNodeWithTag("push_alert_add").performScrollTo().performClick()

            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("push_alert_choose_app", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("push_alert_choose_app", useUnmergedTree = true).performClick()
            val appSearch = compose.onNode(
                hasSetTextAction().and(hasText(context.getString(R.string.find_apps)))
            )
            appSearch.performTextInput(qaPackage)
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("choose_app_$qaPackage").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("choose_app_$qaPackage").performClick()
            compose.onAllNodes(hasSetTextAction(), useUnmergedTree = true)[0].performTextInput("qa-push")
            compose.onNodeWithText(context.getString(R.string.save)).performScrollTo().performClick()
            compose.waitUntil(10_000) { table.listAll(context).size == 1 }
            val created = table.listAll(context).single()
            assertTrue(created.isValid())
            assertEquals(qaPackage, created.pkgName)
            assertEquals("qa-push", created.body)

            val recordId = created.id
            compose.onNodeWithTag("push_alert_item_$recordId").performScrollTo().performClick()
            compose.onNodeWithTag("push_alert_body").assertExists()
            compose.onAllNodes(hasSetTextAction(), useUnmergedTree = true)[0].performTextClearance()
            compose.onAllNodes(hasSetTextAction(), useUnmergedTree = true)[0].performTextInput("qa-push-edited")
            compose.onNodeWithText(context.getString(R.string.save)).performScrollTo().performClick()
            compose.waitUntil(10_000) { table.listAll(context).singleOrNull()?.body == "qa-push-edited" }

            compose.onNodeWithTag("push_alert_item_$recordId").performTouchInput {
                swipeLeft(startX = width - 1f, endX = 1f, durationMillis = 200)
            }
            compose.waitUntil(10_000) { table.listAll(context).isEmpty() }
        } finally {
            table.clearAll(context)
            originalRecords.forEach { table.addWithId(context, it) }
            PushAlertViewModel.reloadDb(context)
            settings.isCollapsed = originalCollapsed
            PushAlertViewModel.listCollapsed.value = originalListCollapsed
            spf.SettingSections(context).isRegexSettingsCollapsed = originalRegexCollapsed
            Permission.notificationAccess.isGranted = originalNotificationAccess
            Permission.batteryUnRestricted.isGranted = originalBatteryPermission
        }
    }

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
        val originalFamily = themePrefs.themeFamily
        val originalAppearance = themePrefs.themeAppearance
        val currentFamily = G.currentThemeChoice.value.family
        val targetFamily = if (currentFamily == ThemeFamily.CATPPUCCIN) {
            ThemeFamily.TRUECALLER
        } else {
            ThemeFamily.CATPPUCCIN
        }
        val targetLabel = if (targetFamily == ThemeFamily.CATPPUCCIN) {
            R.string.theme_catppuccin
        } else {
            R.string.theme_truecaller
        }
        compose.onNodeWithTag("theme_family").performClick()
        compose.onNodeWithText(ctx.getString(targetLabel)).performClick()
        assertEquals(targetFamily, G.currentThemeChoice.value.family)
        compose.activityRule.scenario.recreate()
        assertEquals(targetFamily, G.currentThemeChoice.value.family)
        themePrefs.themeFamily = originalFamily
        themePrefs.themeAppearance = originalAppearance
        compose.activityRule.scenario.recreate()
    }

    @Test
    fun allThemeFamiliesAndAppearanceModesApplyAndPersistTheirPalette() {
        val ctx = compose.activity
        compose.onNodeWithText(ctx.getString(R.string.tab_settings), useUnmergedTree = true)
            .performClick()
        compose.onNodeWithText(ctx.getString(R.string.theme), substring = true)
            .performScrollTo()

        val themePrefs = spf.Theme(ctx)
        val initialChoice = G.currentThemeChoice.value
        val initialFamilyPreference = themePrefs.themeFamily
        val initialAppearancePreference = themePrefs.themeAppearance
        var selectedFamily = initialChoice.family
        var selectedAppearance = initialChoice.appearance
        val initialFamily = initialChoice.family
        val initialAppearance = initialChoice.appearance
        val savedCustomPalette = G.palette.allColors.associate { color ->
            color.key to G.palette.prefs.getInt("color_${color.key}", color.default.toArgb())
        }
        val systemIsDark = (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        fun chooseComboOption(tag: String, itemId: String) {
            compose.onNodeWithTag(tag).performClick()
            val option = compose.onNodeWithTag("dropdown_item_$itemId")
            option.assertExists()
            option.performClick()
            compose.waitForIdle()
        }

        fun selectFamily(family: ThemeFamily) {
            if (family == selectedFamily) return
            chooseComboOption("theme_family", family.name)
            selectedFamily = family
            compose.waitForIdle()
        }

        fun selectAppearance(appearance: ThemeAppearance) {
            if (appearance == selectedAppearance) return
            chooseComboOption("theme_appearance", appearance.name)
            selectedAppearance = appearance
            compose.waitForIdle()
        }

        try {
            ThemeFamily.entries.forEach { family ->
                selectFamily(family)
if (family == ThemeFamily.CUSTOM) {
assertEquals(family, G.currentThemeChoice.value.family)
assertEquals(selectedAppearance, G.currentThemeChoice.value.appearance)
savedCustomPalette.forEach { (key, expected) ->
val actual = G.palette.allColors.first { it.key == key }.state.value.toArgb()
assertEquals("Custom palette color $key", expected, actual)
}
return@forEach
}
                ThemeAppearance.entries.forEach { appearance ->
                    selectAppearance(appearance)
if (family != initialFamily || themePrefs.themeFamily.isNotEmpty())
    assertEquals(family.name, themePrefs.themeFamily)
if (appearance != initialAppearance || themePrefs.themeAppearance.isNotEmpty())
    assertEquals(
        "family=$family appearance=$appearance tracked=$selectedAppearance current=${G.currentThemeChoice.value}",
        appearance.name,
        themePrefs.themeAppearance,
    )
                    assertEquals(family, G.currentThemeChoice.value.family)
                    assertEquals(appearance, G.currentThemeChoice.value.appearance)

                        val dark = when (appearance) {
                            ThemeAppearance.SYSTEM -> systemIsDark
                            ThemeAppearance.LIGHT -> false
                            ThemeAppearance.DARK -> true
                        }
                        val expected = presetColors(family, dark)
                        assertEquals(expected["background"], G.palette.background)
                        assertEquals(expected["text"], G.palette.textGrey)
                }
            }

            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
            assertEquals(ThemeFamily.CUSTOM.name, themePrefs.themeFamily)
            assertEquals(ThemeAppearance.DARK.name, themePrefs.themeAppearance)
            assertEquals(ThemeFamily.CUSTOM, G.currentThemeChoice.value.family)
            assertEquals(ThemeAppearance.DARK, G.currentThemeChoice.value.appearance)
            savedCustomPalette.forEach { (key, expected) ->
                val actual = G.palette.allColors.first { it.key == key }.state.value.toArgb()
                assertEquals("Custom palette color $key after recreation", expected, actual)
            }
        } finally {
            if (selectedFamily == ThemeFamily.CUSTOM) selectFamily(ThemeFamily.TRUECALLER)
            selectFamily(initialFamily)
            selectAppearance(initialAppearance)
            themePrefs.themeFamily = initialFamilyPreference
            themePrefs.themeAppearance = initialAppearancePreference
            assertEquals(initialFamilyPreference, themePrefs.themeFamily)
            assertEquals(initialAppearancePreference, themePrefs.themeAppearance)
        }
    }

    @Test
    fun customThemeColorCanBeEditedAndPersistsAfterActivityRecreation() {
        val ctx = compose.activity
        val themePrefs = spf.Theme(ctx)
        val initialChoice = G.currentThemeChoice.value
        val initialFamilyPreference = themePrefs.themeFamily
        val initialAppearancePreference = themePrefs.themeAppearance
        val initialFamily = initialChoice.family
        val initialAppearance = initialChoice.appearance
        val initialPalette = G.palette.allColors.associate { it.key to it.state.value }
        val textColor = G.palette.allColors.first { it.key == "text" }
        val originalCustomTextColor = G.palette.prefs.getInt("color_text", textColor.default.toArgb())
        val editedTextColor = 0xFF123456.toInt()

        try {
            compose.onNodeWithText(ctx.getString(R.string.tab_settings), useUnmergedTree = true)
                .performClick()
            compose.onNodeWithText(ctx.getString(R.string.theme), substring = true)
                .performScrollTo()

            if (initialFamily != ThemeFamily.CUSTOM) {
                val selectedFamilyLabel = when (initialFamily) {
                    ThemeFamily.CATPPUCCIN -> R.string.theme_catppuccin
                    ThemeFamily.TRUECALLER -> R.string.theme_truecaller
                    ThemeFamily.CUSTOM -> R.string.custom
                }
                compose.onNodeWithText(ctx.getString(selectedFamilyLabel)).performClick()
                compose.onNodeWithText(ctx.getString(R.string.custom)).performClick()
            }

            compose.onNodeWithText(ctx.getString(R.string.customize), substring = true)
                .performScrollTo().performClick()
            compose.onNodeWithText(ctx.getString(R.string.normal_text))
                .performScrollTo().performClick()

            val originalHex = String.format("%08X", originalCustomTextColor)
            val hexField = compose.onNode(hasSetTextAction().and(hasText(originalHex)))
            hexField.performTextClearance()
compose.onAllNodes(hasSetTextAction())[0].performTextInput(String.format("%08X", editedTextColor))
            compose.waitForIdle()
            compose.onNodeWithText(ctx.getString(R.string.save)).performClick()
            compose.waitForIdle()

            assertEquals(editedTextColor, G.palette.textGrey.toArgb())
            assertEquals(editedTextColor, G.palette.prefs.getInt("color_text", 0))

            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
            assertEquals(ThemeFamily.CUSTOM.name, themePrefs.themeFamily)
assertEquals(initialAppearancePreference, themePrefs.themeAppearance)
            assertEquals(ThemeFamily.CUSTOM, G.currentThemeChoice.value.family)
            assertEquals(initialAppearance, G.currentThemeChoice.value.appearance)
            assertEquals(editedTextColor, G.palette.textGrey.toArgb())
            assertEquals(editedTextColor, G.palette.prefs.getInt("color_text", 0))
        } finally {
            G.palette.prefs.edit().putInt("color_text", originalCustomTextColor).apply()
            themePrefs.themeFamily = initialFamilyPreference
            themePrefs.themeAppearance = initialAppearancePreference
            G.currentThemeChoice.value = initialChoice
            G.palette.allColors.forEach { color ->
                color.state.value = initialPalette.getValue(color.key)
            }
            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
        }
    }

    @Test
    fun settingsSectionsPersistCollapseAndExpandActions() {
        val context = compose.activity
        val sections = spf.SettingSections(context).apply {
            isInstantQueryCollapsed = false
            isReportNumberCollapsed = false
            isAutomationCollapsed = false
            isMiscCollapsed = false
        }

        compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
            .performClick()

        val cases = listOf(
            R.string.instant_query to { sections.isInstantQueryCollapsed },
            R.string.report_number to { sections.isReportNumberCollapsed },
            R.string.automation to { sections.isAutomationCollapsed },
            R.string.miscellaneous to { sections.isMiscCollapsed },
        )
        cases.forEach { (titleId, isCollapsed) ->
            val title = compose.onNodeWithText(context.getString(titleId), useUnmergedTree = true)
            title.performScrollTo().performClick()
            assertTrue(isCollapsed())
            title.performScrollTo().performClick()
            assertFalse(isCollapsed())
        }
    }

    @Test
    fun languagePickerShowsSystemAndSupportedLocalesWithoutApplyingOne() {
        val context = compose.activity
        val preferences = spf.Global(context)
        val originalLanguage = preferences.language
        preferences.language = ""
        try {
            compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
                .performClick()
            val sections = spf.SettingSections(context)
            if (sections.isMiscCollapsed) {
                compose.onNodeWithText(context.getString(R.string.miscellaneous), useUnmergedTree = true)
                    .performScrollTo()
                    .performClick()
            }
            val languageRow = compose.onNodeWithText(context.getString(R.string.language), substring = true)
                .performScrollTo()
            val languageY = languageRow.fetchSemanticsNode().boundsInRoot.center.y
        compose.onNodeWithTag("language_picker").performClick()
compose.onNodeWithText("🇩🇪  de", substring = true).assertExists()
            pressBack()
        } finally {
            preferences.language = originalLanguage
        }
    }

    @Test
    fun apiEditorAndBotTemplateListOpenWithoutExternalEffects() {
        val context = compose.activity
        compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
            .performClick()

        compose.onAllNodesWithText(context.getString(R.string.new_))[0]
            .performScrollTo()
            .performClick()
        compose.onNodeWithText(context.getString(R.string.customize)).performClick()
        compose.onNodeWithText(context.getString(R.string.workflow)).assertExists()
        pressBack()
        compose.waitForIdle()

        compose.onAllNodesWithText(context.getString(R.string.new_))[2]
    .performScrollTo().performClick()
            .performClick()
        compose.waitForIdle()
        compose.onNodeWithText(context.getString(R.string.customize)).assertExists()
        compose.onNodeWithText(context.getString(R.string.bot_templates))
            .performScrollTo()
            .performClick()

        val firstTemplateTitle = BotTemplates.getPredefinedTemplates().first().titleRes
        compose.onNodeWithText(context.getString(firstTemplateTitle)).assertExists()
    }

    @Test
    fun autoBackupTemplateRequiresAConfiguredFolderBeforeSaving() {
        val context = QaTestEnvironment.context()
        compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
            .performClick()
        compose.onAllNodesWithText(context.getString(R.string.new_))[2]
            .performScrollTo()
            .performClick()
        compose.onNodeWithText(context.getString(R.string.bot_templates)).performClick()
        compose.onAllNodesWithText(context.getString(R.string.apply))[2].performClick()

        compose.onNodeWithText(context.getString(R.string.save)).assertIsNotEnabled()
        assertTrue(BotTable.listAll(context).isEmpty())
    }

    @Test
    fun autoBackupPresetRequiresAConfiguredFolderBeforeSaving() {
        val context = QaTestEnvironment.context()
        compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
            .performClick()
        compose.onAllNodesWithText(context.getString(R.string.new_))[2]
            .performScrollTo()
            .performClick()
        compose.onNodeWithText(context.getString(R.string.bot_preset_auto_backup))
            .performScrollTo()
            .performClick()

        compose.onNodeWithText(context.getString(R.string.save)).assertIsNotEnabled()
        assertTrue(BotTable.listAll(context).isEmpty())
    }

    @Test
    fun faqAndAboutDialogsCanBeOpenedAndDismissed() {
        val context = compose.activity
        val sections = spf.SettingSections(context)
        compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
            .performClick()
        if (sections.isMiscCollapsed) {
            compose.onNodeWithText(context.getString(R.string.miscellaneous), useUnmergedTree = true)
                .performScrollTo()
                .performClick()
        }

        compose.onNodeWithText(context.getString(R.string.about)).performScrollTo().performClick()
compose.onNode(hasContentDescription(context.getString(R.string.version), substring = true)).assertExists()
compose.onNode(hasContentDescription(context.getString(R.string.source_code), substring = true)).assertExists()
        pressBack()

        compose.onNodeWithText(context.getString(R.string.faq)).performScrollTo().performClick()
        compose.onNodeWithText(context.getString(R.string.priority)).assertExists()
        compose.onNodeWithText(context.getString(R.string.regex_pattern)).assertExists()
        pressBack()
    }

@Test
fun customManualBotCanBeSavedToQaDatabase() {
val context = QaTestEnvironment.context()
compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
.performClick()
compose.onAllNodesWithText(context.getString(R.string.new_))[2]
    .performScrollTo().performClick()
compose.onNodeWithText(context.getString(R.string.customize)).performClick()

val description = "QA manual bot"
compose.onAllNodes(hasSetTextAction())[0].performTextInput(description)
pressBack()
compose.onNodeWithText(context.getString(R.string.save)).performClick()
compose.waitUntil(5_000) {
BotTable.listAll(context).any { it.desc == description }
}
assertTrue(BotTable.listAll(context).any { it.desc == description })
}

@Test
fun customManualBotCanBeDeletedAndRestoredFromSnackbar() {
    val context = QaTestEnvironment.context()
    compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
        .performClick()
    compose.onAllNodesWithText(context.getString(R.string.new_))[2]
    .performScrollTo().performClick()
    compose.onNodeWithText(context.getString(R.string.customize)).performClick()
    val description = "QA delete and restore bot"
    compose.onAllNodes(hasSetTextAction())[0].performTextInput(description)
    compose.onNodeWithText(context.getString(R.string.save)).performClick()
    compose.waitUntil(5_000) { BotTable.listAll(context).any { it.desc == description } }

    compose.onNodeWithText(description).performScrollTo().performTouchInput { swipeLeft() }
    compose.waitUntil(5_000) { BotTable.listAll(context).none { it.desc == description } }
    compose.onNodeWithText(context.getString(R.string.undelete)).performClick()
compose.waitUntil(5_000) { BotTable.listAll(context).any { it.desc == description } }
    assertTrue(BotTable.listAll(context).any { it.desc == description })
}

@Test
    fun backupExportCategoryDialogCanBeOpenedAndCancelled() {
        val context = compose.activity
        val sections = spf.SettingSections(context)
        compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
            .performClick()
        if (sections.isMiscCollapsed) {
            compose.onNodeWithText(context.getString(R.string.miscellaneous), useUnmergedTree = true)
                .performScrollTo()
                .performClick()
        }

        compose.onNodeWithText(context.getString(R.string.export)).performScrollTo().performClick()
        compose.onNodeWithText(context.getString(R.string.include_settings)).assertExists()
        compose.onNodeWithText(context.getString(R.string.exclude_settings)).assertExists()
        compose.activity.runOnUiThread {
            compose.activity.onBackPressedDispatcher.onBackPressed()
        }
    }

    @Test
    fun backupExportWritesArchiveToSelectedQaDocumentUri() {
        val context = compose.activity
        val documentName = "qa-export.gz"
        QaDocumentProvider.clearDocument(context, documentName)
        val uri = QaDocumentProvider.uriFor(documentName)
        Intents.init()
        try {
            intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    Intent().setData(uri).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                )
            )
            val sections = spf.SettingSections(context)
            compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
                .performClick()
            if (sections.isMiscCollapsed) {
                compose.onNodeWithText(context.getString(R.string.miscellaneous), useUnmergedTree = true)
                    .performScrollTo()
                    .performClick()
            }
            compose.onNodeWithText(context.getString(R.string.export)).performScrollTo().performClick()
            compose.onAllNodesWithText(context.getString(R.string.export), useUnmergedTree = true)[1]
                .performClick()
            compose.waitUntil(15_000) { QaDocumentProvider.getDocument(context, documentName)?.isNotEmpty() == true }
            compose.onNodeWithText(context.getString(R.string.exported_successfully)).assertExists()
        } finally {
            Intents.release()
            QaDocumentProvider.clearDocument(context, documentName)
        }
    }

    @Test
    fun malformedBackupImportShowsFailureAndPreservesQaData() {
        val context = QaTestEnvironment.context()
        val documentName = "qa-invalid-backup.gz"
        QaDocumentProvider.putDocument(context, documentName, byteArrayOf(1, 2, 3, 4))
val numberTable = NumberRegexTable()
val originalRules = numberTable.listAll(context)
numberTable.clearAll(context)
numberTable.addNew(context, RegexRule(pattern = "qa-backup-preserve", description = "Keep on failed restore"))
val qaBot = Bot(desc = "qa-backup-preserve")
BotTable.addNew(context, qaBot)
val botDescriptions = BotTable.listAll(context).map { it.desc }
val numberPatterns = numberTable.listAll(context).map { it.pattern }
        Intents.init()
        try {
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    Intent().setData(QaDocumentProvider.uriFor(documentName))
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            )
            val sections = spf.SettingSections(compose.activity)
            compose.onNodeWithText(compose.activity.getString(R.string.tab_settings), useUnmergedTree = true)
                .performClick()
            if (sections.isMiscCollapsed) {
                compose.onNodeWithText(compose.activity.getString(R.string.miscellaneous), useUnmergedTree = true)
                    .performScrollTo()
                    .performClick()
            }
            compose.onNodeWithText(compose.activity.getString(R.string.import_)).performScrollTo().performClick()
            compose.onNodeWithText(compose.activity.getString(R.string.import_fail)).assertExists()
            assertEquals(botDescriptions, BotTable.listAll(context).map { it.desc })
assertEquals(numberPatterns, numberTable.listAll(context).map { it.pattern })
        } finally {
            Intents.release()
            QaDocumentProvider.clearDocument(context, documentName)
numberTable.clearAll(context)
originalRules.forEach { numberTable.addWithId(context, it) }
        }
    }

    @Test
    fun validBackupImportRestoresSelectedRegexRulesFromQaDocumentUri() {
        val context = QaTestEnvironment.context()
        val table = NumberRegexTable()
        val originalRules = table.listAll(context)
        val restoredPattern = "qa-valid-backup-778899"
        val documentName = "qa-valid-backup.gz"
        val uri = QaDocumentProvider.uriFor(documentName)
        val config = Configs().apply {
            categories = CategorySelection(emptySet()).select(Category.REGEX_RULES)
            numberRules = NumberRules().apply {
                rules.addAll(originalRules)
                rules.add(RegexRule(pattern = restoredPattern, description = "QA restored rule"))
            }
        }
        QaDocumentProvider.putDocument(context, documentName, config.toByteArray())
        Intents.init()
        try {
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    Intent().setData(uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            )
            compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
                .performClick()
            if (spf.SettingSections(context).isMiscCollapsed) {
                compose.onNodeWithText(context.getString(R.string.miscellaneous), useUnmergedTree = true)
                    .performScrollTo()
                    .performClick()
            }
            compose.onNodeWithText(context.getString(R.string.import_)).performScrollTo().performClick()
            compose.waitUntil(5_000) {
                Intents.getIntents().any { it.action == Intent.ACTION_OPEN_DOCUMENT }
            }
            val importButtons = compose.onAllNodesWithText(context.getString(R.string.import_), useUnmergedTree = true)
                .fetchSemanticsNodes()
            assertTrue(importButtons.size >= 2)
            compose.onAllNodesWithText(context.getString(R.string.import_), useUnmergedTree = true)[importButtons.lastIndex]
                .performClick()
            compose.waitUntil(10_000) { table.listAll(context).any { it.pattern == restoredPattern } }
            compose.onNodeWithText(context.getString(R.string.imported_successfully)).assertExists()
        } finally {
            Intents.release()
            table.clearAll(context)
            originalRules.forEach { table.addWithId(context, it) }
            QaDocumentProvider.clearDocument(context, documentName)
        }
    }
}
