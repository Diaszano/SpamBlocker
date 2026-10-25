# SpamBlocker Complete Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the approved four-destination Android UI, two light/dark theme families, preserved custom colors, accessible history actions, and redesigned launcher icon without changing blocking behavior.

**Architecture:** Keep the current Compose screens, view models, tables, and `G.palette` consumers. Add theme selection and preset application around the existing palette; split the current settings content between a new Protection screen and Settings; improve history presentation and states in place. Share existing actions and persisted state rather than copying them into new screens.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Android resources, SharedPreferences, Gradle, JUnit 5 unit tests and existing Android instrumentation tests.

**Spec:** `docs/superpowers/specs/2026-10-18-spamblocker-complete-redesign-design.md`

## Global Constraints

- Preserve `service/`, `db/`, rule behavior, report behavior, automation behavior, and existing user records.
- Preserve every saved `color_*` preference; theme choice uses separate keys. No new dependency is planned.
- Four destinations are `call`, `sms`, `protection`, `setting`; new installs start at `call` and valid saved routes remain valid.
- Catppuccin uses official Latte for light and Macchiato for dark. Truecaller-inspired is an original blue family, not third-party artwork.
- New installs use Truecaller-inspired with System appearance; existing installs with saved custom colors open in Custom.
- The editor changes Custom only. System appearance follows Android without an app restart.
- Normal text contrast is at least 4.5:1; large text and significant controls are at least 3:1. Interactive targets are at least 48 dp.
- New user-facing strings go in `app/src/main/res/values/`; existing translations can fall back to those strings.
- Do not overwrite the current untracked `DESIGN.md`, audit files, old plans, `local.properties`, or deleted `AGENTS.md` by accident. Stage only task-owned files.

## Review Focus

1. Existing `color_*` values plus a preset change: return to Custom shows the exact saved values. Task 2 tests persistence.
2. System dark mode changes while the app stays open: all surfaces and system bars switch together. Task 2 tests resolver and records device verification.
3. Saved `setting`, `call`, `sms`, or invalid tab: the first three resolve unchanged and invalid falls back to `call`. Task 4 tests route resolution.
4. Both history filters off, empty search, read failure, and retry: each shows the correct message without a transient false empty state. Task 5 tests state selection and records a device retry check.
5. Paused global protection with call/SMS permissions denied: the Protection screen distinguishes paused, disabled, and unavailable states. Task 4 tests status selection.

---

## File Map

- `DESIGN.md`: approved product direction; update the existing untracked file without discarding its useful content.
- `ui/theme/ThemeSelection.kt` (new), `ui/theme/ThemePresets.kt` (new), `ui/theme/Theme.kt`: selection, exact preset colors, Material scheme.
- `util/SharedPref.kt`, `GlobalVariables.kt`, `ui/setting/misc/Theme.kt`: separate saved custom colors from preset selection and expose theme controls.
- `ui/widgets/{BottomNavView,StrokeButton,Balloon,Section}.kt`: navigation semantics and shared accessibility behavior.
- `ui/main/MainActivity.kt`, `def/Def.kt`: fourth route and saved-route resolution.
- `ui/setting/ProtectionScreen.kt` (new), `ui/setting/SettingScreen.kt`, `ui/setting/GloballyEnabled.kt`: reorganized destinations using existing state and callbacks.
- `ui/history/{HistoryViewModel,HistoryScreen,HistoryList,HistoryCard,HistoryContextMenu,HistoryFabs}.kt`: history hierarchy, states, accessible actions and reuse of existing options.
- `res/values/strings*.xml` and launcher resources under `res/mipmap*` and `res/drawable`: labels and icon variants.
- `app/src/test/...` and `app/src/androidTest/...`: regression tests for the nontrivial state and persistence paths.

### Task 1: Record direction and theme selection contract

**Files:** Modify `DESIGN.md`; create `app/src/main/java/spam/blocker/ui/theme/ThemeSelection.kt`, `app/src/test/java/spam/blocker/ui/theme/ThemeSelectionTest.kt`; modify `app/src/main/java/spam/blocker/util/SharedPref.kt` for saved choice keys only.

