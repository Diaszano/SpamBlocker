package spam.blocker.ui.setting

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.R
import spam.blocker.G
import spam.blocker.def.Def
import spam.blocker.db.NumberRegexTable
import spam.blocker.db.RegexRule
import spam.blocker.ui.main.MainActivity
import spam.blocker.qa.QaTestEnvironment
import spam.blocker.util.Permission
import spam.blocker.util.TimeUtils.timeRangeStr
import spam.blocker.util.spf
import androidx.test.espresso.Espresso.pressBack

@RunWith(AndroidJUnit4::class)
class ProtectionFlowsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun startFromCallsTab() {
        spf.SettingSections(QaTestEnvironment.context()).apply {
            isScreeningSectionCollapsed = false
            isQuickSettingsCollapsed = false
            isRegexSettingsCollapsed = false
        }
        val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        compose.onNode(tab.and(hasText(compose.activity.getString(R.string.tab_settings)))).performClick()
        compose.onNode(tab.and(hasText(compose.activity.getString(R.string.call)))).performClick()
        compose.waitForIdle()
    }

    @After
    fun restoreQaProtectionChannels() {
        val context = QaTestEnvironment.context()
        spf.Global(context).apply {
            isGloballyEnabled = true
            isCallEnabled = true
            isSmsEnabled = true
            isMmsEnabled = false
        }
        spf.OffTime(context).apply {
            isEnabled = false
            startHour = 0
            startMin = 0
            endHour = 0
            endMin = 0
        }
        spf.BlockType(context).type = Def.DEF_BLOCK_TYPE
        compose.runOnIdle {
            G.globallyEnabled.value = true
            G.callEnabled.value = true
            G.smsEnabled.value = true
            G.pausedUntil.value = 0L
        }
    }

    @Test
    fun protectionScreenExposesAllQuickAndAdditionalRuleAreas() {
        spf.SettingSections(QaTestEnvironment.context()).apply {
            isScreeningSectionCollapsed = false
            isQuickSettingsCollapsed = false
            isRegexSettingsCollapsed = false
        }
        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
            .performClick()

        val rules = listOf(
            R.string.contacts,
            R.string.stir_attestation,
            R.string.database,
            R.string.local_ai,
            R.string.repeated_call,
            R.string.dialed_number,
            R.string.answered_number,
            R.string.off_time,
            R.string.emergency,
            R.string.recent_apps,
            R.string.in_meeting,
            R.string.block_type,
            R.string.notification,
            R.string.caller_id,
            R.string.label_number_rules,
            R.string.label_content_rules,
            R.string.quick_copy,
            R.string.push_alert,
            R.string.sms_alert,
            R.string.sms_bomb,
        )

        rules.forEach { id ->
            val label = compose.activity.getString(id)
            compose.onNodeWithText(label, substring = true)
                .performScrollTo()
                .assertExists()
        }
    }

    @Test
    fun protectionRuleSectionsCanBeCollapsedAndExpanded() {
        val context = QaTestEnvironment.context()
        val sections = spf.SettingSections(context)
        val initialQuickCollapsed = sections.isQuickSettingsCollapsed
        val initialRegexCollapsed = sections.isRegexSettingsCollapsed
        val initialScreeningCollapsed = sections.isScreeningSectionCollapsed
        sections.isQuickSettingsCollapsed = false
        sections.isRegexSettingsCollapsed = false
        sections.isScreeningSectionCollapsed = false
        fun isVisible(label: String) = compose.onAllNodesWithText(label, useUnmergedTree = true)
            .fetchSemanticsNodes().isNotEmpty()

        try {
            compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
                .performClick()
            val contacts = compose.activity.getString(R.string.contacts)
            val quickSettings = compose.activity.getString(R.string.quick_settings)
            assertTrue(isVisible(contacts))
            compose.onNodeWithText(quickSettings, useUnmergedTree = true).performClick()
            compose.waitUntil(5_000) { !isVisible(contacts) }
            compose.onNodeWithText(quickSettings, useUnmergedTree = true).performClick()
            compose.waitUntil(5_000) { isVisible(contacts) }

            val numberRules = compose.activity.getString(R.string.label_number_rules)
            val regexSettings = compose.activity.getString(R.string.regex_settings)
            assertTrue(isVisible(numberRules))
compose.onNodeWithText(regexSettings, useUnmergedTree = true).performScrollTo().performClick()
            compose.waitUntil(5_000) { sections.isRegexSettingsCollapsed }
compose.onNodeWithText(regexSettings, useUnmergedTree = true).performScrollTo().performClick()
            compose.waitUntil(5_000) { !sections.isRegexSettingsCollapsed }
        } finally {
            sections.isQuickSettingsCollapsed = initialQuickCollapsed
            sections.isRegexSettingsCollapsed = initialRegexCollapsed
            sections.isScreeningSectionCollapsed = initialScreeningCollapsed
        }
    }

    @Test
    fun ruleTestingFabOpensDialogWithoutRunningTests() {
        val context = QaTestEnvironment.context()
        val global = spf.Global(context)
        val initialIgnoreConflicts = global.ignorePriorityConflict
        global.ignorePriorityConflict = true
        try {
            compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
                .performClick()
            compose.onNodeWithContentDescription(compose.activity.getString(R.string.title_rule_testing))
                .performClick()
            assertTrue(
                compose.onAllNodesWithText(
                    compose.activity.getString(R.string.title_rule_testing), useUnmergedTree = true
                ).fetchSemanticsNodes().isNotEmpty()
            )
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                .sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
        } finally {
            global.ignorePriorityConflict = initialIgnoreConflicts
        }
    }

    @Test
    fun priorityConflictDialogCanBeCancelledAndIgnored() {
        val context = QaTestEnvironment.context()
        val rules = NumberRegexTable()
        val initialRules = rules.listAll(context)
        val global = spf.Global(context)
        val initialIgnoreConflicts = global.ignorePriorityConflict
        val priority = 12_345
        try {
            rules.clearAll(context)
            rules.addNew(context, RegexRule(
                pattern = "^5550101$",
                description = "QA allowed priority conflict",
                priority = priority,
                isBlacklist = false,
                flags = Def.FLAG_FOR_CALL,
            ))
            rules.addNew(context, RegexRule(
                pattern = "^5550102$",
                description = "QA blocked priority conflict",
                priority = priority,
                isBlacklist = true,
                flags = Def.FLAG_FOR_CALL,
            ))
            global.ignorePriorityConflict = false
            compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
                .performClick()
            val testingFab = compose.onNodeWithContentDescription(compose.activity.getString(R.string.title_rule_testing))

            testingFab.performClick()
            compose.onNodeWithText("QA allowed priority conflict").assertExists()
            compose.onNodeWithText("QA blocked priority conflict").assertExists()
            pressBack()
            assertFalse(global.ignorePriorityConflict)

            testingFab.performClick()
            compose.onNodeWithText(compose.activity.getString(R.string.ignore)).performClick()
            assertTrue(global.ignorePriorityConflict)
        } finally {
            rules.clearAll(context)
            initialRules.forEach { rules.addWithId(context, it) }
            global.ignorePriorityConflict = initialIgnoreConflicts
        }
    }

    @Test
    fun globalProtectionSwitchPersistsOffAndOnInQaPackage() {
        val context = QaTestEnvironment.context()
        val global = spf.Global(context)
        global.isGloballyEnabled = true
        global.isCollapsed = false
        spf.SettingSections(context).isScreeningSectionCollapsed = false
        compose.runOnIdle {
            G.globallyEnabled.value = true
            G.pausedUntil.value = 0L
        }

        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
            .performClick()

        val switchMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)
        compose.onAllNodes(switchMatcher, useUnmergedTree = true)[0].performClick()
        assertFalse(global.isGloballyEnabled)
        assertFalse(G.globallyEnabled.value)

        compose.onAllNodes(switchMatcher, useUnmergedTree = true)[0].performClick()
        assertTrue(global.isGloballyEnabled)
        assertTrue(G.globallyEnabled.value)
    }

    @Test
    fun callAndSmsProtectionChannelsCanBeDisabledLocally() {
        val context = QaTestEnvironment.context()
        val global = spf.Global(context).apply {
            isGloballyEnabled = true
            isCallEnabled = true
            isSmsEnabled = true
            isMmsEnabled = false
            isCollapsed = false
        }
        spf.SettingSections(context).isScreeningSectionCollapsed = false
        compose.runOnIdle {
            G.globallyEnabled.value = true
            G.callEnabled.value = true
            G.smsEnabled.value = true
            G.pausedUntil.value = 0L
        }
        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
            .performClick()

        val switches = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)
        compose.onAllNodes(switches, useUnmergedTree = true)[1].performClick()
        assertFalse(global.isCallEnabled)
        assertFalse(G.callEnabled.value)
        compose.onAllNodes(switches, useUnmergedTree = true)[2].performClick()
        assertFalse(global.isSmsEnabled)
        assertFalse(G.smsEnabled.value)
    }

    @Test
    fun protectionCanBePausedAndResumedLocally() {
        val context = QaTestEnvironment.context()
        val global = spf.Global(context).apply {
            isGloballyEnabled = true
            isCollapsed = false
        }
        spf.SettingSections(context).isScreeningSectionCollapsed = false
        compose.runOnIdle {
            G.globallyEnabled.value = true
            G.pausedUntil.value = 0L
        }
        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
            .performClick()

        compose.onNodeWithText(compose.activity.getString(R.string.pause_blocking))
            .performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.pause_15m))
            .performClick()

        compose.runOnIdle {
            assertTrue(G.pausedUntil.value > System.currentTimeMillis())
            assertTrue(global.pausedUntil > System.currentTimeMillis())
            assertTrue(global.isGloballyEnabled)
        }
        compose.onNodeWithText(compose.activity.getString(R.string.resume))
            .performClick()
        compose.runOnIdle {
            assertEquals(0L, G.pausedUntil.value)
            assertEquals(0L, global.pausedUntil)
        }
    }

    @Test
    fun protectionSummaryShowsPermissionRequiredWhenEnabledChannelLacksPermission() {
        val context = QaTestEnvironment.context()
        val global = spf.Global(context)
        val initialGloballyEnabled = global.isGloballyEnabled
        val initialCallEnabled = global.isCallEnabled
        val initialSmsEnabled = global.isSmsEnabled
        val initialCollapsed = global.isCollapsed
        val initialCallPermission = Permission.callScreening.isGranted
        val initialSmsPermission = Permission.receiveSMS.isGranted
        try {
            global.isGloballyEnabled = true
            global.isCallEnabled = true
            global.isSmsEnabled = true
            global.isCollapsed = true
            compose.runOnIdle {
                G.globallyEnabled.value = true
                G.callEnabled.value = true
                G.smsEnabled.value = true
                G.pausedUntil.value = 0L
                Permission.callScreening.isGranted = false
                Permission.receiveSMS.isGranted = false
            }
            compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
                .performClick()

            assertTrue(
                compose.onAllNodesWithText(
                    compose.activity.getString(R.string.protection_status_permission_required)
                ).fetchSemanticsNodes().isNotEmpty()
            )
        } finally {
            global.isGloballyEnabled = initialGloballyEnabled
            global.isCallEnabled = initialCallEnabled
            global.isSmsEnabled = initialSmsEnabled
            global.isCollapsed = initialCollapsed
            compose.runOnIdle {
                G.globallyEnabled.value = initialGloballyEnabled
                G.callEnabled.value = initialCallEnabled
                G.smsEnabled.value = initialSmsEnabled
                G.pausedUntil.value = 0L
                Permission.callScreening.isGranted = initialCallPermission
                Permission.receiveSMS.isGranted = initialSmsPermission
            }
        }
    }

    @Test
    fun protectionSummaryRendersAllFiveStates() {
        val context = QaTestEnvironment.context()
        val global = spf.Global(context)
        val initialGlobalPref = global.isGloballyEnabled
        val initialCallPref = global.isCallEnabled
        val initialSmsPref = global.isSmsEnabled
        val initialPausedPref = global.pausedUntil
        val initialGlobal = G.globallyEnabled.value
        val initialCall = G.callEnabled.value
        val initialSms = G.smsEnabled.value
        val initialPaused = G.pausedUntil.value
        val initialCallPermission = Permission.callScreening.isGranted
        val initialSmsPermission = Permission.receiveSMS.isGranted

        fun configure(
            globalEnabled: Boolean,
            callEnabled: Boolean,
            smsEnabled: Boolean,
            pausedUntil: Long,
            callPermission: Boolean,
            smsPermission: Boolean,
        ) {
            global.isGloballyEnabled = globalEnabled
            global.isCallEnabled = callEnabled
            global.isSmsEnabled = smsEnabled
            global.pausedUntil = pausedUntil
            compose.runOnIdle {
                G.globallyEnabled.value = globalEnabled
                G.callEnabled.value = callEnabled
                G.smsEnabled.value = smsEnabled
                G.pausedUntil.value = pausedUntil
                Permission.callScreening.isGranted = callPermission
                Permission.receiveSMS.isGranted = smsPermission
            }
            compose.waitForIdle()
        }

        try {
            compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
                .performClick()
            compose.onNodeWithText(compose.activity.getString(R.string.screening)).performClick()
            configure(false, true, true, 0L, false, false)
            compose.onNodeWithTag("protection_summary_off").assertExists()
            configure(true, true, true, System.currentTimeMillis() + 60_000L, false, false)
            compose.onNodeWithText(compose.activity.getString(R.string.protection_status_paused)).assertExists()
            configure(true, true, true, 0L, false, false)
            compose.onNodeWithText(compose.activity.getString(R.string.protection_status_permission_required)).assertExists()
            configure(true, false, false, 0L, true, true)
            compose.onNodeWithText(compose.activity.getString(R.string.protection_status_channels_off)).assertExists()
            configure(true, true, true, 0L, true, true)
            compose.onNodeWithTag("protection_summary_active").assertExists()
        } finally {
            global.isGloballyEnabled = initialGlobalPref
            global.isCallEnabled = initialCallPref
            global.isSmsEnabled = initialSmsPref
            global.pausedUntil = initialPausedPref
            compose.runOnIdle {
                G.globallyEnabled.value = initialGlobal
                G.callEnabled.value = initialCall
                G.smsEnabled.value = initialSms
                G.pausedUntil.value = initialPaused
                Permission.callScreening.isGranted = initialCallPermission
                Permission.receiveSMS.isGranted = initialSmsPermission
            }
        }
    }

    @Test
    fun offTimeTogglePersistsAndOpensTimeRangeEditor() {
        val context = QaTestEnvironment.context()
        val offTime = spf.OffTime(context).apply {
            isEnabled = false
            startHour = 21
            startMin = 30
            endHour = 6
            endMin = 45
        }
        spf.SettingSections(context).apply {
            isQuickSettingsCollapsed = false
            isRegexSettingsCollapsed = true
        }

        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
            .performClick()
        compose.waitForIdle()
        val label = compose.activity.getString(R.string.off_time)
        compose.onNodeWithText(label).performScrollTo()
        compose.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)
                .and(hasText(label))
        ).performClick()

        assertTrue(offTime.isEnabled)
        val range = timeRangeStr(context, 21, 30, 6, 45)
        compose.onNode(hasContentDescription("$label: $range"))
            .performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.start_time)).assertExists()
        compose.onNodeWithText(compose.activity.getString(R.string.end_time)).assertExists()
        pressBack()
        assertTrue(offTime.isEnabled)
    }

    @Test
    fun blockTypeCanSwitchBetweenRejectAndSilence() {
        val context = QaTestEnvironment.context()
        val blockType = spf.BlockType(context).apply {
            type = Def.BLOCK_TYPE_REJECT
        }
        spf.SettingSections(context).apply {
            isQuickSettingsCollapsed = false
            isRegexSettingsCollapsed = true
        }
        val options = context.resources.getStringArray(R.array.block_type_list)

        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
            .performClick()
        compose.waitForIdle()
compose.onNodeWithText(compose.activity.getString(R.string.block_type)).performScrollTo()
        compose.onNodeWithTag("block_type_selector").performClick()
        compose.waitForIdle()
        compose.onNodeWithText(options[1], useUnmergedTree = true).assertExists()
        compose.onNodeWithText(options[1]).performClick()
        compose.runOnIdle { assertEquals(Def.BLOCK_TYPE_SILENCE, blockType.type) }
        compose.onNodeWithTag("block_type_selector").performClick()
        compose.onNodeWithText(options[0]).performClick()
        compose.runOnIdle { assertEquals(Def.BLOCK_TYPE_REJECT, blockType.type) }
    }

}
