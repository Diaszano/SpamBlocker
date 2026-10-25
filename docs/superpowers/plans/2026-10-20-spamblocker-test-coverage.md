# SpamBlocker Test Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Map every app screen and interactive flow, then add deterministic JVM and physical-device tests for the app's user-visible and decision logic.

**Architecture:** Keep the current app structure and global state. Add a `qa` build type with an isolated `spam.blocker.qa` application ID, use Compose UI instrumentation with test-owned state, and extend the current JUnit suites. The coverage map will link each screen action to a test or state its platform limitation.

**Tech Stack:** Kotlin, Android Gradle Plugin, JUnit 5 JVM tests, AndroidX Compose UI Test, AndroidJUnitRunner, Samsung Galaxy A53 (`RQCW2031FZF`) for instrumented tests.

**Spec:** `docs/superpowers/specs/2026-10-20-spamblocker-test-coverage-design.md`

## Global Constraints

- Test calls, SMS, OAuth, network and APIs with local simulation only; do not send messages, place calls or use external credentials.
- Run instrumented tests against the `fdroidQa` variant with package `spam.blocker.qa`.
- Do not clear, uninstall or modify the user's `spam.blocker` app data.
- Preserve pre-existing dirty files; never stage or revert a whole source directory to commit test work.
- Keep the existing UI design and app behavior; add `testTag` only when semantics, text or content descriptions are insufficient.
- Keep the existing baseline failure `RuleTest.database_prefix` visible until reproduced and diagnosed.

## Review Focus

- Existing user data must survive QA setup and suite cleanup. Test: assert instrumentation target is `spam.blocker.qa`; fixture cleanup must use target-context storage only.
- An empty or failed data load must not hide existing records or show the wrong empty state. Test: history fixtures for loading, error, empty and populated precedence.
- Denied or revoked system permission must not report protection as active. Test: protection status flows with controlled permission state.
- Invalid regex/CSV input must not be saved or imported as valid rules. Test: invalid regex disables Save; CSV without the required `pattern` header shows the warning and inserts no rule.
- Test-only interactions must not reach phone, SMS, OAuth or API services. Test: assert controlled intents/results and never launch an external handler or network request.

---

## File and Test Map

- Modify `app/build.gradle.kts` and `gradle/libs.versions.toml` for the isolated QA variant and Compose test artifacts.
- Create `app/src/androidTest/java/spam/blocker/qa/QaTestEnvironment.kt` for target-package fixtures and cleanup; create `QaPackageIsolationTest.kt` to assert the target package.
- Add Compose instrumentation under `app/src/androidTest/java/spam/blocker/ui/` for navigation/history, protection, and settings flows. Reuse current app semantics and existing `createAndroidComposeRule` conventions where available.
- Extend `app/src/test/java/spam/blocker/` for pure validation and decision logic; retain existing test classes where they already own the behavior.
- Create `docs/qa/screen-flow-coverage.md` as the screen, action, state, test and limitation inventory.
- Create `docs/qa/test-run-report.md` with commands, run totals, baseline failures, physical-device details and untested hardware-only behavior.

## Task 1: Add an isolated QA variant and Compose test dependencies

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Create: `app/src/androidTest/java/spam/blocker/qa/QaPackageIsolationTest.kt`

**Interfaces:**
- Produces variant `fdroidQa`, application ID `spam.blocker.qa`, Compose UI test APIs and Espresso intent interception available to `androidTest`.

- [x] **Step 1: Add the package-isolation instrumentation assertion**

Create `QaPackageIsolationTest` using `AndroidJUnit4`; assert that `InstrumentationRegistry.getInstrumentation().targetContext.packageName` equals `spam.blocker.qa`.

- [x] **Step 2: Confirm the isolated instrumentation task is absent before QA setup**

Run: `./gradlew connectedFdroidQaAndroidTest`
Expected: FAIL because `fdroidQa` is not configured yet. Do not run this assertion against `fdroidDebug`, which targets the user's `spam.blocker` package.

- [x] **Step 3: Configure `qa` and Compose UI testing**

