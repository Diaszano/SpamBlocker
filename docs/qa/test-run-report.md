# QA test run report

Run date: 2026-10-20  
Variant: `fdroidQa` (`spam.blocker.qa`)  
Emulator: `medtrack-test-api36`, API 36, x86_64

## Results

| Suite | Result | Evidence |
|---|---|---|
| QA JVM | 44 passed, 0 failed | `./gradlew testFdroidQaUnitTest` |
| QA instrumentation | 46 total: 45 passed, 1 failed | `./gradlew connectedFdroidQaAndroidTest` |
| New UI/support cases | 8 passed across navigation, history, protection inventory, settings/theme and support activities | Included in the complete instrumentation run below |
| Production debug assemble | Passed | `./gradlew assembleFdroidDebug` |

Complete-run command:

```bash
./gradlew testFdroidQaUnitTest connectedFdroidQaAndroidTest
```

The command exits non-zero only for the existing `spam.blocker.service.RuleTest.database_prefix` case. The focused baseline command reproduced it independently:

```bash
./gradlew connectedFdroidQaAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.service.RuleTest#database_prefix
```

Observed failure, case 7: expected `1` (“allow because count(2) < min(3)”), got `24` (`RESULT_BLOCKED_BY_DATABASE_PREFIX_REGEX`). This matches the baseline recorded before the coverage work; the expectation and production screening code were not changed.

The final combined command `./gradlew testFdroidQaUnitTest connectedFdroidQaAndroidTest assembleFdroidDebug` stopped at the same instrumentation failure before reporting the assemble task. The independent `./gradlew assembleFdroidDebug` command passed.

## Focused emulator runs

Each command below passed on the API 36 AVD:

```bash
./gradlew connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.main.MainNavigationTest
./gradlew connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.history.HistoryFlowsTest
./gradlew connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.ProtectionFlowsTest
./gradlew connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.SettingsFlowsTest
./gradlew connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.support.SupportFlowsTest
```

The emulator exercised all four bottom tabs, local allowed/blocked call and SMS fixtures, the Allowed filter, visibility of the 14 quick-rule rows and six additional rule areas, Catppuccin/Dark selection, a synthetic crash intent, a local OAuth callback, a call-notification route and canceled SAF open/create intents. These checks are scoped to the QA target and do not perform calls, send SMS, contact APIs, exchange credentials, or open a real document provider.

## Package safety and limits

- `QaPackageIsolationTest` passed and verified the instrumentation target package is `spam.blocker.qa`.
- Before the final full run, both `spam.blocker` and `spam.blocker.qa` were installed. No `pm clear`, uninstall or write to `spam.blocker` was issued. The isolated test fixture cleanup targets only `spam.blocker.qa`.
- The run was headless; no screenshot-based manual visual review was performed.
- Many controls are mapped but still need interaction-level tests: history search/context/report/FAB, protection toggles/editors/permission changes, API and bot CRUD, backup import/export results, FAQ/About/Logcat actions, and non-call notification routes. The coverage map marks those limits explicitly.
- Android special-access panels and live telecom behavior (call screening, SIM/carrier behavior, carrier SMS/MMS and overlay during a call) require device-level validation.