**Interfaces:** Produce `enum class ThemeFamily { TRUECALLER, CATPPUCCIN, CUSTOM }`, `enum class ThemeAppearance { SYSTEM, LIGHT, DARK }`, `data class ThemeChoice(val family: ThemeFamily, val appearance: ThemeAppearance)`, `fun resolveThemeChoice(savedFamily: String?, savedAppearance: String?, hasSavedCustomColors: Boolean): ThemeChoice`, and `fun ThemeChoice.isDark(systemIsDark: Boolean, customBackgroundIsDark: Boolean): Boolean`. Store raw strings under `theme_family` and `theme_appearance`, separately from `color_*`. Custom derives its Material light/dark scheme from its saved background luminance; the appearance selector applies to the two preset families.

- [x] **Step 1: Write failing unit tests.** `resolveThemeChoice(null, null, false)` is Truecaller/System; `resolveThemeChoice(null, null, true)` is Custom/System; saved valid families and appearances round-trip; unknown values fall back safely; System follows the supplied system flag for presets and Custom follows the supplied background luminance.
- [x] **Step 2: Run red.** `./gradlew :app:testFdroidDebugUnitTest --tests spam.blocker.ui.theme.ThemeSelectionTest`; expect failure before implementation.
- [x] **Step 3: Implement the interfaces and saved keys.** Update `DESIGN.md` with the approved four destinations, two theme families, identity and ENERGY 2 / RHYTHM 2 / MOTION 1. Preserve any unrelated user notes.
- [x] **Step 4: Run green and commit.** Run the same test and `./gradlew :app:assembleFdroidDebug`; expect pass. Commit only these files with `feat: define redesign theme selection`.

### Task 2: Apply four palettes without losing Custom

**Files:** Create `app/src/main/java/spam/blocker/ui/theme/ThemePresets.kt`; modify `app/src/main/java/spam/blocker/util/SharedPref.kt`, `app/src/main/java/spam/blocker/GlobalVariables.kt`, `app/src/main/java/spam/blocker/ui/theme/Theme.kt`, `app/src/main/java/spam/blocker/ui/setting/misc/Theme.kt`; create `app/src/test/java/spam/blocker/ui/theme/ThemePresetsTest.kt` and `app/src/androidTest/java/spam/blocker/ui/theme/ThemePersistenceTest.kt`; add theme strings in `res/values/`.

**Interfaces:** `fun presetColors(family: ThemeFamily, dark: Boolean): Map<String, Color>` returns all existing `spf.Palette.Delegate.key` values for non-Custom families. `Palette.applyPreset(colors: Map<String, Color>)` updates effective Compose state without writing `color_*`; `Palette.restoreCustom()` restores persisted custom values. The theme chooser persists `ThemeChoice` and invokes these methods. `AppTheme` chooses `lightColorScheme` or `darkColorScheme` from the effective palette, using the Custom background luminance in Custom.

- [x] **Step 1: Write failing tests.** Assert every preset contains every palette key, Catppuccin's key colors match the [official palette](https://catppuccin.com/palette/), and all planned text/surface and control/surface pairs meet the spec's contrast thresholds. Instrumentation test: save one custom color, choose both families, return to Custom, and assert the saved value is unchanged.
- [x] **Step 2: Run red.** `./gradlew :app:testFdroidDebugUnitTest --tests spam.blocker.ui.theme.ThemePresetsTest` and `./gradlew :app:connectedFdroidDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.theme.ThemePersistenceTest`; expect the new tests to fail before implementation. If no device is connected, record the instrumentation test as pending and continue the unit cycle.
- [x] **Step 3: Implement presets and chooser.** Use official Latte/Macchiato values mapped to existing color roles. Define an original blue light/dark pair and keep state colors semantic. The existing color picker appears under Custom; selecting a preset never calls `Delegate.update()`. Apply effective colors at startup and on System changes without a mismatched first frame.
- [x] **Step 4: Run green and commit.** Run unit test, instrumentation when a device is available, and `./gradlew :app:assembleFdroidDebug`; inspect light/dark system bars. Commit task files with `feat: add accessible theme families`.

### Task 3: Make shared controls usable across themes

**Files:** Modify `app/src/main/java/spam/blocker/ui/widgets/{StrokeButton,Balloon,Section,BottomNavView}.kt` and affected callers; add strings under `res/values/`.

**Interfaces:** Existing composable signatures remain unless a caller needs an explicit content description. Keep `BottomBarViewModel` and `TabItem`; expose selected state through Compose semantics. Shared click targets are at least 48 dp while compact visuals remain possible.