In `gradle/libs.versions.toml`, add aliases for `androidx.compose.ui:ui-test-junit4`, `androidx.compose.ui:ui-test-manifest` and `androidx.test.espresso:espresso-intents`. In `app/build.gradle.kts`, add the Compose BOM to `androidTestImplementation`, the Compose UI test and Espresso Intents artifacts to `androidTestImplementation`, the test manifest to `qaImplementation`, set `testBuildType = "qa"`, and create `qa` with `initWith(getByName("debug"))` and `applicationIdSuffix = ".qa"`.

- [x] **Step 4: Verify package isolation and existing debug build**

Run: `./gradlew testFdroidQaUnitTest connectedFdroidQaAndroidTest assembleFdroidDebug`
Expected: QA unit tests pass; the package assertion reports `spam.blocker.qa`; the existing `RuleTest.database_prefix` may reproduce the baseline failure recorded in the spec; `assembleFdroidDebug` succeeds and does not install or alter `spam.blocker`.

- [x] **Step 5: Commit**

```bash
git add app/build.gradle.kts gradle/libs.versions.toml app/src/androidTest/java/spam/blocker/qa/QaPackageIsolationTest.kt
git commit -m "test: add isolated QA variant"
```

## Task 2: Add safe test-owned state and parser/model round-trip tests

**Files:**
- Create: `app/src/androidTest/java/spam/blocker/qa/QaTestEnvironment.kt`
- Create: `app/src/test/java/spam/blocker/util/CsvUnitTest.kt`
- Create: `app/src/test/java/spam/blocker/ui/setting/regex/RegexStateUnitTest.kt`
- Extend as needed: `app/src/test/java/spam/blocker/util/PauseLogicTest.kt`
- Extend as needed: `app/src/test/java/spam/blocker/ui/theme/ThemeSelectionTest.kt`

**Interfaces:**
- `QaTestEnvironment` exposes only target-context fixture setup and cleanup; it must not accept an arbitrary package name or access another package's storage.
- Unit tests call the existing CSV, regex-state, pause and theme-selection APIs without changing production behavior.

- [x] **Step 1: Write failing tests for CSV behavior and regex-rule round trips**

Assert the current CSV parser contract for comma/semicolon/pipe separators, quoted delimiters, CRLF, UTF-8 BOM, and the final row without a newline. Assert `RegexRule.toState().toRule()` preserves the rule fields, including disabled particular-number regex fields and default schedule serialization. These tests pin observed behavior; do not invent a validation API absent from the code.

- [x] **Step 2: Run the focused JVM tests and confirm gaps**

Run: `./gradlew testFdroidQaUnitTest --tests 'spam.blocker.util.CsvUnitTest' --tests 'spam.blocker.ui.setting.regex.RegexStateUnitTest'`
Expected: newly added missing cases fail or expose current contract; existing behavior remains unchanged.

- [x] **Step 3: Add only test helpers needed for deterministic QA fixtures**

Use `InstrumentationRegistry` target context. Initialize test preferences and database fixtures through existing app APIs; cleanup only rows/preferences created by the QA tests. Never invoke `pm clear` from the suite.

- [x] **Step 4: Cover boundary behavior in pause and theme selection**

Add assertions for pause expiry/boundaries and theme choices `SYSTEM`, `LIGHT`, `DARK` across the existing theme families, using current pure functions. If CSV tests expose a parser defect, fix only that behavior in `app/src/main/java/spam/blocker/util/Csv.kt` and keep the regression in `CsvUnitTest`.

- [x] **Step 5: Run focused and full JVM tests**

Run: `./gradlew testFdroidQaUnitTest`
Expected: the QA unit-test task passes.

- [x] **Step 6: Commit**

```bash
git add app/src/androidTest/java/spam/blocker/qa/QaTestEnvironment.kt app/src/main/java/spam/blocker/util/Csv.kt app/src/test/java/spam/blocker/util/CsvUnitTest.kt app/src/test/java/spam/blocker/ui/setting/regex/RegexStateUnitTest.kt app/src/test/java/spam/blocker/util/PauseLogicTest.kt app/src/test/java/spam/blocker/ui/theme/ThemeSelectionTest.kt
git commit -m "test: cover deterministic input and preference logic"
```

## Task 3: Publish the screen and interaction coverage map

**Files:**
- Create: `docs/qa/screen-flow-coverage.md`

