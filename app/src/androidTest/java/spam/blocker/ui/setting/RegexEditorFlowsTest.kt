package spam.blocker.ui.setting

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.longClick
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import spam.blocker.R
import spam.blocker.def.Def
import spam.blocker.db.ContentRegexTable
import spam.blocker.db.NumberRegexTable
import spam.blocker.db.QuickCopyRegexTable
import spam.blocker.db.RegexRule
import spam.blocker.qa.QaDocumentProvider
import spam.blocker.qa.QaTestEnvironment
import spam.blocker.ui.main.MainActivity
import spam.blocker.util.spf

@RunWith(AndroidJUnit4::class)
class RegexEditorFlowsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearQaNumberRules() {
Intents.init()
        NumberRegexTable().clearAll(QaTestEnvironment.context())
ContentRegexTable().clearAll(QaTestEnvironment.context())
        val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        compose.onNode(tab.and(hasText(compose.activity.getString(R.string.tab_settings)))).performClick()
        compose.onNode(tab.and(hasText(compose.activity.getString(R.string.call)))).performClick()
    }

    @After
    fun removeQaNumberRules() {
        NumberRegexTable().clearAll(QaTestEnvironment.context())
ContentRegexTable().clearAll(QaTestEnvironment.context())
QuickCopyRegexTable().clearAll(QaTestEnvironment.context())
Intents.release()
    }

    @Test
    fun invalidNumberRegexCannotBeSavedOrPersisted() {
        val context = QaTestEnvironment.context()
        val rules = NumberRegexTable()
        rules.clearAll(context)
        openNumberRegexEditor()

        val patternField = compose.onNode(
            hasSetTextAction().and(hasText(compose.activity.getString(R.string.phone_number)))
        )
        patternField.performTextClearance()
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("[")
        patternField.assertTextContains("[", substring = true)
        compose.onNodeWithText(compose.activity.getString(R.string.invalid_regex_pattern), useUnmergedTree = true)
            .assertExists()
        saveButton().assertIsNotEnabled()
        assertTrue(rules.listAll(context).isEmpty())
    }

    @Test
    fun validNumberRegexCanBeSavedToQaDatabase() {
        val context = QaTestEnvironment.context()
        val rules = NumberRegexTable()
        openNumberRegexEditor()

        val patternField = compose.onNode(
            hasSetTextAction().and(hasText(compose.activity.getString(R.string.phone_number)))
        )
        patternField.performTextClearance()
        patternField.performTextInput("12345")
        patternField.assertTextContains("12345")
        saveButton().assertExists()
        pressBack()
        saveButton().performClick()

        compose.waitUntil(15_000) { rules.listAll(context).size == 1 }
        assertTrue("Unexpected imported rule: ${rules.listAll(context).single()}", rules.listAll(context).single().pattern == "12345")
    }

@Test
fun contentRegexCanBeCreatedEditedAndDeletedThroughUi() {
val context = QaTestEnvironment.context()
val rules = ContentRegexTable()
rules.clearAll(context)
spf.Global(context).isCollapsed = false
spf.SettingSections(context).apply {
isQuickSettingsCollapsed = true
isRegexSettingsCollapsed = false
}
spf.RegexOptions(context).isContentCollapsed = false
compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
.performClick()
compose.onNodeWithText(compose.activity.getString(R.string.label_content_rules), substring = true)
.performScrollTo()
val newButtons = compose.onAllNodesWithText(compose.activity.getString(R.string.new_), useUnmergedTree = true)
newButtons[1].performScrollTo().performClick()
compose.onNodeWithText(compose.activity.getString(R.string.customize), useUnmergedTree = true).performClick()
val patternField = compose.onNode(
hasSetTextAction().and(hasText(compose.activity.getString(R.string.sms_content)))
)
patternField.performTextClearance()
compose.onAllNodes(hasSetTextAction())[0].performTextInput("spam")
compose.onNodeWithText(compose.activity.getString(R.string.save)).performClick()
compose.waitUntil(15_000) { rules.listAll(context).size == 1 }
assertTrue(rules.listAll(context).single().pattern == "spam")
compose.onNodeWithText("spam", useUnmergedTree = true).performClick()
val editField = compose.onNode(
hasSetTextAction().and(hasText("spam"))
)
editField.performTextClearance()
compose.onNode(
hasSetTextAction().and(hasText(compose.activity.getString(R.string.sms_content)))
).performTextInput("block")
compose.onNodeWithText(compose.activity.getString(R.string.save)).performClick()
compose.waitUntil(15_000) { rules.listAll(context).singleOrNull()?.pattern == "block" }
compose.onNodeWithTag("regex_item_${rules.listAll(context).single().id}")
    .performTouchInput { swipeLeft(startX = width * 0.8f, endX = width * 0.2f, durationMillis = 200) }
compose.waitUntil(15_000) { rules.listAll(context).isEmpty() }
}

