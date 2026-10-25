# Settings Runtime Failures Implementation Plan
> **For agentic workers:** Use this plan task by task in the active `spamblocker-complete-redesign` worktree. Keep the QA app isolated from the production package.

**Goal:** Resolve the three remaining Galaxy A53 failures in `SettingsFlowsTest` and pass the focused tests plus the full QA instrumentation suite.

**Architecture:** Diagnose each flow at its failing UI boundary before changing production code. Make a product change only when a focused test proves the callback or state transition is broken; otherwise correct the test’s navigation, dialog targeting, or cleanup. Preserve existing screen structure and QA test conventions.

**Tech Stack:** Kotlin, Jetpack Compose UI tests, Android instrumentation, Gradle, Galaxy A53 (API 36).

**References:** [Complete redesign plan](2026-10-18-spamblocker-complete-redesign.md), [QA test run report](../../qa/test-run-report.md).

## Global Constraints

- Run device tests only against the `fdroidQa` application (`spam.blocker.qa`); do not clear or uninstall production package `spam.blocker`.
- Use the connected Galaxy A53 (`RQCW2031FZF`); do not start or restart an emulator.
- Preserve `local.properties`; do not stage or commit changes.
- Keep changes scoped to these three settings flows and their existing test/report files.
- Do not use tests that send SMS, invoke real notification listeners, or contact external services.

## Review Focus

- SMS switch event: the Compose node and its `checked` value must correspond to the intended SMS control.
- SMS preference persistence: turning a switch off must leave the corresponding QA preference false after Activity recreation.
- Theme selector availability: Custom does not render the appearance combo; tests must not try to select a hidden control.
- Theme restoration: restore family first and appearance second, then verify the visible selection and persisted values.
- Push Alert dialog visibility: after tapping New, distinguish failure to open the editor from a selector that exists only outside the visible dialog area.

---

### Task 1: Fix the SMS Bomb switch flow

**Files:**

- Modify: `app/src/androidTest/java/spam/blocker/ui/setting/SettingsFlowsTest.kt`
- Modify: `app/src/main/java/spam/blocker/ui/widgets/Fab.kt`
- Modify only if the focused test proves a product defect: `app/src/main/java/spam/blocker/ui/setting/regex/SmsBomb.kt`
- Modify only if needed for an existing shared preference defect: `app/src/main/java/spam/blocker/util/SharedPref.kt`

**Interfaces:**

- Existing UI selector: `sms_alert_switch` / `sms_bomb_switch`.
- Existing stored values: `spf.SmsAlert(context).isEnabled` and `spf.SmsBomb(context).isEnabled`.
- Existing component callback in `SmsBomb()` writes `spf.isEnabled` and local `isEnabled` when switched off.

- [x] **Step 1: Isolate the SMS Bomb regression.** Keep the test on the Protection tab, close the SMS Bomb editor explicitly, assert the `sms_bomb_switch` is on, tap that exact node, and wait for both its Compose state and QA preference to become off. Include the current tag, checked state, and preference value in any failure message.
- [x] **Step 2: Run the isolated instrumentation test.** Run `./gradlew --max-workers=1 :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.SettingsFlowsTest#smsAlertAndSmsBombSwitchesPersistInQaPreferences`. Confirm the failure is still at the switch transition before changing production code.
- [x] **Step 3: Fix the proven cause.** The current test click opens the Rule Test FAB dialog because the FAB overlays the SMS Bomb switch. Change `FabWrapper` in `Fab.kt` to reserve a bottom action area for its FAB instead of stacking it over scrollable content; keep the FAB at the bottom end and preserve its existing visibility and click behavior. Do not change the SMS preference callback unless the focused test still proves it is broken after the overlap is removed.
- [x] **Step 4: Verify persistence.** Recreate the Activity after both SMS switches are off; assert both switch nodes remain off and both preference values remain false. Restore all saved QA preferences and permissions in `finally`.
- [x] **Step 5: Run the focused SMS test again.** Expected result: one test passes with no external SMS sent.

### Task 2: Correct theme appearance selection and cleanup

**Files:**

- Modify: `app/src/androidTest/java/spam/blocker/ui/setting/SettingsFlowsTest.kt`
- Modify only if a focused assertion proves the screen stores or applies the wrong selection: `app/src/main/java/spam/blocker/ui/setting/misc/Theme.kt`
- Reference behavior: `app/src/main/java/spam/blocker/ui/theme/ThemeSelection.kt`

**Interfaces:**

