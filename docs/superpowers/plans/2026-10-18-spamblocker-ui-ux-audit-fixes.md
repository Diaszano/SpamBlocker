# SpamBlocker UI/UX Audit Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Correct all eight findings in the SpamBlocker UI/UX audit without changing its established Android/Compose structure.

**Architecture:** Fix shared controls once, preserve existing public composable and `reload(ctx)` call signatures, and reuse the existing database, snackbar, palette, and section components. Address visual identity with owner-supplied direction before changing styling. Keep Android UI checks on a device or emulator; add focused automated tests for state and contrast logic.

**Tech Stack:** Kotlin, Jetpack Compose, Android instrumentation, JUnit, Gradle.

**Spec:** `anti-slop/audit-001-2026-10-18.md` and `DESIGN.md`

## Global Constraints

- Cover audit findings 1 through 8; preserve call/SMS filtering behavior and saved user settings.
- Do not switch to Material design. The existing components remain the design system.
- For translated UI labels, prefer short words as required by `AGENTS.md`.
- Do not create icons, imagery, statistics, or visual styling without owner direction. Ask one focused question about visual identity before Task 7; record the owner's answer in `DESIGN.md`.
- Do not add dependencies for these fixes. Keep `local.properties` untouched.
- Every new UI action needs keyboard/TalkBack access, a visible focus state, and a touch target of at least 48 dp.

## Review Focus

- Large system font: nav labels and badges remain readable without clipping; verify in Task 6 device check.
- Long translated labels: controls and section headers wrap or remain reachable; verify in Tasks 2 and 7 device checks.
- Failed history reload after records are visible: old rows remain and a retry action appears; verify in Task 3 test.
- Delete followed by Undo after filtering or reordering: restore the same rule exactly once; verify in Task 4 test.
- User-selected foreground/background with insufficient contrast: reject the change without saving it; verify in Task 5 test.

---

## File Map

- `DESIGN.md`: owner-approved visual direction and ENERGY / RHYTHM / MOTION dials.
- `app/src/main/java/spam/blocker/ui/widgets/StrokeButton.kt`, `Balloon.kt`: shared touch targets and accessible help.
- `app/src/main/java/spam/blocker/ui/history/HistoryViewModel.kt`, `HistoryList.kt`: observable loading, error, retry, and retained rows.
- `app/src/main/java/spam/blocker/ui/setting/regex/RegexList.kt`: one delete path for menu and swipe.
- `app/src/main/java/spam/blocker/ui/setting/misc/Theme.kt`, `app/src/main/java/spam/blocker/util/SharedPref.kt`, `app/src/main/java/spam/blocker/ui/theme/ColorContrast.kt`: validated user palette.
- `app/src/main/java/spam/blocker/ui/widgets/BottomNavView.kt`, `Section.kt`: selection semantics, scalable badges, visible disclosure.
- `app/src/main/res/values/strings_*.xml` and corresponding translations: short labels for newly visible states/actions. Follow existing resource split; avoid editing all locales unless translation is provided.
- Existing `app/src/androidTest` and `app/src/test` directories: focused regressions.
- `anti-slop/audit-002-<date>.md`: follow-up with evidence for each fixed finding and the antislop delivery gate.

### Task 1: Capture the visual direction (finding 8)

**Files:** Create `DESIGN.md` from owner input only.

**Interfaces:** Produces explicit identity, mood, palette, typography, and ENERGY / RHYTHM / MOTION values used by Task 7. No implementation style decisions precede it.

The owner may answer while Tasks 2–6 proceed; only Task 7 waits for the confirmed direction.

- [x] **Step 1: Ask one decisive question.** The owner chose “calmo e legível” on 18/10/2026.
- [x] **Step 2: Write `DESIGN.md`.** Record the owner's choice, current UI as the point of departure, inferred dials, and one-line purposes for color, type, spacing, and section hierarchy.
- [x] **Step 3: Check direction.** Read the file against R-37 and the current app. It contains no placeholders or contradictory instructions and provides clear choices for Task 7.
- [x] **Step 4: Commit** only `DESIGN.md` after confirmation.

### Task 2: Make shared buttons and help usable by touch and accessibility services (findings 1–2)

**Files:** Modify `app/src/main/java/spam/blocker/ui/widgets/StrokeButton.kt`, `Balloon.kt`; inspect representative callers in `setting/quick/Contacts.kt`, `setting/misc/FAQ.kt`, and `setting/SettingScreen.kt`.

**Interfaces:** Preserve `Button`, `StrokeButton`, and `BalloonQuestionMark` signatures. Their visual content may remain compact, but their hit areas become at least 48 × 48 dp. Help exposes an action label to accessibility services.