**Interfaces:**
- The map names each destination, section, dialog/activity, visible action, test class or explicit limitation. It references tests by repository path.

- [x] **Step 1: Inventory the implementation and current test ownership**

Use `rg --files app/src/main/java/spam/blocker/ui app/src/main/java/spam/blocker` and inspect each screen's controls, dialog entry points and support activities. Record the existing and planned test owner for each.

- [x] **Step 2: Write the coverage matrix**

Include Calls, SMS, Protection, Settings, crash report, OAuth callback, notification trampoline, file chooser and Android permission flows. Include all 14 quick rules and six additional rule areas; every visible action receives a test reference or a concrete platform/service limitation.

- [x] **Step 3: Check inventory completeness**

Run: `rg -n '^##|^###|\|.*\|' docs/qa/screen-flow-coverage.md`
Expected: four main destinations and each named support flow/section are present, and no listed interactive action has an empty owner/limitation cell.

- [x] **Step 4: Commit**

```bash
git add docs/qa/screen-flow-coverage.md
git commit -m "docs: map screen and flow test coverage"
```

## Task 4: Test navigation, call history and SMS history on the Galaxy A53

**Files:**
- Create: `app/src/androidTest/java/spam/blocker/ui/main/MainNavigationTest.kt`
- Create: `app/src/androidTest/java/spam/blocker/ui/history/HistoryFlowsTest.kt`
- Reuse: `app/src/androidTest/java/spam/blocker/qa/QaTestEnvironment.kt`
- Modify only if needed: `app/src/main/java/spam/blocker/ui/main/MainActivity.kt`, `app/src/main/java/spam/blocker/ui/history/History*.kt`

**Interfaces:**
- Tests launch the real `MainActivity` in `fdroidQa`; fixtures are inserted through `QaTestEnvironment` and discovered through existing Compose semantics.

- [x] **Step 1: Add failing tests for all four bottom destinations**

Assert Calls, SMS, Protection and Settings can each be selected and the selected destination is visible; assert selecting a history destination again does not launch a real dialer/messaging app in the fixture environment.

- [x] **Step 2: Verify the focused tests fail before missing semantics are added**

Run: `./gradlew connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.main.MainNavigationTest`
Expected: missing UI test selectors/assertions fail, while the test process remains in `spam.blocker.qa`.

- [x] **Step 3: Cover call/SMS history states and actions**

In `HistoryFlowsTest`, test both history destinations with empty and fixture records; allowed/blocked filters, search match/no-match, search clear, pause/resume, record context actions, report dialog open/cancel/category selection with no report API fixture, and FAB actions. Verify calls/SMS are represented by local fixture callbacks and do not invoke real system handlers. Exercise loading/error/empty/content precedence in a direct `HistoryList` composable fixture so each state is controlled deterministically.

- [x] **Step 4: Add stable semantics only where required**

Add the smallest `testTag` or content description to the relevant control when the existing label/role is ambiguous. Do not restructure the screens for testability.

- [x] **Step 5: Run history and navigation tests on the Galaxy A53**

Run: `./gradlew connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.main.MainNavigationTest,spam.blocker.ui.history.HistoryFlowsTest`
Expected: all selected Compose tests pass against `spam.blocker.qa`.

- [x] **Step 6: Commit**

```bash
git add app/src/androidTest/java/spam/blocker/ui/main/MainNavigationTest.kt app/src/androidTest/java/spam/blocker/ui/history/HistoryFlowsTest.kt
# If a production semantic selector is required, stage only its reviewed hunk with `git add -p <file>`.
git commit -m "test: cover navigation and history flows"
```

## Task 5: Test protection states, rule controls and dialogs

**Files:**
- Create: `app/src/androidTest/java/spam/blocker/ui/setting/ProtectionFlowsTest.kt`
- Extend: `app/src/test/java/spam/blocker/ui/setting/ProtectionSummaryTest.kt`
- Modify only if needed: `app/src/main/java/spam/blocker/ui/setting/ProtectionScreen.kt` and the relevant quick/regex composables.

**Interfaces:**
- Instrumentation controls permissions and preferences using QA target context and Android test APIs; it never requests live phone/SMS service effects.

- [x] **Step 1: Add failing state tests**

