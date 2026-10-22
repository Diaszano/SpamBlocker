package spam.blocker.ui.history
import android.content.ClipboardManager
import android.content.Intent

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.pressBack
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import android.app.Activity
import android.app.Instrumentation
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.getIntents
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.R
import spam.blocker.G
import spam.blocker.db.HistoryRecord
import spam.blocker.db.CallTable
import spam.blocker.db.NumberRegexTable
import spam.blocker.db.RegexRule
import spam.blocker.db.SpamTable
import spam.blocker.db.SmsTable
import spam.blocker.def.Def
import spam.blocker.qa.QaTestEnvironment
import spam.blocker.ui.main.MainActivity
import spam.blocker.util.spf
import spam.blocker.util.Permission
import spam.blocker.util.PrefixUtils

@RunWith(AndroidJUnit4::class)
class HistoryFlowsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearQaHistory() {
        Intents.init()
        intending(anyIntent()).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        QaTestEnvironment.clearHistory()
        SpamTable.clearAll(QaTestEnvironment.context())
        NumberRegexTable().clearAll(QaTestEnvironment.context())
        spf.HistoryOptions(QaTestEnvironment.context()).apply {
            showPassed = true
            showBlocked = true
            showTimeColor = false
        }
        resetSearchState()
    }

    @After
    fun removeQaHistory() {
        QaTestEnvironment.clearHistory()
        SpamTable.clearAll(QaTestEnvironment.context())
        NumberRegexTable().clearAll(QaTestEnvironment.context())
        resetSearchState()
        Intents.release()
    }

    @Test
    fun callAndSmsHistoryRenderOnlyQaOwnedRecords() {
        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.call), useUnmergedTree = true).performClick()
        val smsTab = compose.activity.getString(R.string.sms)
        compose.onNodeWithText(smsTab, useUnmergedTree = true).performClick()

        val now = System.currentTimeMillis()
        val callNumber = "+15550000001"
        val blockedCallNumber = "+15550000003"
        val smsNumber = "+15550000002"
        val blockedSmsNumber = "+15550000004"
        QaTestEnvironment.addCall(
            HistoryRecord(peer = callNumber, time = now, result = Def.RESULT_ALLOWED_BY_DEFAULT, reason = "QA call")
        )
        QaTestEnvironment.addCall(
            HistoryRecord(peer = blockedCallNumber, time = now - 1, result = Def.RESULT_BLOCKED_BY_SPAM_DB, reason = "QA blocked call")
        )
        QaTestEnvironment.addSms(
            HistoryRecord(peer = smsNumber, time = now, result = Def.RESULT_ALLOWED_BY_DEFAULT, reason = "QA SMS", extraInfo = "fixture message")
        )
        QaTestEnvironment.addSms(
            HistoryRecord(peer = blockedSmsNumber, time = now - 1, result = Def.RESULT_BLOCKED_BY_SPAM_DB, reason = "QA blocked SMS", extraInfo = "fixture message")
        )

        val allowed = compose.activity.getString(R.string.allowed)
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.smsVM.reload(QaTestEnvironment.context())
        }
        compose.onNodeWithText(smsNumber, useUnmergedTree = true).assertExists()
        compose.onNodeWithText(blockedSmsNumber, useUnmergedTree = true).assertExists()
        filterButton(allowed).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(smsNumber, useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText(blockedSmsNumber, useUnmergedTree = true).assertExists()
        filterButton(allowed).performClick()
        compose.waitForIdle()

        compose.onNodeWithText(compose.activity.getString(R.string.call), useUnmergedTree = true).performClick()
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(QaTestEnvironment.context())
        }
        compose.onNodeWithText(callNumber, useUnmergedTree = true).assertExists()
        compose.onNodeWithText(blockedCallNumber, useUnmergedTree = true).assertExists()
        filterButton(allowed).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(callNumber, useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText(blockedCallNumber, useUnmergedTree = true).assertExists()
        filterButton(allowed).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(callNumber, useUnmergedTree = true).assertExists()
        val blocked = compose.activity.getString(R.string.blocked)
        filterButton(blocked).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(callNumber, useUnmergedTree = true).assertExists()
        compose.onNodeWithText(blockedCallNumber, useUnmergedTree = true).assertDoesNotExist()
        filterButton(blocked).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(blockedCallNumber, useUnmergedTree = true).assertExists()
    }

    @Test
    fun historyContextMenuFiltersRecordsAndSearchCanBeCleared() {
        navigateToCalls()
        val first = "+15550000011"
        val second = "+19998887777"
        val now = System.currentTimeMillis()
        QaTestEnvironment.addCall(HistoryRecord(peer = first, time = now, result = Def.RESULT_ALLOWED_BY_DEFAULT))
        QaTestEnvironment.addCall(HistoryRecord(peer = second, time = now - 1, result = Def.RESULT_ALLOWED_BY_DEFAULT))
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(QaTestEnvironment.context())
        }

        compose.onNodeWithText(first, useUnmergedTree = true).performTouchInput { longClick() }
        compose.onNodeWithText(compose.activity.getString(R.string.filter)).performClick()
        val search = compose.onNode(hasSetTextAction())
        search.performTextInput("00011")
        search.assertTextContains("00011")
        compose.waitUntil(5_000) { G.callVM.records.none { it.peer == second } }
        compose.onNodeWithText(second, useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText(first, useUnmergedTree = true).assertExists()

        search.performTextClearance()
        compose.waitUntil(5_000) { G.callVM.records.any { it.peer == second } }
        compose.onNodeWithText(second, useUnmergedTree = true).assertExists()
        search.performTextInput("no-match")
        compose.waitUntil(5_000) { G.callVM.records.isEmpty() }
        compose.onNodeWithText(compose.activity.getString(R.string.history_no_matches)).assertExists()
    }

    @Test
    fun callHistoryCanPauseAndResumeBlocking() {
        navigateToCalls()
        compose.runOnIdle {
            spf.Global(QaTestEnvironment.context()).isGloballyEnabled = true
            G.globallyEnabled.value = true
            G.pausedUntil.value = 0L
        }

        compose.onNodeWithText(compose.activity.getString(R.string.pause_blocking)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.pause_15m)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.resume)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.pause_blocking)).assertExists()
    }

    @Test
    fun emptyCallHistoryShowsEmptyState() {
        navigateToCalls()
        compose.runOnIdle { G.callVM.reload(QaTestEnvironment.context()) }
        compose.onNodeWithText(compose.activity.getString(R.string.history_empty))
            .assertExists()
    }

    @Test
    fun callRecordMenuOpensRuleTestDialogWithoutRunningIt() {
        navigateToCalls()
        val number = "+15550000021"
        QaTestEnvironment.addCall(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(QaTestEnvironment.context())
        }

        compose.onNodeWithText(number, useUnmergedTree = true).performTouchInput { longClick() }
        compose.onNodeWithText(compose.activity.getString(R.string.test)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.title_rule_testing), substring = true)
            .assertExists()
        compose.onNode(hasText(number) and hasSetTextAction()).assertExists()
    }

    @Test
    fun smsRecordMenuOpensRuleTestDialogWithoutRunningIt() {
        val context = QaTestEnvironment.context()
        val number = "+15550000022"
        QaTestEnvironment.addSms(
            HistoryRecord(
                peer = number,
                time = System.currentTimeMillis(),
                result = Def.RESULT_ALLOWED_BY_DEFAULT,
                reason = "QA",
                extraInfo = "fixture"
            )
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.smsVM.reload(context)
        }
        clickBottomNav(compose.activity.getString(R.string.protection))
        clickBottomNav(compose.activity.getString(R.string.call))
        clickBottomNav(compose.activity.getString(R.string.sms))
        compose.onNodeWithText(number, useUnmergedTree = true).performTouchInput { longClick() }
        compose.onNodeWithText(compose.activity.getString(R.string.test)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.title_rule_testing), substring = true)
            .assertExists()
        compose.onNode(hasText(number) and hasSetTextAction()).assertExists()
    }

    @Test
    fun callHistoryContextMenuCopiesNumber() {
        navigateToCalls()
        val context = QaTestEnvironment.context()
        val number = "+15550000041"
        QaTestEnvironment.addCall(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(context)
        }

        openCallContextMenu(number)
        compose.onNodeWithText(compose.activity.getString(R.string.copy_number)).performClick()
        val copiedNumber = compose.runOnIdle {
            compose.activity.getSystemService(ClipboardManager::class.java)
                .primaryClip?.getItemAt(0)?.coerceToText(compose.activity)?.toString()
        }
        assertEquals(number, copiedNumber)
    }

    @Test
    fun callHistoryContextMenuAddsNumberToSpamDatabase() {
        navigateToCalls()
        val context = QaTestEnvironment.context()
        val number = "+15550000042"
        QaTestEnvironment.addCall(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(context)
        }
        openCallContextMenu(number)
        compose.onNodeWithText(compose.activity.getString(R.string.add_to_db)).performClick()
        assertNotNull(SpamTable.findByNumber(context, number))
    }

    @Test
    fun callHistoryOnlineSearchLaunchesConfiguredUrlWithoutOpeningBrowser() {
        val context = QaTestEnvironment.context()
        val number = "+15550000044"
        val url = "https://example.invalid/search?number={raw_number}"
        spf.HistoryOptions(context).searchOnlineUrl = url
        QaTestEnvironment.addCall(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(context)
        }
        navigateToCalls()
        openCallContextMenu(number)
        compose.onNodeWithText(compose.activity.getString(R.string.search_online)).performClick()
        intended(hasAction(Intent.ACTION_VIEW))
        assertEquals(url.replace("{raw_number}", number), getIntents().last().data.toString())
    }

    @Test
    fun callHistoryOpenActionInterceptsDialer() {
        val number = "+15550000046"
        QaTestEnvironment.addCall(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(QaTestEnvironment.context())
        }
        navigateToCalls()
        openCallContextMenu(number)
        val callItems = compose.onAllNodesWithText(compose.activity.getString(R.string.call), useUnmergedTree = true)
        callItems[callItems.fetchSemanticsNodes().lastIndex].performClick()
        intended(hasAction(Intent.ACTION_DIAL))
        assertEquals("tel:$number", getIntents().last().data.toString())
    }

    @Test
    fun callContextMenuSavesSuggestedPrefixRuleToQaDatabase() {
        val context = QaTestEnvironment.context()
        val number = "+15550000045"
        QaTestEnvironment.addCall(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(context)
        }
        navigateToCalls()
        openCallContextMenu(number)
        compose.onNodeWithText(compose.activity.getString(R.string.block_prefix)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.save)).assertIsEnabled().performClick()

        compose.waitUntil(5_000) {
            NumberRegexTable().listAll(context).any { it.pattern == PrefixUtils.suggestPrefixPattern(number) }
        }
        val rule = NumberRegexTable().listAll(context).single()
        assertTrue(rule.isBlacklist)
    }

    @Test
    fun callContextMenuSavesRegexRuleToQaDatabase() {
        val context = QaTestEnvironment.context()
        val number = "15550000059"
        QaTestEnvironment.addCall(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(context)
        }
        navigateToCalls()
        openCallContextMenu(number)
        compose.onNodeWithText(compose.activity.getString(R.string.add_to_regex_rule)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.save)).assertIsEnabled().performClick()
        compose.waitUntil(5_000) {
            NumberRegexTable().listAll(context).any { it.pattern == number }
        }
    }

    @Test
    fun callContextMenuEditsExistingRegexRule() {
        val context = QaTestEnvironment.context()
        val number = "15550000060"
        val updatedPattern = "155500000601"
        val rules = NumberRegexTable()
        val original = RegexRule(
            pattern = number,
            patternFlags = Def.FLAG_REGEX_RAW_NUMBER,
            description = "QA existing rule",
            priority = 1,
        )
        val originalId = rules.addNew(context, original)
        QaTestEnvironment.addCall(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(context)
        }
        navigateToCalls()
        openCallContextMenu(number)
        compose.onNodeWithText(compose.activity.getString(R.string.edit_regex_rule)).performClick()
        val patternField = compose.onNode(hasSetTextAction().and(hasText(number)))
        patternField.performTextReplacement(updatedPattern)
        compose.onNodeWithText(compose.activity.getString(R.string.save)).performClick()
        compose.waitUntil(5_000) { rules.listAll(context).singleOrNull()?.pattern == updatedPattern }
        assertEquals(originalId, rules.listAll(context).single().id)
    }

    @Test
    fun smsHistoryContextMenuAddsNumberToSpamDatabase() {
        val context = QaTestEnvironment.context()
        val number = "+15550000043"
        QaTestEnvironment.addSms(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT, reason = "QA", extraInfo = "fixture")
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.smsVM.reload(context)
        }
        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.call), useUnmergedTree = true).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.sms), useUnmergedTree = true).performClick()
        compose.onNodeWithText(number, useUnmergedTree = true).performTouchInput { longClick() }
        compose.onNodeWithText(compose.activity.getString(R.string.add_to_db)).performClick()
        assertNotNull(SpamTable.findByNumber(context, number))
    }

    @Test
    fun smsContextMenuSearchesOnlineAndSavesRegexRule() {
        val context = QaTestEnvironment.context()
        val number = "15550000061"
        val url = "https://example.invalid/search?number={raw_number}"
        spf.HistoryOptions(context).searchOnlineUrl = url
        QaTestEnvironment.addSms(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.smsVM.reload(context)
        }
        navigateToSms()
        openSmsContextMenu(number)
        compose.onNodeWithText(compose.activity.getString(R.string.search_online)).performClick()
        intended(hasAction(Intent.ACTION_VIEW))
        assertEquals(url.replace("{raw_number}", number), getIntents().last().data.toString())

        openSmsContextMenu(number)
        compose.onNodeWithText(compose.activity.getString(R.string.add_to_regex_rule)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.save)).assertIsEnabled().performClick()
        compose.waitUntil(5_000) { NumberRegexTable().listAll(context).any { it.pattern == number } }
    }

    @Test
    fun smsContextMenuDeletesOnlySelectedQaRecord() {
        val context = QaTestEnvironment.context()
        val deleted = "+15550000062"
        val remaining = "+15550000063"
        QaTestEnvironment.addSms(
            HistoryRecord(peer = deleted, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        QaTestEnvironment.addSms(
            HistoryRecord(peer = remaining, time = System.currentTimeMillis() - 1, result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.smsVM.reload(context)
        }
        navigateToSms()
        openSmsContextMenu(deleted)
        compose.onNodeWithText(compose.activity.getString(R.string.delete)).performClick()
        compose.waitUntil(5_000) { SmsTable().listAll(context).none { it.peer == deleted } }
        assertTrue(SmsTable().listAll(context).any { it.peer == remaining })
    }

    @Test
    fun smsHistorySearchesAndClearsSearch() {
        val number = "+15550000051"
        val otherNumber = "+19998887777"
        QaTestEnvironment.addSms(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_BLOCKED_BY_SPAM_DB, reason = "QA", extraInfo = "blocked fixture")
        )
        QaTestEnvironment.addSms(
            HistoryRecord(peer = otherNumber, time = System.currentTimeMillis() - 1, result = Def.RESULT_BLOCKED_BY_SPAM_DB, reason = "QA", extraInfo = "other fixture")
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.smsVM.reload(QaTestEnvironment.context())
        }
        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.call), useUnmergedTree = true).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.sms), useUnmergedTree = true).performClick()
        compose.onNodeWithText(number, useUnmergedTree = true).performTouchInput { longClick() }
        compose.onNodeWithText(compose.activity.getString(R.string.filter)).performClick()
        val search = compose.onNode(hasSetTextAction())
        search.performTextInput("00051")
        compose.waitUntil(5_000) {
            val records = G.smsVM.records.toList()
            records.any { it.peer == number } && records.none { it.peer == otherNumber }
        }
        compose.onNodeWithText(number, useUnmergedTree = true).assertExists()
        search.performTextClearance()
        compose.waitUntil(5_000) { G.smsVM.records.toList().any { it.peer == otherNumber } }
        compose.waitForIdle()
        compose.onNodeWithText(otherNumber, useUnmergedTree = true).assertExists()
        search.performTextInput("no-match")
        compose.waitUntil(5_000) { G.smsVM.records.isEmpty() }
        compose.onNodeWithText(compose.activity.getString(R.string.history_no_matches)).assertExists()
    }

    @Test
    fun openingSmsConversationMarksFixtureReadAndInterceptsSmsAppIntent() {
        val context = QaTestEnvironment.context()
        val number = "+15550000053"
        QaTestEnvironment.addSms(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT, reason = "QA", extraInfo = "fixture")
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.smsVM.reload(context)
        }
        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.call), useUnmergedTree = true).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.sms), useUnmergedTree = true).performClick()
        openSmsContextMenu(number)
        compose.onNodeWithText(compose.activity.getString(R.string.open_sms)).performClick()

        intended(hasAction(Intent.ACTION_SENDTO))
        assertEquals("smsto:$number", Intents.getIntents().last().data.toString())
        compose.waitUntil(5_000) { G.smsVM.records.single().read }
    }

    @Test
    fun historySettingsFabOpensItsSettingsPanel() {
        navigateToCalls()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.setting)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.enable_history_logging)).assertExists()
        compose.onNodeWithText(compose.activity.getString(R.string.show_hide)).assertExists()
    }

    @Test
    fun historySettingsFabOpensItsSettingsPanelFromSms() {
        navigateToSms()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.setting)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.enable_history_logging)).assertExists()
        compose.onNodeWithText(compose.activity.getString(R.string.show_hide)).assertExists()
    }

    @Test
    fun historySettingsFabPersistsAllowedRecordVisibility() {
        val context = QaTestEnvironment.context()
        val options = spf.HistoryOptions(context)
        val initialValue = options.showPassed
        navigateToCalls()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.setting)).performClick()
        historySettingSwitch(R.string.allowed_records).performClick()
        compose.waitUntil(5_000) { options.showPassed != initialValue }
        assertEquals(!initialValue, options.showPassed)
        historySettingSwitch(R.string.allowed_records).performClick()
        compose.waitUntil(5_000) { options.showPassed == initialValue }
        assertEquals(initialValue, options.showPassed)
    }

    @Test
    fun historySettingsFabPersistsBlockedRecordVisibility() {
        val context = QaTestEnvironment.context()
        val options = spf.HistoryOptions(context)
        val initialValue = options.showBlocked
        navigateToCalls()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.setting)).performClick()
        try {
            historySettingSwitch(R.string.blocked_records).performClick()
            compose.waitUntil(5_000) { options.showBlocked != initialValue }
            assertEquals(!initialValue, options.showBlocked)
            historySettingSwitch(R.string.blocked_records).performClick()
            compose.waitUntil(5_000) { options.showBlocked == initialValue }
            assertEquals(initialValue, options.showBlocked)
        } finally {
            options.showBlocked = initialValue
            compose.runOnIdle {
                HistoryOptions.showHistoryBlocked.value = initialValue
                G.callVM.reload(context)
            }
        }
    }

    @Test
    fun historySettingsSimSelectionPersistsAutomaticAndAlways() {
        val context = QaTestEnvironment.context()
        val options = spf.HistoryOptions(context)
        val initialForceShowSim = options.forceShowSim
        val initialPhonePermission = Permission.phoneState.isGranted
        try {
            navigateToCalls()
            compose.onNodeWithContentDescription(compose.activity.getString(R.string.setting)).performClick()
            compose.onNodeWithText(compose.activity.getString(R.string.automatic)).performClick()
            compose.runOnIdle { Permission.phoneState.isGranted = true }
            compose.onNodeWithText(compose.activity.getString(R.string.always)).performClick()
            compose.waitUntil(5_000) { options.forceShowSim }
            assertTrue(options.forceShowSim)
            compose.onNodeWithText(compose.activity.getString(R.string.always)).performClick()
            compose.onNodeWithText(compose.activity.getString(R.string.automatic)).performClick()
            compose.waitUntil(5_000) { !options.forceShowSim }
            assertFalse(options.forceShowSim)
        } finally {
            options.forceShowSim = initialForceShowSim
            compose.runOnIdle {
                HistoryOptions.forceShowSIM.value = initialForceShowSim
                Permission.phoneState.isGranted = initialPhonePermission
            }
        }
    }

    @Test
    fun historySettingsPersistIndicatorLocationCarrierAndTimeColor() {
        val context = QaTestEnvironment.context()
        val options = spf.HistoryOptions(context)
        navigateToCalls()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.setting)).performClick()

        fun toggleAndRestore(labelId: Int, read: () -> Boolean) {
            val initialValue = read()
            historySettingSwitch(labelId).performClick()
            compose.waitUntil(5_000) { read() != initialValue }
            assertEquals(!initialValue, read())
            historySettingSwitch(labelId).performClick()
            compose.waitUntil(5_000) { read() == initialValue }
            assertEquals(initialValue, read())
        }

        toggleAndRestore(R.string.rule_indicator) { options.showIndicator }
        toggleAndRestore(R.string.geolocation) { options.showGeoLocation }
        toggleAndRestore(R.string.carrier) { options.showCarrier }
        toggleAndRestore(R.string.time_color) { options.showTimeColor }
    }

    @Test
    fun historyLoggingSmsContentAndExpiryControlsPersist() {
        val context = QaTestEnvironment.context()
        val options = spf.HistoryOptions(context)
        val initialLogging = options.isLoggingEnabled
        val initialSmsContent = options.isLogSmsContentEnabled
        val initialExpiry = options.isExpiryEnabled
        val initialTtl = options.ttl
        val initialRows = options.initialSmsRowCount
        options.isExpiryEnabled = true
        navigateToCalls()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.setting)).performClick()
        try {
            if (!options.isLoggingEnabled) {
                historySettingSwitch(R.string.enable).performClick()
                compose.waitUntil(5_000) { options.isLoggingEnabled }
            }
            fun toggleAndRestore(labelId: Int, read: () -> Boolean) {
                val initialValue = read()
                historySettingSwitch(labelId).performClick()
                compose.waitUntil(5_000) { read() != initialValue }
                assertEquals(!initialValue, read())
                historySettingSwitch(labelId).performClick()
                compose.waitUntil(5_000) { read() == initialValue }
                assertEquals(initialValue, read())
            }
            toggleAndRestore(R.string.enable) { options.isLoggingEnabled }
            toggleAndRestore(R.string.sms_content) { options.isLogSmsContentEnabled }
        val expiryButtonLabel = context.resources.getQuantityString(R.plurals.days, initialTtl, initialTtl)
        compose.onNodeWithText(expiryButtonLabel, useUnmergedTree = true).performClick()
        val expirySwitch = compose.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch),
            useUnmergedTree = true,
        )