- [x] **Step 1: Record current failure.** On a phone emulator, inspect a `StrokeButton` and help icon bounds with Layout Inspector and attempt TalkBack activation. Expected baseline: the hit areas are below 48 dp and help has no clear action name.
- [x] **Step 2: Change shared hit areas.** Place min-size semantics and click handling on the outer interactive bounds; keep the existing compact border/icon inside. Avoid overlapping adjacent actions; fix only callers whose layout reveals overlap.
- [x] **Step 3: Label help.** Give `BalloonQuestionMark` a localized “Help” action description and preserve its tooltip behavior.
- [x] **Step 4: Build and verify.** Run `./gradlew :app:assembleFdroidDebug`; inspect representative settings, FAQ, and popup rows at normal and large font scales. Expected: no clipped labels or overlapping hit areas; TalkBack announces and activates help.
- [x] **Step 5: Commit** these widget changes and any new string resource.

### Task 3: Make history loading and failure visible (finding 3)

**Files:** Modify `app/src/main/java/spam/blocker/ui/history/HistoryViewModel.kt`, `HistoryList.kt`; add localized loading/error/retry strings; extend `app/src/androidTest/java/spam/blocker/ui/history/HistoryEmptyTest.kt` or add `HistoryLoadTest.kt`. Inspect callers in `App.kt`, `main/MainActivity.kt`, `history/HistoryScreen.kt`, and `history/HistoryFabs.kt`.

**Interfaces:** Keep `fun reload(ctx: Context): Unit`; produce observable `isLoading` and `loadError` state. A failed reload leaves previously displayed records intact, shows a message and retry control, and never leaves the UI in perpetual loading.

- [x] **Step 1: Write failing instrumentation tests.** Test success, a query against a deliberately missing history table, and a retry after failure. Assert loading ends, the error is visible in state, and existing records are retained on failure.
- [x] **Step 2: Run the targeted tests.** Run `./gradlew :app:connectedFdroidDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.history.HistoryLoadTest`. Expected: fail before implementation.
- [x] **Step 3: Implement reload state.** Move database work off the main thread behind the unchanged `reload(ctx)` entry point, publish results on the main thread, and cancel or supersede older reloads so a stale search cannot replace newer results. Treat coroutine cancellation separately from query errors. Preserve the existing empty-message selection.
- [x] **Step 4: Render all states.** Show loading text/progress while an empty list loads, show an error message and retry button on failure, and keep existing rows visible with an error notice if a refresh fails.
- [x] **Step 5: Run targeted tests and inspect UI.** Repeat the targeted test command; then verify call and SMS tabs, empty/filtered histories, slow loading, failure, and retry on an emulator. Expected: no blank or frozen screen and no lost rows after failure.
- [x] **Step 6: Commit** the state, UI, tests, and strings.

### Task 4: Make individual rule deletion available from its menu (finding 4)

**Files:** Modify `app/src/main/java/spam/blocker/ui/setting/regex/RegexList.kt`; add a focused instrumentation test under `app/src/androidTest/java/spam/blocker/ui/setting/regex/` if the existing database test setup can be reused.

**Interfaces:** Preserve `RegexList` and `RegexItem`. Both the existing swipe and menu item call one `internal fun deleteRule(ctx: Context, rule: RegexRule, ruleList: SnapshotStateList<RegexRule>, coroutineScope: CoroutineScope)` in `RegexList.kt`; it removes by ID, persists the deletion, and offers the same Undo action. An already absent rule is a no-op.

- [x] **Step 1: Write the failure check.** Demonstrate that selecting the existing “delete rule” menu item only opens swipe instructions. In the regression test, delete after list filtering/reordering, then Undo; assert the original rule ID and contents return once.
- [x] **Step 2: Run the targeted test.** Run `./gradlew :app:connectedFdroidDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.regex.RegexDeleteTest`. Expected: fail before implementation; record the manual menu baseline separately.
- [x] **Step 3: Extract the existing swipe deletion into one local function** in `RegexList.kt`, retaining database update, list update, snackbar, and Undo behavior. Call it from the menu using the selected rule and from `RegexItem` using the swiped rule; remove the swipe-only prompt.
- [x] **Step 4: Verify.** Repeat the targeted test command, then use the menu with TalkBack/keyboard and the swipe on a device. Expected: both delete, both undo, and neither crashes when a filtered item disappears.
- [x] **Step 5: Commit** the change and test.

### Task 5: Guard user palette contrast (finding 5)

**Files:** Create `app/src/main/java/spam/blocker/ui/theme/ColorContrast.kt`; modify `app/src/main/java/spam/blocker/ui/setting/misc/Theme.kt`, `app/src/main/java/spam/blocker/util/SharedPref.kt`, and `app/src/main/java/spam/blocker/ui/widgets/BottomNavView.kt` if badge foreground requires correction; add `app/src/test/java/spam/blocker/ui/theme/ColorContrastTest.kt` and any warning string.

