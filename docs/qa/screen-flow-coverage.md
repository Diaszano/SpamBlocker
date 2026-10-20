# Screen and flow coverage

This inventory records the current emulator/JVM evidence and the remaining limits. “Shown” means a Compose node was reached or asserted; it does not mean its action was exercised. The isolated instrumentation target is `spam.blocker.qa`.

## Main destinations

| Destination | Action / state | Current evidence or limit |
|---|---|---|
| Calls | Select Calls and move among all four tabs | [`MainNavigationTest.kt`](../../app/src/androidTest/java/spam/blocker/ui/main/MainNavigationTest.kt): all four tabs select; reselecting Calls emits the call-log URI while selected state remains, with the external handler stubbed. SMS reselection keeps its selected state; the default SMS launcher route is not asserted because the package is device-dependent. |
| Calls | Render allowed and blocked fixture records; toggle Allowed filter; search and clear; show no-match state | [`HistoryFlowsTest.kt`](../../app/src/androidTest/java/spam/blocker/ui/history/HistoryFlowsTest.kt): allowed/blocked records render, Allowed hides only allowed records, context-menu Filter searches, clear restores records, and an unmatched query shows the empty result. Blocked toggle, retry and loading/error UI are not exercised in Compose. Empty/filter-state logic is covered by [`HistoryEmptyTest.kt`](../../app/src/androidTest/java/spam/blocker/ui/history/HistoryEmptyTest.kt) and [`HistoryEmptyUnitTest.kt`](../../app/src/test/java/spam/blocker/ui/history/HistoryEmptyUnitTest.kt). |
| Calls | Pause for 15 minutes and resume blocking | `HistoryFlowsTest.kt` selects Pause Blocking → 15 minutes → Resume and checks the resumed action is available. Global master switch and expiration by elapsed time are not exercised in Compose. |
| Calls | Open/expand record; test, copy, search online, add/edit regex, block prefix, spam DB, delete, report; history FAB | Not exercised by the current history emulator test. Search/browser, call-log, report API and call actions need controlled UI/service fixtures; no external handler or report endpoint is run. |
| Calls | History FAB: logging, retention, row count, display toggles, SIM/time-color editor, delete all | Not exercised by the current emulator test. Real SIM/carrier/call-log data is device-owned. |
| SMS | Select SMS and show allowed/blocked fixture records; toggle Allowed filter | `MainNavigationTest.kt` and `HistoryFlowsTest.kt`; fixtures are synthetic and stored only in `spam.blocker.qa`. Search/clear, Blocked toggle, retry, empty/error UI and mark-read badge changes are not exercised by Compose. |
| SMS | Re-select SMS, open conversation, test/copy, search online, regex/prefix/database/delete, report, history FAB | Not exercised by the current emulator test. Real SMS app, carrier and report/API behavior are not invoked. |
| Protection | Select screen and expose all quick/additional rule labels | [`ProtectionFlowsTest.kt`](../../app/src/androidTest/java/spam/blocker/ui/setting/ProtectionFlowsTest.kt): traverses the visible rule labels. It does not toggle or configure them. State precedence is covered by [`ProtectionSummaryTest.kt`](../../app/src/test/java/spam/blocker/ui/setting/ProtectionSummaryTest.kt); pause boundaries by [`PauseLogicTest.kt`](../../app/src/test/java/spam/blocker/util/PauseLogicTest.kt). |
| Protection | Global/call/SMS toggles, pause/resume, permissions, rule dialogs, test FAB, priority conflicts | Not exercised by the current emulator test. [`RuleTest.kt`](../../app/src/androidTest/java/spam/blocker/service/RuleTest.kt) covers local screening logic, including the known `database_prefix` baseline failure; it does not simulate carrier or live-call behavior. |

## 14 quick rule controls

`ProtectionFlowsTest.kt` scrolls to and asserts the displayed label for each row. Toggle, configuration, permission and service behavior remains untested in Compose unless stated below.

| Rule | Interaction limit |
|---|---|
| Contacts | Contacts permission/data and strict/lenient settings not exercised. |
| STIR | Unverified-caller toggle not exercised; emulator has no carrier attestation. |
| SpamDB | Add/remove/expiry controls not exercised; local database logic is covered only by existing service tests. |
| LocalAI | Training/filter/save dialog not exercised; no live SMS training input. |
| RepeatedCall | Toggle and SMS-related option not exercised; no calls/messages generated. |
| Dialed | Toggle and permission flow not exercised; call/SMS providers are not populated. |
| Answered | Toggle and period configuration not exercised; no answered call generated. |
| OffTime | Toggle/schedule editor not exercised; schedule utility tests remain in [`ScheduleTest.kt`](../../app/src/androidTest/java/spam/blocker/util/ScheduleTest.kt). |
| EmergencySituation | Configuration not exercised; no emergency call placed. |
| RecentApps | App selection/usage access not exercised; Android special-access UI is system-owned. |
| MeetingMode | App selection/usage access not exercised; system usage/calendar data is not altered. |
| BlockType | Handling mode and call permissions not exercised; no call answered or rejected. |
| Notification | Channel, repeat, schedule, icon and delivery controls not exercised. |
| CallerID | Template/overlay toggle not exercised; overlay over a live call is hardware-only. |