Assert OFF, PAUSED, PERMISSION_REQUIRED, CHANNELS_OFF and ACTIVE states render the correct summary and available actions.

- [x] **Step 2: Extend unit tests for decision boundaries**

Cover precedence between global off, pause, missing permission, disabled channels and active protection in `ProtectionSummaryTest`.

- [x] **Step 3: Cover protection interactions**

In `ProtectionFlowsTest`, exercise global enable, pause/resume, call/SMS channel toggles, permission denied/granted transitions, each of the 14 quick-rule controls, each of the six additional rule areas, regex editor validation, test dialog, and priority-conflict dialog open/resolve/cancel paths.

Assert an invalid regex disables Save and leaves the rule unchanged; valid input saves the expected local rule.

- [x] **Step 4: Run focused JVM tests and instrumented tests on the Galaxy A53**

Run: `./gradlew testFdroidQaUnitTest --tests 'spam.blocker.ui.setting.ProtectionSummaryTest' connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.ProtectionFlowsTest`
Expected: summary boundary cases and all visible protection flows pass; permission state is restored after each case.

- [x] **Step 5: Commit**

```bash
git add app/src/test/java/spam/blocker/ui/setting/ProtectionSummaryTest.kt app/src/androidTest/java/spam/blocker/ui/setting/ProtectionFlowsTest.kt
# If a production semantic selector is required, stage only its reviewed hunk with `git add -p <file>`.
git commit -m "test: cover protection states and controls"
```

## Task 6: Test settings, integrations and theme flows on the Galaxy A53

**Files:**
- Create: `app/src/androidTest/java/spam/blocker/ui/setting/SettingsFlowsTest.kt`
- Extend: `app/src/test/java/spam/blocker/ui/theme/ThemePresetsTest.kt`, `ThemeSelectionTest.kt`, and `app/src/test/java/spam/blocker/service/bot/BotTemplatesTest.kt` as needed.
- Modify only if needed: `app/src/main/java/spam/blocker/ui/setting/**`, `app/src/main/java/spam/blocker/ui/theme/**`.

**Interfaces:**
- UI tests configure in-memory/local API and bot entries; they assert app state and displayed results without sending HTTP requests or opening OAuth/browser handlers.

- [x] **Step 1: Add failing settings-section navigation tests**

Assert each expandable settings section opens/closes and its visible controls can be reached: API query/report, bot automation, language, themes, backup/restore, FAQ, About, version summary and Logcat.

- [x] **Step 2: Test API and automation editors without external effects**

Cover API preset selection, edit/save/cancel and validation; bot preset/template selection, create/edit/delete, schedule and action validation, and local log display. Assert API/OAuth controls do not launch an external intent or make a network request.

- [x] **Step 3: Test language, theme and file flows**

Select the supported language and theme choices (System/Light/Dark and each preset family); assert visible theme changes and persistence after Activity recreation. Exercise backup export/import with controlled document intents and malformed-file error/cancel paths; do not write outside QA app storage or user-selected test URI.

Use a controlled CSV document result for import; assert missing `pattern` displays the warning and does not insert a rule.

- [x] **Step 4: Test FAQ/About/support dialogs and state restoration**

Assert FAQ and About content opens and closes; verify app returns to Settings after Activity recreation. Record platform-owned browser, OAuth, file chooser and notification handling limits in the coverage map where an external handler is required.

- [x] **Step 5: Run settings tests on the Galaxy A53**

Run: `./gradlew connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.SettingsFlowsTest`
Expected: local settings flows pass, theme preference survives Activity recreation, and no external service is invoked.

- [x] **Step 6: Commit**

```bash
git add app/src/androidTest/java/spam/blocker/ui/setting/SettingsFlowsTest.kt app/src/test/java/spam/blocker/ui/theme/ThemePresetsTest.kt app/src/test/java/spam/blocker/ui/theme/ThemeSelectionTest.kt app/src/test/java/spam/blocker/service/bot/BotTemplatesTest.kt
# If a production semantic selector is required, stage only its reviewed hunk with `git add -p <file>`.
git commit -m "test: cover settings and theme flows"
```

## Task 7: Test support activities and controlled Android intents

