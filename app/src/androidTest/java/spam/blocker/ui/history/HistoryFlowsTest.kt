package spam.blocker.ui.history

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
    }

    @After
    fun removeQaHistory() {
        QaTestEnvironment.clearHistory()
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

    private fun filterButton(label: String) = compose.onNode(
        hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
    )
}