**Interfaces:** `fun contrastRatio(foreground: Color, background: Color): Double` and `fun readableOnAll(foreground: Color, backgrounds: List<Color>, minimumRatio: Double): Boolean`. The palette editor checks affected text/background pairs before `Delegate.update` persists a candidate. Normal text requires 4.5:1; large text and non-text indicators require 3:1. Invalid choices stay unsaved and get a readable explanation.

- [x] **Step 1: Write failing unit tests.** Assert black/white is 21:1, same color is 1:1, a low-contrast text/background candidate is rejected, and a valid candidate is accepted. Include the selected badge foreground/background pairing.
- [x] **Step 2: Run `./gradlew :app:testFdroidDebugUnitTest --tests spam.blocker.ui.theme.ColorContrastTest`.** Expected: fail before implementation.
- [x] **Step 3: Implement the ratio and palette check.** Use WCAG relative luminance from Compose `Color` channels. Check the actual surfaces used by editable text, status colors, buttons, and badges; show the warning in the theme dialog before writing preferences. Keep Reset working even if an old saved palette is invalid.
- [x] **Step 4: Run tests and inspect both surfaces.** Repeat the targeted unit test, build, and inspect background plus dialog combinations at normal and large text. Expected: invalid colors never persist; valid colors and Reset work.
- [x] **Step 5: Commit** the contrast code, UI, and tests.

### Task 6: Expose selected navigation state and respect font scaling (finding 6)

**Files:** Modify `app/src/main/java/spam/blocker/ui/widgets/BottomNavView.kt`; reuse localized tab labels and badge count text.

**Interfaces:** Preserve `BottomBar(vm)` and its reselect behavior. Each tab exposes selected state through Compose semantics, and unread counts respect the system font scale.

- [x] **Step 1: Record baseline.** With TalkBack, focus all three tabs and note whether the active tab and unread count are announced; raise system font size and inspect badge clipping.
- [x] **Step 2: Add selection semantics** to the existing clickable tab node, remove the forced `fontScale = 1f`, and adapt badge constraints only if enlarged text clips. Do not duplicate announcements for icon plus label.
- [x] **Step 3: Build and verify.** Run `./gradlew :app:assembleFdroidDebug`; check tab selection, reselect-to-dialer/SMS behavior, unread count, large text, and narrow width. Expected: active tab announced, badge readable, all tabs reachable.
- [x] **Step 4: Commit** the navigation change.

### Task 7: Show section disclosure clearly within the approved visual direction (finding 7)

**Files:** Modify `app/src/main/java/spam/blocker/ui/widgets/Section.kt`; inspect all `Section` callers in `setting/SettingScreen.kt` and popup dialogs. Use `DESIGN.md` from Task 1.

**Interfaces:** Preserve `Section(...)` and saved collapsed state. Show a visible expanded/retracted indicator, keep the whole header interactive, and use the app's existing icons/colors. Keep the existing state description for accessibility.

- [x] **Step 1: Record baseline.** Inspect an expanded and collapsed settings section; confirm the title has no visible affordance.
- [x] **Step 2: Implement the indicator** using the existing arrow resource and the owner-approved spacing, color, and type hierarchy. Keep title wrapping on narrow screens and long translations.
- [x] **Step 3: Build and verify.** Run `./gradlew :app:assembleFdroidDebug`; expand/collapse each settings group and one popup section, navigate by keyboard/TalkBack, and test large font. Expected: visible state and unchanged persistence.
- [x] **Step 4: Commit** the section change.

### Task 8: Whole-app verification and antislop follow-up

**Files:** Create `anti-slop/audit-002-<date>.md`; edit only defects found during verification.

**Interfaces:** Follow-up maps audit findings 1–8 to changes and evidence. No new design features.

- [x] **Step 1: Run `./gradlew :app:testFdroidDebugUnitTest :app:assembleFdroidDebug`** and the targeted Android instrumentation tests. Expected: all pass.
- [x] **Step 2: Device pass.** Click through changed controls one at a time on call/SMS history, regex rules, settings, theme dialog, and bottom navigation; cover empty/loading/error, normal/large text, light or custom palette variants actually shipped, TalkBack/keyboard, and narrow width. Record each action and outcome; check Logcat for errors.
- [x] **Step 3: Write the follow-up report** with one result per original finding and the antislop Delivery Gate PASS/FAIL evidence. Resolve every FAIL before delivery; keep any unverified condition explicitly open rather than claiming a pass.
- [x] **Step 4: Review the diff and commit** the follow-up report. Leave the pre-existing `local.properties` change alone.