@Test
fun quickCopyRegexCanBeCreatedEditedAndDeletedThroughUi() {
val context = QaTestEnvironment.context()
val rules = QuickCopyRegexTable()
rules.clearAll(context)
spf.Global(context).isCollapsed = false
spf.SettingSections(context).apply {
isScreeningSectionCollapsed = false
isRegexSettingsCollapsed = false
}
spf.RegexOptions(context).apply {
isNumberCollapsed = false
isContentCollapsed = false
isQuickCopyCollapsed = false
}
compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
.performClick()
compose.onNodeWithText(compose.activity.getString(R.string.quick_copy)).performScrollTo()
compose.onNodeWithTag("quick_copy_regex_add").performScrollTo().performClick()
compose.onNodeWithText(compose.activity.getString(R.string.customize)).performClick()
val patternField = compose.onAllNodes(hasSetTextAction())[0]
patternField.performTextClearance()
patternField.performTextInput("qa-copy")
compose.onNodeWithText(compose.activity.getString(R.string.phone_number_abbrev))
.performScrollTo().performClick()
val autoCopyLabel = compose.activity.getString(R.string.auto_copy)
compose.onNodeWithContentDescription(autoCopyLabel).performClick()
compose.onNodeWithText(compose.activity.getString(R.string.save)).performClick()
compose.waitUntil(15_000) { rules.listAll(context).any { it.pattern == "qa-copy" } }
val createdRule = rules.listAll(context).single()
assertTrue(createdRule.flags and Def.FLAG_FOR_NUMBER != 0)
assertTrue(createdRule.flags and Def.FLAG_FOR_CONTENT != 0)
assertTrue(createdRule.flags and Def.FLAG_FOR_PASSED != 0)
assertTrue(createdRule.flags and Def.FLAG_AUTO_COPY != 0)
compose.onNodeWithText("qa-copy", useUnmergedTree = true).performClick()
val editField = compose.onNode(hasSetTextAction().and(hasText("qa-copy")))
editField.performTextClearance()
compose.onAllNodes(hasSetTextAction())[0].performTextInput("qa-copy-edited")
compose.onNodeWithText(compose.activity.getString(R.string.save)).performClick()
compose.waitUntil(15_000) { rules.listAll(context).singleOrNull()?.pattern == "qa-copy-edited" }
val ruleId = rules.listAll(context).single().id
compose.onNodeWithTag("regex_item_$ruleId").performTouchInput {
swipeLeft(startX = width * 0.8f, endX = width * 0.2f, durationMillis = 200)
}
compose.waitUntil(15_000) { rules.listAll(context).isEmpty() }
}

@Test
fun csvImportWithoutPatternHeaderShowsWarningAndKeepsRulesUnchanged() {
        val context = QaTestEnvironment.context()
        val rules = NumberRegexTable()
        rules.clearAll(context)
rules.addNew(context, RegexRule(pattern = "qa-existing", description = "Keep on failed import"))
val documentName = "qa-missing-pattern.csv"
QaDocumentProvider.putDocument(context, documentName, "description\nQA rule".toByteArray())
        try {
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
Intent().setData(QaDocumentProvider.uriFor(documentName))
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                )
            )
            openNumberRegexImportMenu()
            onView(withText(containsString("pattern"))).check(matches(isDisplayed()))
assertEquals(listOf("qa-existing"), rules.listAll(context).map { it.pattern })
        } finally {
QaDocumentProvider.clearDocument(context, documentName)
        }
    }

    @Test
    fun csvImportWithPatternHeaderPersistsRule() {
        val context = QaTestEnvironment.context()
        val rules = NumberRegexTable()
        rules.clearAll(context)
        rules.addNew(context, RegexRule(pattern = "qa-existing", description = "Keep on import"))
val documentName = "qa-pattern.csv"
QaDocumentProvider.putDocument(context, documentName, "pattern,description\n12345,QA imported".toByteArray())
        try {
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
Intent().setData(QaDocumentProvider.uriFor(documentName))
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                )
            )
            openNumberRegexImportMenu(itemIndex = 1)
            compose.waitUntil(15_000) { rules.listAll(context).any { it.pattern == "12345" } }
            assertEquals(setOf("qa-existing", "12345"), rules.listAll(context).map { it.pattern }.toSet())
        } finally {
QaDocumentProvider.clearDocument(context, documentName)
        }
    }

    private fun saveButton() = compose.onNode(
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
            .and(hasText(compose.activity.getString(R.string.save)))
    )

    private fun openNumberRegexEditor() {
        val context = QaTestEnvironment.context()
        spf.Global(context).isCollapsed = false
        spf.SettingSections(context).apply {
            isQuickSettingsCollapsed = true
            isRegexSettingsCollapsed = false
        }
        spf.RegexOptions(context).isNumberCollapsed = false

        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
            .performClick()
        val numberRules = compose.onNodeWithText(
            compose.activity.getString(R.string.label_number_rules), substring = true
        ).performScrollTo()
        compose.onNodeWithTag("number_regex_add").performScrollTo().performClick()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText(compose.activity.getString(R.string.customize))
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(compose.activity.getString(R.string.customize)).performClick()
    }

private fun openNumberRegexImportMenu(itemIndex: Int = 0) {
        val context = QaTestEnvironment.context()
        spf.Global(context).isCollapsed = false
        spf.SettingSections(context).apply {
            isQuickSettingsCollapsed = true
            isRegexSettingsCollapsed = false
        }
        spf.RegexOptions(context).isNumberCollapsed = false
        compose.onNodeWithText(compose.activity.getString(R.string.protection), useUnmergedTree = true)
            .performClick()
        compose.onNodeWithText(
            compose.activity.getString(R.string.label_number_rules), substring = true
        ).performScrollTo()
        compose.onNodeWithTag("number_regex_add")
            .performScrollTo()
            .performTouchInput { longClick() }
        compose.onNodeWithText(context.resources.getStringArray(R.array.import_csv_type)[itemIndex])
            .performClick()
    }
}