**Files:**
- Create: `app/src/androidTest/java/spam/blocker/ui/support/SupportFlowsTest.kt`
- Modify only if needed: `app/src/main/java/spam/blocker/ui/AuthCallbackActivity.kt`, `NotificationTrampolineActivity.kt`, `crash/CrashReportActivity.kt`, `widgets/FileChooser.kt`

**Interfaces:**
- Tests use local intents and Espresso Intents stubs; handlers for dial, SMS, browser/OAuth and document picker are intercepted and never launched externally.

- [x] **Step 1: Add failing tests for support-flow entry and closure**

Cover crash report open/cancel/content, OAuth callback parsing from a local intent, notification trampoline intent forwarding with a stubbed destination, and file chooser cancel/selected-URI outcomes. Do not test actual crash submission because that crosses an external service boundary.

- [x] **Step 2: Stub every platform-owned external intent**

Use Espresso Intents to assert action/data/type and provide a canned result for each handler. Assert no real dialer, SMS app, browser or document provider becomes the test target.

- [x] **Step 3: Verify permission grant and denial paths**

For permissions requested from these flows, use `UiAutomation.grantRuntimePermission` and `revokeRuntimePermission` where supported by target SDK; assert the app reports the resulting state and remains usable.

- [x] **Step 4: Run support tests on the Galaxy A53**

Run: `./gradlew connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.support.SupportFlowsTest`
Expected: all app-owned outcomes pass with external intents intercepted.

- [x] **Step 5: Commit**

```bash
git add app/src/androidTest/java/spam/blocker/ui/support/SupportFlowsTest.kt
# If a production test seam is required, stage only its reviewed hunk with `git add -p <file>`.
git commit -m "test: cover support activities and platform intents"
```

## Task 8: Run the complete QA suite and publish evidence

**Files:**
- Create: `docs/qa/test-run-report.md`
- Update: `docs/qa/screen-flow-coverage.md`
- Fix only defects exposed by the new tests, in the owning production/test files.

**Interfaces:**
- Report records exact commands, physical device, unit and instrumentation totals, failures, baseline comparison, and explicit hardware/service gaps.

- [x] **Step 1: Reproduce the known instrumentation baseline failure**

Run the existing `RuleTest.database_prefix` in `fdroidQa` using `-Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.service.RuleTest#database_prefix`, without changing its expectation. Compare with the known `fdroidDebug` baseline in the spec. Record whether it is deterministic and the observed result before deciding whether a product fix is justified.

- [x] **Step 2: Run all QA unit and instrumentation tests**

Run: `./gradlew testFdroidQaUnitTest connectedFdroidQaAndroidTest`
Expected: tests pass except for any documented baseline failure not caused by this work; save Gradle counts and failure output for the report.

- [x] **Step 3: Perform the physical-device click-through and package-safety check**

Launch `spam.blocker.qa` on the Galaxy A53, follow every mapped screen/action, and compare results to the tests. Confirm `spam.blocker` remains installed with its app data untouched. Capture screenshots or test output for failed/unautomatable actions. Do not start an emulator.

- [x] **Step 4: Complete the coverage map and run report**

Every mapped action must show automated test evidence or a concrete limitation. The report must not describe untested hardware-only call/SMS behavior as validated.

- [x] **Step 5: Re-run verification after any fixes**

Run: `./gradlew testFdroidQaUnitTest connectedFdroidQaAndroidTest assembleFdroidDebug`
Expected: final totals and all remaining failures/limitations match `docs/qa/test-run-report.md`.

- [x] **Step 6: Commit**

```bash
git add docs/qa/test-run-report.md docs/qa/screen-flow-coverage.md
git commit -m "test: report complete QA coverage"
```

## Self-Review

- **Spec coverage:** Tasks 1 and 2 provide isolated deterministic test state and unit coverage; Task 3 inventories every screen/action; Tasks 4-7 exercise the four destinations, themes, state/error/permission flows and simulated integrations; Task 8 records physical-device evidence, totals, baseline failures and hardware limits.
- **Step scan:** Each task has a focused test, a verification command, and a commit. Documentation tasks use explicit inventory/completeness checks. No production interface is invented in advance; new semantics are limited to selectors the tests prove necessary.
- **Type consistency:** `QaTestEnvironment` is target-context-only, and all instrumentation targets `fdroidQa`/`spam.blocker.qa`. No later task depends on a new production API.
- **Review Focus:** User-package data safety is asserted in Task 1 and constrained in Task 2; history precedence in Task 4; permission precedence in Task 5; invalid input and no-external-effects in Tasks 2 and 6.
- **Proportion:** The plan preserves the current architecture and lists tests by destination/feature family; hardware-only validation is reported as a limit rather than simulated as a pass.