- Theme family and appearance are represented by `ThemeChoice`, `ThemeFamily`, and `ThemeAppearance`.
- The `theme_appearance` combo exists only for `TRUECALLER` and `CATPPUCCIN`; Custom preserves the selected appearance and hides that combo.
- Selection is persisted through `spf.Theme.themeFamily` and `themeAppearance`.

- [x] **Step 1: Re-run the focused theme test on the current diff.** Use `./gradlew --max-workers=1 :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.SettingsFlowsTest#allThemeFamiliesAndAppearanceModesApplyAndPersistTheirPalette`. Record the first failing loop iteration and whether the mismatch is in the combo, preference, or cleanup.
- [x] **Step 2: Pin selector behavior in the test.** For each preset family, assert the chosen family, each System/Light/Dark appearance, the stored enum name, and the expected palette. For Custom, verify its preserved appearance and palette without querying the hidden appearance combo.
- [x] **Step 3: Fix the smallest cause.** Keep selector helpers synchronized with actual visible state. Restore the original family before selecting the original appearance. If the UI path selects correctly but cleanup needs exact raw preference restoration, restore raw values after state assertions and avoid treating a preference-only reset as an applied UI transition.
- [x] **Step 4: Verify recreation and cleanup.** Recreate in a selected preset state and assert the chosen family, appearance, and palette survive. Then confirm original QA preferences are restored even when an assertion fails.
- [x] **Step 5: Run the focused theme test again.** Expected result: all family/appearance combinations pass and no QA theme state leaks into later tests.

### Task 3: Repair the Push Alert editor and app chooser flow

**Files:**

- Modify: `app/src/androidTest/java/spam/blocker/ui/setting/SettingsFlowsTest.kt`
- Modify only if the editor fails to render after the New button callback: `app/src/main/java/spam/blocker/ui/setting/regex/PushAlert.kt`
- Inspect existing dialog behavior: `app/src/main/java/spam/blocker/ui/widgets/PopupDialog.kt`

**Interfaces:**

- Add button tag: `push_alert_add`.
- Editor selectors: `push_alert_body` and `push_alert_choose_app`.
- QA app chooser selector: `choose_app_spam.blocker.qa`.
- Data persistence: `PushAlertTable` and `PushAlertViewModel`.

- [x] **Step 1: Assert the editor boundary.** Establish the Protection tab and expanded Screening/Push Alert section. Tap the exact `push_alert_add` node, wait for `push_alert_body`, then wait for `push_alert_choose_app`; report separately which node fails.
- [x] **Step 2: Run the isolated Push Alert test.** Run `./gradlew --max-workers=1 :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.SettingsFlowsTest#pushAlertCanBeCreatedEditedAndDeletedWithQaAppSelection`. If the editor is absent, inspect the Compose semantics tree immediately after New. If the editor exists but the chooser button is absent, inspect dialog scrolling and semantics before changing production code.
- [x] **Step 3: Apply the minimal correction.** Correct test navigation/state setup if the button is not actually on the displayed Protection screen. If New is tapped but the editor is not rendered, fix the `PushAlertHeader` trigger path. If the editor renders but the app button is outside the visible dialog viewport, scroll the dialog to that control before tapping. Preserve QA-only app selection and avoid opening system settings.
- [x] **Step 4: Verify create, edit, and delete.** Select `spam.blocker.qa`, save `qa-push`, edit it to `qa-push-edited`, then swipe-delete it. Assert each database transition and restore original table records and permission flags in `finally`.
- [x] **Step 5: Run the focused Push Alert test again.** Expected result: one test passes without leaving a system permission screen open.

### Task 4: Run regression verification and update the report

**Files:**

- Modify: `docs/qa/test-run-report.md`

- [x] **Step 1: Run all `SettingsFlowsTest` cases on the connected A53.** Expected result: all 17 tests pass.
- [x] **Step 2: Run the full `fdroidQa` instrumentation suite.** Run `./gradlew --max-workers=1 :app:connectedFdroidQaAndroidTest`. Expected result: all 120 tests pass with zero failures, errors, or skips.
- [x] **Step 3: Run build and local regression checks.** Run `./gradlew --max-workers=1 :app:testFdroidQaUnitTest :app:assembleFdroidDebug :app:assembleGoogleplayDebug` and `git diff --check`. Expected result: all Gradle tasks succeed and the diff check is clean.
- [x] **Step 4: Record exact evidence.** Update the QA report with device model/serial, commands, counts, and any hardware-only limitation. Do not mark the three flows complete unless the device tests pass.
