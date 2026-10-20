package spam.blocker.ui.support

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.ui.AuthCallbackActivity
import spam.blocker.ui.NotificationTrampolineActivity
import spam.blocker.ui.crash.CrashReportActivity
import spam.blocker.ui.main.MainActivity
import spam.blocker.qa.QaTestEnvironment
import spam.blocker.ui.setting.api.PhoneBlock
import spam.blocker.ui.widgets.FileChooser
import spam.blocker.ui.widgets.InitFile
import spam.blocker.util.spf

@RunWith(AndroidJUnit4::class)
class SupportFlowsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun interceptPlatformActivities() {
        Intents.init()
    }

    @After
    fun releaseIntents() {
        Intents.release()
    }

    @Test
    fun crashReportActivityAcceptsLocalStackTrace() {
        val context = compose.activity
        val intent = Intent(context, CrashReportActivity::class.java)
            .putExtra("stackTrace", "QA-only synthetic exception")
        ActivityScenario.launch<CrashReportActivity>(intent).use { scenario ->
            scenario.onActivity {
                assertEquals("QA-only synthetic exception", it.intent.getStringExtra("stackTrace"))
            }
        }
    }

    @Test
    fun oauthCallbackStoresMatchingLocalTokenAndReturnsToMain() {
        val context = QaTestEnvironment.context()
        spf.OAuth(context).phoneBlockToken = ""
        intending(hasComponent(MainActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        val callback = Intent(context, AuthCallbackActivity::class.java)
            .setData(Uri.parse("spamblocker://auth?state=${PhoneBlock.OAuthState}&loginToken=qa-token"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(callback)

        intended(hasComponent(MainActivity::class.java.name))
        assertEquals("qa-token", spf.OAuth(context).phoneBlockToken)
    }

    @Test
    fun notificationTrampolineRoutesCallsToInterceptedCallLog() {
        intending(hasData("content://call_log/calls"))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
        val intent = Intent(QaTestEnvironment.context(), NotificationTrampolineActivity::class.java)
            .putExtra("type", "call")
            .putExtra("blocked", true)
        ActivityScenario.launch<NotificationTrampolineActivity>(intent).use {
            intended(hasData("content://call_log/calls"))
        }
    }

    @Test
    fun documentPickerReadAndWriteIntentsReturnControlledCancellation() {
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
        compose.activity.runOnUiThread {
            FileChooser.popupRead(InitFile("qa.csv", arrayOf("text/*"))) { }
        }
        intended(hasAction(Intent.ACTION_OPEN_DOCUMENT))

        intending(hasAction(Intent.ACTION_CREATE_DOCUMENT))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
        compose.activity.runOnUiThread {
            FileChooser.popupWrite(InitFile("qa.txt", arrayOf("text/plain"))) { }
        }
        intended(hasAction(Intent.ACTION_CREATE_DOCUMENT))
        assertTrue(QaTestEnvironment.context().packageName.endsWith(".qa"))
    }
}