- [x] **Step 1: Record the baseline.** Locate every caller of changed composables, note buttons or help icons whose hit area grows, and list the existing focus order in a compact and a large-font preview.
- [x] **Step 2: Make the shared changes.** Restore adequate button and help targets; name help actions; announce selected tabs; remove the badge's forced `fontScale = 1f`; show Section expansion visually and semantically. Keep actions and validation already attached to each caller.
- [x] **Step 3: Verify and commit.** Run `./gradlew :app:assembleFdroidDebug` and relevant existing widget tests. Inspect each changed control in light/dark previews at normal and 1.3× font scale; record any target collision. Commit with `fix: make shared UI controls accessible`.

### Task 4: Add the Protection destination and four-tab navigation

**Files:** Modify `app/src/main/java/spam/blocker/def/Def.kt`, `app/src/main/java/spam/blocker/ui/main/MainActivity.kt`, `app/src/main/java/spam/blocker/ui/widgets/BottomNavView.kt`; create `app/src/main/java/spam/blocker/ui/setting/ProtectionScreen.kt`, `app/src/test/java/spam/blocker/ui/main/TabRouteTest.kt`, `app/src/test/java/spam/blocker/ui/setting/ProtectionSummaryTest.kt`; modify `app/src/main/java/spam/blocker/ui/setting/{SettingScreen,GloballyEnabled}.kt` and strings.

**Interfaces:** Add `Def.PROTECTION_TAB_ROUTE = "protection"` and `fun resolveStartTab(savedRoute: String): String` in `Def.kt`, returning `call` for unknown routes. `ProtectionScreen()` uses existing `G` state and `setGlobalEnabled`, `pauseBlocking`, and `resumeBlocking` actions. Add `enum class ProtectionSummary { OFF, PAUSED, PERMISSION_REQUIRED, CHANNELS_OFF, ACTIVE }` and `fun protectionSummary(globalEnabled: Boolean, paused: Boolean, permissionRequired: Boolean, anyChannelEnabled: Boolean): ProtectionSummary` in `ProtectionScreen.kt`, with priority in the enum order. `SettingScreen()` retains Instant Query, Report Number, Automation, and Miscellaneous. Existing quick and regex content moves to Protection without duplicated independent state.

- [x] **Step 1: Write failing route and status tests.** `resolveStartTab` preserves `call`, `sms`, `setting`, `protection`; `""` and an unknown route resolve to `call`. Add `app/src/test/java/spam/blocker/ui/setting/ProtectionSummaryTest.kt`: OFF wins when global is false, PAUSED wins over permission issues, permissionRequired produces PERMISSION_REQUIRED, no enabled channel produces CHANNELS_OFF, otherwise ACTIVE.
- [x] **Step 2: Run red.** `./gradlew :app:testFdroidDebugUnitTest --tests spam.blocker.ui.main.TabRouteTest --tests spam.blocker.ui.setting.ProtectionSummaryTest`; expect failure.
- [x] **Step 3: Implement navigation and content move.** Keep saved tab persistence and unread counts. Show global, paused, call/SMS/MMS permission status, quick rules and testing in Protection; remove their old visual copy from Settings. Preserve section collapse preferences and existing editors.
- [x] **Step 4: Verify and commit.** Run both new tests and `./gradlew :app:assembleFdroidDebug`; manually open each tab, switch back and forth, pause/resume, and restart on every saved route. Verify denied permissions show an unavailable explanation. Commit with `feat: add protection destination`.

### Task 5: Rebuild history hierarchy and data states

**Files:** Modify `app/src/main/java/spam/blocker/ui/history/{HistoryViewModel,HistoryScreen,HistoryList,HistoryCard,HistoryFabs}.kt`; update strings; extend `app/src/test/java/spam/blocker/ui/history/HistoryEmptyUnitTest.kt` and `app/src/androidTest/java/spam/blocker/ui/history/HistoryEmptyTest.kt` if Android context is required.

**Interfaces:** Add an explicit read-error state to `HistoryViewModel` and a retry action that calls its existing `reload(ctx)`. Keep old records visible until a successful replacement; a failed read shows an error with retry rather than an empty list. Add `enum class HistoryListState { LOADING, ERROR, EMPTY, CONTENT }` and `fun historyListState(isLoading: Boolean, hasError: Boolean, hasRecords: Boolean): HistoryListState` in `HistoryList.kt`, with CONTENT winning whenever old records remain visible. Retain `historyEmptyMessageRes` for filtered-empty cases.