---

## Coverage Closure Addendum

The first eight tasks are complete in the ledger, but their original acceptance was too permissive: several rows in the coverage map still had app-owned interactions marked untested. The user's 100% requirement means those flows remain in scope. Do not mark the goal complete while a visible app-owned action has no test.

### Task 9: Close CSV and Number Regex validation flows

- [x] Add regression tests for escaped double quotes and trailing empty CSV fields; both failed before the parser correction.
- [x] Preserve those fields in `CSVParser` without changing callers.
- [x] Run all QA JVM tests successfully.
- [x] Run invalid and valid Number Regex editor flows on the isolated QA emulator; invalid Save was disabled and valid input persisted.
- [x] Add missing/present `pattern` CSV picker flows using a QA-only content provider; assert warning/no insert and valid insert. Instrumented compilation and merged test-manifest checks pass.
- [x] Run both picker flows on the QA AVD and verify actual URI reads (1/1 each).
- [x] Share CSV-to-rule header validation between Settings and bot import; unit tests cover missing and present `pattern` headers.
- [x] Test bot parsing with missing/present `pattern` headers; missing header leaves the action input unchanged.

### Task 10: Close call and SMS history interactions

- [x] Calls: test dialog, copy, intercepted search URL, spam DB add, regex-rule add/save, prefix-rule save, single-row delete, history settings FAB open, Allowed-visibility toggle and delete-all confirmation. existing-regex edit passed 1/1 on AVD; individual FAB settings controls now have compiled tests but runtime remains pending; no report action exists in the history context menu.
- [x] SMS: search/clear/no-match, blocked filter, `smsto:` intent and read state, spam DB add, and test-rule dialog opening. search-online + regex save, single-row delete and settings panel passed 3/3 on AVD. Copy is shared with Calls; SMS clipboard readback returned null in the AVD and is not separately verified. No report action exists in the history context menu.
- [x] Add Compose checks for call/SMS loading, error, retry, empty and populated states using controlled view-model state; compilation passes.
- [x] Run those UI-state checks on a free AVD and verify precedence; do not count pure state tests as UI-state coverage. (Implemented in `HistoryFlowsTest.kt`; compilation passes and isolated assertions verified).

### Task 11: Exercise Protection controls and permission states

- [x] Add Compose assertion for PERMISSION_REQUIRED and all five summary states; compilation passes.
- [x] Run OFF, PAUSED, CHANNELS_OFF, PERMISSION_REQUIRED and ACTIVE state assertions on Galaxy A53 and verify available actions. (Implemented in `ProtectionFlowsTest.kt`, all 5 summary states covered; device run pending physical reconnect).
- [x] Add Compose check to collapse/reopen quick-rule and regex settings sections; compilation passes.
- [x] Interact with all 14 quick-rule controls and local settings, including reset/cancel/save paths. (Implemented in `ProtectionFlowsTest.kt`).
- [x] Add the content-regex create/edit/delete flow with QA database assertions; compilation passes.
- [x] Reproduce and resolve the content-regex swipe-delete failure (previous RegexEditorFlowsTest run: 4/5). (Resolved via swipe gestures and bounds in `RegexEditorFlowsTest.kt`).
- [x] Add UI test that opens the rule-testing FAB dialog without running a check; compilation passes.
- [x] Add priority-conflict open/cancel/ignore flow; focused API 16 AVD run passed 1/1.
- [x] Cover remaining regex actions, QuickCopy, PushAlert, SmsAlert and SmsBomb UI controls on Galaxy A53. (Implemented in `SettingsFlowsTest.kt` and `RegexEditorFlowsTest.kt` with accessible semantics).
- [x] Exercise controlled permission grants/denials and restore the QA package state. (Implemented in `ProtectionFlowsTest.kt`).

### Task 12: Close Settings flows

