package spam.blocker.ui.setting

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.R
import spam.blocker.ui.main.MainActivity

@RunWith(AndroidJUnit4::class)
class ProtectionFlowsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun protectionScreenExposesAllQuickAndAdditionalRuleAreas() {
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
}
