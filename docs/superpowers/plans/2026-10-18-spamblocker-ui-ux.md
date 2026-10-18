# SpamBlocker Dense UI/UX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve scanability, control consistency and accessibility across settings and history while preserving SpamBlocker's compact rule-oriented UI.

**Architecture:** Keep the existing screens and preference keys. Add one presentation component for common setting rows, derive semantic UI colors from `G.palette`, and share the existing global enable action between settings and history. Adapt dialogs and empty states locally; leave rules, database and services untouched.

**Tech Stack:** Kotlin, Jetpack Compose, Android 10+, Compose BOM `2026.09.00`, existing `spf` preferences.

**Spec:** `docs/superpowers/specs/2026-10-18-spamblocker-ui-ux-design.md`

## Global Constraints

- Product code changes: `app/src/main/java/spam/blocker/ui/` and `app/src/main/res/` only. No changes to `db/`, `service/`, blocking decisions or stored preference keys.
- Approved exception: `app/build.gradle.kts` and `gradle/libs.versions.toml` may add only BOM-managed `ui-tooling-preview` (`implementation`) and `ui-tooling` (`debugImplementation`) for the required `@Preview` checks. No other dependency.
- Preserve the dark compact layout, all current controls, the master switch in Settings, saved section/list collapse, hidden choices and custom palette values. No new navigation or large cards.
- New text: English keys in `res/values` only. Leave translation files untouched and list new keys in the final report. Prefer short labels.
- Before a Compose API is used, confirm its signature in the resolved BOM or official reference. Avoid new alpha/experimental APIs.
- After **each** implementation commit run `./gradlew assembleDebug lint`. Render a dark `@Preview` for every changed visual component at 320dp, 1.3× font and German or Russian; run existing UI tests if any are introduced or discovered. Currently there are no Compose UI tests or previews. If no emulator is available for a manual check, report that check as pending.
- One independently reviewable commit per task/theme. Do not edit the approved spec except to record an explicitly approved correction.

## File map

| Area | Files and responsibility |
| --- | --- |
| Tokens and shared controls | `ui/theme/Theme.kt`, `ui/setting/SettingScreen.kt`, `ui/setting/SettingItem.kt` (new), `ui/widgets/SwitchBox.kt`, `ui/widgets/Section.kt`; semantic color, row layout and accessibility |
| Quick Settings | `ui/setting/quick/*.kt`, `ui/setting/GloballyEnabled.kt`, `ui/setting/SettingScreen.kt`; migrate common rows, show status and add lightweight headers |
| Global control | `ui/history/HistoryScreen.kt`, `ui/setting/GloballyEnabled.kt`, `ui/setting/GlobalEnable.kt` (new); one persistence/state action and compact history header |
| Dialogs | `ui/widgets/PopupDialog.kt` and callers with wide content; width and IME behavior |
| History and summaries | `ui/history/{HistoryList,HistoryFabs}.kt`, `ui/widgets/Fab.kt`, `ui/setting/{api/ApiHeader,bot/BotHeader}.kt`; empty states and action names |
| Resources/previews | Relevant `res/values/strings_*.xml` files; preview wrappers beside the changed composables, using sample state rather than database writes. Initialize `G.palette` from the preview context only inside preview wrappers if needed |

Expected new English resource keys: `spam_blocking`, `signals`, `exceptions`, `context_rules`, `behavior`, `needs_training`, `rcs_unavailable`, `history_filters_off`, `history_no_matches`, `history_empty`, `none_active`. Reuse existing `setting`, `delete` and `title_rule_testing` for icon action labels. Check for equivalent existing keys before creating each one; report the actual additions at the end.

## Review Focus

These five scenarios are checked in the owning tasks below; a build alone does not prove them.

1. A 320dp window with 1.3× font and long German/Russian labels shows the full setting name and a distinct switch target (Tasks 1–3).
2. Toggling global protection from either history tab updates Settings and survives restart without changing call/SMS switches (Task 4).
3. TalkBack identifies one switch action with its state, and separate value and switch actions remain distinguishable (Tasks 1–2).
4. With both history visibility filters off, the empty message names the filters; a search miss gets a different message (Task 6).
5. A 320dp dialog with the keyboard open keeps its focused field and actions reachable (Task 5).

---

### Task 1: Semantic colors and common setting control

**Files:** Modify `ui/theme/Theme.kt`, `ui/setting/SettingScreen.kt`, `ui/widgets/SwitchBox.kt`, `ui/widgets/Section.kt`, `app/build.gradle.kts`, `gradle/libs.versions.toml`; create `ui/setting/SettingItem.kt` and preview wrappers beside these components.

