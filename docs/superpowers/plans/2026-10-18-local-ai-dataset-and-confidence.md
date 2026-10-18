# Local AI Dataset Portability and Confidence Scoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable exporting and importing Local AI Bayesian training datasets (JSON/CSV) so users don't start from zero across installs, and surface the AI confidence percentage in the SMS History details.

**Architecture:** Extend `BayesTable` serialization to support standalone JSON import/export in `LocalAI.kt`. In `ComplementNaiveBayes.kt`, expose the normalized prediction probability alongside the binary classification result so `ByNaiveBayes` can pass the confidence score to `HistoryRecord.extraInfo` / `HistoryIndicator`.

**Tech Stack:** Kotlin, Jetpack Compose, Complement Naive Bayes, SQLite, Kotlinx Serialization.

## Global Constraints

- Preserve existing `TABLE_BAYESIAN_FILTER` database schema; import deduplicates via `hash`.
- No network requests or cloud models; all Bayesian inference and dataset handling remain 100% on-device.
- Keep UI compact; dataset export/import actions live inside `LocalAISettings` dialog without expanding the main settings row.
- New string resources in `res/values/strings_ui_ux.xml` in English with `translatable="false"`.

---

### Task 1: Dataset Export and Import Serialization

**Files:**
- Modify: `app/src/main/java/spam/blocker/db/BayesTable.kt`
- Modify: `app/src/main/java/spam/blocker/ui/setting/quick/LocalAI.kt`
- Test: `app/src/test/java/spam/blocker/db/BayesSerializationTest.kt`

**Interfaces:**
- Produces: `fun BayesTable.exportToJson(ctx: Context): String`
- Produces: `fun BayesTable.importFromJson(ctx: Context, json: String): Int` (returns count of newly added samples)

- [x] **Step 1: Write test for Bayes dataset JSON serialization**

```kotlin
package spam.blocker.db

import org.junit.Assert.assertEquals
import org.junit.Test

class BayesSerializationTest {
    @Test
    fun testExportImportRoundTrip() {
        val samples = listOf(
            BayesSample(id = 1, hash = 1234, isSpam = true, content = "Ganhe dinheiro agora"),
            BayesSample(id = 2, hash = 5678, isSpam = false, content = "Codigo de confirmacao 987")
        )
        val json = BayesTable.serializeSamples(samples)
        val parsed = BayesTable.deserializeSamples(json)
        assertEquals(2, parsed.size)
        assertEquals(samples[0].content, parsed[0].content)
        assertEquals(samples[1].isSpam, parsed[1].isSpam)
    }
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew testFdroidDebugUnitTest --tests "spam.blocker.db.BayesSerializationTest"`
Expected: FAIL

- [x] **Step 3: Implement `serializeSamples` / `deserializeSamples` and add Import/Export buttons to `LocalAISettings` dialog**

- [x] **Step 4: Run test to verify it passes**

Run: `./gradlew testFdroidDebugUnitTest --tests "spam.blocker.db.BayesSerializationTest"`
Expected: PASS

---

### Task 2: Confidence Score in Checker and History Display

**Files:**
- Modify: `app/src/main/java/spam/blocker/util/ComplementNaiveBayes.kt`
- Modify: `app/src/main/java/spam/blocker/service/checker/CheckResult.kt`
- Modify: `app/src/main/java/spam/blocker/service/checker/Checker.kt`
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryIndicator.kt`

**Interfaces:**
- `ComplementNaiveBayes.predictWithConfidence(tokens: List<String>): Pair<Boolean, Float>`
- Passes percentage to `ByNaiveBayes(result, confidence = score)` and displays discrete tag `AI: 95%` in `HistoryIndicator`.

- [x] **Step 1: Write test for confidence score calculation**
- [x] **Step 2: Implement `predictWithConfidence` using softmax over log-likelihoods**
- [x] **Step 3: Render discrete tag in history indicators**
- [x] **Step 4: Run `./gradlew assembleDebug lint` and commit: `feat: add local ai dataset export and confidence indicator`**
