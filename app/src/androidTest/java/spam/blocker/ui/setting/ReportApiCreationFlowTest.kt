package spam.blocker.ui.setting

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlin.math.abs
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.R
import spam.blocker.db.ReportApi
import spam.blocker.db.ReportApiTable
import spam.blocker.qa.QaTestEnvironment
import spam.blocker.ui.main.MainActivity
import spam.blocker.service.bot.HttpRequest
import spam.blocker.service.bot.InterceptCall
import spam.blocker.util.Permission
import spam.blocker.util.spf

@RunWith(AndroidJUnit4::class)
class ReportApiCreationFlowTest {
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
        ReportApiTable().clearAll(context)
        Permission.init(context)
        val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        compose.onNode(tab.and(hasText(compose.activity.getString(R.string.tab_settings)))).performClick()
        compose.onNode(tab.and(hasText(compose.activity.getString(R.string.call)))).performClick()
    }

    @After
    fun restoreQaDataAndPermissionCache() {
        ReportApiTable().clearAll(QaTestEnvironment.context())
        Permission.phoneState.isGranted = phoneStateInitiallyGranted
        Permission.callLog.isGranted = callLogInitiallyGranted
    }

    @Test
    fun customReportApiCanBeCreatedEditedDeletedAndRestored() {
        val context = QaTestEnvironment.context()
        val description = "QA report API"
        spf.SettingSections(context).apply {
            isInstantQueryCollapsed = false
            isReportNumberCollapsed = false
        }

        compose.onNodeWithText(context.getString(R.string.tab_settings), useUnmergedTree = true)
            .performClick()
        val reportHeader = compose.onNodeWithText(
            context.getString(R.string.report_api),
            substring = true,
        ).performScrollTo()
        val newButton = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
            .and(hasText(context.getString(R.string.new_)))
        val reportHeaderY = reportHeader.fetchSemanticsNode().boundsInRoot.center.y
        val newButtons = compose.onAllNodes(newButton)
        val buttonNodes = newButtons.fetchSemanticsNodes()
        val reportNewButtonIndex = buttonNodes.indices.minBy { index ->
                abs(buttonNodes[index].boundsInRoot.center.y - reportHeaderY)
            }

        newButtons[reportNewButtonIndex].performClick()
        compose.onNodeWithText(context.getString(R.string.customize)).performClick()
        compose.onAllNodes(hasSetTextAction())[0].performTextInput(description)
        compose.onNodeWithText(context.getString(R.string.save)).performScrollTo().performClick()

        compose.waitUntil(5_000) {
            ReportApiTable().listAll(context).any { it.desc == description }
        }
        val savedApi = ReportApiTable().listAll(context).filterIsInstance<ReportApi>()
            .single { it.desc == description }
        assertTrue(savedApi.actions.size == 2)
        assertTrue(savedApi.actions[0] is InterceptCall)
        assertTrue(savedApi.actions[1] is HttpRequest)

        compose.onNodeWithText(description).performClick()
        val updatedDescription = "QA report API edited"
        val descriptionField = compose.onNode(hasSetTextAction().and(hasText(description)))
    descriptionField.performTextReplacement(updatedDescription)
        compose.onNodeWithText(context.getString(R.string.save)).performScrollTo().performClick()
        compose.waitUntil(5_000) {
            ReportApiTable().listAll(context).any { it.desc == updatedDescription }
        }
        assertFalse(ReportApiTable().listAll(context).any { it.desc == description })

        compose.onNodeWithText(updatedDescription).performTouchInput { swipeLeft() }
        compose.waitUntil(5_000) {
            ReportApiTable().listAll(context).none { it.desc == updatedDescription }
        }
        compose.onNodeWithText(context.getString(R.string.undelete)).performClick()
    compose.waitUntil(5_000) {
        ReportApiTable().listAll(context).any { it.desc == updatedDescription }
    }
    }

    companion object {
        private var phoneStateInitiallyGranted = false
        private var callLogInitiallyGranted = false

        @BeforeClass
        @JvmStatic
        fun captureInitialPermissionState() {
            val context = QaTestEnvironment.context()
            phoneStateInitiallyGranted = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
            callLogInitiallyGranted = context.checkSelfPermission(Manifest.permission.READ_CALL_LOG) ==
                PackageManager.PERMISSION_GRANTED
        }
    }
}