**Interfaces:** Produce `@Composable fun SettingItem(labelId: Int, modifier: Modifier = Modifier, helpTooltip: String? = null, value: String? = null, onValueClick: (() -> Unit)? = null, checked: Boolean? = null, onCheckedChange: ((Boolean) -> Unit)? = null, expanded: Boolean? = null, onExpand: (() -> Unit)? = null)`. It draws one compact row, accepts an optional separate value action and exposes exactly one of toggle/disclosure as the row's primary action. Keep `LabeledRow` available for complex existing content. Add UI-only semantic color accessors backed by `G.palette`; do not replace persisted colors.

- [x] Add the two approved preview artifacts via the existing Compose BOM and confirm that `@Preview(widthDp, fontScale, locale, uiMode)` resolves. Keep the renderer in `debugImplementation`; the annotation artifact is a compile dependency.
- [x] Add a focused preview of switch, value and disclosure rows. Include dark 320dp/1.3×/`de` and `ru` variants and a long-label sample; record the expected full text and separate tap areas before editing layout.
- [x] Implement semantic foreground roles and neutral default text in `SettingLabel`; implement `SettingItem` without no-op clicks. Expand adjacent interactive rows and `SwitchBox` targets to at least 48dp, keep small glyphs, add toggle role/state and section heading expansion semantics. `Section` and `SwitchBox` each get their own preview.
- [x] Check preview rendering and TalkBack traversal: label plus switch is one named toggle; a separate value editor has its own named action; headings announce expansion. Check 4.5:1 default text and 3:1 significant icon/control contrast; do not change a user's saved palette.
- [x] Run `./gradlew assembleDebug lint`; commit as `ui: add semantic compact setting controls`.

### Task 2: Migrate quick-setting switch and value rows

**Files:** Modify `ui/setting/quick/{RepeatedCall,OffTime,Dialed,Answered,Contacts,Stir,SpamDB,BlockType,RecentApps,MeetingMode,CallerID,Notification,Emergency,LocalAI}.kt` only where the common row fits; modify `ui/setting/SettingScreen.kt` for long-label behavior and `ui/setting/{regex/RegexHeader,api/ApiHeader,bot/BotHeader}.kt` for duplicate row click actions; add previews beside changed presentation functions.

**Interfaces:** Consume `SettingItem` from Task 1. Keep each existing switch callback, permission request, editor dialog, priority value and preference write. A value is plain secondary text with a click action; a genuine inline editor keeps its existing control. Do not change summaries or rule order.

- [x] Make a caller checklist from the files above: label, optional value, editor callback, switch callback, permission gate, and expanded/collapsed state. Use it as the migration check before editing.
- [x] Migrate `RepeatedCall` and `OffTime` first. Preview both with enabled/disabled state and a long duration at 320dp/1.3×; verify switch and value actions separately.
- [x] Migrate the remaining equivalent rows. Leave complex nested dialogs on `LabeledRow` where migration would obscure their actions. Remove only redundant row-level `clickable` modifiers; specifically inspect `RegexHeader`, `ApiHeader` and `BotHeader` for duplicate click semantics.
- [x] Preview each changed presentation function in dark mode with a long locale. Check every permission-gated switch still asks the same permission, and every displayed value opens the same editor.
- [x] Run `./gradlew assembleDebug lint`; commit as `ui: clarify settings row actions`.

### Task 3: Quick Settings hierarchy and visible dependencies

**Files:** Modify `ui/setting/SettingScreen.kt`, `ui/setting/quick/LocalAI.kt`, `ui/setting/GloballyEnabled.kt`; add short English strings to the relevant `res/values/strings_*.xml` files and previews of the changed groups.

**Interfaces:** Keep existing `Section` and `spf.SettingSections` state. Group the existing quick-setting sequence with lightweight, non-interactive headers: screening signals (Contacts, STIR, Spam DB, Local AI), exceptions (Repeated, Dialed, Answered), context (Off Time, Emergency, Recent Apps, Meeting Mode), and behavior (Block Type, Notification, Caller ID). Do not add nested screens or collapse states. Local AI's “needs training” status appears only when both existing training counts are zero; RCS gets a short unavailable reason without changing its disabled behavior.

- [x] Preview the four groups at 320dp, 1.3× and `de`/`ru`; verify all current controls remain on the same settings screen and the saved top-level collapse still works.
- [x] Add headers, a neutral text hierarchy and the two inline dependency messages; reuse the current training count refresh path. Keep the existing help tooltips.
- [x] Check fresh install, saved collapsed section, active AI with zero samples, trained AI and disabled RCS. Ensure no label truncation or extra navigation.
- [x] Run `./gradlew assembleDebug lint`; commit as `ui: organize quick settings and explain unavailable features`.

