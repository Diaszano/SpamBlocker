# Diagnóstico Guiado da Proteção Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Explain which call, SMS, or notification protection prerequisites are unmet and link to the exact Android setting that can resolve each one.

**Architecture:** Build a pure diagnostic model from current permission, app preference, pause, channel, and notification state. Render it as a user-invoked card on the Protection screen; refresh after returning from Android Settings rather than persisting computed status.

**Tech Stack:** Kotlin, Jetpack Compose, Android permission/settings APIs, JUnit 5, AndroidX Compose UI Test.

**Spec:** `docs/superpowers/specs/2026-10-30-protection-diagnostics-design.md`

## Global Constraints

- Reuse `Permission`, `PermissionChain`, `ProtectionSummary`, and existing Android settings launchers.
- Never request or change permissions/settings without an explicit user click.
- Missing one optional permission must not imply unrelated features are broken.
- Recompute from live system state on resume; do not store diagnostic state as truth.
- Do not make the first-run wizard mandatory.

## Review Focus

- Runtime permission denied or revoked externally: relevant feature reports action needed.
- Special access screen unavailable on a device: show help text instead of crashing.
- Paused/global-off state: do not mislabel it as a missing system permission.
- SMS notifications enabled in both apps: show the existing duplicate-notification warning path.
- Call-only or SMS-only setup: only evaluate prerequisites for enabled features.

## File and Test Map

- Modify: `app/src/main/java/spam/blocker/ui/setting/ProtectionScreen.kt`
- Modify if needed: `app/src/main/java/spam/blocker/ui/setting/GloballyEnabled.kt`
- Add: `app/src/main/java/spam/blocker/ui/setting/ProtectionDiagnosticsCard.kt`
- Reuse: `app/src/main/java/spam/blocker/util/Permission.kt`, `PermissionChain.kt`, `app/src/main/java/spam/blocker/util/Launcher.kt`
- Add: `app/src/main/java/spam/blocker/ui/setting/ProtectionDiagnostics.kt` for model and pure evaluator.
- Extend: `app/src/test/java/spam/blocker/ui/setting/ProtectionSummaryTest.kt` or add `ProtectionDiagnosticsTest.kt`.
- Extend: `app/src/androidTest/java/spam/blocker/ui/setting/ProtectionFlowsTest.kt`.

## Interfaces

- `enum class DiagnosticStatus { ACTIVE, ACTION_REQUIRED, OPTIONAL }`
- `data class ProtectionDiagnostic(val id: String, val status: DiagnosticStatus, val messageRes: Int, val action: DiagnosticAction?)`
- `fun buildProtectionDiagnostics(state: ProtectionDiagnosticState): List<ProtectionDiagnostic>`; `ProtectionDiagnosticState` contains explicit current fields: `globalEnabled`, `paused`, `callChannelEnabled`, `smsChannelEnabled`, `callScreeningGranted`, `receiveSmsGranted`, `notificationAccessGranted`, `postNotificationsGranted`, and `duplicateSmsNotifications`.
- `DiagnosticAction` identifies an in-app or Android settings destination; UI resolves it through existing launch utilities.

---

### Task 1: Model diagnostic states and precedence

**Files:**
- Add: `app/src/main/java/spam/blocker/ui/setting/ProtectionDiagnostics.kt`
- Add: `app/src/test/java/spam/blocker/ui/setting/ProtectionDiagnosticsTest.kt`

- [ ] **Step 1: Write failing pure tests** for global off, paused, calls missing permission, SMS missing permission, optional permission absent, duplicate SMS notifications, no enabled channel, and fully active state.
- [ ] **Step 2: Run** `./gradlew :app:testFdroidQaUnitTest --tests spam.blocker.ui.setting.ProtectionDiagnosticsTest`; confirm the evaluator is absent.
- [ ] **Step 3: Implement `buildProtectionDiagnostics`** with independent entries per resource and explicit actions. Do not reduce all states to one boolean summary.
- [ ] **Step 4: Run focused JVM tests** and verify unrelated enabled features stay active when one permission is missing.
- [ ] **Step 5: Commit** as `feat: model protection diagnostics`.

### Task 2: Render actionable diagnostics in Protection

**Files:**
- Modify: `app/src/main/java/spam/blocker/ui/setting/ProtectionScreen.kt`
- Add: `app/src/main/java/spam/blocker/ui/setting/ProtectionDiagnosticsCard.kt`
- Modify: `app/src/main/res/values/strings_ui_ux.xml` and matching locale resources.
- Test: `app/src/androidTest/java/spam/blocker/ui/setting/ProtectionFlowsTest.kt`

- [ ] **Step 1: Add failing UI tests** for collapsed/expanded card, each missing-prerequisite message, action visibility, and feature-specific status.
- [ ] **Step 2: Run** `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.ProtectionFlowsTest`.
- [ ] **Step 3: Add a `Check protection` card** that evaluates current system and app state on demand and on resume. Each action button opens the exact corresponding settings page; catch `ActivityNotFoundException` and present a fallback explanation.
- [ ] **Step 4: Run focused tests** and verify no automatic permission dialogs or settings intents occur until a button is tapped.
- [ ] **Step 5: Run** `./gradlew :app:testFdroidQaUnitTest :app:connectedFdroidQaAndroidTest`; confirm all tests pass.
- [ ] **Step 6: Commit** as `feat: guide users through protection setup checks`.

## Review Focus Tests

The five input conditions above map to `ProtectionDiagnosticsTest` and `ProtectionFlowsTest`. Use the existing QA target package and controlled permission state; never alter the production package's settings.
