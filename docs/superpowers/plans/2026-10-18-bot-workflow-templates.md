# Bot Workflow Templates Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provide ready-made workflow templates in the Bots section (e.g. Community SpamDB auto-sync, Telegram/Webhook alert on blocked call, and automatic history cleanup) so users can enable powerful automations with one click.

**Architecture:** Create `BotTemplates.kt` defining verified pre-configured `Workflow` chains using existing `IAction` and `ITriggerAction` implementations. Add a "Templates" button in `BotHeader` that opens a dialog showcasing available templates with descriptions and one-tap addition to the user's workflows.

**Tech Stack:** Kotlin, Jetpack Compose, Kotlinx Serialization, WorkManager (`MyWorkManager.kt`).

## Global Constraints

- No changes to `IAction` interface or execution logic. Templates instantiate existing action objects.
- Retain dark compact UI and existing `BotHeader` collapse states.
- All template strings in English in `res/values/strings_ui_ux.xml`.

---

### Task 1: Define Bot Templates and Presets Registry

**Files:**
- Create: `app/src/main/java/spam/blocker/service/bot/BotTemplates.kt`
- Test: `app/src/test/java/spam/blocker/service/bot/BotTemplatesTest.kt`

**Interfaces:**
- Produces: `object BotTemplates { fun listTemplates(ctx: Context): List<WorkflowTemplate> }`
- `data class WorkflowTemplate(val titleRes: Int, val descriptionRes: Int, val createWorkflow: (Context) -> Workflow)`

- [x] **Step 1: Write unit test validating all templates construct valid, serializable workflows**

```kotlin
package spam.blocker.service.bot

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BotTemplatesTest {
    @Test
    fun testAllTemplatesConstructValidChains() {
        val templates = BotTemplates.getPredefinedTemplates()
        assertTrue(templates.isNotEmpty())
        templates.forEach { template ->
            val workflow = template.factory()
            assertNotNull(workflow.trigger)
            assertTrue(workflow.actions.isNotEmpty())
        }
    }
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew testFdroidDebugUnitTest --tests "spam.blocker.service.bot.BotTemplatesTest"`
Expected: FAIL

- [x] **Step 3: Implement `BotTemplates.kt` with 3 core templates (SpamDB Sync, Telegram Webhook, History Auto-Prune)**

- [x] **Step 4: Run test to verify it passes**

Run: `./gradlew testFdroidDebugUnitTest --tests "spam.blocker.service.bot.BotTemplatesTest"`
Expected: PASS

---

### Task 2: UI Template Selection Dialog in Bots Section

**Files:**
- Create: `app/src/main/java/spam/blocker/ui/setting/bot/BotTemplateDialog.kt`
- Modify: `app/src/main/java/spam/blocker/ui/setting/bot/BotHeader.kt`

- [x] **Step 1: Create `BotTemplateDialog.kt` with compact selection cards and "Apply" action**
- [x] **Step 2: Connect template button in `BotHeader.kt` to append selected template into `G.botVM.workflows`**
- [x] **Step 3: Add dark 320dp/1.3x preview for `BotTemplateDialog`**
- [x] **Step 4: Run `./gradlew assembleDebug lint` and commit: `feat: add preconfigured bot workflow templates`**
