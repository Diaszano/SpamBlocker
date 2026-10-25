package spam.blocker.ui.support

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.edit
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
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
import spam.blocker.qa.QaDocumentProvider
import spam.blocker.ui.setting.api.PhoneBlock
import spam.blocker.ui.widgets.FileChooser
import spam.blocker.ui.widgets.InitFile
import spam.blocker.util.PermissionType
import spam.blocker.util.spf
import java.util.concurrent.atomic.AtomicReference
import androidx.test.platform.app.InstrumentationRegistry

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
    fun invalidOauthCallbacksDoNotOverwriteSavedToken() {
        val context = QaTestEnvironment.context()
        val oauth = spf.OAuth(context)
        oauth.phoneBlockToken = "keep-token"
        intending(hasComponent(MainActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        listOf(
            Uri.parse("spamblocker://auth?loginToken=missing-state"),
            Uri.parse("spamblocker://auth?state=unknown&loginToken=bad-token"),
            Uri.parse("spamblocker://"),
        ).forEach { uri ->
            context.startActivity(
                Intent(context, AuthCallbackActivity::class.java)
                    .setData(uri)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            compose.waitForIdle()
            assertEquals("keep-token", oauth.phoneBlockToken)
        }

        assertEquals(
            3,
            Intents.getIntents().count { it.component?.className == MainActivity::class.java.name },
        )
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
    fun notificationTrampolineRoutesUnblockedSmsToInterceptedConversation() {
        val number = "+15550000061"
        intending(hasAction(Intent.ACTION_SENDTO))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
        val intent = Intent(QaTestEnvironment.context(), NotificationTrampolineActivity::class.java)
            .putExtra("type", "sms")
            .putExtra("blocked", false)
            .putExtra("rawNumber", number)
        ActivityScenario.launch<NotificationTrampolineActivity>(intent).use {
            intended(hasAction(Intent.ACTION_SENDTO))
            intended(hasData("smsto:$number"))
        }
    }

@Test
fun notificationTrampolineRoutesBlockedSmsToInterceptedDefaultSmsApp() {
    val context = QaTestEnvironment.context()
    val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context)
    assumeTrue("A default SMS app must be configured", defaultSmsPackage != null)
    val defaultSmsComponent = context.packageManager
        .getLaunchIntentForPackage(defaultSmsPackage!!)?.component
    assumeTrue("The default SMS app must expose a launcher activity", defaultSmsComponent != null)
    val componentName = defaultSmsComponent!!.className
    intending(hasComponent(componentName))
        .respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))

    val intent = Intent(context, NotificationTrampolineActivity::class.java)
        .putExtra("type", "sms")
        .putExtra("blocked", true)
    ActivityScenario.launch<NotificationTrampolineActivity>(intent).use {
        intended(hasComponent(componentName))
    }
}

@Test
fun notificationTrampolineRoutesUnknownTypeBackToMainActivity() {
        intending(hasComponent(MainActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
        val intent = Intent(QaTestEnvironment.context(), NotificationTrampolineActivity::class.java)
            .putExtra("type", "unknown-qa-route")
        ActivityScenario.launch<NotificationTrampolineActivity>(intent).use {
            intended(hasComponent(MainActivity::class.java.name))
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

@Test
fun documentPickerReturnsSelectedQaUrisAndStoresDirectoryTags() {
val context = QaTestEnvironment.context()
val preferences = spf.SharedPref(context).prefs
val readTag = "qa_test_read_uri"
val writeTag = "qa_test_write_uri"
val readName = "picked-qa.csv"
val writeName = "created-qa.txt"
val readUri = QaDocumentProvider.uriFor(readName)
val writeUri = QaDocumentProvider.uriFor(writeName)
val csv = "pattern\\n12345".toByteArray()
val text = "QA document output".toByteArray()
QaDocumentProvider.putDocument(context, readName, csv)
QaDocumentProvider.clearDocument(context, writeName)
preferences.edit { remove(readTag); remove(writeTag) }

try {
val selectedRead = AtomicReference<Uri?>()
intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(
Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(readUri))
)
compose.activity.runOnUiThread {
FileChooser.popupRead(InitFile(readName, arrayOf("text/*"), readTag)) { selectedRead.set(it) }
}
compose.waitUntil(5_000) { selectedRead.get() == readUri }
assertEquals(readUri.toString(), preferences.getString(readTag, null))
assertEquals("pattern\\n12345", context.contentResolver.openInputStream(readUri)!!.bufferedReader().use { it.readText() })

val selectedWrite = AtomicReference<Uri?>()
intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(
Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(writeUri))
)
compose.activity.runOnUiThread {
FileChooser.popupWrite(InitFile(writeName, arrayOf("text/plain"), writeTag)) { uri ->
selectedWrite.set(uri)
uri?.let { context.contentResolver.openOutputStream(it, "wt")?.use { output -> output.write(text) } }
}
}
compose.waitUntil(5_000) { selectedWrite.get() == writeUri && QaDocumentProvider.getDocument(context, writeName) != null }
assertEquals(writeUri.toString(), preferences.getString(writeTag, null))
assertEquals("QA document output", String(QaDocumentProvider.getDocument(context, writeName)!!))
} finally {
preferences.edit { remove(readTag); remove(writeTag) }
QaDocumentProvider.clearDocument(context, readName)
QaDocumentProvider.clearDocument(context, writeName)
}
}

@Test
fun safDirectoryGrantPersistsAndCanBeRevoked() {
    val context = QaTestEnvironment.context()
    val providerContext = InstrumentationRegistry.getInstrumentation().context
    val uri = QaDocumentProvider.uriFor("qa-directory")
    val read = Intent.FLAG_GRANT_READ_URI_PERMISSION
    val write = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    val persistable = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    val grantFlags = read or write or persistable
    val access = PermissionType.SafDirAccess()

    try {
        access.onResult(context, false, uri)
        assertFalse(access.check(context))

        providerContext.grantUriPermission(context.packageName, uri, grantFlags)
        access.onResult(context, true, uri)
        assertTrue(access.check(context))

        context.contentResolver.releasePersistableUriPermission(uri, read or write)
        assertFalse(access.check(context))
    } finally {
        context.contentResolver.persistedUriPermissions
            .filter { it.uri == uri }
            .forEach { permission ->
                val flags = (if (permission.isReadPermission) read else 0) or
                    (if (permission.isWritePermission) write else 0)
                if (flags != 0) context.contentResolver.releasePersistableUriPermission(uri, flags)
            }
        providerContext.revokeUriPermission(uri, grantFlags)
    }
}
}
