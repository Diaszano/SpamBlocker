package spam.blocker.ui.setting

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.R
import spam.blocker.db.QueryApiTable
import spam.blocker.db.ReportApiTable
import spam.blocker.qa.QaTestEnvironment
import spam.blocker.ui.main.MainActivity
import spam.blocker.util.Permission

@RunWith(AndroidJUnit4::class)
class ApiCreationFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissions = GrantPermissionRule.grant(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
    )

    @Before
    fun prepareQaDataAndPermissions() {
        val context = QaTestEnvironment.context()
        QueryApiTable().clearAll(context)
Permission.init(context)
val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
compose.onNode(tab.and(hasText(compose.activity.getString(R.string.tab_settings)))).performClick()
compose.onNode(tab.and(hasText(compose.activity.getString(R.string.call)))).performClick()
    }

    @After
    fun restoreQaDataAndPermissionCache() {
        QueryApiTable().clearAll(QaTestEnvironment.context())
        Permission.phoneState.isGranted = phoneStateInitiallyGranted
        Permission.callLog.isGranted = callLogInitiallyGranted
    }

    @Test
fun customQueryApiCanBeCreatedEditedDeletedAndRestored() {
        val context = QaTestEnvironment.context()
        compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
            .performClick()
        compose.onAllNodesWithText(context.getString(R.string.new_))[0]
            .performScrollTo()
            .performClick()
        compose.onNodeWithText(context.getString(R.string.customize)).performClick()

        val description = "QA query API"
        compose.onAllNodes(hasSetTextAction())[0].performTextInput(description)
        pressBack()
        compose.onNodeWithText(context.getString(R.string.save)).performScrollTo().performClick()
        compose.waitUntil(5_000) {
            QueryApiTable().listAll(context).any { it.desc == description }
        }

        assertTrue(QueryApiTable().listAll(context).any { it.desc == description })

compose.onNodeWithText(description).performScrollTo().performClick()
    val updatedDescription = "QA API edited"
    val descriptionField = compose.onNode(hasSetTextAction().and(hasText(description)))
    descriptionField.performTextReplacement(updatedDescription)
compose.waitForIdle()
    pressBack()
    compose.onNodeWithText(context.getString(R.string.save)).performScrollTo().performClick()
    compose.waitUntil(5_000) {
        QueryApiTable().listAll(context).any { it.desc == updatedDescription }
    }
    assertFalse(QueryApiTable().listAll(context).any { it.desc == description })

    compose.onNodeWithText(updatedDescription)
        .performTouchInput { swipeLeft() }
    compose.waitUntil(5_000) {
        QueryApiTable().listAll(context).none { it.desc == updatedDescription }
    }
    compose.onNodeWithText(context.getString(R.string.undelete)).performClick()
    compose.waitUntil(5_000) {
        QueryApiTable().listAll(context).any { it.desc == updatedDescription }
    }
    }

 @Test
 fun queryAndReportApiEditorsCanBeCancelledWithoutSaving() {
 val context = QaTestEnvironment.context()
 val queryTable = QueryApiTable()
 val reportTable = ReportApiTable()
 queryTable.clearAll(context)
 reportTable.clearAll(context)
 try {
 compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
 .performClick()
 compose.onAllNodesWithText(context.getString(R.string.new_))[0]
 .performScrollTo()
 .performClick()
 compose.onNodeWithText(context.getString(R.string.customize)).performClick()
 compose.onAllNodes(hasSetTextAction())[0].performTextInput("Cancelled query")
 pressBack()
 assertTrue(queryTable.listAll(context).isEmpty())

 compose.onAllNodesWithText(context.getString(R.string.new_))[1]
 .performScrollTo()
 .performClick()
 compose.onNodeWithText(context.getString(R.string.customize)).performClick()
 compose.onAllNodes(hasSetTextAction())[0].performTextInput("Cancelled report")
 pressBack()
 assertTrue(reportTable.listAll(context).isEmpty())
 } finally {
 queryTable.clearAll(context)
 reportTable.clearAll(context)
 }
 }

 companion object {
        private var phoneStateInitiallyGranted = false
        private var callLogInitiallyGranted = false

        @BeforeClass
        @JvmStatic
        fun captureInitialPermissionState() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            phoneStateInitiallyGranted = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
            callLogInitiallyGranted = context.checkSelfPermission(Manifest.permission.READ_CALL_LOG) ==
                PackageManager.PERMISSION_GRANTED
        }
    }
}
