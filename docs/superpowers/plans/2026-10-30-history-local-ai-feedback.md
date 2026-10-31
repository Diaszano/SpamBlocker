# Treinamento Local a Partir do Histórico Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users correct an SMS spam classification from history and apply the correction to future on-device filtering.

**Architecture:** Add an idempotent feedback operation keyed by the existing `bayesSampleHash(record.time, record.extraInfo)`. Persist the prior label for undo, rebuild the local Complement Naive Bayes model from `BayesTable`, and expose actions only for SMS records whose content is already stored.

**Tech Stack:** Kotlin, Jetpack Compose, SQLite, Kotlinx Serialization, Complement Naive Bayes, JUnit 5, AndroidX instrumentation.

**Spec:** `docs/superpowers/specs/2026-10-30-history-local-ai-feedback-design.md`

## Global Constraints

- Process only `HistoryRecord.extraInfo`; do not request `READ_SMS` or fetch message text from the SMS provider for this action.
- No network calls; all samples and model files remain local.
- Same content/time maps to one training row; changing an existing label updates it rather than adding a duplicate.
- Offer feedback actions only while Local AI is enabled; preserve its current off-state behavior and link to the existing control if needed.
- Undo restores the previous label when one existed; otherwise it removes the newly added row.
- Database and serialized model must not silently diverge when rebuilding or writing fails.

## Review Focus

- SMS content is absent or Local AI is disabled: hide feedback actions and explain the requirement without reading SMS through a new permission or silently storing training data.
- Same correction repeated: row count remains unchanged.
- Existing sample is relabeled then undone: its original class is restored.
- New sample is labeled then undone: it is removed and model is rebuilt.
- Model serialization/write fails: report failure and preserve recoverable DB/model state.

## File and Test Map

- Modify: `app/src/main/java/spam/blocker/db/BayesTable.kt`
- Add: `app/src/main/java/spam/blocker/util/BayesFeedback.kt` for the shared model rebuild/feedback operation if keeping it in `LocalAI.kt` would couple history UI to settings UI.
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryContextMenu.kt`
- Reuse: `app/src/main/java/spam/blocker/ui/setting/quick/LocalAI.kt` model construction and `Bayes.Model_File` storage conventions.
- Add: `app/src/androidTest/java/spam/blocker/db/BayesFeedbackTest.kt`
- Extend: `app/src/androidTest/java/spam/blocker/ui/history/HistoryFlowsTest.kt`

## Interfaces

- `data class BayesFeedbackChange(val hash: Int, val previousLabel: Boolean?, val inserted: Boolean)`
- `BayesTable.applyFeedback(ctx: Context, time: Long, content: String, isSpam: Boolean): BayesFeedbackChange`
- `BayesTable.undoFeedback(ctx: Context, change: BayesFeedbackChange)` restores prior state.
- `BayesFeedback.rebuildModel(ctx: Context)` serializes all current `BayesTable` samples to `Bayes.Model_File`; returns failure instead of swallowing it.

---

### Task 1: Implement feedback row changes and undo semantics

**Files:**
- Modify: `app/src/main/java/spam/blocker/db/BayesTable.kt`
- Test: `app/src/androidTest/java/spam/blocker/db/BayesFeedbackTest.kt`

- [ ] **Step 1: Add failing instrumentation tests** for insert, repeat same label, relabel, undo inserted row, undo restored prior label, and stable hash from timestamp/content. These APIs use Android SQLite and must run in `androidTest`.
- [ ] **Step 2: Run** `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.db.BayesFeedbackTest`; confirm the new API is missing.
- [ ] **Step 3: Implement `applyFeedback` and `undoFeedback`** using the unique hash, existing sample ID, and bound update/delete operations. Return enough prior state to undo exactly one operation.
- [ ] **Step 4: Run the focused instrumentation test**; verify row counts and labels after every operation.
- [ ] **Step 5: Commit** as `feat: persist local ai feedback labels`.

### Task 2: Rebuild the model safely after feedback

**Files:**
- Add: `app/src/main/java/spam/blocker/util/BayesFeedback.kt`
- Modify: `app/src/main/java/spam/blocker/ui/setting/quick/LocalAI.kt` only to delegate to the shared rebuild helper if appropriate.
- Test: `app/src/androidTest/java/spam/blocker/db/BayesFeedbackTest.kt`

**Interfaces:** Produces `BayesFeedback.rebuildModel(ctx)`; consumers treat a failed rebuild as failed feedback and invoke `undoFeedback` before reporting failure.

- [ ] **Step 1: Extend instrumentation tests** for empty dataset, successful model rebuild, serialization error, and preserving the previous model file on failed replacement.
- [ ] **Step 2: Run** `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.db.BayesFeedbackTest` and confirm failure cases expose the missing behavior.
- [ ] **Step 3: Implement a shared rebuild helper** that trains from `BayesTable.listAll`, writes a temporary model file, and replaces the existing file only after serialization succeeds. Return a typed success/error result.
- [ ] **Step 4: Run the focused instrumentation test**; verify failed writes do not leave a partial model file.
- [ ] **Step 5: Commit** as `feat: rebuild local ai model after feedback`.

### Task 3: Add contextual feedback actions and undo

**Files:**
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryContextMenu.kt`
- Modify: `app/src/main/res/values/strings_ui_ux.xml` and matching locale resources.
- Test: `app/src/androidTest/java/spam/blocker/ui/history/HistoryFlowsTest.kt`

**Interfaces:** Consumes `BayesTable.applyFeedback`, `undoFeedback`, and `BayesFeedback.rebuildModel`.

- [ ] **Step 1: Add failing Compose tests** for both labels, no-content state, resulting Bayes sample, undo restoring/removing sample, and visible error when model rebuild fails.
- [ ] **Step 2: Run focused Compose tests** with `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.history.HistoryFlowsTest`.
- [ ] **Step 3: Add `Mark as spam`/`Mark as legitimate` actions** only to SMS records with nonempty `extraInfo`. After success rebuild the model and show a snackbar with undo; use existing history `SnackBar` patterns. Explain in the no-content state that future corrections require SMS-content logging to have been enabled at receipt time.
- [ ] **Step 4: Run focused UI/database tests** and verify no runtime permission or network request occurs.
- [ ] **Step 5: Run** `./gradlew :app:testFdroidQaUnitTest :app:connectedFdroidQaAndroidTest`; confirm all tests pass.
- [ ] **Step 6: Commit** as `feat: train local spam filter from history`.

## Review Focus Tests

All five failure modes are covered by the database, model, and Compose tests above. Never log message text or include it in error messages.