val expiryBeforeToggle = options.isExpiryEnabled
expirySwitch[expirySwitch.fetchSemanticsNodes().lastIndex].performClick()
compose.waitUntil(5_000) { options.isExpiryEnabled != expiryBeforeToggle }
        expirySwitch[expirySwitch.fetchSemanticsNodes().lastIndex].performClick()
compose.waitUntil(5_000) { options.isExpiryEnabled == expiryBeforeToggle }
        val editedTtl = if (initialTtl == 14) 15 else 14
        val ttlField = compose.onNode(hasSetTextAction().and(hasText(initialTtl.toString())))
        ttlField.performTextClearance()
        compose.onNode(hasSetTextAction()).performTextInput(editedTtl.toString())
        compose.waitUntil(5_000) { options.ttl == editedTtl }
        assertEquals(editedTtl, options.ttl)
        pressBack()
        pressBack()
        compose.onNode(hasSetTextAction().and(hasText(editedTtl.toString()))).assertDoesNotExist()
            if (!options.isLogSmsContentEnabled) {
                historySettingSwitch(R.string.sms_content).performClick()
                compose.waitUntil(5_000) { options.isLogSmsContentEnabled }
            }
            val rowsLabel = context.resources.getQuantityString(R.plurals.rows, initialRows, initialRows)
            val editedRows = if (initialRows == 4) 5 else 4
            compose.onNodeWithText(rowsLabel).performClick()
            val rowCountField = compose.onNode(hasSetTextAction().and(hasText(initialRows.toString())))
            rowCountField.performTextClearance()
            compose.onNode(hasSetTextAction()).performTextInput(editedRows.toString())
            compose.waitUntil(5_000) { options.initialSmsRowCount == editedRows }
            assertEquals(editedRows, options.initialSmsRowCount)
        } finally {
            options.isLoggingEnabled = initialLogging
            options.isLogSmsContentEnabled = initialSmsContent
            options.isExpiryEnabled = initialExpiry
            options.ttl = initialTtl
            options.initialSmsRowCount = initialRows
            reScheduleHistoryCleanup(context)
        }
    }

    @Test
    fun historyTimeColorRulesCanBeSavedEditedCancelledAndDeleted() {
        val context = QaTestEnvironment.context()
        val options = spf.HistoryOptions(context)
        val initialColors = options.loadTimeColors()
        val initialShowTimeColor = options.showTimeColor
        val initialVisible = HistoryOptions.showHistoryTimeColor.value
        try {
            options.saveTimeColors(emptyList())
            options.showTimeColor = true
            navigateToCalls()
            compose.onNodeWithContentDescription(compose.activity.getString(R.string.setting)).performClick()
            compose.onNodeWithTag("history_time_color_edit").performClick()

            compose.onNodeWithTag("history_time_colors_add").performClick()
            val durationField = compose.onNode(hasSetTextAction().and(hasText("10min")))
            durationField.performTextReplacement("5min")
            compose.onNodeWithText(compose.activity.getString(R.string.save)).performClick()
            compose.waitUntil(5_000) { options.loadTimeColors().any { it.durationMin == "5min" } }

            compose.onNodeWithText("5min").performClick()
            compose.onNode(hasSetTextAction().and(hasText("5min"))).performTextReplacement("4min")
            compose.onNodeWithText(compose.activity.getString(R.string.save)).performClick()
            compose.waitUntil(5_000) { options.loadTimeColors().any { it.durationMin == "4min" } }
            assertTrue(options.loadTimeColors().none { it.durationMin == "5min" })

            compose.onNodeWithTag("history_time_colors_add").performClick()
            compose.onNode(hasSetTextAction().and(hasText("10min"))).performTextReplacement("3min")
            pressBack()
            pressBack()
            assertTrue(options.loadTimeColors().none { it.durationMin == "3min" })

            compose.onNodeWithText("4min").performClick()
            compose.onNodeWithText(compose.activity.getString(R.string.delete)).performClick()
            compose.waitUntil(5_000) { options.loadTimeColors().isEmpty() }
        } finally {
            options.saveTimeColors(initialColors)
            options.showTimeColor = initialShowTimeColor
            compose.runOnIdle {
                HistoryOptions.historyTimeColors.apply {
                    clear()
                    addAll(initialColors)
                }
                HistoryOptions.showHistoryTimeColor.value = initialVisible
            }
        }
    }

    @Test
    fun deleteAllFabRemovesOnlyQaHistoryRecordsAfterConfirmation() {
        navigateToCalls()
        val number = "+15550000031"
        QaTestEnvironment.addCall(
            HistoryRecord(peer = number, time = System.currentTimeMillis(), result = Def.RESULT_ALLOWED_BY_DEFAULT)
        )
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(QaTestEnvironment.context())
        }

        compose.onNodeWithContentDescription(compose.activity.getString(R.string.delete)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.delete)).performClick()
        compose.onNodeWithText(number, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun smsBlockedFilterHidesAllowedFixtureOnly() {
        val context = QaTestEnvironment.context()
        val allowed = "+15550000054"
        val blocked = "+15550000055"
        QaTestEnvironment.addSms(HistoryRecord(peer = allowed, time = 2, result = Def.RESULT_ALLOWED_BY_DEFAULT, reason = "QA"))
        QaTestEnvironment.addSms(HistoryRecord(peer = blocked, time = 1, result = Def.RESULT_BLOCKED_BY_SPAM_DB, reason = "QA"))
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = false
            HistoryOptions.showHistoryBlocked.value = true
            G.smsVM.reload(context)
        }
        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.call), useUnmergedTree = true).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.sms), useUnmergedTree = true).performClick()
        compose.onNodeWithText(blocked, useUnmergedTree = true).assertExists()
        compose.onNodeWithText(allowed, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun callContextMenuDeletesOnlySelectedQaRecord() {
        val context = QaTestEnvironment.context()
        val deleted = "+15550000057"
        val retained = "+15550000058"
        QaTestEnvironment.addCall(HistoryRecord(peer = deleted, time = 2, result = Def.RESULT_ALLOWED_BY_DEFAULT, reason = "QA"))
        QaTestEnvironment.addCall(HistoryRecord(peer = retained, time = 1, result = Def.RESULT_ALLOWED_BY_DEFAULT, reason = "QA"))
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.reload(context)
        }
        navigateToCalls()
        openCallContextMenu(deleted)
        compose.onNodeWithText(compose.activity.getString(R.string.delete)).performClick()
        compose.waitUntil(5_000) { CallTable().listAll(context).none { it.peer == deleted } }
        assertTrue(CallTable().listAll(context).any { it.peer == retained })
    }

    @Test
    fun callHistoryRendersLoadingErrorRetryEmptyAndContentStates() {
        val context = QaTestEnvironment.context()
        val number = "+15550000088"
        compose.runOnIdle {
            HistoryOptions.showHistoryPassed.value = true
            HistoryOptions.showHistoryBlocked.value = true
            G.callVM.records.clear()
            G.callVM.isLoading.value = true
            G.callVM.hasError.value = false
        }
        navigateToCalls()
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertExists()

        compose.runOnIdle {
            G.callVM.isLoading.value = false
            G.callVM.hasError.value = true
        }
        compose.onNodeWithText(compose.activity.getString(R.string.history_error_loading)).assertExists()
        compose.onNodeWithText(compose.activity.getString(R.string.retry)).performClick()
        compose.waitUntil(5_000) { !G.callVM.isLoading.value }
        compose.onNodeWithText(compose.activity.getString(R.string.history_empty)).assertExists()

        QaTestEnvironment.addCall(
            HistoryRecord(
                peer = number,
                time = System.currentTimeMillis(),
                result = Def.RESULT_ALLOWED_BY_DEFAULT,
                reason = "QA state",
            )
        )
        compose.runOnIdle { G.callVM.reload(context) }
        compose.onNodeWithText(number).assertExists()

        val smsNumber = "+15550000089"
        clickBottomNav(compose.activity.getString(R.string.sms))
        compose.runOnIdle {
            G.smsVM.records.clear()
            G.smsVM.isLoading.value = true
            G.smsVM.hasError.value = false
        }
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertExists()
        compose.runOnIdle {
            G.smsVM.isLoading.value = false
            G.smsVM.hasError.value = true
        }
        compose.onNodeWithText(compose.activity.getString(R.string.history_error_loading)).assertExists()
        compose.onNodeWithText(compose.activity.getString(R.string.retry)).performClick()
        compose.waitUntil(5_000) { !G.smsVM.isLoading.value }
        compose.onNodeWithText(compose.activity.getString(R.string.history_empty)).assertExists()

        QaTestEnvironment.addSms(
            HistoryRecord(
                peer = smsNumber,
                time = System.currentTimeMillis(),
                result = Def.RESULT_ALLOWED_BY_DEFAULT,
                reason = "QA state",
            )
        )
        compose.runOnIdle { G.smsVM.reload(context) }
        compose.onNodeWithText(smsNumber).assertExists()
    }

    private fun historySettingSwitch(labelId: Int) = run {
        val labels = compose.onAllNodesWithText(
            compose.activity.getString(labelId), useUnmergedTree = true
        )
        labels[0].performScrollTo()
        val labelNodes = labels.fetchSemanticsNodes()
        val switches = compose.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch),
            useUnmergedTree = true,
        )
        val nodes = switches.fetchSemanticsNodes()
        if (labelNodes.size == 1) {
            val labelY = labelNodes.single().boundsInRoot.center.y
            return@run switches[nodes.indices.minBy { kotlin.math.abs(nodes[it].boundsInRoot.center.y - labelY) }]
        }
        switches[nodes.indices.minBy { switchIndex ->
            val switchCenter = nodes[switchIndex].boundsInRoot.center
            val labelCenter = labelNodes.last().boundsInRoot.center
            val dx = switchCenter.x - labelCenter.x
            val dy = switchCenter.y - labelCenter.y
            dx * dx + dy * dy
        }]
    }

    private fun filterButton(label: String) = compose.onNode(
        hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
    )

    private fun historySettingButton(labelId: Int) = run {
        val label = compose.onNodeWithText(
            compose.activity.getString(labelId), useUnmergedTree = true
        ).performScrollTo()
        val center = label.fetchSemanticsNode().boundsInRoot.center
        val buttons = compose.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button),
            useUnmergedTree = true,
        )
        val nodes = buttons.fetchSemanticsNodes()
        buttons[nodes.indices.minBy {
            val buttonCenter = nodes[it].boundsInRoot.center
            val dx = buttonCenter.x - center.x
            val dy = buttonCenter.y - center.y
            dx * dx + dy * dy
        }]
    }

    private fun navigateToCalls() {
        clickBottomNav(compose.activity.getString(R.string.protection))
        clickBottomNav(compose.activity.getString(R.string.call))
    }

    private fun navigateToSms() {
        clickBottomNav(compose.activity.getString(R.string.protection))
        clickBottomNav(compose.activity.getString(R.string.call))
        clickBottomNav(compose.activity.getString(R.string.sms))
    }

    private fun clickBottomNav(label: String) {
        val items = compose.onAllNodesWithText(label, useUnmergedTree = true)
        items[items.fetchSemanticsNodes().lastIndex].performClick()
    }

    private fun openCallContextMenu(number: String) {
        openSmsContextMenu(number)
        compose.onNodeWithText(compose.activity.getString(R.string.copy_number)).assertExists()
    }

    private fun openSmsContextMenu(number: String) {
        compose.onNodeWithText(number, useUnmergedTree = true).performTouchInput { longClick() }
        compose.onNodeWithText(compose.activity.getString(R.string.copy_number)).assertExists()
    }

    private fun resetSearchState() = compose.runOnIdle {
        listOf(G.callVM, G.smsVM).forEach { vm ->
            vm.searchEnabled.value = false
            vm.filter.value = ""
            vm.records.clear()
        }
    }
}