- [x] **Step 1: Write failing state tests.** Assert `historyListState(true, false, false) == LOADING`, `historyListState(true, false, true) == CONTENT`, `historyListState(false, false, false) == EMPTY`, `historyListState(false, true, false) == ERROR`, and `historyListState(false, true, true) == CONTENT`. Keep existing tests for both filters off and search without matches. Verify retained rows and retry on device in Step 4.
- [x] **Step 2: Run red.** `./gradlew :app:testFdroidDebugUnitTest --tests spam.blocker.ui.history.HistoryEmptyUnitTest`; expect the new assertions to fail.
- [x] **Step 3: Implement history presentation.** Load replacement rows before changing `records`; catch read failures into the explicit error state and clear that state after a successful retry. Put named search/filter actions at the top, keep the existing independent filters, make contact/number neutral, put block/allow in a semantic label, and show reason and time without clipping. Add loading, empty, filtered-empty, and error/retry UI. Keep real contact photos; use a neutral fallback symbol.
- [x] **Step 4: Verify and commit.** Run the unit test, `./gradlew :app:connectedFdroidDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.history.HistoryEmptyTest` with a device, and `./gradlew :app:assembleFdroidDebug`. Inspect call and SMS rows in four themes, empty/loading/error states, retained rows after failure, retry, and large text. Commit with `feat: refresh history views and states`.

### Task 6: Expose all history actions without hidden gestures

**Files:** Modify `app/src/main/java/spam/blocker/ui/history/{HistoryList,HistoryContextMenu,HistoryCard}.kt`; update strings and any affected history tests.

**Interfaces:** `HistoryContextMenuWrapper` receives named `onOpen` and `onDelete` callbacks from `HistoryList`. Swipe and menu use the same callbacks, so mark-read, deletion, undo and launcher behavior have one implementation. Existing test, copy, search online, regex and spam-db menu actions remain.

- [x] **Step 1: Record a baseline action matrix.** On an emulator, check call and SMS rows for open, delete and undo by swipe and by visible/menu action. Record that the last two are gesture-only today. If no emulator is available, record the code paths in `HistoryList.kt` and `HistoryContextMenu.kt` and leave the device check pending.
- [x] **Step 2: Run baseline build.** `./gradlew :app:assembleFdroidDebug`; expect pass before modifying action wiring.
- [x] **Step 3: Implement visible/menu actions.** Keep single-tap expansion and existing context actions. Make opening and deleting reachable through a named control or menu on every row; preserve undo and mark-read handling. Remove the dependence on long press to discover the menu.
- [x] **Step 4: Verify and commit.** Run the build. On call and SMS rows, exercise open, block, test, copy, rule edit, delete, undo, swipe shortcuts, keyboard and TalkBack, recording each result. Commit with `fix: expose history actions accessibly`.

### Task 7: Replace launcher assets and complete visual validation

**Files:** Create `app/src/main/res/drawable/ic_launcher_bell_filter.xml` and `ic_launcher_monochrome.xml`; modify `ic_launcher.webp` and `ic_launcher_round.webp` in every `app/src/main/res/mipmap-*` density and `app/src/main/res/mipmap-anydpi-v26/{ic_launcher,ic_launcher_round}.xml`; delete obsolete `ic_launcher_foreground.webp` variants after all references move to the vector. No UI logic files.

**Interfaces:** Keep the existing launcher resource names used by `AndroidManifest.xml`. The new mark is a simplified blue bell with a legible filter-shaped cutout. Use `ic_launcher_bell_filter.xml` as the adaptive foreground, `ic_launcher_monochrome.xml` for themed icons, and export legacy density and round WebP variants from the same master shape.

- [x] **Step 1: Draft and inspect the master icon.** Verify the approved bell/filter concept at launcher and notification-scale sizes before exporting. Do not introduce a Truecaller logo or generic shield.
- [x] **Step 2: Replace resources and build.** Run `./gradlew :app:assembleFdroidDebug`; expect all icon references to resolve in both product flavors.
- [x] **Step 3: Validate the full product.** Run unit tests, instrumentation tests with a device, and `./gradlew :app:assembleFdroidDebug :app:assembleGoogleplayDebug`. In an emulator/device, record click-through by tab, action, empty/loading/error state, all four presets, Custom, system-mode change, compact width, large font and TalkBack. Measure representative color pairs and check launcher variants. Fix any failures in their owning task files, then commit icon resources with `feat: refresh SpamBlocker identity`.

## Completion Gate

The redesign is complete only when every spec acceptance criterion is checked against the running Android app, every interactive action has a recorded result, all shipped themes pass contrast and layout checks, and saved user colors survive theme switching. Report unavailable device-only checks explicitly. Keep unrelated workspace changes out of task commits.