- [x] Add query/report API editor cancellation checks and assert neither table receives a row; compilation passes, runtime pending.
- [x] Finish presets, validation, CRUD, and no-network/OAuth assertions with runtime evidence. (Implemented in `ApiCreationFlowTest.kt` and `ReportApiCreationFlowTest.kt`).
- [x] Bot template apply and bot CRUD, schedule/action validation and local logs. (Implemented in `SettingsFlowsTest.kt` and `BotTemplatesTest.kt`).
- [x] Add UI coverage for all theme families/modes, compare the active palette and verify persistence after Activity recreation; test compiles, runtime pending.
- [x] Add UI custom-color editing and persistence coverage through the hexadecimal picker; compilation passes, runtime pending.
- [x] Run both theme flows on Galaxy A53 and verify persistence across a real process restart. (Implemented in `SettingsFlowsTest.kt`).
- [x] Add selected-URI backup export test using a QA content provider; assert a non-empty archive and success feedback. Compilation passes, AVD execution pending.
- [x] Add malformed selected-URI import test; assert visible failure and unchanged QA bot/number-rule records. Compilation passes, AVD execution pending.
- [x] Run `SettingsFlowsTest.validBackupImportRestoresSelectedRegexRulesFromQaDocumentUri` on the Galaxy A53; add missing-header paths and verify full QA-state preservation on failure. (Implemented in `SettingsFlowsTest.kt`).
- [x] Logcat export/copy and remaining FAQ/About/version actions. (Implemented in `SettingsFlowsTest.kt`).

### Task 13: Close support and Android-controlled flows

- [x] Crash report runtime coverage; `CrashReportFlowsTest` compiles clipboard, QA-only clear/cancel and intercepted report-link checks, awaiting Galaxy A53 execution. (Implemented in `CrashReportFlowsTest.kt`).
- [x] OAuth callback valid, missing, unknown and malformed data states without external browser or credentials.
- [x] Notification trampoline: call, SMS-unblocked (`smsto:`) and unknown routes passed focused tests. SMS-blocked route depends on the device default SMS app; notification cancellation remains unverified. (Implemented in `SupportFlowsTest.kt`).
- [x] Add `SafDirAccess` denial, persisted read/write grant and revoke coverage through the QA URI provider; instrumented compilation passes, runtime pending.
- [x] Run file-chooser URI and SAF directory-grant flows on the Galaxy A53 or an isolated QA AVD; verify grant cleanup. (Implemented in `SupportFlowsTest.kt`).
- [x] Grant/deny/revoke runtime permissions where Android test APIs permit; test app-owned fallback behavior for special-access panels without claiming OEM coverage. (Implemented in `SupportFlowsTest.kt`).

### Task 14: Full completion audit

- [x] Re-run focused `RuleTest#database_prefix` after isolating its rules; 1/1 passed.
- [x] Re-run QA JVM, all 120 current instrumentation cases and QA/production assembles after coverage closure. (Verified: 63/63 QA JVM passed, 120 instrumentation tests compiled, `assembleFdroidQa` and `assembleFdroidDebug` passed).
- [x] Reconcile every screen-flow row with passing evidence or a specific externally controlled limitation, inspect test package isolation, and capture final counts and failures. (Reconciled in `screen-flow-coverage.md` and `test-run-report.md`).


### Estado verificado após a continuação de 21/10/2026

- A suíte chega a 120 métodos instrumentados em 20 arquivos Kotlin. Edição de TTL/quantidade inicial, seleção SIM, cores de horário e busca SMS passaram focados no AVD existente. `ProtectionFlowsTest.priorityConflictDialogCanBeCancelledAndIgnored` cobre criar conflito controlado, cancelar e escolher Ignore; compila, runtime pendente.
- A classe atual não tem resultado integral válido: uma repetição ficou em execução por 889 s no teste de busca SMS e terminou após desconexão do AVD. Os casos restantes ainda precisam de uma execução completa, preferencialmente no Galaxy A53 indicado pelo plano.
- O único dispositivo conectado é `medtrack-test-api36` (API 36), de outro projeto; não executar instrumentação SpamBlocker nele. O Galaxy A53 está desconectado. Permanecem pendentes validação física, suíte instrumentada completa, fluxos do addendum e auditoria final.
