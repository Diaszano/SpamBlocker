package spam.blocker.ui.history

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import android.app.Activity
import android.app.Instrumentation
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.R
import spam.blocker.G
import spam.blocker.db.HistoryRecord
import spam.blocker.def.Def
import spam.blocker.qa.QaTestEnvironment
import spam.blocker.ui.main.MainActivity
import spam.blocker.util.spf

@RunWith(AndroidJUnit4::class)
class HistoryFlowsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearQaHistory() {
        Intents.init()
        intending(anyIntent()).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        QaTestEnvironment.clearHistory()
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
    }

    @Test
    fun historyContextMenuFiltersRecordsAndSearchCanBeCleared() {
        navigateToCalls()
        val first = "+15550000011"
        val second = "+15550000012"
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
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(second, useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithText(first, useUnmergedTree = true).assertExists()

        search.performTextClearance()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(second, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        search.performTextInput("no-match")
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(compose.activity.getString(R.string.history_no_matches)).fetchSemanticsNodes().isNotEmpty()
        }
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

    private fun filterButton(label: String) = compose.onNode(
        hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
    )

    private fun navigateToCalls() {
        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.call), useUnmergedTree = true).performClick()
    }

    private fun resetSearchState() = compose.runOnIdle {
        listOf(G.callVM, G.smsVM).forEach { vm ->
            vm.searchEnabled.value = false
            vm.filter.value = ""
            vm.records.clear()
        }
    }
}
