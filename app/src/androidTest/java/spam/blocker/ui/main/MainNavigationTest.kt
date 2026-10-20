package spam.blocker.ui.main

import android.app.Activity
import android.app.Instrumentation
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.SemanticsMatcher
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

@RunWith(AndroidJUnit4::class)
class MainNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearIntents() {
        Intents.init()
    }

    @After
    fun releaseIntents() {
        Intents.release()
    }

    @Test
    fun allFourTabsCanBeSelectedAndHistoryReselectionIsIntercepted() {
        intending(anyIntent()).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        val labels = listOf(R.string.call, R.string.sms, R.string.protection, R.string.tab_settings)
        labels.forEach { id ->
            val label = compose.activity.getString(id)
            compose.onNode(tabWithLabel(label))
                .performClick()
                .assertIsSelected()
        }

        val calls = compose.activity.getString(R.string.call)
        compose.onNode(tabWithLabel(calls))
            .performClick()
            .assertIsSelected()

        val sms = compose.activity.getString(R.string.sms)
        compose.onNode(tabWithLabel(sms))
            .performClick()
            .assertIsSelected()
    }

    private fun tabWithLabel(label: String) =
        hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
}
