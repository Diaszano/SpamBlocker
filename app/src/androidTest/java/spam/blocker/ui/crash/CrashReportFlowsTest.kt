package spam.blocker.ui.crash

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.allOf
import spam.blocker.R
import spam.blocker.db.CallTable
import spam.blocker.db.HistoryRecord
import spam.blocker.db.SmsTable
import spam.blocker.def.Def
import spam.blocker.qa.QaTestEnvironment
import spam.blocker.ui.setting.misc.REPO

@RunWith(AndroidJUnit4::class)
class CrashReportFlowsTest {
    @get:Rule
    val compose = createAndroidComposeRule<CrashReportActivity>()

    @Before
    fun setUp() {
        Intents.init()
        QaTestEnvironment.clearHistory()
        QaTestEnvironment.addCall(
            HistoryRecord(peer = "+15550000071", time = 1L, result = Def.RESULT_ALLOWED_BY_DEFAULT, reason = "QA call")
        )
        QaTestEnvironment.addSms(
            HistoryRecord(peer = "+15550000072", time = 1L, result = Def.RESULT_ALLOWED_BY_DEFAULT, reason = "QA SMS")
        )
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        )
    }

    @After
    fun tearDown() {
        Intents.release()
        QaTestEnvironment.clearHistory()
    }

    @Test
    fun cancelingHistoryClearKeepsQaRecordsAndConfirmingRemovesThem() {
        val context = QaTestEnvironment.context()
        val calls = CallTable()
        val sms = SmsTable()
        assertEquals(1, calls.count(context))
        assertEquals(1, sms.count(context))

        compose.onNodeWithText(context.getString(R.string.clear_history)).performClick()
        compose.onNodeWithText(context.getString(R.string.confirm_to_delete)).assertExists()
        InstrumentationRegistry.getInstrumentation()
            .sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
        compose.waitForIdle()
        assertEquals(1, calls.count(context))
        assertEquals(1, sms.count(context))

        compose.onNodeWithText(context.getString(R.string.clear_history)).performClick()
        compose.onNodeWithText(context.getString(R.string.delete)).performClick()
        compose.waitUntil(5_000) { calls.count(context) == 0 && sms.count(context) == 0 }
    }

    @Test
    fun copyButtonPlacesCrashDetailsOnTheClipboard() {
        compose.onNodeWithText(compose.activity.getString(R.string.copy)).performClick()
        val clipboard = compose.activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertTrue(clipboard.primaryClip?.getItemAt(0)?.text?.contains("android code:") == true)
    }

    @Test
    fun reportBugOpensRepositoryIssuesThroughInterceptedIntent() {
        compose.onNodeWithText(compose.activity.getString(R.string.report_bug)).performClick()
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(Uri.parse("$REPO/issues"))))
    }
}