## Six additional rule areas

`ProtectionFlowsTest.kt` asserts that all six section labels are reachable. Editors, lists and save/delete actions are not currently exercised by emulator tests.

| Area | Remaining coverage limit |
|---|---|
| Number Regex | Search, preset, add/edit/delete and invalid/valid save flow need UI tests. [`RegexStateUnitTest.kt`](../../app/src/test/java/spam/blocker/ui/setting/regex/RegexStateUnitTest.kt) covers model round-trip only. |
| Content Regex | Search/editor/save/delete UI and real message integration are not exercised. |
| QuickCopy Regex | Search/editor/save/delete UI and cross-app clipboard monitoring are not exercised. |
| PushAlert | Rule list/editor and delivered notifications are not exercised. |
| SmsAlert | Rule configuration and SMS delivery are not exercised. |
| SmsBomb | Rule configuration and a live message stream are not exercised. |

## Settings

| Section / action | Current evidence or limit |
|---|---|
| Instant Query | [`SettingsFlowsTest.kt`](../../app/src/androidTest/java/spam/blocker/ui/setting/SettingsFlowsTest.kt) reaches the section heading. Presets, API CRUD/validation and network behavior are not exercised. |
| Report Number | Section heading is reachable. Presets, auto-report configuration and API requests are not exercised. |
| OAuth setup | No settings editor/browser exchange test. Matching callback token persistence is tested below with a local intent only. |
| Automation | Section heading is reachable. Bot CRUD, templates, schedules, action validation/logs are not exercised in Compose; [`BotTemplatesTest.kt`](../../app/src/test/java/spam/blocker/service/bot/BotTemplatesTest.kt) covers template logic. |
| Language | Miscellaneous section is reachable; changing app/system language is not tested. |
| Theme | Settings test selects Catppuccin and Dark and checks persisted preference values; [`ThemeSelectionTest.kt`](../../app/src/test/java/spam/blocker/ui/theme/ThemeSelectionTest.kt), [`ThemePresetsTest.kt`](../../app/src/test/java/spam/blocker/ui/theme/ThemePresetsTest.kt) and [`ThemePersistenceTest.kt`](../../app/src/androidTest/java/spam/blocker/ui/theme/ThemePersistenceTest.kt) cover selection/persistence logic. Custom palette editing and Activity recreation after selection are not part of the new UI test. |
| Backup / Restore | Section is reachable. Category selection, import/export data, malformed files and permission restoration are not exercised by `SettingsFlowsTest`. SAF is tested only for picker action/cancel below. |
| FAQ / About / version / Logcat | Miscellaneous section is reachable. Dialog content, copy/export, external links and version collapse are not exercised. |
| Section expand/collapse | Headings are reached while scrolling; expand/collapse behavior itself is not tested. |

## Support and platform flows

| Flow | Current evidence or limit |
|---|---|
| Crash report | [`SupportFlowsTest.kt`](../../app/src/androidTest/java/spam/blocker/ui/support/SupportFlowsTest.kt) launches the activity with a synthetic stack trace and checks the extra. Copy, report link and clear-history confirmation are not exercised; no report is submitted. |
| OAuth callback | Support test sends a local matching callback, asserts the QA token was saved and intercepts return to MainActivity. The QA manifest removes the external `spamblocker://auth` filter to avoid competing with production when both packages are installed; explicit local activity launch remains covered. No browser/account/credential exchange occurs. Unknown/missing states are not covered. |
| Notification trampoline | Support test exercises only the `call` route with the call-log intent intercepted. SMS blocked/unblocked and unknown-type routes, notification cancellation and OEM behavior are not covered. |
| File chooser (SAF) | Support test asserts Open Document and Create Document intents with canned cancel results. Selected URI/read/write/remembered-directory behavior and document-provider UI are not covered. |
| Runtime permissions | No automated grant/deny/revoke flow yet. Permission-gated rule state must be tested with `UiAutomation` where supported. |
| Special access | Usage access, overlay, notification listener, default dialer/SMS role and exact alarm panels are Android-owned and not validated here. |
| Call/SMS hardware | Call screening, live answer/reject, carrier SMS/MMS, SIM identity and overlay-on-call require dedicated hardware/carrier testing. |

## Existing baseline and isolation

- [`QaPackageIsolationTest.kt`](../../app/src/androidTest/java/spam/blocker/qa/QaPackageIsolationTest.kt) asserts the instrumentation target is `spam.blocker.qa`; `QaTestEnvironment` reads/clears only that package's state.
- Existing database, popup, accessibility, tokenizer, formula and schedule tests remain in the JVM/emulator suites. `RuleTest.database_prefix` is a reproducible baseline failure: case 7 expected `1`, got `24`; its expectation was not changed.