### Task 4: Put the master switch on both history tabs

**Files:** Create `ui/setting/GlobalEnable.kt`; modify `ui/setting/GloballyEnabled.kt`, `ui/history/HistoryScreen.kt`; add a short English label only if no existing resource describes global blocking; add a preview of the compact history header.

**Interfaces:** Produce `fun setGlobalEnabled(ctx: Context, enabled: Boolean)` in `ui/setting/GlobalEnable.kt`. It writes `spf.Global(ctx).isGloballyEnabled`, updates `G.globallyEnabled.value`, and preserves the existing collapse rule: turning off stores collapsed; turning on with both call and SMS disabled stores expanded. `GloballyEnabled` reflects that saved state when revisited. Do not write the individual channel booleans.

- [x] State-check before implementation: list the expected `spf` and `G` values after toggling from Settings, Call History and SMS History, including restart.
- [x] Move the existing persistence action into `setGlobalEnabled` and call it from both switches. Place a compact labeled switch above the history search/list, with 48dp target and no overlap with FABs.
- [x] Preview on Call and SMS variants, dark 320dp/1.3×/long locale. Check the state matrix manually on device or emulator and verify saved collapse and individual channel values.
- [x] Run `./gradlew assembleDebug lint`; commit as `ui: expose global blocking in history`.

### Task 5: Make existing dialogs fit compact windows

**Files:** Modify `ui/widgets/PopupDialog.kt`; adjust only overflowing caller layouts in `ui/history/HistoryFabs.kt`, `ui/setting/Testing.kt`, `ui/setting/{api/EditApiDialog,bot/EditBotDialog,regex/EditRegexDialog,regex/PushAlert}.kt` as needed; add a dialog preview.

**Interfaces:** Change `PopupSize.minWidth()` to `fun minWidth(screenWidthDp: Float): Float = min(minWidthDp.toFloat(), maxWidth(screenWidthDp))`, and pass the current viewport width from `PopupDialog`. Keep existing caller-specified limits on wide screens. Preserve `imePadding`, scrolling, dismissal and each dialog's validation.

- [x] Add `app/src/androidTest/java/spam/blocker/ui/widgets/PopupSizeTest.kt` using the existing JUnit dependency. Assert `PopupSize(minWidthDp = 340, maxWidthPercentage = 0.9f).minWidth(320f) == 288f` and `.minWidth(600f) == 340f`; run `./gradlew connectedFdroidDebugAndroidTest` when a device is available.
- [x] Implement the clamp and adjust only callers whose contents still overflow. Preview a 320dp dialog with 1.3× text and an open editor.
- [x] Check the five Review Focus dialog conditions with the keyboard on an emulator/device. Confirm Back, outside dismiss, scrolling and input validation remain intact.
- [x] Run `./gradlew assembleDebug lint`; commit as `ui: fit dialogs in compact windows`.

### Task 6: Explain empty content and name history actions

**Files:** Modify `ui/history/{HistoryViewModel,HistoryList,HistoryFabs}.kt`, `ui/widgets/Fab.kt`, `ui/setting/{SettingScreen,api/ApiHeader,bot/BotHeader}.kt`; add English empty-state strings in `res/values/`; add previews for the empty list, empty summaries and icon actions.

**Interfaces:** Add `val isLoading = mutableStateOf(true)` to `HistoryViewModel` and set it around `reload` with `try/finally`. `HistoryList` shows no empty message while it is true; afterward it selects exactly one message in this order: both visibility filters off, active search with zero matches, no records. Add required `contentDescription: String` to `Fab` and supply it at its three existing call sites; its icon remains decorative. Empty `ApiSummary`/`BotSummary` say that no item is active without implying there are no configured items.

- [x] Record the four empty-state inputs and expected messages before implementation; include first load and a reload that temporarily clears records.
- [x] Add the UI-only load status and conditional messages. Add action labels to the settings, test and delete FAB callers; preview each changed state at 320dp/1.3× in dark mode.
- [x] Check TalkBack focus order, undo after deleting one history row, confirmation before deleting all records, and search/filter distinctions. Do not change deletion or database code.
- [x] Run `./gradlew assembleDebug lint`; run any UI tests present by this step; commit as `ui: clarify empty history and action labels`.

## Final review

- [x] Review the complete diff against all ten spec points and confirm only the two approved preview dependencies changed files outside product `ui/` and `res/`.
- [x] Run `./gradlew assembleDebug lint` once on the completed branch and render the preview matrix. Run UI tests if present. Record any unavailable emulator or preview rendering limitation rather than claiming a check passed.
- [x] Report changed commits, new English string keys, remaining limitations and contrast results for the default theme and a customized palette. Stop for user review before any merge or release.
