# Prévia de Importação CSV Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show a bounded preview and exact import summary before creating one combined rule or multiple regex rules from CSV.

**Architecture:** Parse the selected file to a summary, show up to five sample rows and counts, then confirm or cancel. Keep only the selected SAF URI and bounded summary in UI state; re-read and revalidate on confirmation. Reuse existing CSV parser and the two existing import modes; import valid rows in one SQLite transaction and report skipped invalid rows.

**Tech Stack:** Kotlin, Jetpack Compose, Android Storage Access Framework, SQLite, JUnit 5, AndroidX Compose UI Test.

**Spec:** `docs/superpowers/specs/2026-10-30-csv-import-preview-design.md`

## Global Constraints

- Preserve CSV header/field parsing and the existing combined-rule and multi-rule modes.
- Preview at most five examples; never put the entire CSV into Compose state.
- Skip empty or invalid regex rows and show the skipped count; abort when there are no importable rows.
- Cancel leaves database unchanged; confirmed batch import must be all-or-nothing.
- Respect SQLite bind-variable limits on Android 10–13 as well as Android 14+ when using the existing multi-row batch insert.
- Continue using the selected SAF `Uri`; do not add broad storage permissions.

## Review Focus

- Missing `pattern` header: show actionable error and insert nothing.
- Blank pattern or malformed regex: count as invalid and skip; importing only invalid rows inserts nothing.
- Quoted separator, UTF-8 BOM, CRLF, and final row without newline: preserve existing parser behavior.
- Large input / low SQLite bind-variable limit: UI holds only five preview examples plus summary, and insert statements stay under the platform limit.
- Batch write fails mid-import: rollback all inserted rows and report failure, not partial success.

## File and Test Map

- Modify: `app/src/main/java/spam/blocker/ui/setting/regex/ImportCsv.kt`
- Modify: `app/src/main/java/spam/blocker/db/RegexTable.kt` to implement the existing `BasicTable.addAll(...)` bulk-insert hooks.
- Modify: `app/src/main/java/spam/blocker/db/BasicTable.kt` to size each multi-row statement under the platform bind-variable limit.
- Add: `app/src/main/java/spam/blocker/ui/setting/regex/CsvImportPreview.kt` for preview model and dialog.
- Modify: `app/src/main/java/spam/blocker/ui/setting/regex/RegexHeader.kt` to host dialog state/confirm callback.
- Modify: `app/src/main/res/values/strings_ui_ux.xml` and matching locale resources.
- Extend: `app/src/test/java/spam/blocker/ui/setting/regex/CsvRuleImportTest.kt` and add `CsvImportPreviewTest.kt`.
- Add: `app/src/androidTest/java/spam/blocker/db/CsvRuleImportTransactionTest.kt`.
- Add: `app/src/test/java/spam/blocker/db/BasicTableBatchSizeTest.kt`.
- Extend: `app/src/androidTest/java/spam/blocker/ui/setting/RegexEditorFlowsTest.kt` or add `CsvImportPreviewFlowsTest.kt`.

## Interfaces

- `data class CsvImportPreview(val filename: String?, val totalRows: Int, val validCount: Int, val invalidRows: Int, val allowCount: Int, val blockCount: Int, val samples: List<RegexRule>)`; `samples` is capped at five and this UI model never contains all rows.
- `fun Csv.toImportPreview(filename: String?, forType: Int): CsvImportPreview?` returns null only for missing required header or unreadable structure; rows failing shared mode-aware regex validation increment `invalidRows`.
- `fun Csv.toValidRegexRules(forType: Int): List<RegexRule>` returns the full set only in the confirmation callback, not Compose state.
- `RegexTable.addAll(ctx, rules)` uses existing `BasicTable.addAll(...)` transaction after `RegexTable` implements `insertColumns()` and `bindInsertStatement(...)`.
- The regex batch hooks cover exactly the persisted non-ID fields from `toContentValues`: pattern, extra pattern, both flag/mode pairs, description, priority, blacklist flag, rule flags, notification channel, schedule, block type/config, and SIM slot.
- `fun importCsvItems(...)` launches preview first; confirmation chooses the original import mode and then invokes the bulk insert.

---

### Task 1: Share rule validation and build preview summary

**Files:**
- Add: `app/src/main/java/spam/blocker/ui/setting/regex/CsvImportPreview.kt`
- Modify if necessary: `app/src/main/java/spam/blocker/db/RegexTable.kt` or a focused `RegexValidation.kt`
- Test: `app/src/test/java/spam/blocker/ui/setting/regex/CsvImportPreviewTest.kt`

- [ ] **Step 1: Add failing pure tests** for missing header, empty input, blank pattern, malformed regex, allow/block counts, and preview capped at five examples.
- [ ] **Step 2: Run** `./gradlew :app:testFdroidQaUnitTest --tests spam.blocker.ui.setting.regex.CsvImportPreviewTest`.
- [ ] **Step 3: Extract the exact regex validation used by the edit flow** into a reusable pure validator if it is currently UI-only; do not change accepted rule semantics. Implement `Csv.toImportPreview(filename, forType)` to validate rows and retain counts plus at most five examples. Implement `toValidRegexRules(forType)` for confirmed import only.
- [ ] **Step 4: Run focused tests** and verify malformed rows are excluded while parser-compatible CSV fields stay unchanged.
- [ ] **Step 5: Commit** as `feat: summarize and validate csv rule imports`.

### Task 2: Add atomic rule batch insertion

**Files:**
- Modify: `app/src/main/java/spam/blocker/db/RegexTable.kt`
- Modify: `app/src/main/java/spam/blocker/db/BasicTable.kt`
- Test: `app/src/test/java/spam/blocker/db/BasicTableBatchSizeTest.kt`
- Test: `app/src/androidTest/java/spam/blocker/db/CsvRuleImportTransactionTest.kt`

**Interfaces:** Enables inherited `RegexTable.addAll(ctx, rules)` by implementing the batch hooks already consumed by `BasicTable.addAll(...)`.

- [ ] **Step 1: Add failing tests** for computed batch sizes at API 29/33 and API 34+, plus successful multi-row insertion and rollback when a QA-only SQLite trigger aborts the second row.
- [ ] **Step 2: Run** `./gradlew :app:testFdroidQaUnitTest --tests spam.blocker.db.BasicTableBatchSizeTest` and `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.db.CsvRuleImportTransactionTest`.
- [ ] **Step 3: Implement `insertColumns()` and `bindInsertStatement(...)` in `RegexTable`** using existing column constants and field serialization. Adjust `BasicTable.addAll(...)` to use `min(existingApiCap, floor(bindVariableLimit / columnCount))`, with 999 variables before Android 14 and 32766 on Android 14+; keep every chunk in the same transaction.
- [ ] **Step 4: Run focused unit and DB tests** and verify the calculated statements stay under the limit and rollback leaves initial fixture rows untouched.
- [ ] **Step 5: Commit** as `feat: import regex rules transactionally`.

### Task 3: Present preview and confirm the two import modes

**Files:**
- Modify: `app/src/main/java/spam/blocker/ui/setting/regex/ImportCsv.kt`
- Modify: `app/src/main/java/spam/blocker/ui/setting/regex/RegexHeader.kt`
- Add/modify: `app/src/main/java/spam/blocker/ui/setting/regex/CsvImportPreview.kt`
- Modify: `app/src/main/res/values/strings_ui_ux.xml` and matching locale resources.
- Test: `app/src/androidTest/java/spam/blocker/ui/setting/regex/CsvImportPreviewFlowsTest.kt`

- [ ] **Step 1: Add failing UI tests** for preview counts/examples, invalid-only input, missing header, cancel/no write, confirm combined mode, confirm multi mode, and batch failure message.
- [ ] **Step 2: Run** `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.regex.CsvImportPreviewFlowsTest`.
- [ ] **Step 3: Replace immediate import with preview confirmation.** The preview states total rows, valid rows, invalid rows, allow/block totals, and at most five pattern/description examples. Combined mode previews the number of consolidated rules; multi mode previews valid rule count. UI state stores only the URI/mode/summary. On confirmation re-read and revalidate; if counts changed, show the updated preview and require confirmation again. Otherwise bulk insert transactionally and reload the existing `RegexViewModel`.
- [ ] **Step 4: Run focused UI/DB tests** and confirm cancel or any validation/write error inserts no rows.
- [ ] **Step 5: Run** `./gradlew :app:testFdroidQaUnitTest :app:connectedFdroidQaAndroidTest`; confirm all tests pass.
- [ ] **Step 6: Commit** as `feat: preview csv rule imports before saving`.

## Review Focus Tests

The five cases in Review Focus are covered by `CsvImportPreviewTest`, `CsvRuleImportTransactionTest`, and `CsvImportPreviewFlowsTest`. Never report an import as successful until the transaction commits.
